package com.what3words.androidwrapper.helpers

import androidx.core.util.Consumer
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.request.SourceApi
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AutosuggestHelper(private val api: What3WordsV3) {
    private var allowFlexibleDelimiters: Boolean = false
    private var options: AutosuggestOptions? = null
    private var searchJob: Job? = null

    fun update(
        searchText: String,
        onSuccessListener: Consumer<List<Suggestion>>,
        onFailureListener: Consumer<APIResponse.What3WordsError>? = null,
        onDidYouMeanListener: Consumer<Suggestion>? = null
    ) {
        var isDidYouMean = false
        val searchFiltered: String? = when {
            searchText.isPossible3wa() -> searchText
            !allowFlexibleDelimiters && searchText.didYouMean3wa() -> {
                isDidYouMean = true
                searchText.split(split_regex, 3).joinToString(".")
            }
            allowFlexibleDelimiters && searchText.didYouMean3wa() -> searchText.split(
                split_regex,
                3
            ).joinToString(".")
            else -> null
        }
        if (searchFiltered == null) {
            onSuccessListener.accept(emptyList())
        } else {
            performAutosuggest(
                searchFiltered,
                isDidYouMean,
                onSuccessListener,
                onFailureListener,
                onDidYouMeanListener
            )
        }
    }

    private fun performAutosuggest(
        finalQuery: String,
        isDidYouMean: Boolean,
        onSuccessListener: Consumer<List<Suggestion>>,
        onFailureListener: Consumer<APIResponse.What3WordsError>? = null,
        onDidYouMeanListener: Consumer<Suggestion>? = null
    ) {
        searchJob?.cancel()
        searchJob = CoroutineScope(Dispatchers.IO).launch {
            delay(300)
            val builder = api.autosuggest(finalQuery)
            if (options != null) builder.options(options)
            val res = builder.execute()
            CoroutineScope(Dispatchers.Main).launch {
                if (res.isSuccessful) {
                    if (isDidYouMean) {
                        res.suggestions.firstOrNull { it.words == finalQuery }?.let {
                            onDidYouMeanListener?.accept(it)
                        }
                    } else {
                        onSuccessListener.accept(res.suggestions)
                    }
                } else {
                    onFailureListener?.accept(res.error)
                }
            }
        }
    }

    fun selected(
        rawString: String,
        suggestion: Suggestion,
        onSuccessListener: Consumer<Suggestion>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val builder = api.autosuggestionSelection(
                rawString,
                suggestion.words,
                suggestion.rank,
                SourceApi.TEXT
            )
            if (options != null) builder.options(options)
            builder.execute()
        }
        onSuccessListener.accept(suggestion)
    }

    fun selectedWithCoordinates(
        rawString: String,
        suggestion: Suggestion,
        onSuccessListener: Consumer<SuggestionWithCoordinates>,
        onFailureListener: Consumer<APIResponse.What3WordsError>? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val builder = api.autosuggestionSelection(
                rawString,
                suggestion.words,
                suggestion.rank,
                SourceApi.TEXT
            )
            if (options != null) builder.options(options)
            val builderConvert = api.convertToCoordinates(suggestion.words)
            builder.execute()
            val res = builderConvert.execute()
            CoroutineScope(Dispatchers.Main).launch {
                if (res.isSuccessful) {
                    val newSuggestion = SuggestionWithCoordinates(suggestion, res)
                    onSuccessListener.accept(newSuggestion)
                } else {
                    onFailureListener?.accept(res.error)
                }
            }
        }
    }

    /**
     * Set all options at once using <code>AutosuggestOptions</code>
     *
     * @param options the AutoSuggestOptions
     * @return a {@link Builder} instance suitable for invoking a <code>autosuggest</code> API request
     */
    fun options(options: AutosuggestOptions): AutosuggestHelper {
        this.options = options
        return this
    }

    /**
     * Flexible delimiters feature allows our regex to be less precise on delimiters, this means that "filled count soa" or "filled,count,soa" will be parsed to "filled.count.soa" and send to our autosuggest API.
     *
     * @param boolean enables flexible delimiters feature enabled (false by default)
     * @return a {@link W3WAutoSuggestEditText} instance
     */
    fun allowFlexibleDelimiters(boolean: Boolean): AutosuggestHelper {
        allowFlexibleDelimiters = boolean
        return this
    }
}
