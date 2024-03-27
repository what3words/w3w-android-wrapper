package com.what3words.androidwrapper.voice.mappers

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
                center = W3WCoordinates(
                    lat = from.coordinates.lat,
                    lng = from.coordinates.lng
                ),
                square = W3WRectangle(
                    southwest = W3WCoordinates(
                        lat = from.square.southwest.lat,
                        lng = from.square.southwest.lng
                    ),
                    northeast = W3WCoordinates(
                        lat = from.square.northeast.lat,
                        lng = from.square.northeast.lng
                    )
                ),
                language = W3WProprietaryLanguage(from.language, from.locale),
                country = W3WCountry(from.country),
                nearestPlace = from.nearestPlace
            ),
            distanceToFocus = from.distanceToFocusKm?.toDouble()?.let(::W3WDistance),
            rank = from.rank
        )
    }
}