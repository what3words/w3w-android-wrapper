package com.what3words.androidwrapper.datasource.text.api.mappers

import com.what3words.androidwrapper.common.Mapper
import com.what3words.androidwrapper.datasource.text.api.dto.CoordinatesDto
import com.what3words.androidwrapper.datasource.text.api.dto.SquareDto
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WRectangle

internal class SquareDtoToDomainMapper(
    private val coordinatesDtoToDomainMapper: Mapper<CoordinatesDto, W3WCoordinates>
) : Mapper<SquareDto, W3WRectangle> {
    override fun mapFrom(from: SquareDto): W3WRectangle {
        return with(from) {
            W3WRectangle(
                southwest = coordinatesDtoToDomainMapper.mapFrom(southwest),
                northeast = coordinatesDtoToDomainMapper.mapFrom(northeast)
            )
        }
    }
}