package com.what3words.androidwrapper.datasource.text.api.error

/**
 * Error indicating an I/O exception occurred while making a network request to the what3words server.
 *
 * This error is typically thrown when there is an issue with network connectivity or communication during the API request process.
 *
 */
class NetworkError : W3WApiError {
    val code: String?
    override val message: String?
    /**
     * Constructor for creating a NetworkError instance with a specific error code and message.
     *
     * @param code The error code associated with the network error.
     * @param message A descriptive message providing additional information about the error.
     */
    constructor(code: String, message: String) : super(
        code = code,
        errorMessage = message
    ){
        this.code = code
        this.message = message
    }


    /**
     * Constructor for creating a NetworkError instance from a Throwable.
     *
     * @param throwable The Throwable object representing the exception that occurred.
     */
    constructor(throwable: Throwable?) : super(throwable){
        this.code = null
        this.message = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NetworkError

        if (code != other.code) return false
        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        var result = code?.hashCode() ?: 0
        result = 31 * result + (message?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "NetworkError(code=$code, message=$message)"
    }
}
