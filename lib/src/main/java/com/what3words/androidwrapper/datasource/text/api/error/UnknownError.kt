package com.what3words.androidwrapper.datasource.text.api.error

/**
 * Error indicating that the error returned by the what3words API couldn't be mapped to one of the
 * predefined [W3WApiError] classes in the Android wrapper.
 *
 * This error is used when the specific type of error returned by the API is not recognized or handled
 * by the wrapper, but the provided code and message contain all the information about the error.
 *
 */
class UnknownError : W3WApiError {
    val code: String?
    override val message: String?

    /**
     * Constructor for creating an UnknownError instance with a specific error code and message.
     *
     * @param code The error code associated with the unknown error.
     * @param message A descriptive message providing additional information about the error.
     */
    constructor(code: String, message: String) : super(
        code = code,
        errorMessage = message
    ) {
        this.code = code
        this.message = message
    }

    /**
     * Constructor for creating an UnknownError instance from a Throwable.
     *
     * @param throwable The Throwable object representing the exception that occurred.
     */

    constructor(throwable: Throwable?) : super(throwable) {
        this.code = null
        this.message = null
    }
}