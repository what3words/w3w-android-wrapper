package com.what3words.androidwrapper.datasource.text.api.mapper

import com.what3words.androidwrapper.datasource.text.api.dto.CoordinatesDto
import com.what3words.androidwrapper.datasource.text.api.dto.SquareDto
import com.what3words.androidwrapper.datasource.text.api.mappers.ConvertTo3waResponseMapper
import com.what3words.androidwrapper.datasource.text.api.mappers.CoordinatesDtoToDomainMapper
import com.what3words.androidwrapper.datasource.text.api.mappers.SquareDtoToDomainMapper
import com.what3words.androidwrapper.datasource.text.api.response.ConvertTo3waResponse
import org.junit.Assert
import org.junit.Test

class ConvertTo3waResponseMapperTest {

    private val coordinatesDtoToDomainMapper = CoordinatesDtoToDomainMapper()

    private val convertTo3waResponseMapper = ConvertTo3waResponseMapper(
        coordinatesDtoToDomainMapper = coordinatesDtoToDomainMapper,
        squareDtoToDomainMapper = SquareDtoToDomainMapper(coordinatesDtoToDomainMapper)
    )

    @Test
    fun `mapFrom should return correct W3WAddress when all fields are present`() {
        // Arrange
        val convertTo3waResponse = ConvertTo3waResponse(
            words = "index.home.raft",
            language = "en",
            coordinates = CoordinatesDto(lng = -0.203586, lat = 51.521251),
            square = SquareDto(
                CoordinatesDto(lng = -0.203607, lat = 51.521238),
                CoordinatesDto(lng = -0.203564, lat = 51.521265)
            ),
            country = "GB",
            nearestPlace = "Bayswater, London",
            locale = null,
            error = null,
            map = "https://w3w.co/index.home.raft"
        )

        // Act
        val result = convertTo3waResponseMapper.mapFrom(convertTo3waResponse)

        // Assert
        assert(result.address == "///index.home.raft")
        assert(result.language.w3wCode == "en")
        assert(result.language.w3wLocale == null)
        assert(result.center?.lng == -0.203586)
        assert(result.center?.lat == 51.521251)
        assert(result.square?.southwest?.lng == -0.203607)
        assert(result.square?.southwest?.lat == 51.521238)
        assert(result.square?.northeast?.lng == -0.203564)
        assert(result.square?.northeast?.lat == 51.521265)
        assert(result.country.twoLetterCode == "GB")
        assert(result.nearestPlace == "Bayswater, London")
    }

    @Test
    fun `mapFrom should throw NullPointerException when words field is null`() {
        // Arrange
        val convertTo3waResponse = ConvertTo3waResponse(
            words = null,
            language = "en",
            coordinates = CoordinatesDto(lng = -0.203586, lat = 51.521251),
            square = SquareDto(
                CoordinatesDto(lng = -0.203607, lat = 51.521238),
                CoordinatesDto(lng = -0.203564, lat = 51.521265)
            ),
            country = "GB",
            nearestPlace = "Bayswater, London",
            locale = null,
            error = null,
            map = "https://w3w.co/index.home.raft"
        )

        // Act & Assert
        Assert.assertThrows(NullPointerException::class.java) {
            convertTo3waResponseMapper.mapFrom(convertTo3waResponse)
        }
    }

    @Test
    fun `mapFrom should throw NullPointerException when coordinates field is null`() {
        // Arrange
        val convertTo3waResponse = ConvertTo3waResponse(
            words = "index.home.raft",
            language = "en",
            coordinates = null,
            square = SquareDto(
                CoordinatesDto(lng = -0.203607, lat = 51.521238),
                CoordinatesDto(lng = -0.203564, lat = 51.521265)
            ),
            country = "GB",
            nearestPlace = "Bayswater, London",
            locale = null,
            error = null,
            map = "https://w3w.co/index.home.raft"
        )

        // Act & Assert
        Assert.assertThrows(NullPointerException::class.java) {
            convertTo3waResponseMapper.mapFrom(convertTo3waResponse)
        }
    }

    @Test
    fun `mapFrom should throw NullPointerException when square field is null`() {
        // Arrange
        val convertTo3waResponse = ConvertTo3waResponse(
            words = "index.home.raft",
            language = "en",
            coordinates = CoordinatesDto(lng = -0.203586, lat = 51.521251),
            square = null,
            country = "GB",
            nearestPlace = "Bayswater, London",
            locale = null,
            error = null,
            map = "https://w3w.co/index.home.raft"
        )

        // Act & Assert
        Assert.assertThrows(NullPointerException::class.java) {
            convertTo3waResponseMapper.mapFrom(convertTo3waResponse)
        }
    }

    @Test
    fun `mapFrom should throw NullPointerException when language field is null`() {
        // Arrange
        val convertTo3waResponse = ConvertTo3waResponse(
            words = "index.home.raft",
            language = null,
            coordinates = CoordinatesDto(lng = -0.203586, lat = 51.521251),
            square = SquareDto(
                CoordinatesDto(lng = -0.203607, lat = 51.521238),
                CoordinatesDto(lng = -0.203564, lat = 51.521265)
            ),
            country = "GB",
            nearestPlace = "Bayswater, London",
            locale = null,
            error = null,
            map = "https://w3w.co/index.home.raft"
        )

        // Act & Assert
        Assert.assertThrows(NullPointerException::class.java) {
            convertTo3waResponseMapper.mapFrom(convertTo3waResponse)
        }
    }

    @Test
    fun `mapFrom should throw NullPointerException when country field is null`() {
        // Arrange
        val convertTo3waResponse = ConvertTo3waResponse(
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

        // Act & Assert
        Assert.assertThrows(NullPointerException::class.java) {
            convertTo3waResponseMapper.mapFrom(convertTo3waResponse)
        }
    }

    @Test
    fun `mapFrom should throw NullPointerException when nearestPlace field is null`() {
        // Arrange
        val convertTo3waResponse = ConvertTo3waResponse(
            words = "index.home.raft",
            language = "en",
            coordinates = CoordinatesDto(lng = -0.203586, lat = 51.521251),
            square = SquareDto(
                CoordinatesDto(lng = -0.203607, lat = 51.521238),
                CoordinatesDto(lng = -0.203564, lat = 51.521265)
            ),
            country = "GB",
            nearestPlace = null,
            locale = null,
            error = null,
            map = "https://w3w.co/index.home.raft"
        )

        // Act & Assert
        Assert.assertThrows(NullPointerException::class.java) {
            convertTo3waResponseMapper.mapFrom(convertTo3waResponse)
        }
    }

}