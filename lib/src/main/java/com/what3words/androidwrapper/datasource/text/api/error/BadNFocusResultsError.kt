package com.what3words.androidwrapper.datasource.text.api.error


class BadNFocusResultsError(val code: String, override val message: String) : W3WApiError(
    code = code,
    errorMessage = message
)