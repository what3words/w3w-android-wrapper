package com.what3words.androidwrapper.datasource.text.api.mappers

import com.what3words.androidwrapper.common.Mapper
import com.what3words.androidwrapper.datasource.text.api.dto.CoordinatesDto
import com.what3words.androidwrapper.datasource.text.api.response.ConvertToCoordinatesResponse
import com.what3words.core.types.geometry.W3WCoordinates

internal class ConvertToCoordinatesResponseMapper(
    private val coordinatesDtoToDomainMapper: Mapper<CoordinatesDto, W3WCoordinates>,
) : Mapper<ConvertToCoordinatesResponse, W3WCoordinates> {
    override fun mapFrom(from: ConvertToCoordinatesResponse): W3WCoordinates {
        return with(from) {
            coordinates?.let { coordinatesDtoToDomainMapper.mapFrom(it) }
                ?: throw NullPointerException("Coordinates property cannot be null")
        }
    }
}