package com.what3words.androidwrapper.datasource.text.fake

import com.what3words.androidwrapper.datasource.text.api.What3WordsV3Service
import com.what3words.androidwrapper.datasource.text.api.dto.CoordinatesDto
import com.what3words.androidwrapper.datasource.text.api.dto.LanguageDto
import com.what3words.androidwrapper.datasource.text.api.dto.LineDto
import com.what3words.androidwrapper.datasource.text.api.dto.SquareDto
import com.what3words.androidwrapper.datasource.text.api.dto.SuggestionDto
import com.what3words.androidwrapper.common.extensions.W3WDomainToApiStringExtensions.toAPIString
import com.what3words.androidwrapper.datasource.text.api.response.AutosuggestResponse
import com.what3words.androidwrapper.datasource.text.api.response.AvailableLanguagesResponse
import com.what3words.androidwrapper.datasource.text.api.response.ConvertTo3waResponse
import com.what3words.androidwrapper.datasource.text.api.response.ConvertToCoordinatesResponse
import com.what3words.androidwrapper.datasource.text.api.response.GridSectionResponse
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WRectangle
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

internal class FakeWhat3WordsV3Service : What3WordsV3Service {
    val coordinatesDto = CoordinatesDto(105.574460, 10.251020)
    val squareDto = SquareDto(coordinatesDto, coordinatesDto)
    val w3w = "country.square.words"

    val invalidRectangle =
        W3WRectangle(W3WCoordinates(51.1122, 0.12221), W3WCoordinates(51.1333, 0.1223))

    override suspend fun convertTo3wa(
        coordinates: String?,
        language: String?,
        locale: String?,
        headers: Map<String, String>
    ): Response<ConvertTo3waResponse> {
        return if (!verifyLatLng(
                getLatFromApiSting(coordinates!!),
                getLngFromApiSting(coordinates)
            )
        ) {
            Response.error(
                400, """
                    {
                      "error": {
                        "code": "BadCoordinates", 
                        "message": "Bad coordinates"
                      }
                    }
                """.trimIndent().toResponseBody()
            )
        } else {
            Response.success(
                ConvertTo3waResponse(
                    "en",
                    squareDto,
                    "",
                    coordinatesDto,
                    w3w,
                    "",
                    "",
                    "",
                    null
                )
            )
        }
    }

    override suspend fun convertToCoordinates(
        address: String?,
        headers: Map<String, String>
    ): Response<ConvertToCoordinatesResponse> {
        return if (!verifyW3wa(address!!)) {
            Response.error(
                400, """
                    {
                      "error": {
                        "code": "BadWords", 
                        "message": "Bad Words"
                      }
                    }
                """.trimIndent().toResponseBody()
            )
        } else {
            Response.success(
                ConvertToCoordinatesResponse(
                    "en",
                    squareDto,
                    "",
                    coordinatesDto,
                    null,
                    "",
                    "",
                    "",
                    null,
                )
            )
        }
    }

    override suspend fun autosuggest(
        input: String?,
        options: Map<String, String>,
        headers: Map<String, String>
    ): Response<AutosuggestResponse> {
        return Response.success(
            AutosuggestResponse(
                listOf(
                    SuggestionDto(
                        w3w,
                        "en",
                        "",
                        0,
                        "",
                        "",
                        0,
                        squareDto,
                        null
                    )
                ), null
            )
        )
    }

    override suspend fun autosuggestWithCoordinates(
        input: String?,
        options: Map<String, String>,
        headers: Map<String, String>
    ): Response<AutosuggestResponse> {
        return Response.success(
            AutosuggestResponse(
                listOf(
                    SuggestionDto(
                        w3w,
                        "en",
                        "",
                        0,
                        "",
                        "",
                        0,
                        squareDto,
                        coordinatesDto
                    )
                ), null
            )
        )
    }

    override suspend fun gridSection(
        bbox: String?,
        headers: Map<String, String>
    ): Response<GridSectionResponse> {
        return if (bbox != null && invalidRectangle.toAPIString() == bbox) {
            Response.error(
                400, """
                    {
                      "error": {
                        "code": "BadBoundingBox", 
                        "message": "Bad bounding box"
                      }
                    }
                """.trimIndent().toResponseBody()
            )

        } else Response.success(
            GridSectionResponse(
                listOf(LineDto(coordinatesDto, coordinatesDto)),
                null
            )
        )
    }

    override suspend fun availableLanguages(headers: Map<String, String>): Response<AvailableLanguagesResponse> {
        return Response.success(
            AvailableLanguagesResponse(
                listOf(
                    LanguageDto(
                        "English", "en", "English", listOf(
                            LanguageDto.LocaleDto("English America", "en_US", "English America"),
                            LanguageDto.LocaleDto(
                                "English United Kingdom",
                                "en_GB",
                                "English United Kingdom"
                            )
                        )
                    ),
                    LanguageDto(
                        "Tiếng Việt", "vi", "Vietnamese", null
                    ),
                ), null
            )
        )
    }

    override fun autosuggestSelection(
        rawInput: String?,
        selection: String?,
        rank: String?,
        sourceApi: String?,
        header: Map<String, String>
    ) {
        // Do nothing
    }

    private fun verifyLatLng(lat: Double, lng: Double): Boolean {
        if (lat < -90 || lat > 90) {
            return false
        } else if (lng < -180 || lng > 180) {
            return false
        }

        return true
    }

    private fun getLngFromApiSting(string: String): Double {
        return string.split(",")[1].toDouble()
    }

    private fun getLatFromApiSting(string: String): Double {
        return string.split(",")[0].toDouble()
    }

    private fun verifyW3wa(w3wa: String): Boolean {
        val w3waList = w3wa.split(".")
        return w3waList.size == 3
    }
}