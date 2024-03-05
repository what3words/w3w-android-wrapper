package com.what3words.androidwrapper.datasource.text.api.error

/**
 * Represents an error that occurs when an invalid input type is passed to the [com.what3words.core.types.options.W3WAutosuggestOptions.inputType]
 * property in the [com.what3words.core.types.options.W3WAutosuggestOptions] to make a request on the what3words API.
 *
 * This error typically indicates that the specified input type is not valid or does not conform to the required
 * format for the request. Instances of this error class provide information about the error through the error code
 * and message.
 *
 * @param code The error code associated with the BadInputTypeError.
 * @param message A descriptive message detailing the nature of the error.
 */
class BadInputTypeError(val code: String, override val message: String) : W3WApiError(
    code = code,
    errorMessage = message
)