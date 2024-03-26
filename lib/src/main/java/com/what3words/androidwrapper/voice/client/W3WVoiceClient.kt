package com.what3words.androidwrapper.voice.client

import android.media.AudioFormat
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.what3words.androidwrapper.datasource.text.api.extensions.W3WDomainToApiStringExtensions.toAPIString
import com.what3words.androidwrapper.helpers.plusAssign
import com.what3words.androidwrapper.voice.BaseVoiceMessagePayload
import com.what3words.androidwrapper.voice.ErrorPayload
import com.what3words.androidwrapper.voice.SuggestionsWithCoordinatesPayload
import com.what3words.androidwrapper.voice.W3WErrorPayload
import com.what3words.androidwrapper.voice.error.W3WApiVoiceError
import com.what3words.core.datasource.voice.audiostream.W3WAudioStream
import com.what3words.core.datasource.voice.audiostream.W3WAudioStreamEncoding
import com.what3words.core.datasource.voice.audiostream.W3WAudioStreamProxy
import com.what3words.core.types.options.W3WAutosuggestOptions
import com.what3words.javawrapper.response.APIError
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class W3WVoiceClient(
    private val apiKey: String,
    private val endPoint: String?,
    private val client: OkHttpClient = OkHttpClient()
) {

    private var socket: WebSocket? = null
    private lateinit var request: Request
    private lateinit var audioInputStreamProxy: W3WAudioStreamProxy

    internal fun initialize(
        languageCode: String,
        autoSuggestOptions: W3WAutosuggestOptions?,
        audioInputStream: W3WAudioStream
    ): W3WVoiceClient {
        val url = createSocketUrl(
            endPoint ?: BASE_URL,
            languageCode,
            autoSuggestOptions ?: W3WAutosuggestOptions.Builder().build(),
            apiKey
        )
        request = Request.Builder().url(url).build()
        audioInputStreamProxy = W3WAudioStreamProxy(audioInputStream)
        return this
    }

    internal fun openWebSocketAndStartRecognition(
        onStatusChanged: (status: Status) -> Unit
    ) {
        if (!::request.isInitialized || !::audioInputStreamProxy.isInitialized) {
            throw IllegalStateException("initialize() must be called before open()")
        }

        socket = client.newWebSocket(request, webSocketListener(onStatusChanged))
    }

    private fun webSocketListener(onStatusChanged: (status: Status) -> Unit): WebSocketListener {
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
                            onStatusChanged(Status.Suggestions(result.suggestions))
                        }

                        BaseVoiceMessagePayload.Error -> {
                            val result = Gson().fromJson(text, ErrorPayload::class.java)
                            onStatusChanged(
                                Status.Error(
                                    W3WApiVoiceError(
                                        code = result.code?.toString() ?: "StreamingError",
                                        message = "${result.type} - ${result.reason}"
                                    )
                                )
                            )
                        }

                        BaseVoiceMessagePayload.W3WError -> {
                            val result = Gson().fromJson(text, W3WErrorPayload::class.java)
                            onStatusChanged(
                                Status.Error(
                                    W3WApiVoiceError(
                                        code = result.error.code,
                                        message = result.error.message
                                    )
                                )
                            )
                        }
                    }

                } catch (ex: Exception) {
                    onStatusChanged(
                        Status.Error(
                            W3WApiVoiceError(
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
                        Status.Error(
                            W3WApiVoiceError(
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
                            Status.Error(
                                W3WApiVoiceError(
                                    code = result.code,
                                    message = result.message
                                )
                            )
                        )
                    } catch (e: Exception) {
                        Status.Error(
                            W3WApiVoiceError(
                                code = "NetworkError",
                                message = reason
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

    internal fun close(reason: String) {
        audioInputStreamProxy.closeAudioInputStream()
        socket?.close(MANUAL_CLOSE_CODE, reason)
    }

    @VisibleForTesting
    internal fun createSocketUrl(
        baseUrl: String,
        voiceLanguage: String,
        autoSuggestOptions: W3WAutosuggestOptions,
        apiKey: String
    ): String {
        with(autoSuggestOptions) {
            val appendedUrl = StringBuilder(baseUrl)
            appendedUrl += if (this.includeCoordinates) URL_WITH_COORDINATES
            else URL_WITHOUT_COORDINATES
            appendedUrl += if (voiceLanguage == "zh") "?voice-language=cmn"
            else "?voice-language=$voiceLanguage"
            nResults.let {
                appendedUrl += "&n-results=$nResults"
            }
            focus?.let {
                appendedUrl += "&focus=${focus!!.lat},${focus!!.lng}"
                if (nFocusResults != null) {
                    appendedUrl += "&n-focus-results=$nFocusResults"
                }
            }
            clipToCountry.let {
                appendedUrl += "&clip-to-country=${it.joinToString(",")}"
            }
            clipToCircle?.let {
                appendedUrl += "&clip-to-circle=${it.center.lat},${it.center.lng},${it.radius.km()}"
            }
            clipToPolygon?.let {
                appendedUrl += "&clip-to-polygon=${it.toAPIString()}}"
            }
            clipToBoundingBox?.let {
                appendedUrl += "&clip-to-bounding-box=${it.toAPIString()}"
            }
            appendedUrl += "&key=$apiKey"

            return appendedUrl.toString()
        }
    }

    private fun W3WAudioStreamEncoding.toApiString(): String {
        return when (this.value) {
            AudioFormat.ENCODING_PCM_16BIT -> "pcm_s16le"
            AudioFormat.ENCODING_PCM_FLOAT -> "pcm_f32le"
            AudioFormat.ENCODING_PCM_8BIT -> "mulaw"
            else -> "pcm_s16le"
        }
    }

    sealed interface Status {
        data class Suggestions(val suggestions: List<SuggestionWithCoordinates>) : Status
        data class Error(val error: W3WApiVoiceError) : Status
    }

    companion object {
        const val BASE_URL = "wss://voiceapi.what3words.com/v1/"
        const val URL_WITHOUT_COORDINATES =
            "autosuggest"
        const val URL_WITH_COORDINATES =
            "autosuggest-with-coordinates"
        const val MANUAL_CLOSE_CODE = 1000
    }
}