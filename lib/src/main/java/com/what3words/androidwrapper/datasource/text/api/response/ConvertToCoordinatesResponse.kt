package com.what3words.androidwrapper.datasource.text.api.response

import com.what3words.androidwrapper.datasource.text.api.dto.CoordinatesDto
import com.what3words.androidwrapper.datasource.text.api.dto.ErrorDto
import com.what3words.androidwrapper.datasource.text.api.dto.SquareDto

internal data class ConvertToCoordinatesResponse(
    val country: String?,
    val square: SquareDto?,
    val nearestPlace: String?,
    val coordinates: CoordinatesDto?,
    val words: String?,
    val language: String?,
    val locale: String?,
    val map: String?,
    override val error: ErrorDto?
) : APIResponse