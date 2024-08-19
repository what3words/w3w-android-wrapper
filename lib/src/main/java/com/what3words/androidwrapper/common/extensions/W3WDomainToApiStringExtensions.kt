package com.what3words.androidwrapper.common.extensions

import android.media.AudioFormat
import com.what3words.core.datasource.voice.audiostream.W3WAudioStreamEncoding
import com.what3words.core.types.domain.W3WCountry
import com.what3words.core.types.geometry.W3WCircle
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WPolygon
import com.what3words.core.types.geometry.W3WRectangle
import com.what3words.core.types.geometry.km
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
            put("n-results", nResults.toString())
            focus?.let {
                put("focus", it.toAPIString())
            }
            language?.let {
                if (it.w3wLocale != null) put("locale", it.w3wLocale!!)
                else put("language", it.w3wCode)
            }
            nFocusResults?.let {
                put("n-focus-results", it.toString())
            }
            if (clipToCountry.isNotEmpty()) {
                put("clip-to-country", clipToCountry.toAPIString())
            }
            clipToCircle?.let {
                put("clip-to-circle", it.toAPIString())
            }
            clipToPolygon?.let {
                put("clip-to-polygon", it.toAPIString())
            }
            clipToBoundingBox?.let {
                put("clip-to-bounding-box", it.toAPIString())
            }
            inputType?.let {
                put("input-type", it.toString())
            }
            put("prefer-land", preferLand.toString())

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