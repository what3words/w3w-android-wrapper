package com.what3words.androidwrapper.datasource.text.api.mappers

import com.what3words.androidwrapper.common.Mapper
import com.what3words.androidwrapper.datasource.text.api.dto.LanguageDto
import com.what3words.androidwrapper.datasource.text.api.response.AvailableLanguagesResponse
import com.what3words.core.types.language.W3WProprietaryLanguage

internal class AvailableLanguagesResponseMapper(
    private val languageDtoToDomainMapper: Mapper<LanguageDto, List<W3WProprietaryLanguage>>
) : Mapper<AvailableLanguagesResponse, Set<W3WProprietaryLanguage>> {
    override fun mapFrom(from: AvailableLanguagesResponse): Set<W3WProprietaryLanguage> {
        return buildSet {
            from.languages.forEach { language: LanguageDto ->
                addAll(languageDtoToDomainMapper.mapFrom(language))
            }
        }
    }
}