package com.what3words.androidwrapper.datasource.text.api.error

/**
 * Error indicating that the referrer provided in the API request is invalid.
 *
 * This error occurs when the referrer information supplied with the API request is not recognized or accepted by the what3words server.
 * Ensure that the referrer is correctly formatted and corresponds to a valid source authorized to make API requests.
 *
 * @param code The error code associated with the invalid referrer error.
 * @param message A descriptive message providing additional information about the error.
 */
class InvalidReferrerError(val code: String, override val message: String) : W3WApiError(
    code = code,
    errorMessage = message
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InvalidReferrerError

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
        return "InvalidReferrerError(code='$code', message='$message')"
    }
}