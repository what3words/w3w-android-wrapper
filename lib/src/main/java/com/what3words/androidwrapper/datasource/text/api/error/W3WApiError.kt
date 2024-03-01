package com.what3words.androidwrapper.datasource.text.api.error

import com.what3words.core.types.common.W3WError

/**
 * Base class representing errors that can occur in the what3words API integration.
 *
 * This sealed class serves as the parent class for all specific error types encountered during API interactions.
 * It provides a consistent interface for handling errors across the what3words Android wrapper.
 *
 * @constructor Creates a W3WApiError instance with the specified error code and message.
 * @constructor Creates a W3WApiError instance from a Throwable.
 */

sealed class W3WApiError : W3WError {
    /**
     * Constructor for creating a W3WApiError instance with a specific error code and message.
     *
     * @param code The error code associated with the error.
     * @param errorMessage A descriptive message providing additional information about the error.
     */
    constructor(code: String?, errorMessage: String?) : super(message = "$code: $errorMessage")

    /**
     * Constructor for creating a W3WApiError instance from a Throwable.
     *
     * @param throwable The Throwable object representing the exception that occurred.
     */
    constructor(throwable: Throwable?) : super(throwable)
}