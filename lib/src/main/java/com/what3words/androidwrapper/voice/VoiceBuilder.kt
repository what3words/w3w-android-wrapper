package com.what3words.androidwrapper.voice

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioRecord.STATE_INITIALIZED
import android.media.MediaRecorder
import android.util.Log
import androidx.core.util.Consumer
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.androidwrapper.voice.VoiceBuilder.Microphone.Companion.RECORDING_RATE
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.APIError
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import okio.ByteString
import kotlin.math.abs

class VoiceBuilder(
    private val api: What3WordsV3,
    private val mic: Microphone,
    private val voiceLanguage: String
) : VoiceApiListener {
    private var clipToPolygon: Array<Coordinates>? = null
    private var clipToBoundingBox: BoundingBox? = null
    private var clipToCircle: Coordinates? = null
    private var clipToCircleRadius: Double? = null
    private var clipToCountry: Array<String>? = null
    private var nFocusResults: Int? = null
    private var focus: Coordinates? = null
    private var nResults: Int = 3
    private var onSuggestionsCallback: Consumer<List<Suggestion>>? = null
    private var onErrorCallback: Consumer<APIResponse.What3WordsError>? = null
    private var isListening = false

    init {
        api.voiceApi.listener = this
    }

    override fun connected(socket: WebSocket) {
        mic.startRecording(socket)
    }

    override fun suggestions(suggestions: List<Suggestion>) {
        mic.stopRecording()
        isListening = false
        CoroutineScope(Dispatchers.Main).launch {
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
        CoroutineScope(Dispatchers.Main).launch {
            onErrorCallback?.accept(errorEnum)
        }
    }

    fun onSuggestions(callback: Consumer<List<Suggestion>>): VoiceBuilder {
        this.onSuggestionsCallback = callback
        return this
    }

    fun onError(callback: Consumer<APIResponse.What3WordsError>): VoiceBuilder {
        this.onErrorCallback = callback
        return this
    }

    fun startListening(): VoiceBuilder {
        isListening = true
        api.voiceApi.open(
            RECORDING_RATE,
            url = createSocketUrl()
        )
        return this
    }

    fun isListening(): Boolean {
        return isListening
    }

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
    fun focus(coordinates: Coordinates?): VoiceBuilder {
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
    fun nResults(n: Int?): VoiceBuilder {
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
    fun nFocusResults(n: Int?): VoiceBuilder {
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
        radius: Double?
    ): VoiceBuilder {
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
    fun clipToCountry(countryCodes: List<String>): VoiceBuilder {
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
    ): VoiceBuilder {
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
    ): VoiceBuilder {
        clipToPolygon = if (polygon.isNotEmpty()) polygon.toTypedArray() else null
        return this
    }

    private fun createSocketUrl(): String {
        var url = VoiceApi.BASE_URL
        url += if (voiceLanguage == "zh") "?voice-language=cmn"
        else "?voice-language=$voiceLanguage"
        url += "&n-results=$nResults"
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

    class Microphone() {
        companion object {
            const val RECORDING_RATE = 44100
            const val CHANNEL = AudioFormat.CHANNEL_IN_MONO
            const val FORMAT = AudioFormat.ENCODING_PCM_16BIT
        }

        private var onListeningCallback: Consumer<Float?>? = null
        private var onErrorCallback: Consumer<String>? = null

        private val bufferSize = AudioRecord.getMinBufferSize(
            RECORDING_RATE, CHANNEL, FORMAT
        )

        private var recorder: AudioRecord? = null
        private var continueRecording: Boolean = false

        fun onListening(callback: Consumer<Float?>): Microphone {
            this.onListeningCallback = callback
            return this
        }

        fun onError(callback: Consumer<String>): Microphone {
            this.onErrorCallback = callback
            return this
        }

        internal fun stopRecording() {
            continueRecording = false
            recorder?.release()
        }

        internal fun startRecording(socket: WebSocket) {
            recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                RECORDING_RATE,
                CHANNEL,
                FORMAT,
                bufferSize
            ).also { audioRecord ->
                if (audioRecord.state == STATE_INITIALIZED) {
                    continueRecording = true
                    CoroutineScope(Dispatchers.IO).launch {
                        val buffer = ByteArray(bufferSize)
                        var oldTimestamp = System.currentTimeMillis()
                        audioRecord.startRecording()
                        while (continueRecording) {
                            audioRecord.read(buffer, 0, buffer.size)
                            if ((System.currentTimeMillis() - oldTimestamp) > 100) {
                                oldTimestamp = System.currentTimeMillis()
                                val dB =
                                    VoiceSignalParser.transform(buffer.map { abs(it.toDouble()) }
                                        .sum())
                                CoroutineScope(Dispatchers.Main).launch {
                                    onListeningCallback?.accept(dB)
                                }
                            }
                            socket.send(ByteString.of(*buffer))
                        }
                    }
                } else {
                    Log.e(
                        "VoiceBuilder",
                        "Failed to initialize AudioRecord, please request AUDIO_RECORD permission."
                    )
                    CoroutineScope(Dispatchers.Main).launch {
                        onErrorCallback?.accept("Failed to initialize AudioRecord, please request AUDIO_RECORD permission.")
                    }
                }
            }
        }
    }
}