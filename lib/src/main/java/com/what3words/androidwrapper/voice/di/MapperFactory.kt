package com.what3words.androidwrapper.voice.di

import com.what3words.androidwrapper.voice.mappers.SuggestionWithCoordinatesMapper

internal object MapperFactory {

    private val suggestionWithCoordinatesMapper: SuggestionWithCoordinatesMapper by lazy {
        SuggestionWithCoordinatesMapper()
    }

    fun provideSuggestionWithCoordinatesMapper(): SuggestionWithCoordinatesMapper {
        return suggestionWithCoordinatesMapper
    }
}