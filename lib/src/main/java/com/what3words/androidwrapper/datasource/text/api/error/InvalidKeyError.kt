package com.what3words.androidwrapper.datasource.text.api.error

/**
 * Error indicating that an invalid API key was used to initialize the [com.what3words.androidwrapper.datasource.text.W3WApiTextDatasource] class.
 *
 * This error typically occurs when the provided API key does not match the expected format or is not valid for accessing the what3words API.
 *
 * @param code The error code associated with the invalid key error.
 * @param message A descriptive message providing additional information about the error.
 */

class InvalidKeyError(val code: String, override val message: String) : W3WApiError(
    code = code,
    errorMessage = message
)