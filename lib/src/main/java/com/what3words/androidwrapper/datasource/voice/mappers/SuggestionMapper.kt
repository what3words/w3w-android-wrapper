package com.what3words.androidwrapper.datasource.voice.mappers

import com.what3words.androidwrapper.common.Mapper
import com.what3words.core.types.domain.W3WAddress
import com.what3words.core.types.domain.W3WCountry
import com.what3words.core.types.domain.W3WSuggestion
import com.what3words.core.types.geometry.W3WDistance
import com.what3words.core.types.language.W3WProprietaryLanguage
import com.what3words.javawrapper.response.Suggestion

class SuggestionMapper: Mapper<Suggestion, W3WSuggestion> {
    override fun mapFrom(from: Suggestion): W3WSuggestion {
        return W3WSuggestion(
            w3wAddress = W3WAddress(
                words = from.words,
                center = null,
                square = null,
                language = W3WProprietaryLanguage(from.language, from.locale),
                country = W3WCountry(from.country),
                nearestPlace = from.nearestPlace
            ),
            distanceToFocus = from.distanceToFocusKm?.toDouble()?.let(::W3WDistance),
            rank = from.rank
        )
    }
}