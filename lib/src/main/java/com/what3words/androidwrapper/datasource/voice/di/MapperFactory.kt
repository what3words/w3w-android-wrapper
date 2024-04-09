package com.what3words.androidwrapper.datasource.voice.di

import com.what3words.androidwrapper.datasource.voice.mappers.SuggestionWithCoordinatesMapper

internal object MapperFactory {

    private val suggestionWithCoordinatesMapper: SuggestionWithCoordinatesMapper by lazy {
        SuggestionWithCoordinatesMapper()
    }

    fun provideSuggestionWithCoordinatesMapper(): SuggestionWithCoordinatesMapper {
        return suggestionWithCoordinatesMapper
    }
}