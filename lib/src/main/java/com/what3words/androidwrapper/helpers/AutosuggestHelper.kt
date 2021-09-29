package com.what3words.androidwrapper.helpers

import androidx.core.util.Consumer
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.javawrapper.request.AutosuggestRequest
import com.what3words.javawrapper.request.AutosuggestSelectionRequest
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
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
    private var clipToPolygon: Array<Coordinates>? = null
    private var clipToBoundingBox: BoundingBox? = null
    private var clipToCircle: Coordinates? = null
    private var clipToCircleRadius: Double? = null
    private var clipToCountry: Array<String>? = null
    private var nFocusResults: Int? = null
    private var focus: Coordinates? = null
    private var nResults: Int? = null
    private var searchJob: Job? = null

    fun update(
        searchText: String,
        onSuccessListener: Consumer<List<Suggestion>>,
        onFailureListener: Consumer<APIResponse.What3WordsError>? = null
    ) {
        if (!searchText.isPossible3wa()) {
            onSuccessListener.accept(emptyList())
        } else {
            searchJob?.cancel()
            searchJob = CoroutineScope(Dispatchers.IO).launch {
                delay(300)
                val builder = api.autosuggest(searchText)
                applyFilters(builder)
                val res = builder.execute()
                CoroutineScope(Dispatchers.Main).launch {
                    if (res.isSuccessful) {
                        onSuccessListener.accept(res.suggestions)
                    } else {
                        onFailureListener?.accept(res.error)
                    }
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
            applyFilters(builder)
            builder.execute()
        }
        onSuccessListener.accept(suggestion)
    }

    fun selectedWithCoordinates(
        rawString: String,
        suggestion: Suggestion,
        onSuccessListener: Consumer<ConvertToCoordinates>,
        onFailureListener: Consumer<APIResponse.What3WordsError>? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val builder = api.autosuggestionSelection(
                rawString,
                suggestion.words,
                suggestion.rank,
                SourceApi.TEXT
            )
            val builderConvert = api.convertToCoordinates(suggestion.words)
            applyFilters(builder)
            builder.execute()
            val res = builderConvert.execute()
            CoroutineScope(Dispatchers.Main).launch {
                if (res.isSuccessful) {
                    onSuccessListener.accept(res)
                } else {
                    onFailureListener?.accept(res.error)
                }
            }
        }
    }

    /**
     * This is a location, specified as a latitude (often where the user making the query is). If specified, the results will be weighted to
     * give preference to those near the <code>focus</code>. For convenience, longitude is allowed to wrap around the 180 line, so 361 is equivalent to 1.
     *
     * @param coordinates the focus to use
     * @return a {@link W3WAutoSuggestEditText} instance
     */
    fun focus(coordinates: Coordinates): AutosuggestHelper {
        focus = coordinates
        return this
    }

    /**
     * Set the number of AutoSuggest results to return. A maximum of 100 results can be specified, if a number greater than this is requested,
     * this will be truncated to the maximum. The default is 3
     *
     * @param n the number of AutoSuggest results to return
     * @return a {@link VoiceBuilder} instance
     */
    fun nResults(n: Int): AutosuggestHelper {
        nResults = n
        return this
    }

    /**
     * Specifies the number of results (must be &lt;= nResults) within the results set which will have a focus. Defaults to <code>nResults</code>.
     * This allows you to run autosuggest with a mix of focussed and unfocussed results, to give you a "blend" of the two. This is exactly what the old V2
     * <code>standardblend</code> did, and <code>standardblend</code> behaviour can easily be replicated by passing <code>nFocusResults=1</code>,
     * which will return just one focussed result and the rest unfocussed.
     *
     * @param n number of results within the results set which will have a focus
     * @return a {@link VoiceBuilder} instance
     */
    fun nFocusResults(n: Int): AutosuggestHelper {
        nFocusResults = n
        return this
    }

    /**
     * Restrict autosuggest results to a circle, specified by <code>Coordinates</code> representing the centre of the circle, plus the
     * <code>radius</code> in kilometres. For convenience, longitude is allowed to wrap around 180 degrees. For example 181 is equivalent to -179.
     *
     * @param centre the centre of the circle
     * @param radius the radius of the circle in kilometres
     * @return a {@link VoiceBuilder} instance
     */
    fun clipToCircle(
        centre: Coordinates,
        radius: Double = 1.0
    ): AutosuggestHelper {
        clipToCircle = centre
        clipToCircleRadius = radius
        return this
    }

    /**
     * Restricts autosuggest to only return results inside the countries specified by comma-separated list of uppercase ISO 3166-1 alpha-2 country codes
     * (for example, to restrict to Belgium and the UK, use <code>clipToCountry("GB", "BE")</code>. <code>clipToCountry</code> will also accept lowercase
     * country codes. Entries must be two a-z letters. WARNING: If the two-letter code does not correspond to a country, there is no error: API simply
     * returns no results.
     *
     * @param countryCodes countries to clip results too
     * @return a {@link VoiceBuilder} instance
     */
    fun clipToCountry(countryCodes: List<String>): AutosuggestHelper {
        clipToCountry = if (countryCodes.isNotEmpty()) countryCodes.toTypedArray() else null
        return this
    }

    /**
     * Restrict autosuggest results to a <code>BoundingBox</code>.
     *
     * @param boundingBox <code>BoundingBox</code> to clip results too
     * @return a {@link VoiceBuilder} instance
     */
    fun clipToBoundingBox(
        boundingBox: BoundingBox
    ): AutosuggestHelper {
        clipToBoundingBox = boundingBox
        return this
    }

    /**
     * Restrict autosuggest results to a polygon, specified by a collection of <code>Coordinates</code>. The polygon should be closed,
     * i.e. the first element should be repeated as the last element; also the list should contain at least 4 entries. The API is currently limited to
     * accepting up to 25 pairs.
     *
     * @param polygon the polygon to clip results too
     * @return a {@link VoiceBuilder} instance
     */
    fun clipToPolygon(
        polygon: List<Coordinates>
    ): AutosuggestHelper {
        clipToPolygon = if (polygon.isNotEmpty()) polygon.toTypedArray() else null
        return this
    }

    private fun applyFilters(builder: AutosuggestRequest.Builder) {
        nResults?.let {
            builder.nResults(it)
        }
        focus?.let {
            builder.focus(it)
            if (nFocusResults != null) {
                builder.nFocusResults(nFocusResults!!)
            }
        }
        clipToCountry?.let {
            builder.clipToCountry(*it)
        }
        clipToCircle?.let {
            builder.clipToCircle(it, clipToCircleRadius ?: 1.0)
        }
        clipToPolygon?.let {
            builder.clipToPolygon(*it)
        }
        clipToBoundingBox?.let {
            builder.clipToBoundingBox(it)
        }
    }

    private fun applyFilters(builder: AutosuggestSelectionRequest.Builder) {
        nResults?.let {
            builder.nResults(it)
        }
        focus?.let {
            builder.focus(it)
            if (nFocusResults != null) {
                builder.nResults(nFocusResults!!)
            }
        }
        clipToCountry?.let {
            builder.clipToCountry(*it)
        }
        clipToCircle?.let {
            builder.clipToCircle(it, clipToCircleRadius ?: 1.0)
        }
        clipToPolygon?.let {
            builder.clipToPolygon(*it)
        }
        clipToBoundingBox?.let {
            builder.clipToBoundingBox(it)
        }
    }
}
