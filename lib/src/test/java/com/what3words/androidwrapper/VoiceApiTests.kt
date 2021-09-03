package com.what3words.androidwrapper

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

    private fun mockWebSocket(
        message: String,
        nextMessage: String,
        closeCode: Int = 1000,
        closeReason: String = "closed by server"
    ) {
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
            mockClient.newWebSocket(any(), any())
        }.answers {
            val wsl =
                this.arg<WebSocketListener>(1)

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

    @Test
    fun `autosuggest returns suggestions`() {
        //given
        mockClient = mockk()
        mockWebSocket = mockk()
        val jsonStart = ClassLoader.getSystemResource("started.json").readText()
        val jsonSuggestions = ClassLoader.getSystemResource("suggestions.json").readText()
        mockWebSocket(jsonStart, jsonSuggestions)
        val listener = mockk<VoiceApiListener>()

        justRun { listener.connected(any()) }
        justRun { listener.suggestions(any()) }

        val api = VoiceApi("any", mockClient)

        //when
        api.open(
            Microphone.RECORDING_RATE,
            url = BASE_URL,
            withCoordinates = false,
            listener = listener
        )
        mockWebSocket.send("voice1")
        mockWebSocket.send("voice2")
        mockWebSocket.send("voice3")
        mockWebSocket.send("voiceEnd")
        mockWebSocket.close(1000, "closed by server")

        //then
        verify(exactly = 1) { listener.connected(mockWebSocket) }
        verify(exactly = 1) { listener.suggestions(any()) }
        verify(exactly = 0) { listener.error(any()) }
        assert(api.socket == null)
    }

    @Test
    fun `autosuggest-with-coordinates returns suggestions`() {
        //given
        mockClient = mockk()
        mockWebSocket = mockk()
        val jsonStart = ClassLoader.getSystemResource("started.json").readText()
        val jsonSuggestionsWithCoordinates =
            ClassLoader.getSystemResource("suggestions-with-coordinates.json").readText()
        mockWebSocket(jsonStart, jsonSuggestionsWithCoordinates)

        val listener = mockk<VoiceApiListenerWithCoordinates>()

        justRun { listener.connected(any()) }
        justRun { listener.suggestionsWithCoordinates(any()) }

        val api = VoiceApi("any", mockClient)

        //when
        api.open(
            Microphone.RECORDING_RATE,
            url = BASE_URL,
            withCoordinates = true,
            listener = listener
        )
        mockWebSocket.send("voice1")
        mockWebSocket.send("voice2")
        mockWebSocket.send("voice3")
        mockWebSocket.send("voiceEnd")
        mockWebSocket.close(1000, "closed by server")

        //then
        verify(exactly = 1) { listener.connected(mockWebSocket) }
        verify(exactly = 1) {
            listener.suggestionsWithCoordinates(any())
        }
        verify(exactly = 0) { listener.error(any()) }
        assert(api.socket == null)
    }

    @Test
    fun `autosuggest returns invalid api key error`() {
        //given
        mockClient = mockk()
        mockWebSocket = mockk()
        val jsonStart = ClassLoader.getSystemResource("started.json").readText()
        val jsonError = ClassLoader.getSystemResource("error-invalid-api-key.json").readText()
        mockWebSocket(jsonStart, jsonError, 1003, jsonError)
        val listener = mockk<VoiceApiListener>()

        justRun { listener.connected(any()) }
        justRun { listener.error(any()) }

        val api = VoiceApi("any", mockClient)

        //when
        api.open(
            Microphone.RECORDING_RATE,
            url = BASE_URL,
            withCoordinates = false,
            listener = listener
        )
        mockWebSocket.close(1003, jsonError)

        //then
        verify(exactly = 1) { listener.connected(mockWebSocket) }
        verify(exactly = 0) { listener.suggestions(any()) }
        verify(exactly = 1) { listener.error(any()) }
        assert(api.socket == null)
    }

    @Test
    fun `autosuggest returns missing api key error`() {
        //given
        mockClient = mockk()
        mockWebSocket = mockk()
        val jsonStart = ClassLoader.getSystemResource("started.json").readText()
        val jsonError = ClassLoader.getSystemResource("error-missing-api-key.json").readText()
        mockWebSocket(jsonStart, jsonError, 1003, jsonError)
        val listener = mockk<VoiceApiListener>()

        justRun { listener.connected(any()) }
        justRun { listener.error(any()) }

        val api = VoiceApi("any", mockClient)

        //when
        api.open(
            Microphone.RECORDING_RATE,
            url = BASE_URL,
            withCoordinates = false,
            listener = listener
        )
        mockWebSocket.close(1003, jsonError)

        //then
        verify(exactly = 1) { listener.connected(mockWebSocket) }
        verify(exactly = 0) { listener.suggestions(any()) }
        verify(exactly = 1) { listener.error(any()) }
        assert(api.socket == null)
    }

    @Test
    fun `autosuggest returns badfocus error`() {
        //given
        mockClient = mockk()
        mockWebSocket = mockk()
        val jsonStart = ClassLoader.getSystemResource("started.json").readText()
        val jsonError = ClassLoader.getSystemResource("error-bad-focus.json").readText()
        mockWebSocket(jsonStart, jsonError)
        val listener = mockk<VoiceApiListener>()

        justRun { listener.connected(any()) }
        justRun { listener.error(any()) }

        val api = VoiceApi("any", mockClient)

        //when
        api.open(
            Microphone.RECORDING_RATE,
            url = BASE_URL,
            withCoordinates = false,
            listener = listener
        )
        mockWebSocket.send("voice1")
        mockWebSocket.send("voice2")
        mockWebSocket.send("voice3")
        mockWebSocket.send("voiceEnd")
        mockWebSocket.close(1000, "closed by server")

        //then
        verify(exactly = 1) { listener.connected(mockWebSocket) }
        verify(exactly = 0) { listener.suggestions(any()) }
        verify(exactly = 1) { listener.error(any()) }
        assert(api.socket == null)
    }

    @Test
    fun `autosuggest returns json syntax error`() {
        //given
        mockClient = mockk()
        mockWebSocket = mockk()
        val jsonStart = ClassLoader.getSystemResource("started.json").readText()
        val jsonError = "json syntax error"
        mockWebSocket(jsonStart, jsonError)
        val listener = mockk<VoiceApiListener>()

        justRun { listener.connected(any()) }
        justRun { listener.error(any()) }

        val api = VoiceApi("any", mockClient)

        //when
        api.open(
            Microphone.RECORDING_RATE,
            url = BASE_URL,
            withCoordinates = false,
            listener = listener
        )
        mockWebSocket.send("voice1")
        mockWebSocket.send("voice2")
        mockWebSocket.send("voice3")
        mockWebSocket.send("voiceEnd")
        mockWebSocket.close(1000, "closed by server")

        //then
        verify(exactly = 1) { listener.connected(mockWebSocket) }
        verify(exactly = 0) { listener.suggestions(any()) }
        verify(exactly = 1) { listener.error(any()) }
        assert(api.socket == null)
    }

    @Test
    fun `autosuggest returns streaming error`() {
        //given
        mockClient = mockk()
        mockWebSocket = mockk()
        val jsonStart = ClassLoader.getSystemResource("started.json").readText()
        val jsonError = ClassLoader.getSystemResource("streaming-error.json").readText()
        mockWebSocket(jsonStart, jsonError)
        val listener = mockk<VoiceApiListener>()

        justRun { listener.connected(any()) }
        justRun { listener.error(any()) }

        val api = VoiceApi("any", mockClient)

        //when
        api.open(
            Microphone.RECORDING_RATE,
            url = BASE_URL,
            withCoordinates = false,
            listener = listener
        )
        mockWebSocket.send("voice1")
        mockWebSocket.send("voice2")
        mockWebSocket.send("voice3")
        mockWebSocket.send("voiceEnd")
        mockWebSocket.close(1000, "closed by server")

        //then
        verify(exactly = 1) { listener.connected(mockWebSocket) }
        verify(exactly = 0) { listener.suggestions(any()) }
        verify(exactly = 1) { listener.error(any()) }
        assert(api.socket == null)
    }
}