package com.what3words.androidwrapper.datasource.text.api.utils

import com.squareup.moshi.JsonDataException
import com.what3words.androidwrapper.common.Mapper
import com.what3words.androidwrapper.datasource.text.api.error.NetworkError
import com.what3words.androidwrapper.datasource.text.api.error.UnknownError
import com.what3words.androidwrapper.datasource.text.api.response.APIResponse
import com.what3words.androidwrapper.datasource.text.api.mappers.ErrorDtoToDomainMapper
import com.what3words.core.types.common.W3WResult
import kotlinx.coroutines.runBlocking
import java.io.IOException

/**
 * Executes a suspend function representing a [com.what3words.androidwrapper.datasource.text.api.What3WordsV3Service] API request and maps its response or error to a W3WResult.
 *
 * @param resultMapper The [Mapper] to convert the API response to the desired domain object.
 * @param request A suspend function representing the API request.
 * @return A W3WResult encapsulating the success or failure of the API call.
 */
internal fun <T : APIResponse, R : Any> executeApiRequestAndHandleResponse(
    resultMapper: Mapper<T, R>,
    request: suspend () -> T,
): W3WResult<R> {
    return try {
        val result = runBlocking { request() }

        if (result.isSuccessful()) {
            val mappedResult = resultMapper.mapFrom(result)
            W3WResult.Success(mappedResult)
        } else {
            val error = result.error ?: throw NullPointerException("Error object is null")
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
