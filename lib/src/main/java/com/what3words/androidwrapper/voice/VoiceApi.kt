package com.what3words.androidwrapper.voice

import android.media.AudioFormat
import android.util.Log
import com.google.gson.Gson
import com.what3words.javawrapper.response.APIError
import java.nio.ByteBuffer
import java.nio.ByteOrder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject

interface VoiceProvider {
    fun initialize(
        sampleRate: Int,
        encoding: Int,
        url: String,
        listener: VoiceApiListener
    )

    fun initialize(
        sampleRate: Int,
        encoding: Int,
        url: String,
        listener: VoiceApiListenerWithCoordinates
    )

    fun sendData(readCount: Int, buffer: ShortArray)

    fun forceStop()
    var baseUrl: String
}

class VoiceApi(
    private var apiKey: String,
    override var baseUrl: String = BASE_URL,
    private var client: OkHttpClient = OkHttpClient()
) : VoiceProvider {

    companion object {
        const val BASE_URL = "wss://voiceapi.what3words.com/v1/"
        const val URL_WITHOUT_COORDINATES =
            "autosuggest"
        const val URL_WITH_COORDINATES =
            "autosuggest-with-coordinates"
    }

    internal var socket: WebSocket? = null
    private var listener: VoiceApiListener? = null
    var listenerWithCoordinates: VoiceApiListenerWithCoordinates? = null

    override fun initialize(
        sampleRate: Int,
        encoding: Int,
        url: String,
        listener: VoiceApiListener
    ) {
        this.listener = listener
        open(sampleRate, encoding, url)
    }

    override fun initialize(
        sampleRate: Int,
        encoding: Int,
        url: String,
        listener: VoiceApiListenerWithCoordinates
    ) {
        this.listenerWithCoordinates = listener
        open(sampleRate, encoding, url)
    }

    override fun sendData(readCount: Int, buffer: ShortArray) {
        val bufferBytes: ByteBuffer =
            ByteBuffer.allocate(readCount * 2) // 2 bytes per short
        bufferBytes.order(ByteOrder.LITTLE_ENDIAN) // save little-endian byte from short buffer
        bufferBytes.asShortBuffer().put(buffer, 0, readCount)
        socket?.send(ByteString.of(*bufferBytes.array()))
    }

    /**
     * open a WebSocket and communicate the parameters to Voice API
     * autoSuggest parameters are passed in the URL QueryString, and audio parameters are passed as a JSON message
     * @param sampleRate: the sample rate of the recording
     * @param encoding: the encoding of the audio, pcm_f32le (32 bit float little endian), pcm_s16le (16 bit signed int little endian), mulaw (8 bit mu-law encoding) supported
     */
    private fun open(
        sampleRate: Int,
        encoding: Int,
        url: String
    ) {
        if (socket != null) forceStop()
        val urlWithKey = "$url&key=$apiKey"
        val request = Request.Builder().url(urlWithKey).build()

        socket = client.newWebSocket(
            request,
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    super.onOpen(webSocket, response)
                    val message = JSONObject(
                        mapOf(
                            "message" to "StartRecognition",
                            "audio_format" to mapOf(
                                "type" to "raw",
                                "encoding" to encoding.toW3Wencoding(),
                                "sample_rate" to sampleRate
                            )
                        )
                    )
                    webSocket.send(message.toString())
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    super.onMessage(webSocket, text)
                    try {
                        val socketMessage =
                            Gson().fromJson(text, BaseVoiceMessagePayload::class.java)

                        when (socketMessage.message) {
                            BaseVoiceMessagePayload.RecognitionStarted -> {
                                listenerWithCoordinates?.connected(this@VoiceApi)
                                listener?.connected(this@VoiceApi)
                            }
                            BaseVoiceMessagePayload.Suggestions -> {
                                listenerWithCoordinates?.let {
                                    val result =
                                        Gson().fromJson(
                                            text,
                                            SuggestionsWithCoordinatesPayload::class.java
                                        )
                                    it.suggestionsWithCoordinates(result.suggestions)
                                }
                                listener?.let {
                                    val result =
                                        Gson().fromJson(text, SuggestionsPayload::class.java)
                                    it.suggestions(result.suggestions)
                                }
                            }
                            BaseVoiceMessagePayload.Error -> {
                                val result = Gson().fromJson(text, ErrorPayload::class.java)
                                listenerWithCoordinates?.error(
                                    APIError().apply {
                                        code = result.code?.toString() ?: "StreamingError"
                                        this.message = "${result.type} - ${result.reason}"
                                    }
                                )
                                listener?.error(
                                    APIError().apply {
                                        code = result.code?.toString() ?: "StreamingError"
                                        this.message = "${result.type} - ${result.reason}"
                                    }
                                )
                            }
                            BaseVoiceMessagePayload.W3WError -> {
                                val result = Gson().fromJson(text, W3WErrorPayload::class.java)
                                listenerWithCoordinates?.error(
                                    APIError().apply {
                                        code = result.error.code
                                        this.message = result.error.message
                                    }
                                )
                                listener?.error(
                                    APIError().apply {
                                        code = result.error.code
                                        this.message = result.error.message
                                    }
                                )
                            }
                        }
                    } catch (ex: Exception) {
                        listenerWithCoordinates?.error(
                            APIError().apply {
                                code = "UnknownError"
                                this.message = ex.message
                            }
                        )
                        listener?.error(
                            APIError().apply {
                                code = "UnknownError"
                                this.message = ex.message
                            }
                        )
                    }
                }

                override fun onFailure(
                    webSocket: WebSocket,
                    t: Throwable,
                    response: Response?
                ) {
                    Log.e("VoiceFlow", "onFailure: ${t.message}")
                    if (socket != null) t.message?.let {
                        listenerWithCoordinates?.error(
                            APIError().apply {
                                code = "NetworkError"
                                message = it
                            }
                        )
                        listener?.error(
                            APIError().apply {
                                code = "NetworkError"
                                message = it
                            }
                        )
                    }
                    socket = null
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    if (code != 1000 && reason.isNotEmpty()) {
                        try {
                            listenerWithCoordinates?.error(
                                Gson().fromJson(
                                    reason,
                                    APIError::class.java
                                )
                            )
                            listener?.error(Gson().fromJson(reason, APIError::class.java))
                        } catch (e: Exception) {
                            listenerWithCoordinates?.error(
                                APIError().apply {
                                    this.code = "UnknownError"
                                    this.message = reason
                                }
                            )
                            listener?.error(
                                APIError().apply {
                                    this.code = "UnknownError"
                                    this.message = reason
                                }
                            )
                        }
                    }
                    webSocket.close(1000, null)
                    socket = null
                }
            }
        )
    }

    override fun forceStop() {
        socket?.close(1000, "Aborted by user")
    }
}

private fun Int.toW3Wencoding(): String {
    return when (this) {
        AudioFormat.ENCODING_PCM_16BIT -> "pcm_s16le"
        AudioFormat.ENCODING_PCM_FLOAT -> "pcm_f32le"
        AudioFormat.ENCODING_PCM_8BIT -> "mulaw"
        else -> "pcm_s16le"
    }
}
