package com.what3words.androidwrapper.datasource.text.api.error

class BadLanguageError(code: String, message: String) : W3WApiError(
    code = code,
    errorMessage = message
)
