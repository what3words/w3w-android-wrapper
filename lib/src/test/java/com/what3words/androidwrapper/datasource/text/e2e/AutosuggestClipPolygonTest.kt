package com.what3words.androidwrapper.datasource.text.e2e

import com.what3words.androidwrapper.BuildConfig.PRE_PROD_API_KEY
import com.what3words.androidwrapper.BuildConfig.PRE_PROD_API_URL
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.androidwrapper.datasource.text.api.error.BadClipToPolygonError
import com.what3words.core.types.common.W3WResult
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WPolygon
import com.what3words.core.types.options.W3WAutosuggestOptions
import com.what3words.javawrapper.What3WordsV3
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.APIResponse.What3WordsError
import org.junit.Assert
import org.junit.Test
import java.util.Arrays

class AutosuggestClipPolygonTest {
    private val api = W3WApiTextDataSource.create(
        apiKey = PRE_PROD_API_KEY, endPoint = PRE_PROD_API_URL
    )

    @Test
    fun testPolygonClip() {
        // Polygon must have at least 4 entries
        val p1 = W3WCoordinates(51.0, -1.0)
        val p2 = W3WCoordinates(53.0, 0.0)
        val p3 = W3WCoordinates(51.0, 1.0)

        val autosuggest = api.autosuggest("index.home.ra", W3WAutosuggestOptions.Builder().clipToPolygon(
            W3WPolygon(listOf(p1, p2, p3, p1))
        ).build())

        val suggestions = autosuggest as W3WResult.Success

        var found = false
        for (s in suggestions.value) {
            if (s.w3wAddress.words.equals("index.home.raft", ignoreCase = true)) {
                found = true
            }
        }

        Assert.assertTrue("Can't find index.home.raft", found)
    }

    @Test
    fun testPolygonClipWithTooFewPoints() {
        // Polygon must have at least 4 entries
        val p1 = W3WCoordinates(51.0, -1.0)
        val p2 = W3WCoordinates(53.0, 0.0)
        val p3 = W3WCoordinates(51.0, 1.0)

        val autosuggest = api.autosuggest("index.home.ra", W3WAutosuggestOptions.Builder().clipToPolygon(W3WPolygon(listOf(p1, p2, p3))).build())
        Assert.assertTrue(autosuggest is W3WResult.Failure && autosuggest.error is BadClipToPolygonError)
    }

    @Test
    fun testPolygonClipWithHugeLongitude() {
        //Polygon must have at least 4 entries
        val p1 = W3WCoordinates(51.0, (-1 - 180).toDouble())
        val p2 = W3WCoordinates(53.0, 0.0)
        val p3 = W3WCoordinates(51.0, (1 + 180).toDouble())

        val autosuggest = api.autosuggest("index.home.ra", W3WAutosuggestOptions.Builder().clipToPolygon(W3WPolygon(listOf(p1, p2, p3, p1))).build())

        val suggestions = autosuggest as W3WResult.Success

        var found = false
        for (s in suggestions.value) {
            if (s.w3wAddress.words.equals("index.home.raft", ignoreCase = true)) {
                found = true
            }
        }

        Assert.assertTrue("Can't find index.home.raft", found)
    }
}
