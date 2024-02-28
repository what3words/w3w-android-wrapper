package com.what3words.androidwrapper.datasource.text.api.dto

internal data class LanguageDto(
    val nativeName: String,
    val code: String,
    val name: String,
    val locales: List<LocaleDto>?
) {
    data class LocaleDto(
        val nativeName: String,
        val code: String,
        val name: String
    )
}
