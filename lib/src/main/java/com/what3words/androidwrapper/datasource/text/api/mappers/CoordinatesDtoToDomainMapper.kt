package com.what3words.androidwrapper.datasource.text.api.mappers

import com.what3words.androidwrapper.common.Mapper
import com.what3words.androidwrapper.datasource.text.api.dto.CoordinatesDto
import com.what3words.core.types.geometry.W3WCoordinates

internal class CoordinatesDtoToDomainMapper : Mapper<CoordinatesDto, W3WCoordinates> {
    override fun mapFrom(from: CoordinatesDto): W3WCoordinates {
        return with(from) {
            W3WCoordinates(
                lat = lat,
                lng = lng
            )
        }
    }
}