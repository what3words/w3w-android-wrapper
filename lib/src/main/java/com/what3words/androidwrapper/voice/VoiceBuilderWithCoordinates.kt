package com.what3words.androidwrapper.voice

import androidx.core.util.Consumer
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.androidwrapper.helpers.DefaultDispatcherProvider
import com.what3words.androidwrapper.helpers.DispatcherProvider
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.APIError
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.WebSocket

class VoiceBuilderWithCoordinates(
    private val api: What3WordsV3,
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
     * onSuggestions callback will be called when VoiceAPI returns a set of suggestion with coordinates after
     * receiving the voice data, this can be empty in case of no suggestions available for the provided voice record.
     *
     * @param callback with a list of {@link SuggestionWithCoordinates} returned by our VoiceAPI
     * @return a {@link VoiceBuilder} instance
     */
    fun onSuggestions(callback: Consumer<List<SuggestionWithCoordinates>>): VoiceBuilderWithCoordinates {
        this.onSuggestionsCallback = callback
        return this
    }

    /**
     * onError callback will be called when some API error occurs on the VoiceAPI
     *
     * @param callback will be called when an {@link APIResponse.What3WordsError} occurs
     * @return a {@link VoiceBuilder} instance
     */
    fun onError(callback: Consumer<APIResponse.What3WordsError>): VoiceBuilderWithCoordinates {
        this.onErrorCallback = callback
        return this
    }

    override fun connected(socket: WebSocket) {
        mic.startRecording(socket)
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
     * startListening() starts the {@link Microphone} recording and starts sending voice data to our VoiceAPI.
     *
     * @return a {@link VoiceBuilder} instance
     */
    fun startListening(): VoiceBuilderWithCoordinates {
        isListening = true
        api.voiceApi.open(
            mic.recordingRate,
            mic.encoding,
            url = createSocketUrl(),
            listener = this
        )
        return this
    }

    /**
     * isListening() can be used to check if is currently in recording state.
     *
     * @return a {@link VoiceBuilder} instance
     */
    fun isListening(): Boolean {
        return isListening
    }

    /**
     * stopListening() forces the {@link Microphone} to stop recording and closes the socket with our VoiceAPI.
     *
     * @return a {@link VoiceBuilder} instance
     */
    fun stopListening() {
        isListening = false
        mic.stopRecording()
        api.voiceApi.forceStop()
    }

    /**
     * This is a location, specified as a latitude (often where the user making the query is). If specified, the results will be weighted to
     * give preference to those near the <code>focus</code>. For convenience, longitude is allowed to wrap around the 180 line, so 361 is equivalent to 1.
     *
     * @param coordinates the focus to use
     * @return a {@link W3WAutoSuggestEditText} instance
     */
    fun focus(coordinates: Coordinates?): VoiceBuilderWithCoordinates {
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
    fun nResults(n: Int?): VoiceBuilderWithCoordinates {
        nResults = n ?: 3
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
    fun nFocusResults(n: Int?): VoiceBuilderWithCoordinates {
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
        centre: Coordinates?,
        radius: Double? = 1.0
    ): VoiceBuilderWithCoordinates {
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
    fun clipToCountry(countryCodes: List<String>): VoiceBuilderWithCoordinates {
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
        boundingBox: BoundingBox?
    ): VoiceBuilderWithCoordinates {
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
    ): VoiceBuilderWithCoordinates {
        clipToPolygon = if (polygon.isNotEmpty()) polygon.toTypedArray() else null
        return this
    }

    private fun createSocketUrl(): String {
        var url = VoiceApi.BASE_URL_WITH_COORDINATES
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
