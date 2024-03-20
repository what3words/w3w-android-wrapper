package com.what3words.androidwrapper.datasource.text.api.mappers

import com.what3words.androidwrapper.common.Mapper
import com.what3words.androidwrapper.datasource.text.api.dto.CoordinatesDto
import com.what3words.androidwrapper.datasource.text.api.dto.SquareDto
import com.what3words.androidwrapper.datasource.text.api.response.ConvertTo3waResponse
import com.what3words.core.types.domain.W3WAddress
import com.what3words.core.types.domain.W3WCountry
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WRectangle
import com.what3words.core.types.language.W3WProprietaryLanguage

internal class ConvertTo3waResponseMapper(
    private val coordinatesDtoToDomainMapper: Mapper<CoordinatesDto, W3WCoordinates>,
    private val squareDtoToDomainMapper: Mapper<SquareDto, W3WRectangle>
) : Mapper<ConvertTo3waResponse, W3WAddress> {
    override fun mapFrom(from: ConvertTo3waResponse): W3WAddress {
        return with(from) {
            val words = words ?: throw NullPointerException("Words property cannot be null")
            val center = coordinates?.let { coordinatesDtoToDomainMapper.mapFrom(it) }
                ?: throw NullPointerException("Center coordinates property cannot be null")
            val square = square?.let { squareDtoToDomainMapper.mapFrom(it) }
                ?: throw NullPointerException("Square property cannot be null")
            val language = language?.let {
                W3WProprietaryLanguage(code = it, locale = locale)
            } ?: throw NullPointerException("Language property cannot be null")
            val country = country?.let { W3WCountry(twoLetterCode = it) }
                ?: throw NullPointerException("Country property cannot be null")
            val nearestPlace =
                nearestPlace ?: throw NullPointerException("Nearest place property cannot be null")

            W3WAddress(words, center, square, language, country, nearestPlace)
        }
    }
}
