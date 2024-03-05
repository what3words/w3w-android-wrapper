package com.what3words.androidwrapper.datasource.text.api.retrofit

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.what3words.androidwrapper.datasource.text.api.What3WordsV3Service
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

internal object W3WV3RetrofitApiClient {
    private const val DEFAULT_ENDPOINT = "https://api.what3words.com/v3/"

    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val CONTENT_TYPE_JSON = "application/json"
    const val HEADER_WHAT3WORDS_API_KEY = "X-Api-Key"
    const val W3W_WRAPPER = "X-W3W-Wrapper"
    const val ANDROID_CERT_HEADER = "X-Android-Cert"
    const val ANDROID_PACKAGE_HEADER = "X-Android-Package"

    private val moshi: Moshi by lazy {
        Moshi
            .Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private fun setUpOkHttpClient(
        apiKey: String,
        packageName: String?,
        signature: String?,
        headers: Map<String, String> = mapOf()
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addNetworkInterceptor(
                What3WordsV3Interceptor(apiKey, packageName, signature, headers)
            )
            .build()
    }

    fun createW3WV3Service(
        apiKey: String,
        endPoint: String?,
        packageName: String?,
        signature: String?,
        headers: Map<String, String> = mapOf()
    ): What3WordsV3Service {

        return Retrofit.Builder()
            .baseUrl(endPoint ?: DEFAULT_ENDPOINT)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(setUpOkHttpClient(apiKey, packageName, signature, headers))
            .build()
            .create(What3WordsV3Service::class.java)
    }
}