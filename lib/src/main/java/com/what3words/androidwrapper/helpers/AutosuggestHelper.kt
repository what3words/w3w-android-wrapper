package com.what3words.androidwrapper.helpers

import androidx.core.util.Consumer
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.core.datasource.text.W3WTextDataSource
import com.what3words.core.types.common.W3WError
import com.what3words.core.types.common.W3WResult
import com.what3words.core.types.domain.W3WAddress
import com.what3words.core.types.domain.W3WSuggestion
import com.what3words.core.types.options.W3WAutosuggestOptions
import com.what3words.javawrapper.What3WordsV3.didYouMean3wa
import com.what3words.javawrapper.What3WordsV3.isPossible3wa
import com.what3words.javawrapper.request.SourceApi
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

interface IAutosuggestHelper {
    fun update(
        searchText: String,
        onSuccessListener: Consumer<List<W3WSuggestion>>,
        onFailureListener: Consumer<W3WError>? = null,
        onDidYouMeanListener: Consumer<W3WSuggestion>? = null
    )

    fun selected(
        rawString: String,
        suggestion: W3WSuggestion,
        onSuccessListener: Consumer<W3WSuggestion>
    )

    fun selectedWithCoordinates(
        rawString: String,
        suggestion: W3WSuggestion,
        onSuccessListener: Consumer<W3WSuggestion>,
        onFailureListener: Consumer<W3WError>? = null
    )

    fun options(options: W3WAutosuggestOptions): IAutosuggestHelper

    fun allowFlexibleDelimiters(boolean: Boolean): IAutosuggestHelper
}

/**
 * The helper class to incorporate the what3words autosuggest API into a Text field. For more details, refer to the tutorial:
 * https://github.com/what3words/w3w-android-wrapper/blob/master/autosuggest-helper-tutorial.md
 */
class AutosuggestHelper(
    private val dataSource: W3WTextDataSource,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : IAutosuggestHelper {
    private var allowFlexibleDelimiters: Boolean = false
    private var options: W3WAutosuggestOptions? = null
    private var searchJob: Job? = null

    override fun update(
        searchText: String,
        onSuccessListener: Consumer<List<W3WSuggestion>>,
        onFailureListener: Consumer<W3WError>?,
        onDidYouMeanListener: Consumer<W3WSuggestion>?
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
        onSuccessListener: Consumer<List<W3WSuggestion>>,
        onFailureListener: Consumer<W3WError>? = null,
        onDidYouMeanListener: Consumer<W3WSuggestion>? = null
    ) {
        searchJob?.cancel()
        searchJob = CoroutineScope(dispatchers.io()).launch {
            delay(250)
            val res = dataSource.autosuggest(finalQuery, options)

            withContext(dispatchers.main()) {
                when (res) {
                    is W3WResult.Success -> {
                        if (isDidYouMean) {
                            res.value.firstOrNull {
                                it.w3wAddress.address.lowercase(Locale.getDefault()) == "///${finalQuery}".lowercase(
                                    Locale.getDefault()
                                )
                            }?.let {
                                onDidYouMeanListener?.accept(it)
                            }
                        } else {
                            onSuccessListener.accept(res.value)
                        }
                    }

                    is W3WResult.Failure -> {
                        onFailureListener?.accept(res.error)
                    }
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
    override fun selected(
        rawString: String,
        suggestion: W3WSuggestion,
        onSuccessListener: Consumer<W3WSuggestion>
    ) {
        (dataSource as? W3WApiTextDataSource)?.let {
            CoroutineScope(dispatchers.io()).launch {
                it.autosuggestionSelection(
                    rawString,
                    suggestion.w3wAddress.address.substring(3), // remove "///",
                    suggestion.rank,
                    SourceApi.TEXT,
                    options
                )
            }
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
    override fun selectedWithCoordinates(
        rawString: String,
        suggestion: W3WSuggestion,
        onSuccessListener: Consumer<W3WSuggestion>,
        onFailureListener: Consumer<W3WError>?
    ) {
        val word = suggestion.w3wAddress.address.substring(3) // remove "///"

        CoroutineScope(dispatchers.io()).launch {
            (dataSource as? W3WApiTextDataSource)?.autosuggestionSelection(
                rawString,
                word,
                suggestion.rank,
                SourceApi.TEXT,
                options
            )

            val res = dataSource.convertToCoordinates(word)
            withContext(dispatchers.main()) {
                when (res) {
                    is W3WResult.Success -> {
                        val newSuggestion = W3WSuggestion(
                            w3wAddress = W3WAddress(
                                words = word,
                                center = res.value,
                                square = suggestion.w3wAddress.square,
                                language = suggestion.w3wAddress.language,
                                country = suggestion.w3wAddress.country,
                                nearestPlace = suggestion.w3wAddress.nearestPlace
                            ),
                            distanceToFocus = suggestion.distanceToFocus,
                            rank = suggestion.rank
                        )
                        onSuccessListener.accept(newSuggestion)
                    }

                    is W3WResult.Failure -> {
                        onFailureListener?.accept(res.error)
                    }
                }
            }
        }
    }

    /**
     * Set all options at once using [W3WAutosuggestOptions]
     *
     * @param options the [W3WAutosuggestOptions] with all filters/clipping needed to be applied to the search
     * @return a [AutosuggestHelper] instance suitable for invoking a autosuggest API request
     */
    override fun options(options: W3WAutosuggestOptions): IAutosuggestHelper {
        this.options = options
        return this
    }

    /**
     * Flexible delimiters feature allows our regex to be less precise on delimiters, this means that "filled count soa" or "filled,count,soa" will be parsed to "filled.count.soa" and send to our autosuggest API.
     *
     * @param boolean enables flexible delimiters feature enabled (false by default)
     * @return a [AutosuggestHelper] instance
     */
    override fun allowFlexibleDelimiters(boolean: Boolean): AutosuggestHelper {
        allowFlexibleDelimiters = boolean
        return this
    }
}
