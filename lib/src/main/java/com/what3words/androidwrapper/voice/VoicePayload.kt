package com.what3words.androidwrapper.voice

import com.what3words.javawrapper.response.Suggestion

class SuggestionsPayload : BaseVoiceMessagePayload() {
    var suggestions: List<Suggestion> = emptyList()
}

open class BaseVoiceMessagePayload {
    companion object {
        const val RecognitionStarted = "RecognitionStarted"
        const val Suggestions = "Suggestions"
    }

    var message: String? = null
    var code: String? = null
    var id: String? = null
}