package com.what3words.androidwrapper.helpers

import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion

interface VoiceAutosuggestManager {
    fun isListening(): Boolean
    fun stopListening()
    fun updateOptions(options: AutosuggestOptions)
    suspend fun startListening(): Either<APIResponse.What3WordsError, List<Suggestion>>
}