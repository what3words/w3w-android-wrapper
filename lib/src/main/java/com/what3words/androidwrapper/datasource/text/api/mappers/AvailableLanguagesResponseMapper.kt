package com.what3words.androidwrapper.datasource.text.api.mappers

import com.what3words.androidwrapper.common.Mapper
import com.what3words.androidwrapper.datasource.text.api.dto.LanguageDto
import com.what3words.androidwrapper.datasource.text.api.response.AvailableLanguagesResponse
import com.what3words.core.types.language.W3WLanguage

internal class AvailableLanguagesResponseMapper(
    private val languageDtoToDomainMapper: Mapper<LanguageDto, List<W3WLanguage>>
) : Mapper<AvailableLanguagesResponse, Set<W3WLanguage>> {
    override fun mapFrom(from: AvailableLanguagesResponse): Set<W3WLanguage> {
        return buildSet {
            from.languages.forEach { language: LanguageDto ->
                addAll(languageDtoToDomainMapper.mapFrom(language))
            }
        }
    }
}