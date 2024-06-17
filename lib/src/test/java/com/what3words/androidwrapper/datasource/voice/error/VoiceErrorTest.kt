package com.what3words.androidwrapper.datasource.voice.error

import com.what3words.androidwrapper.CoroutineTestRule
import com.what3words.androidwrapper.datasource.voice.error.W3WApiVoiceError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VoiceErrorTest {

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    @Test
    fun `ConnectionError returns correct formatted message`() = runTest {
        // Arrange
        val error = W3WApiVoiceError.ConnectionError("400", "Bad Request")

        // Act
        val message = error.message

        // Assert
        assert(message == "Bad Request")
    }

    @Test
    fun `StreamingError returns correct formatted message when code is null`() = runTest {
        // Arrange
        val error = W3WApiVoiceError.StreamingError("Network", "Network Unavailable")

        // Act
        val message = error.message

        // Assert
        assert(message == "Streaming error: Network, Network Unavailable")
    }

    @Test
    fun `StreamingError returns correct formatted message when code is not null`() = runTest {
        // Arrange
        val error = W3WApiVoiceError.StreamingError("Network", "Network Unavailable", 500)

        // Act
        val message = error.message

        // Assert
        assert(message == "Streaming error: Network, Network Unavailable")
    }
}