package com.what3words.androidwrapper.common.extensions

import android.media.AudioFormat
import com.what3words.core.datasource.voice.audiostream.W3WAudioStreamEncoding
import com.what3words.core.types.domain.W3WCountry
import com.what3words.core.types.geometry.W3WCircle
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WPolygon
import com.what3words.core.types.geometry.W3WRectangle
import com.what3words.core.types.language.W3WLanguage
import com.what3words.core.types.options.W3WAutosuggestOptions

/**
 * Extension methods to convert What3words core types to strings for API requests.
 */
internal object W3WDomainToApiStringExtensions {
    /**
     * Converts a list of [W3WCountry] to a string format suitable for API requests.
     */
    fun List<W3WCountry>.toAPIString(): String {
        return this.joinToString(separator = ",") {
            it.twoLetterCode
        }
    }

    /**
     * Converts [W3WCoordinates] to a string format suitable for API requests.
     */
    fun W3WCoordinates.toAPIString(): String {
        return listOf(
            "$lat",
            "$lng",
        ).joinToString(separator = ",")
    }

    /**
     * Converts [W3WPolygon] to a string format suitable for API requests.
     */
    fun W3WPolygon.toAPIString(): String {
        return points.joinToString(separator = ",") {
            "${it.lat},${it.lng}"
        }
    }

    /**
     * Converts [W3WCircle] to a string format suitable for API requests.
     */
    fun W3WCircle.toAPIString(): String {
        return listOf(
            "${center.lat}",
            "${center.lng}",
            "${radius.km()}"
        ).joinToString(separator = ",")
    }

    /**
     * Converts a nullable [W3WRectangle] to a string format suitable for API requests.
     */
    fun W3WRectangle?.toAPIString(): String {
        return this?.let {
            listOf(
                "${it.southwest.lat}",
                "${it.southwest.lng}",
                "${it.northeast.lat}",
                "${it.northeast.lng}"
            ).joinToString(separator = ",")
        } ?: ""
    }

    /**
     * Converts [W3WAutosuggestOptions] to a map of query parameters suitable for API requests.
     */
    fun W3WAutosuggestOptions.toQueryMap(): Map<String, String> {
        return buildMap {
            nResults.let {
                "n-results" to it
            }
            focus?.let {
                "focus" to it.toAPIString()
            }
            language?.let {
                "language" to it.w3wCode
                "locale" to it.w3wLocale
            }
            nFocusResults?.let {
                "n-focus-results" to it
            }
            clipToCountry.let {
                "clip-to-country to ${
                    it.joinToString(",") { w3wCountry ->
                        w3wCountry.twoLetterCode
                    }
                }"
            }
            clipToCircle?.let {
                "clip-to-circle" to it.toAPIString()
            }
            clipToPolygon?.let {
                "clip-to-polygon" to it.toAPIString()
            }
            clipToBoundingBox?.let {
                "clip-to-bounding-box" to it.toAPIString()
            }
            inputType?.let {
                "input-type" to it
            }
            preferLand.let {
                "prefer-land" to it.toString()
            }

        }
    }

    /**
     * Converts [W3WAudioStreamEncoding] to a string format suitable for API requests.
     */
    fun W3WAudioStreamEncoding.toApiString(): String {
        return when (this.value) {
            AudioFormat.ENCODING_PCM_16BIT -> "pcm_s16le"
            AudioFormat.ENCODING_PCM_FLOAT -> "pcm_f32le"
            AudioFormat.ENCODING_PCM_8BIT -> "mulaw"
            else -> "pcm_s16le"
        }
    }

    /**
     * Converts [W3WLanguage] to a string format suitable for voice api request
     */
    fun W3WLanguage.toVoiceApiString(): String {
        return when (w3wCode) {
            "zh" -> "cmn"
            else -> w3wCode
        }
    }
}