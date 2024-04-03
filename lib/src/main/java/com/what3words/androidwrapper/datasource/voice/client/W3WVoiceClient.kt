package com.what3words.androidwrapper.datasource.voice.client

import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.what3words.androidwrapper.common.Mapper
import com.what3words.androidwrapper.common.extensions.W3WDomainToApiStringExtensions.toApiString
import com.what3words.androidwrapper.common.extensions.W3WDomainToApiStringExtensions.toQueryMap
import com.what3words.androidwrapper.common.extensions.W3WDomainToApiStringExtensions.toVoiceApiString
import com.what3words.androidwrapper.datasource.text.api.dto.ErrorDto
import com.what3words.androidwrapper.datasource.text.api.error.UnknownError
import com.what3words.androidwrapper.datasource.text.api.mappers.ErrorDtoToDomainMapper
import com.what3words.androidwrapper.voice.BaseVoiceMessagePayload
import com.what3words.androidwrapper.voice.ErrorPayload
import com.what3words.androidwrapper.voice.SuggestionsWithCoordinatesPayload
import com.what3words.androidwrapper.voice.W3WErrorPayload
import com.what3words.androidwrapper.datasource.voice.error.W3WApiVoiceError
import com.what3words.core.datasource.voice.audiostream.W3WAudioStream
import com.what3words.core.datasource.voice.audiostream.W3WAudioStreamProxy
import com.what3words.core.types.common.W3WError
import com.what3words.core.types.common.W3WResult
import com.what3words.core.types.language.W3WLanguage
import com.what3words.core.types.options.W3WAutosuggestOptions
import com.what3words.javawrapper.response.APIError
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * This class is responsible for handling voice recognition using the what3words API.
 * It uses a WebSocket to communicate with the API.
 *
 * @property apiKey The API key to use for the what3words API.
 * @property endPoint Override the default public API endpoint.
 * @property client The OkHttpClient to use for the WebSocket connection.
 */
