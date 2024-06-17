package com.what3words.androidwrapper.datasource.text.api.mapper

import com.what3words.androidwrapper.datasource.text.api.dto.ErrorDto
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
import com.what3words.androidwrapper.datasource.text.api.error.BadLanguageError
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
import com.what3words.androidwrapper.datasource.text.api.error.SuspendedKeyError
import com.what3words.androidwrapper.datasource.text.api.error.UnknownError
import com.what3words.androidwrapper.datasource.text.api.mappers.ErrorDtoToDomainMapper
import org.junit.Assert
import org.junit.Test

class ErrorDtoToDomainMapperTest {

    private val mapper = ErrorDtoToDomainMapper()

    @Test
    fun `test mapFrom BadCoordinates`() {
        val errorDto = ErrorDto("BadCoordinates", "Bad coordinates")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is BadCoordinatesError)
    }

    @Test
    fun `test mapFrom BadLanguage`() {
        val errorDto = ErrorDto("BadLanguage", "Bad language")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is BadLanguageError)
    }

    @Test
    fun `test mapFrom BadWords`() {
        val errorDto = ErrorDto("BadWords", "Bad words")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is BadWordsError)
    }

    @Test
    fun `test mapFrom BadInput`() {
        val errorDto = ErrorDto("BadInput", "Bad input")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is BadInputError)
    }

    @Test
    fun `test mapFrom BadNResults`() {
        val errorDto = ErrorDto("BadNResults", "Bad n results")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is BadNResultsError)
    }

    @Test
    fun `test mapFrom BadNFocusResults`() {
        val errorDto = ErrorDto("BadNFocusResults", "Bad n focus results")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is BadNFocusResultsError)
    }

    @Test
    fun `test mapFrom BadFocus`() {
        val errorDto = ErrorDto("BadFocus", "Bad focus")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is BadFocusError)
    }

    @Test
    fun `test mapFrom BadClipToCircle`() {
        val errorDto = ErrorDto("BadClipToCircle", "Bad clip to circle")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is BadClipToCircleError)
    }

    @Test
    fun `test mapFrom BadClipToBoundingBox`() {
        val errorDto = ErrorDto("BadClipToBoundingBox", "Bad clip to bounding box")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is BadClipToBoundingBoxError)
    }

    @Test
    fun `test mapFrom BadClipToCountry`() {
        val errorDto = ErrorDto("BadClipToCountry", "Bad clip to country")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is BadClipToCountryError)
    }

    @Test
    fun `test mapFrom BadClipToPolygon`() {
        val errorDto = ErrorDto("BadClipToPolygon", "Bad clip to polygon")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is BadClipToPolygonError)
    }

    @Test
    fun `test mapFrom BadInputType`() {
        val errorDto = ErrorDto("BadInputType", "Bad input type")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is BadInputTypeError)
    }

    @Test
    fun `test mapFrom BadBoundingBox`() {
        val errorDto = ErrorDto("BadBoundingBox", "Bad bounding box")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is BadBoundingBoxError)
    }

    @Test
    fun `test mapFrom BadBoundingBoxTooBig`() {
        val errorDto = ErrorDto("BadBoundingBoxTooBig", "Bad bounding box too big")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is BadBoundingBoxTooBigError)
    }

    @Test
    fun `test mapFrom InternalServerError`() {
        val errorDto = ErrorDto("InternalServerError", "Internal server error")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is InternalServerError)
    }

    @Test
    fun `test mapFrom InvalidKey`() {
        val errorDto = ErrorDto("InvalidKey", "Invalid key")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is InvalidKeyError)
    }

    @Test
    fun `test mapFrom SuspendedKey`() {
        val errorDto = ErrorDto("SuspendedKey", "Suspended key")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is SuspendedKeyError)
    }

    @Test
    fun `test mapFrom NetworkError`() {
        val errorDto = ErrorDto("NetworkError", "Network error")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is NetworkError)
    }

    @Test
    fun `test mapFrom InvalidApiVersion`() {
        val errorDto = ErrorDto("InvalidApiVersion", "Invalid API version")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is InvalidApiVersionError)
    }

    @Test
    fun `test mapFrom InvalidReferrer`() {
        val errorDto = ErrorDto("InvalidReferrer", "Invalid referrer")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is InvalidReferrerError)
    }

    @Test
    fun `test mapFrom InvalidIpAddress`() {
        val errorDto = ErrorDto("InvalidIpAddress", "Invalid IP address")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is InvalidIPAddressError)
    }

    @Test
    fun `test mapFrom InvalidAppCredentials`() {
        val errorDto = ErrorDto("InvalidAppCredentials", "Invalid app credentials")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is InvalidAppCredentialsError)
    }

    @Test
    fun `test mapFrom SdkError`() {
        val errorDto = ErrorDto("SdkError", "SDK error")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is SDKError)
    }

    @Test
    fun `test mapFrom UnknownError`() {
        val errorDto = ErrorDto("UnknownError", "Unknown error")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is UnknownError)
    }

    @Test
    fun `Unknown error should be returned when the error code is not recognized`() {
        val errorDto = ErrorDto("zzzzzz", "zzzzzz")
        val result = mapper.mapFrom(errorDto)
        Assert.assertTrue(result is UnknownError)
    }

}