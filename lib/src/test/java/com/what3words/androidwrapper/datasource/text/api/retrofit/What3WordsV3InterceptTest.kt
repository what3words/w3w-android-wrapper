package com.what3words.androidwrapper.datasource.text.api.retrofit

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class What3WordsV3InterceptTest {

    private var mockWebServer = MockWebServer()

    @Before
    fun setup() {
        mockWebServer.start()
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `interceptor adds headers to request`() {
        // Arrange
        val apiKey = "api_key"
        val packageName = "com.what3words.android"
        val signature = "signature"
        val headers = mapOf("header1" to "value1", "header2" to "value2")

        val interceptor = What3WordsV3Interceptor(apiKey, packageName, signature, headers)

        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(interceptor)
            .build()

        val request = Request.Builder()
            .url(mockWebServer.url("/"))
            .build()

        mockWebServer.enqueue(MockResponse())

        // Act
        client.newCall(request).execute()
        val requestHeaders = mockWebServer.takeRequest().headers

        // Assert
        assert(requestHeaders["header1"] == "value1")
        assert(headers["header2"] == "value2")
        assert(apiKey == requestHeaders[W3WV3RetrofitApiClient.HEADER_WHAT3WORDS_API_KEY])
        assert(packageName == requestHeaders[W3WV3RetrofitApiClient.ANDROID_PACKAGE_HEADER])
        assert(signature == requestHeaders[W3WV3RetrofitApiClient.ANDROID_CERT_HEADER])
    }

    @Test
    fun `interceptor adds headers to request with null packageName and signature`() {
        // Arrange
        val apiKey = "api_key"
        val packageName = null
        val signature = null
        val headers = mapOf("header1" to "value1", "header2" to "value2")

        val interceptor = What3WordsV3Interceptor(apiKey, packageName, signature, headers)

        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(interceptor)
            .build()

        val request = Request.Builder()
            .url(mockWebServer.url("/"))
            .build()

        mockWebServer.enqueue(MockResponse())

        // Act
        client.newCall(request).execute()
        val requestHeaders = mockWebServer.takeRequest().headers

        // Assert
        assert(requestHeaders["header1"] == "value1")
        assert(headers["header2"] == "value2")
        assert(apiKey == requestHeaders[W3WV3RetrofitApiClient.HEADER_WHAT3WORDS_API_KEY])
        assert(requestHeaders[W3WV3RetrofitApiClient.ANDROID_PACKAGE_HEADER] == null)
        assert(requestHeaders[W3WV3RetrofitApiClient.ANDROID_CERT_HEADER] == null)
    }

    @Test
    fun `interceptor adds headers to request with empty headers`() {
        // Arrange
        val apiKey = "api_key"
        val packageName = "com.what3words.android"
        val signature = "signature"

        val interceptor = What3WordsV3Interceptor(apiKey, packageName, signature)

        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(interceptor)
            .build()

        val request = Request.Builder()
            .url(mockWebServer.url("/"))
            .build()

        mockWebServer.enqueue(MockResponse())

        // Act
        client.newCall(request).execute()
        val requestHeaders = mockWebServer.takeRequest().headers

        // Assert
        assert(apiKey == requestHeaders[W3WV3RetrofitApiClient.HEADER_WHAT3WORDS_API_KEY])
        assert(packageName == requestHeaders[W3WV3RetrofitApiClient.ANDROID_PACKAGE_HEADER])
        assert(signature == requestHeaders[W3WV3RetrofitApiClient.ANDROID_CERT_HEADER])
    }
}