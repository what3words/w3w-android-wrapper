package com.what3words.androidwrapper.datasource.text.api.error

/**
 * Represents an error that occurs when an invalid bounding box is passed for the [com.what3words.core.types.options.W3WAutosuggestOptions.clipToBoundingBox] property
 * in the [com.what3words.core.types.options.W3WAutosuggestOptions] to make a what3words request.
 *
 * This error typically indicates that the specified bounding box is not valid or does not conform to the required
 * format for the request. Instances of this error class provide information about the error through the error code
 * and message.
 *
 * @param code The error code associated with the BadClipToBoundingBoxError.
 * @param message A descriptive message detailing the nature of the error.
 */
class BadClipToBoundingBoxError(val code: String, override val message: String) : W3WApiError(
    code = code,
    errorMessage = message
)