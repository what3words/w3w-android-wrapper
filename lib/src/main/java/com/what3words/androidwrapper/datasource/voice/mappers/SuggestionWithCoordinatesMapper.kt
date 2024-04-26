package com.what3words.androidwrapper.datasource.voice.mappers

import com.what3words.androidwrapper.common.Mapper
import com.what3words.core.types.domain.W3WAddress
import com.what3words.core.types.domain.W3WCountry
import com.what3words.core.types.domain.W3WSuggestion
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WDistance
import com.what3words.core.types.geometry.W3WRectangle
import com.what3words.core.types.language.W3WProprietaryLanguage
import com.what3words.javawrapper.response.SuggestionWithCoordinates

class SuggestionWithCoordinatesMapper : Mapper<SuggestionWithCoordinates, W3WSuggestion> {
    override fun mapFrom(from: SuggestionWithCoordinates): W3WSuggestion {
        return W3WSuggestion(
            w3wAddress = W3WAddress(
                words = from.words,
                center = from.coordinates?.let { W3WCoordinates(it.lat, it.lng) },
                square = from.square?.let {
                    W3WRectangle(
                        W3WCoordinates(
                            it.southwest.lat,
                            it.southwest.lng
                        ),
                        W3WCoordinates(
                            it.northeast.lat,
                            it.northeast.lng
                        )
                    )
                },
                language = W3WProprietaryLanguage(
                    code = from.language,
                    locale = from.locale,
                    name = null,
                    nativeName = null
                ),
                country = W3WCountry(from.country),
                nearestPlace = from.nearestPlace
            ),
            distanceToFocus = from.distanceToFocusKm?.toDouble()?.let(::W3WDistance),
            rank = from.rank
        )
    }
}