package com.what3words.androidwrapper.datasource.text.api.response

import com.what3words.androidwrapper.datasource.text.api.dto.ErrorDto

internal sealed interface APIResponse {
    val error: ErrorDto?

    fun isSuccessful(): Boolean {
        return error == null
    }
}