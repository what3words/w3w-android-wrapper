package com.what3words.androidwrapper.datasource.text.api.error

/**
 * Error indicating that the what3words API key has been suspended and requires reactivation.
 *
 * This error occurs when attempting to use a suspended API key with the what3words Android wrapper.
 *
 * @param code The error code associated with the suspended key error.
 * @param message A descriptive message providing additional information about the error.
 */
class SuspendedKeyError(val code: String, override val message: String) : W3WApiError(
    code = code,
    errorMessage = message
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SuspendedKeyError

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
        return "SuspendedKeyError(code='$code', message='$message')"
    }
}