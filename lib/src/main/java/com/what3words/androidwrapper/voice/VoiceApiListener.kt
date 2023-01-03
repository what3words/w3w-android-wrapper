package com.what3words.androidwrapper.voice

import android.speech.tts.Voice
import com.what3words.javawrapper.response.APIError
import com.what3words.javawrapper.response.Suggestion
import okhttp3.WebSocket

/**
 * Implement this listener to receive the callbacks from VoiceApi
 */
interface VoiceApiListener {
    /**
     * When WebSocket successfully does the handshake with VoiceAPI
     */
    fun connected(voiceProvider: VoiceProvider)

    /**
     * When VoiceAPI receive the recording, processed it and retrieved what3word addresses
     */
    fun suggestions(suggestions: List<Suggestion>)

    /**
     * When there's an error with the VoiceAPI connection, please find all errors at: https://developer.what3words.com/voice-api/docs#error-handling
     */
    fun error(message: APIError)
}
