package com.what3words.androidwrapper.voice

import androidx.core.util.Consumer
import com.what3words.androidwrapper.What3WordsAndroidWrapper
import com.what3words.androidwrapper.helpers.DefaultDispatcherProvider
import com.what3words.androidwrapper.helpers.DispatcherProvider
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.APIError
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class VoiceBuilder(
    private val api: What3WordsAndroidWrapper,
    private val mic: Microphone,
    private val voiceLanguage: String,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : VoiceApiListener {
    private var clipToPolygon: Array<Coordinates>? = null
    private var clipToBoundingBox: BoundingBox? = null
    private var clipToCircle: Coordinates? = null
    private var clipToCircleRadius: Double? = null
    private var clipToCountry: Array<String>? = null
    private var nFocusResults: Int? = null
    private var focus: Coordinates? = null
    private var nResults: Int? = null
    private var onSuggestionsCallback: Consumer<List<Suggestion>>? = null
    private var onErrorCallback: Consumer<APIResponse.What3WordsError>? = null
    private var isListening = false

    /**
     * onSuggestions callback will be called when VoiceAPI returns a set of suggestion after
     * receiving the voice data, this can be empty in case of no suggestions available for the provided voice record.
     *
     * @param callback with a list of [Suggestion] returned by our VoiceAPI
     * @return a [VoiceBuilder] instance
     */
    fun onSuggestions(callback: Consumer<List<Suggestion>>): VoiceBuilder {
        this.onSuggestionsCallback = callback
        return this
    }

    /**
     * onError callback will be called when some API error occurs on the VoiceAPI
     *
     * @param callback will be called when an [APIResponse.What3WordsError] occurs
     * @return a [VoiceBuilder] instance
     */
    fun onError(callback: Consumer<APIResponse.What3WordsError>): VoiceBuilder {
        this.onErrorCallback = callback
        return this
    }

    override fun connected(voiceProvider: VoiceProvider) {
        mic.startRecording(voiceProvider)
    }

    override fun suggestions(suggestions: List<Suggestion>) {
        mic.stopRecording()
        isListening = false
        CoroutineScope(dispatchers.main()).launch {
            onSuggestionsCallback?.accept(suggestions)
        }
    }

    override fun error(message: APIError) {
        mic.stopRecording()
        isListening = false
        // look for the error within the available error enums
        var errorEnum = APIResponse.What3WordsError.get(message.code)

        // Haven't found the error, return UNKNOWN_ERROR
        if (errorEnum == null) {
            errorEnum = APIResponse.What3WordsError.UNKNOWN_ERROR
        }
        errorEnum.message = message.message
        CoroutineScope(dispatchers.main()).launch {
            onErrorCallback?.accept(errorEnum)
        }
    }

    /**
     * startListening() starts the [Microphone] recording and starts sending voice data to our VoiceAPI.
     *
     * @return a [VoiceBuilder] instance
     */
    fun startListening(): VoiceBuilder {
        isListening = true
        api.voiceProvider.initialize(
            mic.recordingRate,
            mic.encoding,
            url = createSocketUrl(api.voiceProvider.baseUrl),
            listener = this
        )
        return this
    }

    /**
     * isListening() can be used to check if is currently in recording state.
     *
     * @return a [link VoiceBuilder] instance
     */
    fun isListening(): Boolean {
        return isListening
    }

    /**
     * stopListening() forces the [Microphone] to stop recording and closes the socket with our VoiceAPI.
     *
     * @return a [VoiceBuilder] instance
     */
    fun stopListening() {
        isListening = false
        mic.stopRecording()
        api.voiceProvider.forceStop()
    }

    /**
     * This is a location, specified as a latitude (often where the user making the query is). If specified, the results will be weighted to
     * give preference to those near the focus. For convenience, longitude is allowed to wrap around the 180 line, so 361 is equivalent to 1.
     *
     * @param coordinates the focus to use
     * @return a [VoiceBuilder] instance
     */
    fun focus(coordinates: Coordinates?): VoiceBuilder {
        focus = coordinates
        return this
    }

    /**
     * Set the number of AutoSuggest results to return. A maximum of 100 results can be specified, if a number greater than this is requested,
     * this will be truncated to the maximum. The default is 3
     *
     * @param n the number of AutoSuggest results to return
     * @return a [VoiceBuilder] instance
     */
    fun nResults(n: Int?): VoiceBuilder {
        nResults = n
        return this
    }

    /**
     * Specifies the number of results (must be &lt;= nResults) within the results set which will have a focus. Defaults to nResults.
     * This allows you to run autosuggest with a mix of focussed and unfocussed results, to give you a "blend" of the two. This is exactly what the old V2
     * standardblend did, and standardblend behaviour can easily be replicated by passing nFocusResults=1,
     * which will return just one focussed result and the rest unfocussed.
     *
     * @param n number of results within the results set which will have a focus
     * @return a [VoiceBuilder] instance
     */
    fun nFocusResults(n: Int?): VoiceBuilder {
        nFocusResults = n
        return this
    }

    /**
     * Restrict autosuggest results to a circle, specified by [Coordinates] representing the [centre] of the circle, plus the
     * [radius] in kilometres. For convenience, longitude is allowed to wrap around 180 degrees. For example 181 is equivalent to -179.
     *
     * @param centre the centre of the circle
     * @param radius the radius of the circle in kilometres
     * @return a [VoiceBuilder] instance
     */
    fun clipToCircle(
        centre: Coordinates?,
        radius: Double? = 1.0
    ): VoiceBuilder {
        clipToCircle = centre
        clipToCircleRadius = radius
        return this
    }

    /**
     * Restricts autosuggest to only return results inside the countries specified by comma-separated list of uppercase ISO 3166-1 alpha-2 country codes
     * (for example, to restrict to Belgium and the UK, use clipToCountry("GB", "BE"). clipToCountry will also accept lowercase
     * country codes. Entries must be two a-z letters. WARNING: If the two-letter code does not correspond to a country, there is no error: API simply
     * returns no results.
     *
     * @param countryCodes countries to clip results too
     * @return a [VoiceBuilder] instance
     */
    fun clipToCountry(countryCodes: List<String>): VoiceBuilder {
        clipToCountry = if (countryCodes.isNotEmpty()) countryCodes.toTypedArray() else null
        return this
    }

    /**
     * Restrict autosuggest results to a [BoundingBox].
     *
     * @param boundingBox [BoundingBox] to clip results too
     * @return a [VoiceBuilder] instance
     */
    fun clipToBoundingBox(
        boundingBox: BoundingBox?
    ): VoiceBuilder {
        clipToBoundingBox = boundingBox
        return this
    }

    /**
     * Restrict autosuggest results to a [polygon], specified by a collection of [Coordinates]. The polygon should be closed,
     * i.e. the first element should be repeated as the last element; also the list should contain at least 4 entries. The API is currently limited to
     * accepting up to 25 pairs.
     *
     * @param polygon the list of [Coordinates] that form the polygon to clip results too
     * @return a [VoiceBuilder] instance
     */
    fun clipToPolygon(
        polygon: List<Coordinates>
    ): VoiceBuilder {
        clipToPolygon = if (polygon.isNotEmpty()) polygon.toTypedArray() else null
        return this
    }

    internal fun createSocketUrl(baseUrl: String): String {
        var url = "${baseUrl}${VoiceApi.URL_WITHOUT_COORDINATES}"
        url += if (voiceLanguage == "zh") "?voice-language=cmn"
        else "?voice-language=$voiceLanguage"
        nResults?.let {
            url += "&n-results=$nResults"
        }
        focus?.let {
            url += "&focus=${focus!!.lat},${focus!!.lng}"
            if (nFocusResults != null) {
                url += "&n-focus-results=$nFocusResults"
            }
        }
        clipToCountry?.let {
            url += "&clip-to-country=${it.joinToString(",")}"
        }
        clipToCircle?.let {
            url += "&clip-to-circle=${it.lat},${it.lng},${clipToCircleRadius ?: 1}"
        }
        clipToPolygon?.let {
            url += "&clip-to-polygon=${it.joinToString(",") { coordinates -> "${coordinates.lat},${coordinates.lng}" }}"
        }
        clipToBoundingBox?.let {
            url += "&clip-to-bounding-box=${it.sw.lat},${it.sw.lng},${it.ne.lat},${it.ne.lng}"
        }
        return url
    }
}
