package com.what3words.androidwrapper.datasource.text.api.error

/**
 * Error indicating that an invalid API key was used to initialize the [com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource] class.
 *
 * This error typically occurs when the provided API key does not match the expected format or is not valid for accessing the what3words API.
 *
 * @param code The error code associated with the invalid key error.
 * @param message A descriptive message providing additional information about the error.
 */

class InvalidKeyError(val code: String, override val message: String) : W3WApiError(
    code = code,
    errorMessage = message
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InvalidKeyError

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
        return "InvalidKeyError(code='$code', message='$message')"
    }
}