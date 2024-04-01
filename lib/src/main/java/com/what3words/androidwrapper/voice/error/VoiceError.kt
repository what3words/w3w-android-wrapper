package com.what3words.androidwrapper.voice.error

import com.what3words.core.types.common.W3WError

/**
 * This is a base class that represents the errors that can occur during the integration of the what3words Voice API.
 * The errors are divided into two categories:
 * 1. Errors that occur during the initial connection before a StartRecognition message is sent.
 * 2. Errors that occur during WebSocket streaming.
 *
 * For more details on the error types, refer to the following document: https://developer.what3words.com/voice-api/docs#error-handling
 */
sealed class W3WApiVoiceError(errorMessage: String) : W3WError(errorMessage) {

    /**
     * This is a data class that represents a connection error.
     *
     * @property code The error code.
     * @property message The error message.
     */
    data class ConnectionError(val code: String, override val message: String) :
        W3WApiVoiceError("$code: $message")


    /**
     * This is a data class that represents a streaming error.
     *
     * @property type The type of the error.
     * @property reason The reason for the error.
     * @property code The error code. It is optional and can be null.
     */
    data class StreamingError(val type: String, val reason: String, val code: Int? = null) :
        W3WApiVoiceError("Streaming error: $type, $reason")
}