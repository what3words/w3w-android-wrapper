package com.what3words.androidwrapper.datasource.text.e2e

import com.what3words.androidwrapper.BuildConfig.PRE_PROD_API_KEY
import com.what3words.androidwrapper.BuildConfig.PRE_PROD_API_URL
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.androidwrapper.datasource.text.api.error.BadBoundingBoxTooBigError
import com.what3words.core.types.common.W3WResult
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WRectangle
import com.what3words.javawrapper.What3WordsV3
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.APIResponse.What3WordsError
import org.junit.Assert
import org.junit.Test

class GridSectionTest {
    private val api = W3WApiTextDataSource.create(
        apiKey = PRE_PROD_API_KEY, endPoint = PRE_PROD_API_URL
    )

    @Test
    fun invalidGridSectionTest() {
        val response =
            api.gridSection(W3WRectangle(W3WCoordinates(51.0, 0.0), W3WCoordinates(52.0, 0.1)))

        Assert.assertTrue(response is W3WResult.Failure && response.error is BadBoundingBoxTooBigError)
    }

    @Test
    fun validGridSectionTest() {
        val response = api.gridSection(
            W3WRectangle(
                W3WCoordinates(51.1122, 0.12221),
                W3WCoordinates(51.1333, 0.1223)
            )
        )

        Assert.assertTrue(response is W3WResult.Success && response.value.lines.isNotEmpty())
    }
}

