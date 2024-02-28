package com.what3words.androidwrapper.datasource.text.api.extensions

import com.what3words.core.types.domain.W3WCountry
import com.what3words.core.types.geometry.W3WCircle
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WPolygon
import com.what3words.core.types.geometry.W3WRectangle
/**
 * Extension methods to convert What3words core types to strings for API requests.
 */
object W3WDomainToApiStringExtensions {
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
            "$radius"
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
}