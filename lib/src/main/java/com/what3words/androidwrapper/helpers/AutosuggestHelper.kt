package com.what3words.androidwrapper.helpers

import androidx.core.util.Consumer
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.request.SourceApi
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.what3words.javawrapper.What3WordsV3.didYouMean3wa
import com.what3words.javawrapper.What3WordsV3.isPossible3wa

class AutosuggestHelper(
    private val api: What3WordsV3,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) {
    private var allowFlexibleDelimiters: Boolean = false
    private var options: AutosuggestOptions? = null
    private var searchJob: Job? = null

    /**
     * Update AutosuggestHelper query and receive suggestions (strong regex applied) or a did you mean (flexible regex applied) from our Autosuggest API.
     *
     * @param searchText the updated query.
     * @param onSuccessListener the callback for suggestions.
     * @param onFailureListener the callback for API errors [APIResponse.What3WordsError].
     * @param onDidYouMeanListener the callback for did you mean results.
     */
    fun update(
        searchText: String,
        onSuccessListener: Consumer<List<Suggestion>>,
        onFailureListener: Consumer<APIResponse.What3WordsError>? = null,
        onDidYouMeanListener: Consumer<Suggestion>? = null
    ) {
        var isDidYouMean = false
        val searchFiltered: String? = when {
            isPossible3wa(searchText) -> searchText
            !allowFlexibleDelimiters && didYouMean3wa(searchText) -> {
                isDidYouMean = true
                searchText.split(splitRegex, 3).joinToString(".")
            }
            allowFlexibleDelimiters && didYouMean3wa(searchText) -> searchText.split(
                splitRegex,
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
        searchJob = CoroutineScope(dispatchers.io()).launch {
            delay(250)
            val builder = api.autosuggest(finalQuery)
            if (options != null) builder.options(options)
            val res = builder.execute()
            CoroutineScope(dispatchers.main()).launch {
                if (res.isSuccessful) {
                    if (isDidYouMean) {
                        res.suggestions.firstOrNull { it.words.lowercase(Locale.getDefault()) == finalQuery.lowercase(Locale.getDefault()) }?.let {
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

    /**
     * When suggestion is selected this will provide all three word address information needed (without coordinates).
     *
     * @param rawString the updated raw query.
     * @param suggestion the selected suggestion.
     * @param onSuccessListener the callback for the full suggestion information (without coordinates) [Suggestion].
     */
    fun selected(
        rawString: String,
        suggestion: Suggestion,
        onSuccessListener: Consumer<Suggestion>
    ) {
        CoroutineScope(dispatchers.io()).launch {
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

    /**
     * When suggestion is selected this will provide all three word address information needed with coordinates.
     * Note that selectedWithCoordinates() will convert the three word address to a lat/lng which will count against your plan's quota.
     *
     * @param rawString the updated raw query.
     * @param suggestion the selected suggestion.
     * @param onSuccessListener the callback for the full suggestion information with coordinates [SuggestionWithCoordinates].
     * @param onFailureListener the callback for API errors [APIResponse.What3WordsError].
     */
    fun selectedWithCoordinates(
        rawString: String,
        suggestion: Suggestion,
        onSuccessListener: Consumer<SuggestionWithCoordinates>,
        onFailureListener: Consumer<APIResponse.What3WordsError>? = null
    ) {
        CoroutineScope(dispatchers.io()).launch {
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
            CoroutineScope(dispatchers.main()).launch {
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
     * Set all options at once using [AutosuggestOptions]
     *
     * @param options the [AutosuggestOptions] with all filters/clipping needed to be applied to the search
     * @return a [AutosuggestHelper] instance suitable for invoking a autosuggest API request
     */
    fun options(options: AutosuggestOptions): AutosuggestHelper {
        this.options = options
        return this
    }

    /**
     * Flexible delimiters feature allows our regex to be less precise on delimiters, this means that "filled count soa" or "filled,count,soa" will be parsed to "filled.count.soa" and send to our autosuggest API.
     *
     * @param boolean enables flexible delimiters feature enabled (false by default)
     * @return a [AutosuggestHelper] instance
     */
    fun allowFlexibleDelimiters(boolean: Boolean): AutosuggestHelper {
        allowFlexibleDelimiters = boolean
        return this
    }
}
