package com.what3words.androidwrapper.datasource.text.api.error

/**
 * Represents an error that occurs when an invalid input is provided to the what3words API.
 *
 * This error typically indicates that the provided input is not valid or does not meet the required criteria
 * for the API request. Instances of this error class provide information about the error through the error code
 * and message.
 *
 * @param code The error code associated with the BadInputError.
 * @param message A descriptive message detailing the nature of the error.
 */
class BadInputError(val code: String, override val message: String) : W3WApiError(
    code = code,
    errorMessage = message
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BadInputError

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
        return "BadInputError(code='$code', message='$message')"
    }
}