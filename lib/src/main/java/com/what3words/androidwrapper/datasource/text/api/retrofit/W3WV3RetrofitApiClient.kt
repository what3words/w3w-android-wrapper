package com.what3words.androidwrapper.datasource.text.api.retrofit

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.what3words.androidwrapper.BuildConfig
import com.what3words.androidwrapper.common.Mapper
import com.what3words.androidwrapper.datasource.text.api.What3WordsV3Service
import com.what3words.androidwrapper.datasource.text.api.error.NetworkError
import com.what3words.androidwrapper.datasource.text.api.error.UnknownError
import com.what3words.androidwrapper.datasource.text.api.mappers.ErrorDtoToDomainMapper
import com.what3words.androidwrapper.datasource.text.api.response.APIResponse
import com.what3words.core.types.common.W3WResult
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException

internal object W3WV3RetrofitApiClient {
    private const val DEFAULT_ENDPOINT = "${BuildConfig.BASE_TEXT_API_ENDPOINT}/${BuildConfig.TEXT_API_VERSION}/"

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

    /**
     * Executes an API request and handles the response.
     * @param resultMapper Mapper to convert successful API response to desired result type.
     * @param request Lambda function representing the API request.
     * @return [W3WResult] representing the result of the API request, either success with mapped result or failure with error details.
     */
    inline fun <reified T : APIResponse, R : Any> executeApiRequestAndHandleResponse(
        resultMapper: Mapper<T, R>,
        crossinline request: suspend () -> Response<T>,
    ): W3WResult<R> {
        return try {
            // Perform the API request
            val result: Response<T> = runBlocking { request() }

            if (result.isSuccessful && result.body()?.isSuccessful() == true) {
                val mappedResult = resultMapper.mapFrom(result.body()!!)
                W3WResult.Success(mappedResult)
            } else {
                // Handle error response
                val adapter = moshi.adapter(T::class.java).lenient()
                // Parse error from JSON response or throw NullPointerException if error object is null
                val error = result.errorBody()?.source()?.let { adapter.fromJson(it)?.error }
                    ?: throw NullPointerException("Error object is null")
                val errorMapper = ErrorDtoToDomainMapper()
                val mappedError = errorMapper.mapFrom(error)
                W3WResult.Failure(message = mappedError.message, error = mappedError)
            }
        } catch (e: JsonDataException) {
            W3WResult.Failure(
                message = e.message ?: "JSON parsing error",
                error = UnknownError(e.cause)
            )
        } catch (e: IOException) {
            W3WResult.Failure(message = e.message ?: "IO error", error = NetworkError(e.cause))
        } catch (e: Exception) {
            W3WResult.Failure(message = e.message ?: "Unknown error", error = UnknownError(e.cause))
        }
    }
}