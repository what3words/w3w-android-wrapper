package com.what3words.androidwrapper.datasource.text.api.mapper

import com.what3words.androidwrapper.datasource.text.api.dto.CoordinatesDto
import com.what3words.androidwrapper.datasource.text.api.dto.ErrorDto
import com.what3words.androidwrapper.datasource.text.api.dto.LineDto
import com.what3words.androidwrapper.datasource.text.api.mappers.CoordinatesDtoToDomainMapper
import com.what3words.androidwrapper.datasource.text.api.mappers.GridSectionResponseMapper
import com.what3words.androidwrapper.datasource.text.api.mappers.LineDtoToDomainMapper
import com.what3words.androidwrapper.datasource.text.api.response.GridSectionResponse
import org.junit.Test

class GridSectionResponseMapperTest {

    private val gridSectionResponseMapper =
        GridSectionResponseMapper(LineDtoToDomainMapper(CoordinatesDtoToDomainMapper()))

    @Test
    fun `mapFrom should return W3WGridSection`() {
        // Arrange
        val gridSectionResponse = GridSectionResponse(
            lines = listOf(
                LineDto(
                    start = CoordinatesDto(lng = -0.203607, lat = 51.521238),
                    end = CoordinatesDto(lng = -0.203564, lat = 51.521265)
                ),
                LineDto(
                    start = CoordinatesDto(lng = 0.116126, lat = 52.208009918068136),
                    end = CoordinatesDto(lng = 0.11754, lat = 52.208009918068136)
                )
            ),
            error = null
        )

        // Act
        val result = gridSectionResponseMapper.mapFrom(gridSectionResponse)

        // Assert
        assert(result.lines.size == 2)
        assert(result.lines[0].start.lng == -0.203607)
        assert(result.lines[0].start.lat == 51.521238)
        assert(result.lines[0].end.lng == -0.203564)
        assert(result.lines[0].end.lat == 51.521265)

        assert(result.lines[1].start.lng == 0.116126)
        assert(result.lines[1].start.lat == 52.208009918068136)
        assert(result.lines[1].end.lng == 0.11754)
        assert(result.lines[1].end.lat == 52.208009918068136)
    }

    @Test
    fun `mapFrom should return empty list when lines field is null`() {
        // Arrange
        val gridSectionResponse = GridSectionResponse(
            lines = null,
            error = ErrorDto("UNKNOWN", "Unknown error")
        )

        // Act
        val value = gridSectionResponseMapper.mapFrom(gridSectionResponse)

        // Assert
        assert(value.lines.isEmpty())
    }
}