package com.what3words.androidwrapper.datasource.text.api.error

/**
 * Represents an error that occurs when an invalid coordinate is specified for the convertTo3wa method
 * in the [com.what3words.core.datasource.W3WTextDatasource].
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
)