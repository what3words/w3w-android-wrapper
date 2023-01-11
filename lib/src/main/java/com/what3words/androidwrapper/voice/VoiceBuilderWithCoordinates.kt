package com.what3words.androidwrapper.voice

import androidx.core.util.Consumer
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.androidwrapper.What3WordsAndroidWrapper
import com.what3words.androidwrapper.helpers.DefaultDispatcherProvider
import com.what3words.androidwrapper.helpers.DispatcherProvider
import com.what3words.androidwrapper.voice.VoiceApi.Companion.URL_WITH_COORDINATES
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.APIError
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class VoiceBuilderWithCoordinates(
    private val api: What3WordsAndroidWrapper,
    private val mic: Microphone,
    private val voiceLanguage: String,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : VoiceApiListenerWithCoordinates {
    private var clipToPolygon: Array<Coordinates>? = null
    private var clipToBoundingBox: BoundingBox? = null
    private var clipToCircle: Coordinates? = null
    private var clipToCircleRadius: Double? = null
    private var clipToCountry: Array<String>? = null
    private var nFocusResults: Int? = null
    private var focus: Coordinates? = null
    private var nResults: Int? = null
    private var onSuggestionsCallback: Consumer<List<SuggestionWithCoordinates>>? = null
    private var onErrorCallback: Consumer<APIResponse.What3WordsError>? = null
    private var isListening = false

    /**
     * onSuggestions callback will be called when VoiceAPI returns a set of [SuggestionWithCoordinates] with coordinates after
     * receiving the voice data, this can be empty in case of no suggestions available for the provided voice record.
     *
     * @param callback with a list of [SuggestionWithCoordinates] returned by our VoiceAPI
     * @return a [VoiceBuilder] instance
     */
    fun onSuggestions(callback: Consumer<List<SuggestionWithCoordinates>>): VoiceBuilderWithCoordinates {
        this.onSuggestionsCallback = callback
        return this
    }

    /**
     * onError callback will be called when some API error occurs on the VoiceAPI
     *
     * @param callback will be called when an [APIResponse.What3WordsError] occurs
     * @return a [VoiceBuilderWithCoordinates] instance
     */
    fun onError(callback: Consumer<APIResponse.What3WordsError>): VoiceBuilderWithCoordinates {
        this.onErrorCallback = callback
        return this
    }

    override fun connected(voiceProvider: VoiceProvider) {
        mic.startRecording(voiceProvider)
    }

    override fun suggestionsWithCoordinates(suggestions: List<SuggestionWithCoordinates>) {
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
     * [startListening] starts the [Microphone] recording and starts sending voice data to our VoiceAPI.
     *
     * @return a [VoiceBuilderWithCoordinates] instance
     */
    fun startListening(): VoiceBuilderWithCoordinates {
        isListening = true
        api.voiceProvider.initialize(
            mic.recordingRate,
            mic.encoding,
            url = createSocketUrlWithCoordinates(api.voiceProvider.baseUrl),
            listener = this
        )
        return this
    }

    /**
     * [isListening] can be used to check if is currently in recording state.
     *
     * @return a [VoiceBuilderWithCoordinates] instance
     */
    fun isListening(): Boolean {
        return isListening
    }

    /**
     * [stopListening] forces the [Microphone] to stop recording and closes the socket with our VoiceAPI.
     *
     * @return a [VoiceBuilderWithCoordinates] instance
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
     * @return a [VoiceBuilderWithCoordinates] instance
     */
    fun focus(coordinates: Coordinates?): VoiceBuilderWithCoordinates {
        focus = coordinates
        return this
    }

    /**
     * Set the number of AutoSuggest results to return. A maximum of 100 results can be specified, if a number greater than this is requested,
     * this will be truncated to the maximum. The default is 3
     *
     * @param n the number of results to return
     * @return a [VoiceBuilderWithCoordinates] instance
     */
    fun nResults(n: Int?): VoiceBuilderWithCoordinates {
        nResults = n ?: 3
        return this
    }

    /**
     * Specifies the number of results within the results set which will have a focus. Defaults to [nResults].
     * This allows you to run autosuggest with a mix of focussed and unfocussed results, to give you a "blend" of the two. This is exactly what the old V2
     * standardblend did, and standardblend behaviour can easily be replicated by passing [nFocusResults]=1,
     * which will return just one focussed result and the rest unfocussed.
     *
     * @param n number of results within the results set which will have a focus
     * @return a [VoiceBuilderWithCoordinates] instance
     */
    fun nFocusResults(n: Int?): VoiceBuilderWithCoordinates {
        nFocusResults = n
        return this
    }

    /**
     * Restrict autosuggest results to a circle, specified by [Coordinates] representing the centre of the circle, plus the
     * [radius] in kilometres. For convenience, longitude is allowed to wrap around 180 degrees. For example 181 is equivalent to -179.
     *
     * @param centre the centre of the circle
     * @param radius the radius of the circle in kilometres
     * @return a [VoiceBuilderWithCoordinates] instance
     */
    fun clipToCircle(
        centre: Coordinates?,
        radius: Double? = 1.0
    ): VoiceBuilderWithCoordinates {
        clipToCircle = centre
        clipToCircleRadius = radius
        return this
    }

    /**
     * Restricts autosuggest to only return results inside the countries specified by comma-separated list of uppercase ISO 3166-1 alpha-2 country codes
     * (for example, to restrict to Belgium and the UK, use [clipToCountry] ("GB", "BE"). [clipToCountry] will also accept lowercase
     * country codes. Entries must be two a-z letters. WARNING: If the two-letter code does not correspond to a country, there is no error: API simply
     * returns no results.
     *
     * @param countryCodes countries to clip results too
     * @return a [VoiceBuilderWithCoordinates] instance
     */
    fun clipToCountry(countryCodes: List<String>): VoiceBuilderWithCoordinates {
        clipToCountry = if (countryCodes.isNotEmpty()) countryCodes.toTypedArray() else null
        return this
    }

    /**
     * Restrict autosuggest results to a [BoundingBox].
     *
     * @param boundingBox [BoundingBox] to clip results too
     * @return a [VoiceBuilderWithCoordinates] instance
     */
    fun clipToBoundingBox(
        boundingBox: BoundingBox?
    ): VoiceBuilderWithCoordinates {
        clipToBoundingBox = boundingBox
        return this
    }

    /**
     * Restrict autosuggest results to a polygon, specified by a collection of [Coordinates]. The polygon should be closed,
     * i.e. the first element should be repeated as the last element; also the list should contain at least 4 entries. The API is currently limited to
     * accepting up to 25 pairs.
     *
     * @param polygon the list of [Coordinates] that form the polygon to clip results too
     * @return a [VoiceBuilderWithCoordinates] instance
     */
    fun clipToPolygon(
        polygon: List<Coordinates>
    ): VoiceBuilderWithCoordinates {
        clipToPolygon = if (polygon.isNotEmpty()) polygon.toTypedArray() else null
        return this
    }

    internal fun createSocketUrlWithCoordinates(baseUrl: String): String {
        var url = "${baseUrl}${URL_WITH_COORDINATES}"
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
            url += "&clip-to-polygon=${it.joinToString(",") { "${it.lat},${it.lng}" }}"
        }
        clipToBoundingBox?.let {
            url += "&clip-to-bounding-box=${it.sw.lat},${it.sw.lng},${it.ne.lat},${it.ne.lng}"
        }
        return url
    }
}
