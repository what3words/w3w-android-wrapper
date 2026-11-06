package com.what3words.androidwrapper.common.extensions

import com.what3words.androidwrapper.common.extensions.W3WCoordinatesExtensions.distanceTo
import com.what3words.core.types.domain.W3WSuggestion
import com.what3words.core.types.geometry.W3WCoordinates

internal object W3WSuggestionExtensions {

    /**
     * The API rounds the distance to focus to whole kilometres, so it is recalculated locally whenever
     * a [focus] was requested and the suggestion has coordinates.
     */
    fun List<W3WSuggestion>.withRecalculatedDistanceToFocus(
        focus: W3WCoordinates?
    ): List<W3WSuggestion> {
        if (focus == null) return this
        return map { suggestion ->
            suggestion.w3wAddress.center?.let {
                suggestion.copy(distanceToFocus = focus.distanceTo(it))
            } ?: suggestion
        }
    }
}
