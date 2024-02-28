package com.what3words.androidwrapper.datasource.text.api.mappers

import com.what3words.androidwrapper.common.Mapper
import com.what3words.androidwrapper.datasource.text.api.dto.CoordinatesDto
import com.what3words.androidwrapper.datasource.text.api.dto.LineDto
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WLine

internal class LineDtoToDomainMapper(private val coordinatesDtoToDomainMapper: Mapper<CoordinatesDto, W3WCoordinates>) :
    Mapper<LineDto, W3WLine> {
    override fun mapFrom(from: LineDto): W3WLine {
        return with(from) {
            W3WLine(
                start = coordinatesDtoToDomainMapper.mapFrom(start),
                end = coordinatesDtoToDomainMapper.mapFrom(end)
            )
        }
    }
}