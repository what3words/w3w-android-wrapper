package com.what3words.androidwrapper.datasource.text.api.error

/**
 * Represents an error that occurs when the requested bounding box exceeds a size of 4km from corner to corner
 * on the What3words API.
 *
 * This error typically indicates that the specified bounding box is too large for the request and exceeds the
 * maximum allowable size of 4km from one corner to another. Instances of this error class provide information
 * about the error through the error code and message.
 *
 * @param code The error code associated with the BadBoundingBoxTooBigError.
 * @param message A descriptive message detailing the nature of the error.
 */
class BadBoundingBoxTooBigError(val code: String, override val message: String) : W3WApiError(
    code = code,
    errorMessage = message
)