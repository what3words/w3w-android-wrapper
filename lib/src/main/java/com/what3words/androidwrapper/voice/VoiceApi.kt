package com.what3words.androidwrapper.voice

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.what3words.javawrapper.response.APIError
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates
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

interface VoiceApiListenerWithCoordinates {
    /**
     * When WebSocket successfully does the handshake with VoiceAPI
     */
    fun connected(socket: WebSocket)

    /**
     * When VoiceAPI receive the recording, processed it and retrieved what3word addresses with coordinates
     */
    fun suggestionsWithCoordinates(suggestions: List<SuggestionWithCoordinates>)

    /**
     * When there's an error with the VoiceAPI connection, please find all errors at: https://developer.what3words.com/voice-api/docs#error-handling
     */
    fun error(message: APIError)
}


class VoiceApi constructor(
    private val apiKey: String
) {
    companion object {
        const val BASE_URL = "wss://voiceapi.what3words.com/v1/autosuggest"
        const val BASE_URL_WITH_COORDINATES =
            "wss://voiceapi.what3words.com/v1/autosuggest-with-coordinates"
    }

    private var socket: WebSocket? = null
    var listener: VoiceApiListener? = null
    var listenerWithCoordinates: VoiceApiListenerWithCoordinates? = null

    /**
     * open a WebSocket and communicate the parameters to Voice API
     * autoSuggest parameters are passed in the URL QueryString, and audio parameters are passed as a JSON message
     * @param sampleRate: the sample rate of the recording
     * @param encoding: the encoding of the audio, pcm_f32le (32 bit float little endian), pcm_s16le (16 bit signed int little endian), mulaw (8 bit mu-law encoding) supported
     */
    fun open(
        sampleRate: Int,
        encoding: String = "pcm_s16le",
        url: String,
        withCoordinates: Boolean = false
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
                val socketMessage = Gson().fromJson(text, BaseVoiceMessagePayload::class.java)
                if (socketMessage.message == BaseVoiceMessagePayload.RecognitionStarted) {
                    if (withCoordinates) {
                        listenerWithCoordinates?.connected(webSocket)
                    } else {
                        listener?.connected(webSocket)
                    }
                }

                if (socketMessage.message == BaseVoiceMessagePayload.Suggestions) {
                    if (withCoordinates) {
                        val result =
                            Gson().fromJson(text, SuggestionsWithCoordinatesPayload::class.java)
                        listenerWithCoordinates?.suggestionsWithCoordinates(result.suggestions)
                    } else {
                        val result = Gson().fromJson(text, SuggestionsPayload::class.java)
                        listener?.suggestions(result.suggestions)
                    }
                }

                if (socketMessage.message == BaseVoiceMessagePayload.Error) {
                    val result = Gson().fromJson(text, ErrorPayload::class.java)
                    if (withCoordinates) {
                        listenerWithCoordinates?.error(APIError().apply {
                            code = "UnknownError"
                            this.message = "${result.type} - ${result.reason}"
                        })
                    } else {
                        listener?.error(APIError().apply {
                            code = "UnknownError"
                            this.message = "${result.type} - ${result.reason}"
                        })
                    }
                }

                if (socketMessage.message == BaseVoiceMessagePayload.W3WError) {
                    val result = Gson().fromJson(text, W3WErrorPayload::class.java)
                    if (withCoordinates) {
                        listenerWithCoordinates?.error(APIError().apply {
                            code = result.error.code
                            this.message = result.error.message
                        })
                    } else {
                        listener?.error(APIError().apply {
                            code = result.error.code
                            this.message = result.error.message
                        })
                    }
                }
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: Response?
            ) {
                super.onFailure(webSocket, t, response)
                if (socket != null) t.message?.let {
                    if (withCoordinates) {
                        listenerWithCoordinates?.error(APIError().apply {
                            code = "NetworkError"
                            message = it
                        })
                    } else {
                        listener?.error(APIError().apply {
                            code = "NetworkError"
                            message = it
                        })
                    }
                    socket = null
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                if (code != 1000) {
                    if (withCoordinates) {
                        try {
                            listenerWithCoordinates?.error(
                                Gson().fromJson(
                                    reason,
                                    APIError::class.java
                                )
                            )
                        } catch (e: JsonSyntaxException) {
                            listenerWithCoordinates?.error(APIError().apply {
                                this.code = "UnknownError"
                                this.message = reason
                            })
                        }
                    } else {
                        try {
                            listener?.error(Gson().fromJson(reason, APIError::class.java))
                        } catch (e: JsonSyntaxException) {
                            listener?.error(APIError().apply {
                                this.code = "UnknownError"
                                this.message = reason
                            })
                        }
                    }
                }
                webSocket.close(code, reason)
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