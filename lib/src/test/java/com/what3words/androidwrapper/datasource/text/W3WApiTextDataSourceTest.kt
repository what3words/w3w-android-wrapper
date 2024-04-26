package com.what3words.androidwrapper.datasource.text

import com.what3words.androidwrapper.datasource.text.api.di.MappersFactory
import com.what3words.androidwrapper.datasource.text.api.error.BadBoundingBoxError
import com.what3words.androidwrapper.datasource.text.api.error.BadCoordinatesError
import com.what3words.androidwrapper.datasource.text.api.error.BadWordsError
import com.what3words.androidwrapper.datasource.text.fake.FakeWhat3WordsV3Service
import com.what3words.core.types.common.W3WResult
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WRectangle
import com.what3words.core.types.language.W3WProprietaryLanguage
import com.what3words.core.types.language.W3WRFC5646Language
import com.what3words.core.types.options.W3WAutosuggestOptions
import org.junit.Test

class W3WApiTextDataSourceTest {

    private val fakeWhat3WordsV3Service = FakeWhat3WordsV3Service()

    private val w3WApiTextDataSource = W3WApiTextDataSource(
        fakeWhat3WordsV3Service,
        MappersFactory.providesConvertTo3waDtoToDomainMapper(),
        MappersFactory.providesConvertToCoordinatesResponseMapper(),
        MappersFactory.providesAutosuggestResponseMapper(),
        MappersFactory.providesAvailableLanguagesResponseMapper(),
        MappersFactory.providesGridSectionResponseMapper()
    )

    @Test
    fun convertTo3wa_withInvalidCoordinates_returnsBadCoordinatesError() {
        val result = w3WApiTextDataSource.convertTo3wa(
            W3WCoordinates(-91.0, 181.0),
            W3WProprietaryLanguage("en", "en_GB", "English", "English")
        )
        assert(result is W3WResult.Failure)
        result as W3WResult.Failure
        assert(result.error is BadCoordinatesError)
    }

    @Test
    fun convertTo3wa_withValidCoordinates_returnsW3WAddress() {
        val result = w3WApiTextDataSource.convertTo3wa(
            W3WCoordinates(10.251020, 105.574460),
            W3WProprietaryLanguage("en", "en_GB", name = null, nativeName = null)
        )
        assert(result is W3WResult.Success)
        result as W3WResult.Success
        assert(result.value.address == "///${fakeWhat3WordsV3Service.w3w}")
    }

    @Test
    fun convertTo3wa_withValidCoordinatesAndRFC5646Language_returnsW3WAddress() {
        val result = w3WApiTextDataSource.convertTo3wa(
            W3WCoordinates(10.251020, 105.574460),
            W3WRFC5646Language.EN_GB
        )
        assert(result is W3WResult.Success)
        result as W3WResult.Success
        assert(result.value.address == "///${fakeWhat3WordsV3Service.w3w}")
    }

    @Test
    fun convertToCoordinates_withValidAddress_returnsW3WCoordinates() {
        val result = w3WApiTextDataSource.convertToCoordinates("country.square.words")
        assert(result is W3WResult.Success)
        result as W3WResult.Success
        assert(result.value.center?.lat == fakeWhat3WordsV3Service.coordinatesDto.lat)
        assert(result.value.center?.lng == fakeWhat3WordsV3Service.coordinatesDto.lng)
    }

    @Test
    fun convertToCoordinates_withInvalidAddress_returnsBadWordsError() {
        val result = w3WApiTextDataSource.convertToCoordinates("invalid.address")
        assert(result is W3WResult.Failure)
        result as W3WResult.Failure
        assert(result.error is BadWordsError)
    }

    @Test
    fun autosuggest_withoutCoordinates_returnsSuggestionsWithoutCoordinates() {
        val result = w3WApiTextDataSource.autosuggest(
            "country.square.wor",
            options = W3WAutosuggestOptions.Builder().includeCoordinates(false).build()
        )
        assert(result is W3WResult.Success)
        result as W3WResult.Success
        assert(result.value[0].w3wAddress.center == null)
    }

    @Test
    fun autosuggest_withCoordinates_returnsSuggestionsWithCoordinates() {
        val result = w3WApiTextDataSource.autosuggest(
            "country.square.wor",
            options = W3WAutosuggestOptions.Builder().includeCoordinates(true).build()
        )
        assert(result is W3WResult.Success)
        result as W3WResult.Success
        assert(result.value[0].w3wAddress.center != null)
    }

    @Test
    fun autosuggest_withNullOptions_returnsSuggestionsWithoutCoordinates() {
        val result = w3WApiTextDataSource.autosuggest(
            "country.square.wor",
            options = null
        )
        assert(result is W3WResult.Success)
        result as W3WResult.Success
        assert(result.value[0].w3wAddress.center == null)
    }

    @Test
    fun gridSection_withValidBoundingBox_returnsGridSection() {
        val result = w3WApiTextDataSource.gridSection(
            W3WRectangle(W3WCoordinates(51.0, 0.0), W3WCoordinates(52.0, 0.1))
        )
        assert(result is W3WResult.Success)
        result as W3WResult.Success
    }

    @Test
    fun gridSection_withInvalidBoundingBox_returnsBadBoundingBoxError() {
        val result = w3WApiTextDataSource.gridSection(
            fakeWhat3WordsV3Service.invalidRectangle
        )
        assert(result is W3WResult.Failure)
        result as W3WResult.Failure
        assert(result.error is BadBoundingBoxError)
    }

    @Test
    fun availableLanguages_returnsLanguages() {
        val result = w3WApiTextDataSource.availableLanguages()
        assert(result is W3WResult.Success)
        result as W3WResult.Success
        assert(result.value.isNotEmpty())
    }
}