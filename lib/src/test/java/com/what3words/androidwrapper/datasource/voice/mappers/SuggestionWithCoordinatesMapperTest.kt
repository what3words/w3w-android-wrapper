package com.what3words.androidwrapper.datasource.voice.mappers

import com.what3words.androidwrapper.datasource.voice.mappers.SuggestionWithCoordinatesMapper
import com.what3words.core.types.domain.formattedWords
import com.what3words.core.types.geometry.km
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import org.junit.Test

class SuggestionWithCoordinatesMapperTest {

    private val mapper = SuggestionWithCoordinatesMapper()

    @Test
    fun `maps suggestion with coordinates to W3W suggestion`() {
        // Arrange
        val suggestionWithCoordinates = SuggestionWithCoordinates(
            Suggestion(
                "index.home.raft",
                "London",
                "GB",
                1,
                1,
                "en",
                null,
            ),
            51.521251,
            -0.203586,
            51.521251,
            -0.203586,
            51.521251,
            -0.203586,
        )

        // Act
        val w3wSuggestion = mapper.mapFrom(suggestionWithCoordinates)

        // Assert
        assert(w3wSuggestion.w3wAddress.formattedWords() == "///index.home.raft")
        assert(w3wSuggestion.w3wAddress.center?.lat == 51.521251)
        assert(w3wSuggestion.w3wAddress.center?.lng == -0.203586)
        assert(w3wSuggestion.w3wAddress.square?.northeast?.lat == 51.521251)
        assert(w3wSuggestion.w3wAddress.square?.northeast?.lng == -0.203586)
        assert(w3wSuggestion.w3wAddress.square?.southwest?.lat == 51.521251)
        assert(w3wSuggestion.w3wAddress.square?.southwest?.lng == -0.203586)
        assert(w3wSuggestion.w3wAddress.language.w3wCode == "en")
        assert(w3wSuggestion.w3wAddress.language.w3wLocale == null)
        assert(w3wSuggestion.w3wAddress.nearestPlace == "London")
        assert(w3wSuggestion.distanceToFocus?.km()?.toInt() == 1)
        assert(w3wSuggestion.rank == 1)
    }

    @Test
    fun `maps suggestion with null coordinates to W3W suggestion`() {
        // Arrange
        val suggestionWithCoordinates = SuggestionWithCoordinates(
            Suggestion(
                "index.home.raft",
                "London",
                "GB",
                1,
                1,
                "en",
                null,
            ),
        )

        // Act
        val w3wSuggestion = mapper.mapFrom(suggestionWithCoordinates)

        // Assert
        assert(w3wSuggestion.w3wAddress.formattedWords() == "///index.home.raft")
        assert(w3wSuggestion.w3wAddress.center?.lat == null)
        assert(w3wSuggestion.w3wAddress.center?.lng == null)
        assert(w3wSuggestion.w3wAddress.square?.northeast?.lat == null)
        assert(w3wSuggestion.w3wAddress.square?.northeast?.lat == null)
        assert(w3wSuggestion.w3wAddress.square?.southwest?.lat == null)
        assert(w3wSuggestion.w3wAddress.square?.southwest?.lat == null)
        assert(w3wSuggestion.w3wAddress.language.w3wCode == "en")
        assert(w3wSuggestion.w3wAddress.language.w3wLocale == null)
        assert(w3wSuggestion.w3wAddress.nearestPlace == "London")
        assert(w3wSuggestion.distanceToFocus?.km()?.toInt() == 1)
        assert(w3wSuggestion.rank == 1)
    }
}