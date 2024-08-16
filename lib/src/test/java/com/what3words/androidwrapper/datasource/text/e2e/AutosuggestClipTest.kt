package com.what3words.androidwrapper.datasource.text.e2e

import com.what3words.androidwrapper.BuildConfig.PRE_PROD_API_KEY
import com.what3words.androidwrapper.BuildConfig.PRE_PROD_API_URL
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.androidwrapper.datasource.text.api.error.BadClipToBoundingBoxError
import com.what3words.androidwrapper.datasource.text.api.error.BadClipToCircleError
import com.what3words.androidwrapper.datasource.text.api.error.BadClipToCountryError
import com.what3words.core.types.common.W3WResult
import com.what3words.core.types.domain.W3WCountry
import com.what3words.core.types.geometry.W3WCircle
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WDistance
import com.what3words.core.types.geometry.W3WRectangle
import com.what3words.core.types.options.W3WAutosuggestOptions
import com.what3words.javawrapper.What3WordsV3
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.APIResponse.What3WordsError
import org.junit.Assert
import org.junit.Test


class AutosuggestClipTest {
    private val api = W3WApiTextDataSource.create(
        apiKey = PRE_PROD_API_KEY, endPoint = PRE_PROD_API_URL
    )

    @Test
    fun testSimpleCircleClip() {
        val autosuggest = api.autosuggest(
            "index.home.ra", W3WAutosuggestOptions.Builder().clipToCircle(
                W3WCircle(
                    W3WCoordinates(-90.000000, 360.0),
                    W3WDistance(100.0)
                )
            ).build()
        )

        Assert.assertNotNull(autosuggest)
    }

    @Test
    fun testSimpleCircleClipLatCannotWrap() {
        val autosuggest = api.autosuggest(
            "index.home.ra",
            W3WAutosuggestOptions.Builder()
                .clipToCircle(W3WCircle(W3WCoordinates(-91.000000, 360.0), W3WDistance(100.0)))
                .build()
        )
        Assert.assertTrue(autosuggest is W3WResult.Failure && autosuggest.error is BadClipToCircleError)
    }

    @Test
    fun testSimpleCircleClipLatBigDistance() {
        val autosuggest = api.autosuggest(
            "index.home.ra",
            W3WAutosuggestOptions.Builder().clipToCircle(
                W3WCircle(
                    W3WCoordinates(0.000000, 0.0),
                    W3WDistance(10000000.0)
                )
            ).build()
        )

        Assert.assertNotNull(autosuggest)
    }

    @Test
    fun testSimpleCircleClipLatNegativeDistance() {
        val autosuggest = api.autosuggest(
            "index.home.ra",
            W3WAutosuggestOptions.Builder()
                .clipToCircle(W3WCircle(W3WCoordinates(0.000000, 0.0), W3WDistance(-1.0))).build()
        )
        Assert.assertNotNull(autosuggest)
    }

    @Test
    fun testBoundingBox() {
        val sw = W3WCoordinates(50.0, -5.0)
        val ne = W3WCoordinates(53.0, 2.0)
        val rect = W3WRectangle(sw, ne)

        val autosuggest = api.autosuggest("index.home.ra", W3WAutosuggestOptions.Builder().clipToBoundingBox(rect).build())

        val suggestions = autosuggest as W3WResult.Success
        var found = false
        for (s in suggestions.value) {
            if (s.w3wAddress.words.equals("index.home.raft", ignoreCase = true)) {
                found = true
            }
        }
        Assert.assertTrue("Failed to find index.home.raft", found)
    }

    @Test
    fun testBoundingBoxInfinitelySmall() {
        val sw = W3WCoordinates(50.0, -5.0)
        val ne = W3WCoordinates(50.0, -5.0)
        val rect = W3WRectangle(sw, ne)

        val autosuggest = api.autosuggest("index.home.ra", W3WAutosuggestOptions.Builder().clipToBoundingBox(rect).build())

        val suggestions = autosuggest as W3WResult.Success

        var found = false
        for (s in suggestions.value) {
            if (s.w3wAddress.words.equals("index.home.raft", ignoreCase = true)) {
                found = true
            }
        }

        Assert.assertTrue("Found index.home.raft", !found)
    }

    @Test
    fun testBoundingBoxLngWraps() {
        val sw = W3WCoordinates(50.0, -5.0)
        val ne = W3WCoordinates(53.0, 3544.0)

        val rect = W3WRectangle(sw, ne)
        val autosuggest = api.autosuggest("index.home.ra", W3WAutosuggestOptions.Builder().clipToBoundingBox(rect).build())

        val suggestions = autosuggest as W3WResult.Success
        var found = false
        for (s in suggestions.value) {
            if (s.w3wAddress.words.equals("index.home.raft", ignoreCase = true)) {
                found = true
            }
        }

        Assert.assertTrue("Failed to find index.home.raft", found)
    }

    @Test
    fun testBoundingBoxThatWrapsAroundWorldButExcludesLondon() {
        val sw = W3WCoordinates(50.0, 2.0)
        val ne = W3WCoordinates(53.0, (-5 + 360).toDouble())

        val rect = W3WRectangle(sw, ne)
        val autosuggest = api.autosuggest("index.home.ra", W3WAutosuggestOptions.Builder().clipToBoundingBox(rect).build())

        val suggestions = autosuggest as W3WResult.Success
        var found = false
        for (s in suggestions.value) {
            if (s.w3wAddress.words.equals("index.home.raft", ignoreCase = true)) {
                found = true
            }
        }

        Assert.assertTrue("Found index.home.raft", !found)
    }

    @Test
    fun testBoundingBoxThatWrapsAroundPolesButExcludesLondon() {
        val sw = W3WCoordinates(53.0, -5.0)
        val ne = W3WCoordinates((50 + 180).toDouble(), 2.0)

        val rect = W3WRectangle(sw, ne)
        val autosuggest = api.autosuggest("index.home.ra", W3WAutosuggestOptions.Builder().clipToBoundingBox(rect).build())

        Assert.assertTrue(autosuggest is W3WResult.Failure && autosuggest.error is BadClipToBoundingBoxError)
    }

    @Test
    fun clipToCountryThatDoesNotExist() {
        val autosuggest = api.autosuggest("index.home.raf", W3WAutosuggestOptions.Builder().clipToCountry(
            W3WCountry("ZX")).build())

        val suggestions = autosuggest as W3WResult.Success

        var found = false
        for (s in suggestions.value) {
            if (s.w3wAddress.words.equals("index.home.raft", ignoreCase = true)) {
                found = true
            }
        }

        Assert.assertTrue("Found index.home.raft", !found)
    }

    @Test
    fun clipToInvalidCountry() {
        val response = api.autosuggest("index.home.raf", W3WAutosuggestOptions.Builder().clipToCountry(W3WCountry("ZXC")).build())

        Assert.assertTrue(response is W3WResult.Failure && response.error is BadClipToCountryError)
    }
}
