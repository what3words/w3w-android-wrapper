package com.what3words.androidwrapper.datasource.text.api.error

/**
 * Error indicating an internal server error occurred while processing the request on the what3words server.
 *
 * This error typically indicates a problem with the server-side implementation or infrastructure.
 * Clients encountering this error should reach out to support@what3words.com for assistance, and we'll do our best to resolve the issue.
 *
 * @param code The error code associated with the internal server error.
 * @param message A descriptive message providing additional information about the error.
 */
class InternalServerError(val code: String, override val message: String) : W3WApiError(
    code = code,
    errorMessage = message
)