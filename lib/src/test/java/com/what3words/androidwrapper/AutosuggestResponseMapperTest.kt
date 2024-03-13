package com.what3words.androidwrapper

import com.what3words.androidwrapper.datasource.text.api.dto.ErrorDto
import com.what3words.androidwrapper.datasource.text.api.dto.SuggestionDto
import com.what3words.androidwrapper.datasource.text.api.mappers.AutosuggestResponseMapper
import com.what3words.androidwrapper.datasource.text.api.mappers.CoordinatesDtoToDomainMapper
import com.what3words.androidwrapper.datasource.text.api.mappers.SquareDtoToDomainMapper
import com.what3words.androidwrapper.datasource.text.api.response.AutosuggestResponse
import org.junit.Test

class AutosuggestResponseMapperTest {

    private val coordinatesDtoToDomainMapper = CoordinatesDtoToDomainMapper()

    private val autosuggestResponseMapper = AutosuggestResponseMapper(
        coordinatesDtoToDomainMapper = coordinatesDtoToDomainMapper,
        squareDtoToDomainMapper = SquareDtoToDomainMapper(coordinatesDtoToDomainMapper)
    )

    @Test
    fun `mapFrom should return empty list when suggestions is null`() {
        // Arrange
        val autosuggestResponse =
            AutosuggestResponse(suggestions = null, error = ErrorDto("UNKNOWN", "Unknown error"))

        // Act
        val result = autosuggestResponseMapper.mapFrom(autosuggestResponse)

        // Assert
        assert(result.isEmpty())
    }

    @Test
    fun `mapFrom should return correct W3WSuggestion list`() {
        // Arrange
        val autosuggestResponse = AutosuggestResponse(
            suggestions = listOf(
                SuggestionDto(
                    country = "GB",
                    nearestPlace = "Bayswater, London",
                    words = "index.home.raft",
                    rank = 1,
                    language = "en",
                    coordinates = null,
                    locale = null,
                    distanceToFocusKm = null,
                    square = null
                ),
                SuggestionDto(
                    country = "US",
                    nearestPlace = "Prosperity, West Virginia",
                    words = "indexes.home.raft",
                    rank = 2,
                    language = "en",
                    coordinates = null,
                    locale = null,
                    distanceToFocusKm = null,
                    square = null
                ),
                SuggestionDto(
                    country = "US",
                    nearestPlace = "Greensboro, North Carolina",
                    words = "index.homes.raft",
                    rank = 3,
                    language = "en",
                    coordinates = null,
                    locale = null,
                    distanceToFocusKm = null,
                    square = null
                ),
            ),
            error = null
        )

        // Act
        val result = autosuggestResponseMapper.mapFrom(autosuggestResponse)

        // Assert
        assert(result.size == 3)
        assert(result[0].w3wAddress.address == "///index.home.raft")
        assert(result[0].w3wAddress.nearestPlace == "Bayswater, London")
        assert(result[0].w3wAddress.language.code == "en")
        assert(result[0].w3wAddress.country.twoLetterCode == "GB")
        assert(result[0].w3wAddress.center == null)
        assert(result[0].w3wAddress.square == null)
        assert(result[0].w3wAddress.language.locale == null)
        assert(result[0].distanceToFocus == null)
        assert(result[0].rank == 1)

        assert(result[1].w3wAddress.address == "///indexes.home.raft")
        assert(result[1].w3wAddress.nearestPlace == "Prosperity, West Virginia")
        assert(result[1].w3wAddress.language.code == "en")
        assert(result[1].w3wAddress.country.twoLetterCode == "US")
        assert(result[1].w3wAddress.center == null)
        assert(result[1].w3wAddress.square == null)
        assert(result[1].w3wAddress.language.locale == null)
        assert(result[1].distanceToFocus == null)
        assert(result[1].rank == 2)

        assert(result[2].w3wAddress.address == "///index.homes.raft")
        assert(result[2].w3wAddress.nearestPlace == "Greensboro, North Carolina")
        assert(result[2].w3wAddress.language.code == "en")
        assert(result[2].w3wAddress.country.twoLetterCode == "US")
        assert(result[2].w3wAddress.center == null)
        assert(result[2].w3wAddress.square == null)
        assert(result[2].w3wAddress.language.locale == null)
        assert(result[2].distanceToFocus == null)
        assert(result[2].rank == 3)
    }
}