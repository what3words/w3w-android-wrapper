package com.what3words.androidwrapper.fake

import com.what3words.androidwrapper.datasource.text.api.What3WordsV3Service
import com.what3words.androidwrapper.datasource.text.api.dto.CoordinatesDto
import com.what3words.androidwrapper.datasource.text.api.dto.ErrorDto
import com.what3words.androidwrapper.datasource.text.api.dto.LanguageDto
import com.what3words.androidwrapper.datasource.text.api.dto.LineDto
import com.what3words.androidwrapper.datasource.text.api.dto.SquareDto
import com.what3words.androidwrapper.datasource.text.api.dto.SuggestionDto
import com.what3words.androidwrapper.datasource.text.api.extensions.W3WDomainToApiStringExtensions.toAPIString
import com.what3words.androidwrapper.datasource.text.api.response.AutosuggestResponse
import com.what3words.androidwrapper.datasource.text.api.response.AvailableLanguagesResponse
import com.what3words.androidwrapper.datasource.text.api.response.ConvertTo3waResponse
import com.what3words.androidwrapper.datasource.text.api.response.ConvertToCoordinatesResponse
import com.what3words.androidwrapper.datasource.text.api.response.GridSectionResponse
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WRectangle

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
    ): ConvertTo3waResponse {
        if (!verifyLatLng(getLatFromApiSting(coordinates!!), getLngFromApiSting(coordinates))) {
            return ConvertTo3waResponse(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ErrorDto("BadCoordinates", "Bad coordinates")
            )
        } else {
            return ConvertTo3waResponse(
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
        }
    }

    override suspend fun convertToCoordinates(
        address: String?,
        headers: Map<String, String>
    ): ConvertToCoordinatesResponse {
        if (!verifyW3wa(address!!)) {
            return ConvertToCoordinatesResponse(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ErrorDto("BadWords", "Bad words")
            )
        } else {
            return ConvertToCoordinatesResponse(
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
        }
    }

    override suspend fun autosuggest(
        input: String?,
        nResults: String?,
        focus: String?,
        nFocusResults: String?,
        clipToCountry: String?,
        clipToBoundingBox: String?,
        clipToCircle: String?,
        clipToPolygon: String?,
        inputType: String?,
        lang: String?,
        locale: String?,
        preferLand: String?,
        headers: Map<String, String>
    ): AutosuggestResponse {
        return AutosuggestResponse(
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
    }

    override suspend fun autosuggestWithCoordinates(
        input: String?,
        nResults: String?,
        focus: String?,
        nFocusResults: String?,
        clipToCountry: String?,
        clipToBoundingBox: String?,
        clipToCircle: String?,
        clipToPolygon: String?,
        inputType: String?,
        lang: String?,
        locale: String?,
        preferLand: String?,
        headers: Map<String, String>
    ): AutosuggestResponse {
        return AutosuggestResponse(
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
    }

    override suspend fun gridSection(
        bbox: String?,
        format: String?,
        headers: Map<String, String>
    ): GridSectionResponse {
        if (bbox != null && invalidRectangle.toAPIString() == bbox) {
            return GridSectionResponse(
                emptyList(),
                ErrorDto("BadBoundingBox", "Bad bounding box")
            )
        }
        return GridSectionResponse(listOf(LineDto(coordinatesDto, coordinatesDto)), null)
    }

    override suspend fun availableLanguages(headers: Map<String, String>): AvailableLanguagesResponse {
        return AvailableLanguagesResponse(
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