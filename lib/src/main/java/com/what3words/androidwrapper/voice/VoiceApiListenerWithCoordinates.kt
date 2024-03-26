package com.what3words.androidwrapper.voice

import com.what3words.javawrapper.response.APIError
import com.what3words.javawrapper.response.SuggestionWithCoordinates

@Deprecated("This class is deprecated, please use new W3WApiVoiceDatasource instead")
interface VoiceApiListenerWithCoordinates {
    /**
     * When WebSocket successfully does the handshake with VoiceAPI
     */
    fun connected(voiceProvider: VoiceProvider)

    /**
     * When VoiceAPI receive the recording, processed it and retrieved what3word addresses with coordinates
     */
    fun suggestionsWithCoordinates(suggestions: List<SuggestionWithCoordinates>)

    /**
     * When there's an error with the VoiceAPI connection, please find all errors at: https://developer.what3words.com/voice-api/docs#error-handling
     */
    fun error(message: APIError)
}
