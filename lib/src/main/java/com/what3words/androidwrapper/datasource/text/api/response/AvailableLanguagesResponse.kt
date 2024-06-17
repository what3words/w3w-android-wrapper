package com.what3words.androidwrapper.datasource.text.api.response

import com.what3words.androidwrapper.datasource.text.api.dto.ErrorDto
import com.what3words.androidwrapper.datasource.text.api.dto.LanguageDto

internal data class AvailableLanguagesResponse(
    val languages: List<LanguageDto>,
    override val error: ErrorDto?
) : APIResponse
