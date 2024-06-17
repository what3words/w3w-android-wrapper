package com.what3words.androidwrapper.datasource.text.api.error

/**
 * Represents an error that occurs when an invalid coordinate is specified for the convertTo3wa method
 * in the [com.what3words.core.datasource.W3WTextDataSource].
 *
 * This error typically indicates that the specified coordinate is not valid or does not conform to the required
 * format for the request. Instances of this error class provide information about the error through the error code
 * and message.
 *
 * @param code The error code associated with the BadCoordinatesError.
 * @param message A descriptive message detailing the nature of the error.
 */
class BadCoordinatesError(val code: String, override val message: String) : W3WApiError(
    code = code,
    errorMessage = message
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BadCoordinatesError

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
        return "BadCoordinatesError(code='$code', message='$message')"
    }
}