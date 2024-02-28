package com.what3words.androidwrapper.datasource.text.api.error

/**
 * Error indicating an I/O exception occurred while making a network request to the what3words server.
 *
 * This error is typically thrown when there is an issue with network connectivity or communication during the API request process.
 *
 */
class NetworkError : W3WApiError {
    /**
     * Constructor for creating a NetworkError instance with a specific error code and message.
     *
     * @param code The error code associated with the network error.
     * @param message A descriptive message providing additional information about the error.
     */
    constructor(code: String, message: String) : super(
        code = code,
        errorMessage = message
    )

    /**
     * Constructor for creating a NetworkError instance from a Throwable.
     *
     * @param throwable The Throwable object representing the exception that occurred.
     */
    constructor(throwable: Throwable?) : super(throwable)
}
