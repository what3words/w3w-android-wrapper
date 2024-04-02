package com.what3words.androidwrapper.voice

import com.what3words.javawrapper.response.APIError
import com.what3words.javawrapper.response.Suggestion

/**
 * Implement this listener to receive the callbacks from VoiceApi
 */
@Deprecated("This class is deprecated, please use new W3WApiVoiceDataSource instead")
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
