package com.what3words.androidwrapper.datasource.text.e2e

import com.what3words.androidwrapper.BuildConfig.PRE_PROD_API_KEY
import com.what3words.androidwrapper.BuildConfig.PRE_PROD_API_URL
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.androidwrapper.datasource.text.api.error.BadCoordinatesError
import com.what3words.core.types.common.W3WResult
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.language.W3WRFC5646Language
import com.what3words.javawrapper.What3WordsV3
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.APIResponse.What3WordsError
import org.junit.Assert
import org.junit.Test

class ConvertTo3WATest {
    private val api = W3WApiTextDataSource.create(
        apiKey = PRE_PROD_API_KEY, endPoint = PRE_PROD_API_URL
    )
    @Test
    fun twoInvalidCoordsTest() {
        val response = api.convertTo3wa(W3WCoordinates(-200.0, -200.0), W3WRFC5646Language.EN_GB)

        Assert.assertTrue(response is W3WResult.Failure && response.error is BadCoordinatesError)
    }

    @Test
    fun validCoordsTest() {
        val twa = api.convertTo3wa(W3WCoordinates(51.520847, -0.19552100), W3WRFC5646Language.EN_GB)

        Assert.assertTrue(twa is W3WResult.Success)
        twa as W3WResult.Success

        Assert.assertEquals("filled.count.soap", twa.value.words)
        Assert.assertEquals("GB", twa.value.country.twoLetterCode)

        Assert.assertEquals(-0.195543, twa.value.square!!.southwest.lng, 0.0)
        Assert.assertEquals(51.520833, twa.value.square!!.southwest.lat, 0.0)
        Assert.assertEquals(-0.195499, twa.value.square!!.northeast.lng, 0.0)
        Assert.assertEquals(51.52086, twa.value.square!!.northeast.lat, 0.0)
        Assert.assertEquals(-0.195521, twa.value.center!!.lng, 0.0)
        Assert.assertEquals(51.520847, twa.value.center!!.lat, 0.0)

        Assert.assertEquals("en", twa.value.language.w3wCode)
        Assert.assertEquals("Bayswater, London", twa.value.nearestPlace)
    }


    @Test
    fun validCoordsWithLanguageTest() {
        val twa = api.convertTo3wa(W3WCoordinates(51.520847, -0.19552100), W3WRFC5646Language.PT_PT)

        Assert.assertTrue(twa is W3WResult.Success)
        twa as W3WResult.Success

        Assert.assertEquals("refrigerando.valem.touro", twa.value.words)
        Assert.assertEquals("GB", twa.value.country.twoLetterCode)

        Assert.assertEquals(-0.195543, twa.value.square!!.southwest.lng, 0.0)
        Assert.assertEquals(51.520833, twa.value.square!!.southwest.lat, 0.0)
        Assert.assertEquals(-0.195499, twa.value.square!!.northeast.lng, 0.0)
        Assert.assertEquals(51.52086, twa.value.square!!.northeast.lat, 0.0)

        Assert.assertEquals(-0.195521, twa.value.center!!.lng, 0.0)
        Assert.assertEquals(51.520847, twa.value.center!!.lat, 0.0)

        Assert.assertEquals("pt", twa.value.language.w3wCode)
        Assert.assertEquals("Londres", twa.value.nearestPlace)
    }


    @Test
    fun validCoordsWithLocaleTest() {
        val twa = api.convertTo3wa(W3WCoordinates(51.520847, -0.19552100), W3WRFC5646Language.MN_LATN)

        Assert.assertTrue(twa is W3WResult.Success)
        twa as W3WResult.Success

        Assert.assertEquals("seruuhen.zemseg.dagaldah", twa.value.words)
        Assert.assertEquals("GB", twa.value.country.twoLetterCode)

        Assert.assertEquals(-0.195543, twa.value.square!!.southwest.lng, 0.0)
        Assert.assertEquals(51.520833, twa.value.square!!.southwest.lat, 0.0)
        Assert.assertEquals(-0.195499, twa.value.square!!.northeast.lng, 0.0)
        Assert.assertEquals(51.52086, twa.value.square!!.northeast.lat, 0.0)
        Assert.assertEquals(-0.195521, twa.value.center!!.lng, 0.0)
        Assert.assertEquals(51.520847, twa.value.center!!.lat, 0.0)

        Assert.assertEquals("mn", twa.value.language.w3wCode)
        Assert.assertEquals("mn_la", twa.value.language.w3wLocale)
        Assert.assertEquals("Лондон", twa.value.nearestPlace)
    }
}