internal class W3WVoiceClient(
    private val apiKey: String,
    private val endPoint: String?,
    private val client: OkHttpClient = OkHttpClient(),
    private val errorMapper: Mapper<ErrorDto, W3WError> = ErrorDtoToDomainMapper()
) {

    @VisibleForTesting
    internal var socket: WebSocket? = null
    private lateinit var request: Request
    private lateinit var audioInputStreamProxy: W3WAudioStreamProxy

    /**
     * Initializes the client with the necessary parameters.
     *
     * @param voiceLanguage The language used to initialize the ASR engine. Accepts instances
     * of [W3WRFC5646Language] or [W3WProprietaryLanguage].
     * @param autoSuggestOptions Additional options for tuning the address suggestions.
     * @param audioInputStream The audio input stream to use for voice recognition.
     * @return The initialized client.
     */
    internal fun initialize(
        voiceLanguage: W3WLanguage,
        autoSuggestOptions: W3WAutosuggestOptions?,
        audioInputStream: W3WAudioStream
    ): W3WVoiceClient {
        request = buildRequest(autoSuggestOptions, voiceLanguage, apiKey)
        audioInputStreamProxy = W3WAudioStreamProxy(audioInputStream)
        return this
    }

    /**
     * Opens a WebSocket connection and starts the voice recognition process.
     * This method should be called after [initialize] method.
     *
     * @param onStatusChanged A callback that is called when the status of the voice recognition
     * process changes. Providing a [W3WResult] instance containing a list of what3words address
     * suggestions in case of success or [W3WApiVoiceError] in case of failure.
     * @throws IllegalStateException If the [initialize] method has never been called before this method.
     */
    @Throws(IllegalStateException::class)
    internal fun openWebSocketAndStartRecognition(
        onStatusChanged: (recognitionStatus: RecognitionStatus) -> Unit
    ) {
        if (!::request.isInitialized || !::audioInputStreamProxy.isInitialized) {
            throw IllegalStateException("initialize() must be called before openWebSocketAndStartRecognition()")
        }

        socket?.close(MANUAL_CLOSE_CODE, "Aborted by new request")
        socket = client.newWebSocket(request, webSocketListener(onStatusChanged))
    }

    private fun webSocketListener(onStatusChanged: (recognitionStatus: RecognitionStatus) -> Unit): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                val message = JSONObject(
                    mapOf(
                        "message" to "StartRecognition",
                        "audio_format" to mapOf(
                            "type" to "raw",
                            "encoding" to audioInputStreamProxy.config.encoding.toApiString(),
                            "sample_rate" to audioInputStreamProxy.config.sampleRateInHz
                        )
                    )
                )
                webSocket.send(message.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val socketMessage =
                        Gson().fromJson(text, BaseVoiceMessagePayload::class.java)

                    when (socketMessage.message) {
                        BaseVoiceMessagePayload.RecognitionStarted -> {
                            audioInputStreamProxy.openAudioInputStream({ readCount, buffer ->
                                sendData(webSocket, readCount, buffer)
                            }, false)
                        }

                        BaseVoiceMessagePayload.Suggestions -> {
                            val result =
                                Gson().fromJson(
                                    text,
                                    SuggestionsWithCoordinatesPayload::class.java
                                )
                            audioInputStreamProxy.closeAudioInputStream()
                            onStatusChanged(RecognitionStatus.Suggestions(result.suggestions))
                        }

                        BaseVoiceMessagePayload.Error -> {
                            val result = Gson().fromJson(text, ErrorPayload::class.java)
                            audioInputStreamProxy.closeAudioInputStream()
                            onStatusChanged(
                                RecognitionStatus.Error(
                                    W3WApiVoiceError.StreamingError(
                                        type = result.type,
                                        code = result.code,
                                        reason = result.reason,
                                    )
                                )
                            )
                        }

                        BaseVoiceMessagePayload.W3WError -> {
                            val result = Gson().fromJson(text, W3WErrorPayload::class.java)
                            val errorDto = ErrorDto(
                                code = result.error.code,
                                message = result.error.message
                            )
                            audioInputStreamProxy.closeAudioInputStream()
                            onStatusChanged(
                                RecognitionStatus.Error(
                                    errorMapper.mapFrom(errorDto)
                                )
                            )
                        }
                    }

                } catch (ex: Exception) {
                    audioInputStreamProxy.closeAudioInputStream()
                    onStatusChanged(
                        RecognitionStatus.Error(
                            UnknownError(
                                code = "UnknownError",
                                message = ex.message ?: "Unknown error"
                            )
                        )
                    )
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                if (socket != null) t.message?.let {
                    onStatusChanged(
                        RecognitionStatus.Error(
                            W3WApiVoiceError.ConnectionError(
                                code = "NetworkError",
                                message = it
                            )
                        )
                    )
                }
                socket = null
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                if (code != MANUAL_CLOSE_CODE && reason.isNotEmpty()) {
                    try {
                        val result = Gson().fromJson(reason, APIError::class.java)
                        onStatusChanged(
                            RecognitionStatus.Error(
                                W3WApiVoiceError.ConnectionError(
                                    code = result.code,
                                    message = result.message
                                )
                            )
                        )
                    } catch (e: Exception) {
                        onStatusChanged(
                            RecognitionStatus.Error(
                                W3WApiVoiceError.ConnectionError(
                                    code = "NetworkError",
                                    message = reason
                                )
                            )
                        )
                    }
                }
                webSocket.close(1000, null)
                socket = null
            }
        }
    }

    private fun sendData(webSocket: WebSocket, readCount: Int, buffer: ShortArray) {
        val bufferBytes: ByteBuffer =
            ByteBuffer.allocate(readCount * 2) // 2 bytes per short
        bufferBytes.order(ByteOrder.LITTLE_ENDIAN) // save little-endian byte from short buffer
        bufferBytes.asShortBuffer().put(buffer, 0, readCount)
        webSocket.send(ByteString.of(*bufferBytes.array()))
    }

    /**
     * Explicitly closes the WebSocket connection and audio stream.
     */
    internal fun close(reason: String) {
        audioInputStreamProxy.closeAudioInputStream()
        socket?.close(MANUAL_CLOSE_CODE, reason)
    }

    @VisibleForTesting
    internal fun buildRequest(
        autoSuggestOptions: W3WAutosuggestOptions?,
        voiceLanguage: W3WLanguage,
        apiKey: String
    ): Request {
        val queryMap = autoSuggestOptions?.toQueryMap()
        val requestWithCoordinates = autoSuggestOptions?.includeCoordinates == true
        return Request.Builder()
            .url(
                HttpUrl.Builder().scheme("https").host(endPoint ?: BASE_URL)
                    .addEncodedPathSegment("v1")
                    .addEncodedPathSegment(if (requestWithCoordinates) URL_WITH_COORDINATES else URL_WITHOUT_COORDINATES)
                    .apply {
                        queryMap?.forEach {
                            addQueryParameter(it.key, it.value)
                        }
                        // specify non-optional voice language parameter and api key
                        addQueryParameter("voice-language", voiceLanguage.toVoiceApiString())
                        addQueryParameter("key", apiKey)
                    }.build()
            ).build()
    }

    sealed interface RecognitionStatus {
        data class Suggestions(val suggestions: List<SuggestionWithCoordinates>) : RecognitionStatus
        data class Error(val error: W3WError) : RecognitionStatus
    }

    companion object {
        const val BASE_URL = "voiceapi.what3words.com"
        const val URL_WITHOUT_COORDINATES =
            "autosuggest"
        const val URL_WITH_COORDINATES =
            "autosuggest-with-coordinates"
        const val MANUAL_CLOSE_CODE = 1000
    }
}