package com.what3words.androidwrapper.datasource.text.api.mappers

import com.what3words.androidwrapper.common.Mapper
import com.what3words.androidwrapper.datasource.text.api.error.BadBoundingBoxError
import com.what3words.androidwrapper.datasource.text.api.error.BadBoundingBoxTooBigError
import com.what3words.androidwrapper.datasource.text.api.error.BadClipToBoundingBoxError
import com.what3words.androidwrapper.datasource.text.api.error.BadClipToCircleError
import com.what3words.androidwrapper.datasource.text.api.error.BadClipToCountryError
import com.what3words.androidwrapper.datasource.text.api.error.BadClipToPolygonError
import com.what3words.androidwrapper.datasource.text.api.error.BadCoordinatesError
import com.what3words.androidwrapper.datasource.text.api.error.BadFocusError
import com.what3words.androidwrapper.datasource.text.api.error.BadInputError
import com.what3words.androidwrapper.datasource.text.api.error.BadInputTypeError
import com.what3words.androidwrapper.datasource.text.api.error.BadNFocusResultsError
import com.what3words.androidwrapper.datasource.text.api.error.BadNResultsError
import com.what3words.androidwrapper.datasource.text.api.error.BadWordsError
import com.what3words.androidwrapper.datasource.text.api.error.InternalServerError
import com.what3words.androidwrapper.datasource.text.api.error.InvalidApiVersionError
import com.what3words.androidwrapper.datasource.text.api.error.InvalidAppCredentialsError
import com.what3words.androidwrapper.datasource.text.api.error.InvalidIPAddressError
import com.what3words.androidwrapper.datasource.text.api.error.InvalidKeyError
import com.what3words.androidwrapper.datasource.text.api.error.InvalidReferrerError
import com.what3words.androidwrapper.datasource.text.api.error.NetworkError
import com.what3words.androidwrapper.datasource.text.api.error.SDKError
import com.what3words.androidwrapper.datasource.text.api.error.UnknownError
import com.what3words.androidwrapper.datasource.text.api.error.SuspendedKeyError
import com.what3words.androidwrapper.datasource.text.api.dto.ErrorDto
import com.what3words.androidwrapper.datasource.text.api.error.BadLanguageError
import com.what3words.core.types.common.W3WError

internal class ErrorDtoToDomainMapper : Mapper<ErrorDto, W3WError> {
    override fun mapFrom(from: ErrorDto): W3WError {
        return when (from.code) {
            "BadCoordinates" -> BadCoordinatesError(code = from.code, message = from.message)
            "BadLanguage" -> BadLanguageError(code = from.code, message = from.message)
            "BadWords" -> BadWordsError(code = from.code, message = from.message)
            "BadInput" -> BadInputError(code = from.code, message = from.message)
            "BadNResults" -> BadNResultsError(code = from.code, message = from.message)
            "BadNFocusResults" -> BadNFocusResultsError(code = from.code, message = from.message)
            "BadFocus" -> BadFocusError(code = from.code, message = from.message)
            "BadClipToCircle" -> BadClipToCircleError(code = from.code, message = from.message)
            "BadClipToBoundingBox" -> BadClipToBoundingBoxError(
                code = from.code,
                message = from.message
            )

            "BadClipToCountry" -> BadClipToCountryError(code = from.code, message = from.message)
            "BadClipToPolygon" -> BadClipToPolygonError(code = from.code, message = from.message)
            "BadInputType" -> BadInputTypeError(code = from.code, message = from.message)
            "BadBoundingBox" -> BadBoundingBoxError(code = from.code, message = from.message)
            "BadBoundingBoxTooBig" -> BadBoundingBoxTooBigError(
                code = from.code,
                message = from.message
            )

            "InternalServerError" -> InternalServerError(code = from.code, message = from.message)
            "InvalidKey" -> InvalidKeyError(code = from.code, message = from.message)
            "SuspendedKey" -> SuspendedKeyError(code = from.code, message = from.message)
            "UnknownError" -> UnknownError(code = from.code, message = from.message)
            "NetworkError" -> NetworkError(code = from.code, message = from.message)
            "InvalidApiVersion" -> InvalidApiVersionError(code = from.code, message = from.message)
            "InvalidReferrer" -> InvalidReferrerError(code = from.code, message = from.message)
            "InvalidIpAddress" -> InvalidIPAddressError(code = from.code, message = from.message)
            "InvalidAppCredentials" -> InvalidAppCredentialsError(
                code = from.code,
                message = from.message
            )

            "SdkError" -> SDKError(code = from.code, message = from.message)
            else -> {
                UnknownError(code = from.code, message = from.message)
            }
        }
    }
}