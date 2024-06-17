package com.what3words.androidwrapper.datasource.text.api.response

import com.what3words.androidwrapper.datasource.text.api.dto.ErrorDto
import com.what3words.androidwrapper.datasource.text.api.dto.SuggestionDto

internal data class AutosuggestResponse(
    val suggestions: List<SuggestionDto>?,
    override val error: ErrorDto?
) : APIResponse
