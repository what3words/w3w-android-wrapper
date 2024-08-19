package com.what3words.androidwrapper.datasource.text.e2e

import com.what3words.androidwrapper.BuildConfig.PRE_PROD_API_KEY
import com.what3words.androidwrapper.BuildConfig.PRE_PROD_API_URL
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.androidwrapper.datasource.text.api.error.InvalidKeyError
import com.what3words.core.types.common.W3WResult
import com.what3words.javawrapper.What3WordsV3
import com.what3words.javawrapper.response.APIResponse
import org.junit.Assert
import org.junit.Test

class IsValid3waTest {
    private val api = W3WApiTextDataSource.create(
        apiKey = PRE_PROD_API_KEY, endPoint = PRE_PROD_API_URL
    )

    @Test
    fun testIsValid3waValidWords() {
        val response = api.isValid3wa("filled.count.soap")
        Assert.assertTrue(response is W3WResult.Success && response.value)
    }

    @Test
    fun testIsValid3waInvalidWords() {
        val response = api.isValid3wa("filled.count.sos")
        Assert.assertTrue(response is W3WResult.Success && !response.value)
    }

    @Test
    fun testIsValid3waApiKeyError() {
        val errorApi = W3WApiTextDataSource.create(
            apiKey = "NOKEY", endPoint = PRE_PROD_API_URL
        )
        val response = errorApi.isValid3wa("filled.count.soap")
        Assert.assertTrue(response is W3WResult.Failure && response.error is InvalidKeyError)
    }
}