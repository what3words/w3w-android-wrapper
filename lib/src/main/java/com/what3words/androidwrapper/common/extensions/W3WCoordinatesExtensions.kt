package com.what3words.androidwrapper.common.extensions

import android.location.Location
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WDistance

/**
 * Extension methods for geometry calculations on What3words core types.
 */
internal object W3WCoordinatesExtensions {

    /**
     * Distance between this coordinate and [other], using the WGS84 ellipsoid.
     */
    fun W3WCoordinates.distanceTo(other: W3WCoordinates): W3WDistance {
        val results = FloatArray(1)
        Location.distanceBetween(lat, lng, other.lat, other.lng, results)
        return W3WDistance(results[0] / 1000.0)
    }
}
