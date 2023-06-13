package com.what3words.androidwrapper

import android.media.AudioFormat
import com.what3words.androidwrapper.voice.Microphone
import com.what3words.androidwrapper.voice.VoiceApi
import com.what3words.androidwrapper.voice.VoiceApi.Companion.BASE_URL
import com.what3words.androidwrapper.voice.VoiceApiListener
import com.what3words.androidwrapper.voice.VoiceApiListenerWithCoordinates
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.junit.Before
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class VoiceApiTests {
    @MockK
    private lateinit var mockWebSocket: WebSocket

    @MockK
    private lateinit var mockClient: OkHttpClient

    @MockK
    private lateinit var listenerWithCoordinates: VoiceApiListenerWithCoordinates

    @MockK
    private lateinit var listener: VoiceApiListener

    private lateinit var voiceApi: VoiceApi

    @Before
    fun setup() {
        mockClient = mockk()
        mockWebSocket = mockk()
        listener = mockk()
        listenerWithCoordinates = mockk()

        justRun {
            listener.connected(any())
            listener.suggestions(any())
            listener.error(any())
        }
        justRun {
            listenerWithCoordinates.connected(any())
            listenerWithCoordinates.suggestionsWithCoordinates(any())
            listenerWithCoordinates.error(any())
        }

        every {
            mockWebSocket.send("voice1")
        }.answers {
            true
        }

        every {
            mockWebSocket.send("voice2")
        }.answers {
            true
        }

        every {
            mockWebSocket.send("voice3")
        }.answers {
            true
        }

        every {
            mockWebSocket.send("voice3")
        }.answers {
            true
        }

        // any other send() i.e onOpen json

        every {
            mockWebSocket.send(any<String>())
        }.answers {
            true
        }

        every {
            mockWebSocket.close(any(), any())
        }.answers {
            true
        }

        voiceApi = VoiceApi("any", "any", mockClient)
    }

    private fun mockWebSocket(
        message: String,
        nextMessage: String,
        closeCode: Int = 1000,
        closeReason: String = "closed by server"
    ) {
        every {
            mockClient.newWebSocket(any(), any())
        }.answers {
            val wsl =
                this.arg<WebSocketListener>(1)

            wsl.onOpen(mockWebSocket, mockk())
            wsl.onMessage(mockWebSocket, message)

            every {
                mockWebSocket.send("voiceEnd")
            }.answers {
                wsl.onMessage(
                    mockWebSocket, nextMessage
                )
                true
            }

            every {
                mockWebSocket.close(closeCode, closeReason)
            }.answers {
                wsl.onClosing(mockWebSocket, closeCode, closeReason)
                true
            }
            mockWebSocket
        }
    }

    private fun mockWebSocketFailure(
        message: String,
        failureMessage: String
    ) {
        every {
            mockClient.newWebSocket(any(), any())
        }.answers {
            val wsl =
                this.arg<WebSocketListener>(1)

            wsl.onOpen(mockWebSocket, mockk())
            wsl.onMessage(mockWebSocket, message)

            every {
                mockWebSocket.send("voice2")
            }.answers {
                wsl.onFailure(mockWebSocket, Exception(failureMessage), null)
                true
            }

            mockWebSocket
        }
    }

    @Test
    fun `autosuggest returns suggestions`() {
        // given
        val jsonStart = ClassLoader.getSystemResource("started.json").readText()
        val jsonSuggestions = ClassLoader.getSystemResource("voice-suggestions.json").readText()
        mockWebSocket(jsonStart, jsonSuggestions)

        // when
        voiceApi.initialize(
            Microphone.DEFAULT_RECORDING_RATE,
            AudioFormat.ENCODING_PCM_16BIT,
            BASE_URL,
            listener
        )
        mockWebSocket.send("voice1")
        mockWebSocket.send("voice2")
        mockWebSocket.send("voice3")
        mockWebSocket.send("voiceEnd")
        mockWebSocket.close(1000, "closed by server")

        // then
        verify(exactly = 5) { mockWebSocket.send(any<String>()) }
        verify(exactly = 1) { listener.connected(voiceApi) }
        verify(exactly = 1) { listener.suggestions(any()) }
        verify(exactly = 0) { listener.error(any()) }
        assert(voiceApi.socket == null)
    }

    @Test
    fun `autosuggest-with-coordinates returns suggestions`() {
        // given
        val jsonStart = ClassLoader.getSystemResource("started.json").readText()
        val jsonSuggestionsWithCoordinates =
            ClassLoader.getSystemResource("voice-suggestions-with-coordinates.json").readText()
        mockWebSocket(jsonStart, jsonSuggestionsWithCoordinates)

        // when
        voiceApi.initialize(
            Microphone.DEFAULT_RECORDING_RATE,
            AudioFormat.ENCODING_PCM_16BIT,
            BASE_URL,
            listenerWithCoordinates
        )
        mockWebSocket.send("voice1")
        mockWebSocket.send("voice2")
        mockWebSocket.send("voice3")
        mockWebSocket.send("voiceEnd")
        mockWebSocket.close(1000, "closed by server")

        // then
        verify(exactly = 1) { listenerWithCoordinates.connected(voiceApi) }
        verify(exactly = 1) {
            listenerWithCoordinates.suggestionsWithCoordinates(any())
        }
        verify(exactly = 0) { listenerWithCoordinates.error(any()) }
        assert(voiceApi.socket == null)
    }

    @Test
    fun `autosuggest returns invalid api key error`() {
        // given
        val jsonStart = ClassLoader.getSystemResource("started.json").readText()
        val jsonError = ClassLoader.getSystemResource("error-invalid-api-key.json").readText()
        mockWebSocket(jsonStart, jsonError, 1003, jsonError)

        // when
        voiceApi.initialize(
            Microphone.DEFAULT_RECORDING_RATE,
            AudioFormat.ENCODING_PCM_16BIT,
            BASE_URL,
            listener
        )
        mockWebSocket.close(1003, jsonError)

        // then
        verify(exactly = 1) { listener.connected(voiceApi) }
        verify(exactly = 0) { listener.suggestions(any()) }
        verify(exactly = 1) { listener.error(any()) }
        assert(voiceApi.socket == null)
    }

    @Test
    fun `autosuggest-with-coordinates returns invalid api key error`() {
        // given
        val jsonStart = ClassLoader.getSystemResource("started.json").readText()
        val jsonError = ClassLoader.getSystemResource("error-invalid-api-key.json").readText()
        mockWebSocket(jsonStart, jsonError, 1003, jsonError)

        // when
        voiceApi.initialize(
            Microphone.DEFAULT_RECORDING_RATE,
            AudioFormat.ENCODING_PCM_16BIT,
            BASE_URL,
            listenerWithCoordinates
        )
        mockWebSocket.close(1003, jsonError)

        // then
        verify(exactly = 1) { listenerWithCoordinates.connected(voiceApi) }
        verify(exactly = 0) { listenerWithCoordinates.suggestionsWithCoordinates(any()) }
        verify(exactly = 1) { listenerWithCoordinates.error(any()) }
        assert(voiceApi.socket == null)
    }

    @Test
    fun `autosuggest returns missing api key error`() {
        // given
        val jsonStart = ClassLoader.getSystemResource("started.json").readText()
        val jsonError = ClassLoader.getSystemResource("error-missing-api-key.json").readText()
        mockWebSocket(jsonStart, jsonError, 1003, jsonError)

        // when
        voiceApi.initialize(
            Microphone.DEFAULT_RECORDING_RATE,
            AudioFormat.ENCODING_PCM_16BIT,
            BASE_URL,
            listener
        )
        mockWebSocket.close(1003, jsonError)

        // then
        verify(exactly = 1) { listener.connected(voiceApi) }
        verify(exactly = 0) { listener.suggestions(any()) }
        verify(exactly = 1) { listener.error(any()) }
        assert(voiceApi.socket == null)
    }

    @Test
    fun `autosuggest-with-coordinates returns missing api key error`() {
        // given
        val jsonStart = ClassLoader.getSystemResource("started.json").readText()
        val jsonError = ClassLoader.getSystemResource("error-missing-api-key.json").readText()
        mockWebSocket(jsonStart, jsonError, 1003, jsonError)

        // when
        voiceApi.initialize(
            Microphone.DEFAULT_RECORDING_RATE,
            AudioFormat.ENCODING_PCM_16BIT,
            BASE_URL,
            listenerWithCoordinates
        )
        mockWebSocket.close(1003, jsonError)

        // then
        verify(exactly = 1) { listenerWithCoordinates.connected(voiceApi) }
        verify(exactly = 0) { listenerWithCoordinates.suggestionsWithCoordinates(any()) }
        verify(exactly = 1) { listenerWithCoordinates.error(any()) }
        assert(voiceApi.socket == null)
    }

    @Test
    fun `autosuggest returns badfocus error`() {
        // given
        val jsonStart = ClassLoader.getSystemResource("started.json").readText()
        val jsonError = ClassLoader.getSystemResource("error-bad-focus.json").readText()
        mockWebSocket(jsonStart, jsonError)

        // when
        voiceApi.initialize(
            Microphone.DEFAULT_RECORDING_RATE,
            AudioFormat.ENCODING_PCM_16BIT,
            BASE_URL,
            listener
        )
        mockWebSocket.send("voice1")
        mockWebSocket.send("voice2")
        mockWebSocket.send("voice3")
        mockWebSocket.send("voiceEnd")
        mockWebSocket.close(1000, "closed by server")

        // then
        verify(exactly = 1) { listener.connected(voiceApi) }
        verify(exactly = 0) { listener.suggestions(any()) }
        verify(exactly = 1) { listener.error(any()) }
        assert(voiceApi.socket == null)
    }

    @Test
    fun `autosuggest-with-coordinates returns badfocus error`() {
        // given
        val jsonStart = ClassLoader.getSystemResource("started.json").readText()
        val jsonError = ClassLoader.getSystemResource("error-bad-focus.json").readText()
        mockWebSocket(jsonStart, jsonError)

        // when
        voiceApi.initialize(
            Microphone.DEFAULT_RECORDING_RATE,
            AudioFormat.ENCODING_PCM_16BIT,
            BASE_URL,
            listenerWithCoordinates
        )
        mockWebSocket.send("voice1")
        mockWebSocket.send("voice2")
        mockWebSocket.send("voice3")
        mockWebSocket.send("voiceEnd")
        mockWebSocket.close(1000, "closed by server")

        // then
        verify(exactly = 1) { listenerWithCoordinates.connected(voiceApi) }
        verify(exactly = 0) { listenerWithCoordinates.suggestionsWithCoordinates(any()) }
        verify(exactly = 1) { listenerWithCoordinates.error(any()) }
        assert(voiceApi.socket == null)
    }

    @Test
    fun `autosuggest returns json syntax error`() {
        // given
        val jsonStart = ClassLoader.getSystemResource("started.json").readText()
        val jsonError = "json syntax error"
        mockWebSocket(jsonStart, jsonError)

        // when
        voiceApi.initialize(
            Microphone.DEFAULT_RECORDING_RATE,
            6,
            BASE_URL,
            listener
        )
        mockWebSocket.send("voice1")
        mockWebSocket.send("voice2")
        mockWebSocket.send("voice3")
        mockWebSocket.send("voiceEnd")
        mockWebSocket.close(1000, "closed by server")

        // then
        verify(exactly = 1) { listener.connected(voiceApi) }
        verify(exactly = 0) { listener.suggestions(any()) }
        verify(exactly = 1) { listener.error(any()) }
        assert(voiceApi.socket == null)
    }

    @Test
    fun `autosuggest-with-coordinates returns json syntax error`() {
        // given
        val jsonStart = ClassLoader.getSystemResource("started.json").readText()
        val jsonError = "json syntax error"
        mockWebSocket(jsonStart, jsonError)

        // when
        voiceApi.initialize(
            Microphone.DEFAULT_RECORDING_RATE,
            AudioFormat.ENCODING_PCM_16BIT,
            BASE_URL,
            listenerWithCoordinates
        )
        mockWebSocket.send("voice1")
        mockWebSocket.send("voice2")
        mockWebSocket.send("voice3")
        mockWebSocket.send("voiceEnd")
        mockWebSocket.close(1000, "closed by server")

        // then
        verify(exactly = 1) { listenerWithCoordinates.connected(voiceApi) }
        verify(exactly = 0) { listenerWithCoordinates.suggestionsWithCoordinates(any()) }
        verify(exactly = 1) { listenerWithCoordinates.error(any()) }
        assert(voiceApi.socket == null)
    }

    @Test
    fun `autosuggest returns streaming error`() {
        // given
        val jsonStart = ClassLoader.getSystemResource("started.json").readText()
        val jsonError = ClassLoader.getSystemResource("streaming-error.json").readText()
        mockWebSocket(jsonStart, jsonError)

        // when
        voiceApi.initialize(
            Microphone.DEFAULT_RECORDING_RATE,
            AudioFormat.ENCODING_PCM_16BIT,
            BASE_URL,
            listener
        )
        mockWebSocket.send("voice1")
        mockWebSocket.send("voice2")
        mockWebSocket.send("voice3")
        mockWebSocket.send("voiceEnd")
        mockWebSocket.close(1000, "closed by server")

        // then
        verify(exactly = 1) { listener.connected(voiceApi) }
        verify(exactly = 0) { listener.suggestions(any()) }
        verify(exactly = 1) { listener.error(any()) }
        assert(voiceApi.socket == null)
    }

    @Test
    fun `autosuggest-with-coordinates returns streaming error`() {
        // given
        val jsonStart = ClassLoader.getSystemResource("started.json").readText()
        val jsonError = ClassLoader.getSystemResource("streaming-error.json").readText()
        mockWebSocket(jsonStart, jsonError)

        // when
        voiceApi.initialize(
            Microphone.DEFAULT_RECORDING_RATE,
            AudioFormat.ENCODING_PCM_16BIT,
            BASE_URL,
            listenerWithCoordinates
        )
        mockWebSocket.send("voice1")
        mockWebSocket.send("voice2")
        mockWebSocket.send("voice3")
        mockWebSocket.send("voiceEnd")
        mockWebSocket.close(1000, "closed by server")

        // then
        verify(exactly = 1) { listenerWithCoordinates.connected(voiceApi) }
        verify(exactly = 0) { listenerWithCoordinates.suggestionsWithCoordinates(any()) }
        verify(exactly = 1) { listenerWithCoordinates.error(any()) }
        assert(voiceApi.socket == null)
    }

    @Test
    fun `autosuggest returns a failure`() {
        // given
        val jsonStart = ClassLoader.getSystemResource("started.json").readText()
        val webSocketError = "websocket error"
        mockWebSocketFailure(jsonStart, webSocketError)

        // when
        voiceApi.initialize(
            Microphone.DEFAULT_RECORDING_RATE,
            AudioFormat.ENCODING_PCM_FLOAT,
            BASE_URL,
            listener
        )

        mockWebSocket.send("voice1")
        mockWebSocket.send("voice2")

        // then
        verify(exactly = 1) { listener.connected(voiceApi) }
        verify(exactly = 0) { listener.suggestions(any()) }
        verify(exactly = 1) { listener.error(any()) }
        assert(voiceApi.socket == null)
    }

    @Test
    fun `autosuggest-with-coordinates returns a failure`() {
        // given
        val jsonStart = ClassLoader.getSystemResource("started.json").readText()
        val webSocketError = "websocket error"
        mockWebSocketFailure(jsonStart, webSocketError)

        // when
        voiceApi.initialize(
            Microphone.DEFAULT_RECORDING_RATE,
            AudioFormat.ENCODING_PCM_8BIT,
            BASE_URL,
            listenerWithCoordinates
        )

        mockWebSocket.send("voice1")
        mockWebSocket.send("voice2")

        // then
        verify(exactly = 1) { listenerWithCoordinates.connected(voiceApi) }
        verify(exactly = 0) { listenerWithCoordinates.suggestionsWithCoordinates(any()) }
        verify(exactly = 1) { listenerWithCoordinates.error(any()) }
        assert(voiceApi.socket == null)
    }

    @Test
    fun `autosuggest forceStop`() {
        // given
        val jsonStart = ClassLoader.getSystemResource("started.json").readText()
        val jsonSuggestions =
            ClassLoader.getSystemResource("voice-suggestions.json").readText()
        mockWebSocket(jsonStart, jsonSuggestions, 1000, "Aborted by user")

        // when
        voiceApi.initialize(
            Microphone.DEFAULT_RECORDING_RATE,
            AudioFormat.ENCODING_PCM_16BIT,
            BASE_URL,
            listener
        )
        mockWebSocket.send("voice1")
        mockWebSocket.send("voice2")
        mockWebSocket.send("voice3")
        voiceApi.forceStop()

        // then
        verify(exactly = 1) { listener.connected(voiceApi) }
        verify(exactly = 0) {
            listener.suggestions(any())
        }
        verify(exactly = 0) { listener.error(any()) }
        assert(voiceApi.socket == null)
    }
}
