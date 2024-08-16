package com.what3words.androidwrapper.datasource.text.e2e

import com.what3words.androidwrapper.BuildConfig.PRE_PROD_API_KEY
import com.what3words.androidwrapper.BuildConfig.PRE_PROD_API_URL
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.androidwrapper.datasource.text.api.error.BadWordsError
import com.what3words.core.types.common.W3WResult
import org.junit.Assert
import org.junit.Test

class ConvertToCoordinatesTest {
    private val api = W3WApiTextDataSource.create(
        apiKey = PRE_PROD_API_KEY, endPoint = PRE_PROD_API_URL
    )

    @Test
    fun invalid3waTest() {
        val c2c = api.convertToCoordinates("filled")
        Assert.assertTrue(c2c is W3WResult.Failure && c2c.error is BadWordsError)
    }

    @Test
    fun valid3waTest() {
        val c2c = api.convertToCoordinates("filled.count.soap")

        Assert.assertTrue(c2c is W3WResult.Success)
        c2c as W3WResult.Success

        Assert.assertEquals("filled.count.soap", c2c.value.words)
        Assert.assertEquals("GB", c2c.value.country.twoLetterCode)

        Assert.assertEquals(-0.195543, c2c.value.square!!.southwest.lng, 0.0)
        Assert.assertEquals(51.520833, c2c.value.square!!.southwest.lat, 0.0)
        Assert.assertEquals(-0.195499, c2c.value.square!!.northeast.lng, 0.0)
        Assert.assertEquals(51.52086, c2c.value.square!!.northeast.lat, 0.0)

        Assert.assertEquals(-0.195521, c2c.value.center!!.lng, 0.0)
        Assert.assertEquals(51.520847, c2c.value.center!!.lat, 0.0)

        Assert.assertEquals("en", c2c.value.language.w3wCode)
        Assert.assertEquals("Bayswater, London", c2c.value.nearestPlace)
    }

    @Test
    fun valid3waWithLocaleTest() {
        val c2c = api.convertToCoordinates("seruuhen.zemseg.dagaldah")

        Assert.assertTrue(c2c is W3WResult.Success)
        c2c as W3WResult.Success

        Assert.assertEquals("seruuhen.zemseg.dagaldah", c2c.value.words)
        Assert.assertEquals("GB", c2c.value.country.twoLetterCode)

        Assert.assertEquals(-0.195543, c2c.value.square!!.southwest.lng, 0.0)
        Assert.assertEquals(51.520833, c2c.value.square!!.southwest.lat, 0.0)
        Assert.assertEquals(-0.195499, c2c.value.square!!.northeast.lng, 0.0)
        Assert.assertEquals(51.52086, c2c.value.square!!.northeast.lat, 0.0)

        Assert.assertEquals(-0.195521, c2c.value.center!!.lng, 0.0)
        Assert.assertEquals(51.520847, c2c.value.center!!.lat, 0.0)

        Assert.assertEquals("mn", c2c.value.language.w3wCode)
        Assert.assertEquals("mn_la", c2c.value.language.w3wLocale)
        Assert.assertEquals("Лондон", c2c.value.nearestPlace)
    }
}

