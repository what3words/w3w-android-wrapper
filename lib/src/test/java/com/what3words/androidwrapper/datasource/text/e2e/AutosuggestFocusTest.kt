package com.what3words.androidwrapper.datasource.text.e2e

import com.what3words.androidwrapper.BuildConfig.PRE_PROD_API_KEY
import com.what3words.androidwrapper.BuildConfig.PRE_PROD_API_URL
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.androidwrapper.datasource.text.api.error.BadFocusError
import com.what3words.androidwrapper.datasource.text.api.error.BadLanguageError
import com.what3words.core.types.common.W3WResult
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.language.W3WLanguage
import com.what3words.core.types.language.W3WProprietaryLanguage
import com.what3words.core.types.language.W3WRFC5646Language
import com.what3words.core.types.options.W3WAutosuggestOptions
import com.what3words.javawrapper.What3WordsV3
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.APIResponse.What3WordsError
import org.junit.Assert
import org.junit.Test

class AutosuggestFocusTest {
    private val api = W3WApiTextDataSource.create(
        apiKey = PRE_PROD_API_KEY, endPoint = PRE_PROD_API_URL
    )

    @Test
    fun testValidFocus() {
        val autosuggest = api.autosuggest(
            "index.home.ra", W3WAutosuggestOptions.Builder().focus(
                W3WCoordinates(51.2, 0.2)
            ).build()
        )

        Assert.assertTrue(autosuggest is W3WResult.Success)
        autosuggest as W3WResult.Success

        var found = false
        for (s in autosuggest.value) {
            if (s.w3wAddress.words.equals("index.home.raft", ignoreCase = true)) {
                found = true
            }
        }

        Assert.assertTrue("Failed to find index.home.raft", found)
    }

    @Test
    fun testValidLocale() {
        val options = W3WAutosuggestOptions.Builder().focus(
            W3WCoordinates(51.0, 1.0)
        ).language(W3WRFC5646Language.MN_LATN).build()

        val autosuggest = api.autosuggest("a.a.a", options)

        Assert.assertTrue(autosuggest is W3WResult.Success)
        autosuggest as W3WResult.Success

        Assert.assertEquals("mn_la", autosuggest.value.first().w3wAddress.language.w3wLocale)
    }

    @Test
    fun testInvalidLocale() {
        val options = W3WAutosuggestOptions.Builder().focus(
            W3WCoordinates(51.0, 1.0)
        ).language(
            W3WProprietaryLanguage(
                locale = "mn_las",
                code = "mn",
                name = null,
                nativeName = null
            )
        ).build()
        val autosuggest = api.autosuggest("a.a.a", options)

        Assert.assertTrue(autosuggest is W3WResult.Failure && autosuggest.error is BadLanguageError)
    }

    @Test
    fun testInvalidLocaleButValidLanguage() {
        val options = W3WAutosuggestOptions.Builder().focus(
            W3WCoordinates(51.0, 1.0)
        ).language(
            W3WProprietaryLanguage(
                locale = "mn_las",
                code = "mn",
                name = null,
                nativeName = null
            )
        ).build()

        val autosuggest = api.autosuggest("a.a.a", options)

        Assert.assertTrue(autosuggest is W3WResult.Failure && autosuggest.error is BadLanguageError)
    }

    @Test
    fun testFocusLatitudeTooBig() {
        val response = api.autosuggest("index.home.ra", W3WAutosuggestOptions.Builder().focus(W3WCoordinates(151.2, 0.2)).build())
        Assert.assertTrue(response is W3WResult.Failure && response.error is BadFocusError)
    }


    @Test
    fun testFocusLatitudeTooSmall() {
        val response = api.autosuggest("index.home.ra", W3WAutosuggestOptions.Builder().focus(W3WCoordinates(-151.2, 0.2)).build())
        Assert.assertTrue(response is W3WResult.Failure && response.error is BadFocusError)
    }

    @Test
    fun testFocusBigLongitude() {
        val autosuggest = api.autosuggest("index.home.ra", W3WAutosuggestOptions.Builder().focus(W3WCoordinates(51.2, 360.2)).build())

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
    fun testFocusSmallLongitude() {
        val autosuggest = api.autosuggest("index.home.ra", W3WAutosuggestOptions.Builder().focus(W3WCoordinates(51.2, -360.0)).build())

        val suggestions = autosuggest as W3WResult.Success

        var found = false
        for (s in suggestions.value) {
            if (s.w3wAddress.words.equals("index.home.raft", ignoreCase = true)) {
                found = true
            }
        }

        Assert.assertTrue("Failed to find index.home.raft", found)
    }
}
