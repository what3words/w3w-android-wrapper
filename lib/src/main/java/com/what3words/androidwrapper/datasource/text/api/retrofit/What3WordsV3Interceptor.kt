package com.what3words.androidwrapper.datasource.text.api.retrofit

import com.what3words.androidwrapper.BuildConfig
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response


internal class What3WordsV3Interceptor(
    private val apiKey: String,
    private val packageName: String?,
    private val signature: String?,
    private val headers: Map<String, String> = mapOf()
) : Interceptor {

    private val userAgent: String =
        ("what3words-Android/" + BuildConfig.VERSION_NAME + " (Java " + System.getProperty("java.version") + "; "
                + System.getProperty("os.name") + " " + System.getProperty("os.version") + ")")

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val builder: Request.Builder = request.newBuilder()

        // set required content type, api key and request specific API version
        builder.header(W3WV3RetrofitApiClient.HEADER_CONTENT_TYPE, W3WV3RetrofitApiClient.CONTENT_TYPE_JSON)
        builder.header(W3WV3RetrofitApiClient.HEADER_WHAT3WORDS_API_KEY, apiKey)
        builder.header(W3WV3RetrofitApiClient.W3W_WRAPPER, userAgent)

        // add any custom headers
        val it = headers.keys.iterator()
        while (it.hasNext()) {
            val name = it.next()
            val value = headers[name] ?: continue
            builder.header(name, value)
        }

        if (packageName != null) {
            builder.header(W3WV3RetrofitApiClient.ANDROID_PACKAGE_HEADER, packageName)
        }

        if (signature != null) {
            builder.header(W3WV3RetrofitApiClient.ANDROID_CERT_HEADER, signature)
        }

        return chain.proceed(builder.build())
    }

}