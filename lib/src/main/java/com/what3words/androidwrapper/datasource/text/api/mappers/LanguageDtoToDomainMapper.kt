package com.what3words.androidwrapper.datasource.text.api.mappers

import com.what3words.androidwrapper.common.Mapper
import com.what3words.androidwrapper.datasource.text.api.dto.LanguageDto
import com.what3words.core.types.language.W3WLanguage

internal class LanguageDtoToDomainMapper : Mapper<LanguageDto, List<W3WLanguage>> {
    override fun mapFrom(from: LanguageDto): List<W3WLanguage> {
        return buildList {
            if (from.locales.isNullOrEmpty()) {
                add(
                    W3WLanguage(
                        code = from.code, locale = null
                    )
                )
            } else {
                addAll(from.locales.map { localeDto ->
                    W3WLanguage(
                        code = from.code, locale = localeDto.code
                    )
                })
            }
        }
    }
}