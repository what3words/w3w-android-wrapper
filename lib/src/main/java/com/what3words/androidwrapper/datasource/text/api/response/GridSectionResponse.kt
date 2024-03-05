package com.what3words.androidwrapper.datasource.text.api.response

import com.what3words.androidwrapper.datasource.text.api.dto.ErrorDto
import com.what3words.androidwrapper.datasource.text.api.dto.LineDto

internal data class GridSectionResponse(
    val lines: List<LineDto>,
    override val error: ErrorDto?
): APIResponse
