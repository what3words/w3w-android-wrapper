package com.what3words.androidwrapper.datasource.text.api.error

/**
 * Represents an error that occurs internally with the what3words SDK used on the server.
 * This error typically indicates a problem with the server-side implementation or infrastructure.
 * Clients encountering this error should reach out to support@what3words.com for assistance, and we'll do our best to resolve the issue.
 * **/
class SDKError(val code: String, override val message: String) : W3WApiError(
    code = code,
    errorMessage = message
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SDKError

        if (code != other.code) return false
        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        var result = code.hashCode()
        result = 31 * result + message.hashCode()
        return result
    }

    override fun toString(): String {
        return "SDKError(code='$code', message='$message')"
    }
}