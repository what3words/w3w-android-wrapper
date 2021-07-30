package com.what3words.androidwrapper.voice

import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates

data class SuggestionsPayload(val suggestions: List<Suggestion>) : BaseVoiceMessagePayload()

data class SuggestionsWithCoordinatesPayload(val suggestions: List<SuggestionWithCoordinates>) : BaseVoiceMessagePayload()

data class ErrorPayload(val type: String, val code: Int?, val reason: String) : BaseVoiceMessagePayload()

data class W3WErrorPayload(val error: W3WError) : BaseVoiceMessagePayload()

data class W3WError(val code: String, val message: String)

open class BaseVoiceMessagePayload {
    companion object {
        const val RecognitionStarted = "RecognitionStarted"
        const val Suggestions = "Suggestions"
        const val Error = "Error"
        const val W3WError = "W3WError"
    }

    var message: String? = null
}