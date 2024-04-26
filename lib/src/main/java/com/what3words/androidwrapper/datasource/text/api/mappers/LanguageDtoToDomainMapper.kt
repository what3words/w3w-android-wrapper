package com.what3words.androidwrapper.datasource.text.api.mappers

import com.what3words.androidwrapper.common.Mapper
import com.what3words.androidwrapper.datasource.text.api.dto.LanguageDto
import com.what3words.core.types.language.W3WProprietaryLanguage

internal class LanguageDtoToDomainMapper : Mapper<LanguageDto, List<W3WProprietaryLanguage>> {
    override fun mapFrom(from: LanguageDto): List<W3WProprietaryLanguage> {
        return buildList {
            add(
                W3WProprietaryLanguage(
                    code = from.code,
                    locale = null,
                    name = from.name,
                    nativeName = from.nativeName
                )
            )
            from.locales?.map {
                W3WProprietaryLanguage(
                    code = from.code,
                    locale = it.code,
                    name = it.name,
                    nativeName = it.nativeName
                )
            }?.let(::addAll)
        }
    }
}