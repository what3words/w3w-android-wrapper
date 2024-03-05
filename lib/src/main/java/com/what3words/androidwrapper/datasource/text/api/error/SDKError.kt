package com.what3words.androidwrapper.datasource.text.api.error

/**
 * Represents an error that occurs internally with the what3words SDK used on the server.
 * This error typically indicates a problem with the server-side implementation or infrastructure.
 * Clients encountering this error should reach out to support@what3words.com for assistance, and we'll do our best to resolve the issue.
 * **/
class SDKError(val code: String, override val message: String) : W3WApiError(
    code = code,
    errorMessage = message
)