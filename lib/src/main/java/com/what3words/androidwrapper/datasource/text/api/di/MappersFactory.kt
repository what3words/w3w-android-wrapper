package com.what3words.androidwrapper.datasource.text.api.di

import com.what3words.androidwrapper.common.Mapper
import com.what3words.androidwrapper.datasource.text.api.mappers.AutosuggestResponseMapper
import com.what3words.androidwrapper.datasource.text.api.mappers.AvailableLanguagesResponseMapper
import com.what3words.androidwrapper.datasource.text.api.mappers.ConvertTo3waResponseMapper
import com.what3words.androidwrapper.datasource.text.api.mappers.ConvertToCoordinatesResponseMapper
import com.what3words.androidwrapper.datasource.text.api.mappers.CoordinatesDtoToDomainMapper
import com.what3words.androidwrapper.datasource.text.api.mappers.GridSectionResponseMapper
import com.what3words.androidwrapper.datasource.text.api.mappers.LanguageDtoToDomainMapper
import com.what3words.androidwrapper.datasource.text.api.mappers.LineDtoToDomainMapper
import com.what3words.androidwrapper.datasource.text.api.mappers.SquareDtoToDomainMapper
import com.what3words.androidwrapper.datasource.text.api.response.AutosuggestResponse
import com.what3words.androidwrapper.datasource.text.api.response.AvailableLanguagesResponse
import com.what3words.androidwrapper.datasource.text.api.response.ConvertTo3waResponse
import com.what3words.androidwrapper.datasource.text.api.response.ConvertToCoordinatesResponse
import com.what3words.androidwrapper.datasource.text.api.response.GridSectionResponse
import com.what3words.core.types.domain.W3WAddress
import com.what3words.core.types.domain.W3WSuggestion
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WGridSection
import com.what3words.core.types.language.W3WLanguage

internal object MappersFactory {
    // Lazy delegates to create mapper instances only when they are accessed
    private val gridSectionResponseMapper: GridSectionResponseMapper by lazy {
        GridSectionResponseMapper(lineDtoToDomainMapper = lineDtoToDomainMapper)
    }

    private val availableLanguagesResponseMapper: AvailableLanguagesResponseMapper by lazy {
        AvailableLanguagesResponseMapper(languageDtoToDomainMapper = languageDtoToDomainMapper)
    }

    private val autosuggestResponseMapper: AutosuggestResponseMapper by lazy {
        AutosuggestResponseMapper(
            coordinatesDtoToDomainMapper = coordinatesDtoToDomainMapper,
            squareDtoToDomainMapper = squareDtoToDomainMapper
        )
    }

    private val convertToCoordinatesResponseMapper: ConvertToCoordinatesResponseMapper by lazy {
        ConvertToCoordinatesResponseMapper(coordinatesDtoToDomainMapper = coordinatesDtoToDomainMapper)
    }

    private val convertTo3waDtoToDomainMapper: ConvertTo3waResponseMapper by lazy {
        ConvertTo3waResponseMapper(
            coordinatesDtoToDomainMapper = coordinatesDtoToDomainMapper,
            squareDtoToDomainMapper = squareDtoToDomainMapper
        )
    }

    private val coordinatesDtoToDomainMapper: CoordinatesDtoToDomainMapper by lazy {
        CoordinatesDtoToDomainMapper()
    }

    private val languageDtoToDomainMapper: LanguageDtoToDomainMapper by lazy {
        LanguageDtoToDomainMapper()
    }

    private val squareDtoToDomainMapper: SquareDtoToDomainMapper by lazy {
        SquareDtoToDomainMapper(coordinatesDtoToDomainMapper = coordinatesDtoToDomainMapper)
    }

    private val lineDtoToDomainMapper: LineDtoToDomainMapper by lazy {
        LineDtoToDomainMapper(coordinatesDtoToDomainMapper = coordinatesDtoToDomainMapper)
    }

    // Factories for creating mapper instances
    fun providesGridSectionResponseMapper(): Mapper<GridSectionResponse, W3WGridSection> =
        gridSectionResponseMapper

    fun providesAvailableLanguagesResponseMapper(): Mapper<AvailableLanguagesResponse, Set<W3WLanguage>> =
        availableLanguagesResponseMapper

    fun providesAutosuggestResponseMapper(): Mapper<AutosuggestResponse, List<W3WSuggestion>> =
        autosuggestResponseMapper

    fun providesConvertToCoordinatesResponseMapper(): Mapper<ConvertToCoordinatesResponse, W3WCoordinates> =
        convertToCoordinatesResponseMapper

    fun providesConvertTo3waDtoToDomainMapper(): Mapper<ConvertTo3waResponse, W3WAddress> =
        convertTo3waDtoToDomainMapper
}