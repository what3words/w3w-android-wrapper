package com.what3words.androidwrapper.datasource.text.api.mappers

import com.what3words.androidwrapper.common.Mapper
import com.what3words.androidwrapper.datasource.text.api.dto.CoordinatesDto
import com.what3words.androidwrapper.datasource.text.api.dto.SquareDto
import com.what3words.androidwrapper.datasource.text.api.dto.SuggestionDto
import com.what3words.androidwrapper.datasource.text.api.response.AutosuggestResponse
import com.what3words.core.types.domain.W3WAddress
import com.what3words.core.types.domain.W3WCountry
import com.what3words.core.types.domain.W3WSuggestion
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WDistance
import com.what3words.core.types.geometry.W3WRectangle
import com.what3words.core.types.language.W3WProprietaryLanguage

internal class AutosuggestResponseMapper(
    private val coordinatesDtoToDomainMapper: Mapper<CoordinatesDto, W3WCoordinates>,
    private val squareDtoToDomainMapper: Mapper<SquareDto, W3WRectangle>
) : Mapper<AutosuggestResponse, List<W3WSuggestion>> {
    override fun mapFrom(from: AutosuggestResponse): List<W3WSuggestion> {
        return from.suggestions?.map { suggestion: SuggestionDto ->
            W3WSuggestion(
                w3wAddress = W3WAddress(
                    words = suggestion.words,
                    nearestPlace = suggestion.nearestPlace,
                    center = suggestion.coordinates?.let {
                        coordinatesDtoToDomainMapper.mapFrom(
                            it
                        )
                    },
                    square = suggestion.square?.let {
                        squareDtoToDomainMapper.mapFrom(it)
                    },
                    language = W3WProprietaryLanguage(
                        code = suggestion.language,
                        locale = suggestion.locale,
                        name = null,
                        nativeName = null
                    ),
                    country = W3WCountry(suggestion.country)
                ),
                distanceToFocus = suggestion.distanceToFocusKm?.toDouble()?.let(::W3WDistance),
                rank = suggestion.rank
            )
        } ?: emptyList()
    }
}