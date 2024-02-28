package com.what3words.androidwrapper.datasource.text.api.error


class BadNFocusResultsError(code: String, message: String) : W3WApiError(
    code = code,
    errorMessage = message
)