package com.what3words.androidwrapper.datasource.text.api.error


/**
 * Represents an error that occurs when an invalid country is passed for the [com.what3words.core.types.options.W3WAutosuggestOptions.clipToCountry] property
 * in the [com.what3words.core.types.options.W3WAutosuggestOptions] to make a request on the What3words Text Datasource.
 *
 * This error typically indicates that the specified country is not valid or does not conform to the required
 * format for the request. Instances of this error class provide information about the error through the error code
 * and message.
 *
 * @param code The error code associated with the BadClipToCountryError.
 * @param message A descriptive message detailing the nature of the error.
 */
class BadClipToCountryError(code: String, message: String) : W3WApiError(
    code = code,
    errorMessage = message
)