package com.what3words.androidwrapper.datasource.text.api.mapper

import com.what3words.androidwrapper.datasource.text.api.dto.LanguageDto
import com.what3words.androidwrapper.datasource.text.api.mappers.LanguageDtoToDomainMapper
import com.what3words.core.types.language.W3WProprietaryLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageDtoToDomainMapperTest {

    private val mapper = LanguageDtoToDomainMapper()

    @Test
    fun `mapFrom should return list of W3WLanguage when locales is not null`() {
        val languageDto = LanguageDto(
            nativeName = "English",
            code = "en",
            name = "English",
            locales = listOf(
                LanguageDto.LocaleDto(
                    code = "en_US",
                    name = "United States",
                    nativeName = "United States"
                ),
                LanguageDto.LocaleDto(
                    code = "en_GB",
                    name = "United Kingdom",
                    nativeName = "United Kingdom"
                )
            )
        )

        val expected = listOf(
            W3WProprietaryLanguage(
                code = "en",
                locale = null,
                name = "English",
                nativeName = "English",
            ),
            W3WProprietaryLanguage(
                code = "en",
                locale = "en_US",
                name = "United States",
                nativeName = "United States"
            ),
            W3WProprietaryLanguage(
                code = "en",
                locale = "en_GB",
                name = "United Kingdom",
                nativeName = "United Kingdom"
            )
        )

        val result = mapper.mapFrom(languageDto)

        assertEquals(expected, result)
    }

    @Test
    fun `mapFrom should return list with single W3WLanguage when locales is null`() {
        val languageDto = LanguageDto(
            nativeName = "Tiếng Việt",
            code = "vi",
            name = "Vietnamese",
            locales = null
        )

        val expected = listOf(
            W3WProprietaryLanguage(
                code = "vi",
                locale = null,
                name = "Vietnamese",
                nativeName = "Tiếng Việt"
            )
        )

        val result = mapper.mapFrom(languageDto)

        assertEquals(result.size, 1)
        assertEquals(expected, result)
    }

    @Test
    fun `mapFrom should return empty list when locales is empty`() {
        val languageDto = LanguageDto(
            nativeName = "Tiếng Việt",
            code = "vi",
            name = "Vietnamese",
            locales = emptyList()
        )

        val expected = listOf(
            W3WProprietaryLanguage(
                code = "vi",
                locale = null,
                name = "Vietnamese",
                nativeName = "Tiếng Việt"
            )
        )

        val result = mapper.mapFrom(languageDto)

        assertEquals(result.size, 1)
        assertEquals(expected, result)
    }
}