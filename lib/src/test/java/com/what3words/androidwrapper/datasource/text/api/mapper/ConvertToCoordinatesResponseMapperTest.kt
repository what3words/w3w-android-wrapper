package com.what3words.androidwrapper.datasource.text.api.mapper

import com.what3words.androidwrapper.datasource.text.api.dto.CoordinatesDto
import com.what3words.androidwrapper.datasource.text.api.dto.ErrorDto
import com.what3words.androidwrapper.datasource.text.api.dto.SquareDto
import com.what3words.androidwrapper.datasource.text.api.mappers.ConvertToCoordinatesResponseMapper
import com.what3words.androidwrapper.datasource.text.api.mappers.CoordinatesDtoToDomainMapper
import com.what3words.androidwrapper.datasource.text.api.response.ConvertToCoordinatesResponse
import org.junit.Assert
import org.junit.Test

class ConvertToCoordinatesResponseMapperTest {

    private val convertToCoordinatesResponseMapper =
        ConvertToCoordinatesResponseMapper(CoordinatesDtoToDomainMapper())

    @Test
    fun `mapFrom should return W3WCoordinates`() {
        // Arrange
        val convertToCoordinatesResponse = ConvertToCoordinatesResponse(
            words = "index.home.raft",
            language = "en",
            coordinates = CoordinatesDto(lng = -0.203586, lat = 51.521251),
            square = SquareDto(
                CoordinatesDto(lng = -0.203607, lat = 51.521238),
                CoordinatesDto(lng = -0.203564, lat = 51.521265)
            ),
            country = null,
            nearestPlace = "Bayswater, London",
            locale = null,
            error = null,
            map = "https://w3w.co/index.home.raft"
        )

        // Act
        val result = convertToCoordinatesResponseMapper.mapFrom(convertToCoordinatesResponse)

        // Assert
        assert(result.lat == 51.521251)
        assert(result.lng == -0.203586)
    }

    @Test
    fun `mapFrom should throw NullPointerException when coordinates field is null`() {
        // Arrange
        val convertToCoordinatesResponse = ConvertToCoordinatesResponse(
            words = null,
            language = null,
            coordinates = null,
            square = null,
            country = null,
            nearestPlace = null,
            locale = null,
            error = ErrorDto("Unknown", "Unknown error"),
            map = null
        )

        // Act and assert
        Assert.assertThrows(NullPointerException::class.java) {
            convertToCoordinatesResponseMapper.mapFrom(convertToCoordinatesResponse)
        }
    }
}