package com.what3words.androidwrapper.helpers

import androidx.core.util.Consumer
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.javawrapper.request.SourceApi
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.ConvertToCoordinates
import com.what3words.javawrapper.response.Suggestion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AutosuggestHelper(private val api: What3WordsV3) {

    private var searchJob: Job? = null

    fun update(
        searchText: String,
        callback: Consumer<List<Suggestion>>,
        errorCallback: Consumer<APIResponse.What3WordsError>? = null
    ) {
        if (!searchText.isPossible3wa()) {
            callback.accept(emptyList())
        } else {
            searchJob?.cancel()
            searchJob = CoroutineScope(Dispatchers.IO).launch {
                delay(300)
                val res = api.autosuggest(searchText).execute()
                CoroutineScope(Dispatchers.Main).launch {
                    if (res.isSuccessful) {
                        callback.accept(res.suggestions)
                    } else {
                        errorCallback?.accept(res.error)
                    }
                }
            }
        }
    }

    fun selected(
        rawString: String,
        suggestion: Suggestion,
        callback: Consumer<Suggestion>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            api.autosuggestionSelection(
                rawString,
                suggestion.words,
                suggestion.rank,
                SourceApi.TEXT
            ).execute()
        }
        callback.accept(suggestion)
    }

    fun selectedWithCoordinates(
        rawString: String,
        suggestion: Suggestion,
        callback: Consumer<ConvertToCoordinates>,
        errorCallback: Consumer<APIResponse.What3WordsError>? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            api.autosuggestionSelection(
                rawString,
                suggestion.words,
                suggestion.rank,
                SourceApi.TEXT
            ).execute()
            val res = api.convertToCoordinates(suggestion.words).execute()
            CoroutineScope(Dispatchers.Main).launch {
                if (res.isSuccessful) {
                    callback.accept(res)
                } else {
                    errorCallback?.accept(res.error)
                }
            }
        }
    }
}
