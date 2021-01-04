package com.what3words.androidwrapper.voice

import android.util.Log
import com.google.gson.Gson
import com.what3words.javawrapper.response.APIError
import com.what3words.javawrapper.response.Suggestion
import okhttp3.*
import org.json.JSONObject

/**
 * Implement this listener to receive the callbacks from VoiceApi
 */
interface VoiceApiListener {
    /**
     * When WebSocket successfully does the handshake with VoiceAPI
     */
    fun connected(socket: WebSocket)

    /**
     * When VoiceAPI receive the recording, processed it and retrieved what3word addresses
     */
    fun suggestions(suggestions: List<Suggestion>)

    /**
     * When there's an error with the VoiceAPI connection, please find all errors at: https://developer.what3words.com/voice-api/docs#error-handling
     */
    fun error(message: APIError)
}

/**
 * This class is a helper to use VoiceAPI with OkHttp3 WebSocket
 *
 * @param listener set the listener for this class (in this example MainActivity)
 */
class VoiceApi constructor(
    private val apiKey: String
) {
    companion object {
        const val BASE_URL = "wss://voiceapi.what3words.com/v1/autosuggest"
    }

    private var socket: WebSocket? = null
    var listener: VoiceApiListener? = null

    /**
     * open a WebSocket and communicate the parameters to Voice API
     * autoSuggest parameters are passed in the URL QueryString, and audio parameters are passed as a JSON message
     * @param sampleRate: the sample rate of the recording
     * @param encoding: the encoding of the audio, pcm_f32le (32 bit float little endian), pcm_s16le (16 bit signed int little endian), mulaw (8 bit mu-law encoding) supported
     */
    fun open(
        sampleRate: Int,
        encoding: String = "pcm_s16le",
        url: String
    ) {
        if (socket != null) throw Exception("socket already open")
        val urlWithKey = "$url&key=$apiKey"
        val request = Request.Builder().url(urlWithKey).build()

        socket = OkHttpClient().newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                val message = JSONObject(
                    mapOf(
                        "message" to "StartRecognition",
                        "audio_format" to mapOf(
                            "type" to "raw",
                            "encoding" to encoding,
                            "sample_rate" to sampleRate
                        )
                    )
                )
                webSocket.send(message.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                val message = Gson().fromJson(text, BaseVoiceMessagePayload::class.java)
                if (message.message == BaseVoiceMessagePayload.RecognitionStarted) {
                    listener?.connected(webSocket)
                }

                if (message.message == BaseVoiceMessagePayload.Suggestions) {
                    val result = Gson().fromJson(text, SuggestionsPayload::class.java)
                    listener?.suggestions(result.suggestions)
                    webSocket.close(1000, "JOB FINISHED")
                }

                if (message.code != null && message.message != null) {
                    listener?.error(APIError().apply {
                        code = "UnknownError"
                        this.message = message.message
                    })
                    webSocket.close(1002, "JOB FINISHED WITH ERRORS")
                }
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: Response?
            ) {
                super.onFailure(webSocket, t, response)
                if (socket != null) t.message?.let {
                    listener?.error(APIError().apply {
                        code = "NetworkError"
                        message = it
                    })
                    socket = null
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                if (code != 1000) {
                    listener?.error(Gson().fromJson(reason, APIError::class.java))
                    webSocket.close(code, reason)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.i("VoiceApi", "onClosed - code:$code, reason:$reason")
                super.onClosed(webSocket, code, reason)
                socket = null
            }
        })
    }

    fun forceStop() {
        socket?.close(1000, "Aborted by user")
    }
}