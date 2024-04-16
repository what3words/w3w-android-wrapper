package com.what3words.androidwrapper.datasource.voice.di

import com.what3words.androidwrapper.datasource.voice.mappers.SuggestionMapper
import com.what3words.androidwrapper.datasource.voice.mappers.SuggestionWithCoordinatesMapper

internal object MapperFactory {

    private val suggestionWithCoordinatesMapper: SuggestionWithCoordinatesMapper by lazy {
        SuggestionWithCoordinatesMapper()
    }

    private val suggestionMapper: SuggestionMapper by lazy {
        SuggestionMapper()
    }

    fun provideSuggestionWithCoordinatesMapper(): SuggestionWithCoordinatesMapper {
        return suggestionWithCoordinatesMapper
    }

    fun provideSuggestionMapper(): SuggestionMapper {
        return suggestionMapper
    }
}