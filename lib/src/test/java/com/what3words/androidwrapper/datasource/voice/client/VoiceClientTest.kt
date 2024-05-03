package com.what3words.androidwrapper.datasource.voice.client

import com.google.gson.Gson
import com.what3words.androidwrapper.CoroutineTestRule
import com.what3words.androidwrapper.datasource.text.api.error.BadFocusError
import com.what3words.androidwrapper.voice.SuggestionsWithCoordinatesPayload
import com.what3words.androidwrapper.datasource.voice.client.W3WVoiceClient
import com.what3words.androidwrapper.datasource.voice.client.W3WVoiceClient.Companion.BASE_URL
import com.what3words.androidwrapper.datasource.voice.error.W3WApiVoiceError
import com.what3words.core.datasource.voice.audiostream.W3WAudioStream
import com.what3words.core.datasource.voice.audiostream.W3WMicrophone
import com.what3words.core.types.common.W3WError
import com.what3words.core.types.domain.W3WCountry
import com.what3words.core.types.geometry.W3WCircle
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WDistance
import com.what3words.core.types.geometry.W3WPolygon
import com.what3words.core.types.geometry.W3WRectangle
import com.what3words.core.types.language.W3WRFC5646Language
import com.what3words.core.types.options.W3WAutosuggestOptions
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VoiceClientTest {
    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    @MockK
    private lateinit var mockWebSocket: WebSocket

    @MockK
    private lateinit var mockClient: OkHttpClient

    @MockK
    private lateinit var voiceClient: W3WVoiceClient

    private lateinit var audioStream: W3WAudioStream

    private var expectedExceptions: List<SuggestionWithCoordinates>? = null

    private lateinit var jsonSuggestions: String

    private lateinit var jsonStart: String

    private lateinit var jsonError: String

    @Before
    fun setup() {
        loadAssets()
        mockWebSocket = mockk()
        mockClient = mockk()
        audioStream = W3WMicrophone()

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

        voiceClient = W3WVoiceClient("any", BASE_URL, mockClient)
    }

    private fun loadAssets() {
        jsonStart = ClassLoader.getSystemResource("started.json").readText()
        jsonSuggestions = ClassLoader.getSystemResource("voice-suggestions.json").readText()
        jsonError = ClassLoader.getSystemResource("streaming-error.json").readText()
        val payload = Gson().fromJson(
            jsonSuggestions,
            SuggestionsWithCoordinatesPayload::class.java
        )

        expectedExceptions = payload.suggestions
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

    private fun mockWebSocketClosingWithError(
        message: String,
        closeCode: Int,
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
                wsl.onClosing(mockWebSocket, closeCode, "Don't know why")
                true
            }

            mockWebSocket
        }
    }

    private fun mockWebSocketApiError() {
        val errorMessage = ClassLoader.getSystemClassLoader().getResource("error-invalid-api-key.json").readText()
        every {
            mockClient.newWebSocket(any(), any())
        }.answers {
            val wsl =
                this.arg<WebSocketListener>(1)

            wsl.onClosing(mockWebSocket, 2500, errorMessage)

            mockWebSocket
        }
    }

    private fun mockWebSocketW3WError() {
        val errorMessage = ClassLoader.getSystemClassLoader().getResource("error-bad-focus.json").readText()
        every {
            mockClient.newWebSocket(any(), any())
        }.answers {
            val wsl =
                this.arg<WebSocketListener>(1)

            wsl.onOpen(mockWebSocket, mockk())
            wsl.onMessage(mockWebSocket, jsonStart)

            every {
                mockWebSocket.send("voiceEnd")
            }.answers {
                wsl.onMessage(
                    mockWebSocket, errorMessage
                )
                true
            }

            every {
                mockWebSocket.close(1000, "close by server")
            }.answers {
                wsl.onClosing(mockWebSocket, 1000, "close by server")
                true
            }
            mockWebSocket
        }
    }

    @Test
    fun `autosuggest returns suggestions`() {
        // Arrange
        mockWebSocket(jsonStart, jsonSuggestions)

        var suggestions: List<SuggestionWithCoordinates>? = null
        var error: W3WError? = null
        val listener: (W3WVoiceClient.RecognitionStatus) -> Unit = {
            when (it) {
                is W3WVoiceClient.RecognitionStatus.Suggestions -> {
                    suggestions = it.suggestions
                }

                is W3WVoiceClient.RecognitionStatus.Error -> {
                    error = it.error
                }
            }
        }

        // Act
        voiceClient.initialize(W3WRFC5646Language.EN_GB, null, audioStream)
            .openWebSocketAndStartRecognition(listener)
        mockWebSocket.send("voice1")
        mockWebSocket.send("voice2")
        mockWebSocket.send("voice3")
        mockWebSocket.send("voiceEnd")
        mockWebSocket.close(1000, "closed by server")

        // Assert
        assert(suggestions != null)
        assert(suggestions?.get(0)?.words == expectedExceptions?.get(0)?.words)
        assert(error == null)
        assert(voiceClient.socket == null)
    }

    @Test
    fun `autosuggest-with-coordinates returns streaming error`() {
        // Arrange
        mockWebSocket(jsonStart, jsonError)
        var suggestions: List<SuggestionWithCoordinates>? = null
        var error: W3WError? = null
        val listener: (W3WVoiceClient.RecognitionStatus) -> Unit = {
            when (it) {
                is W3WVoiceClient.RecognitionStatus.Suggestions -> {
                    suggestions = it.suggestions
                }

                is W3WVoiceClient.RecognitionStatus.Error -> {
                    error = it.error
                }
            }
        }

        // Act
        voiceClient.initialize(W3WRFC5646Language.EN_GB, null, audioStream)
            .openWebSocketAndStartRecognition(listener)
        mockWebSocket.send("voice1")
        mockWebSocket.send("voice2")
        mockWebSocket.send("voice3")
        mockWebSocket.send("voiceEnd")
        mockWebSocket.close(1000, "closed by server")

        // Assert
        assert(suggestions == null)
        assert(error != null)
        assert(voiceClient.socket == null)
    }

    @Test
    fun `autosuggest returns a failure`() {
        // Arrange
        val webSocketError = "websocket error"
        mockWebSocketFailure(jsonStart, webSocketError)
        var suggestions: List<SuggestionWithCoordinates>? = null
        var error: W3WError? = null
        val listener: (W3WVoiceClient.RecognitionStatus) -> Unit = {
            when (it) {
                is W3WVoiceClient.RecognitionStatus.Suggestions -> {
                    suggestions = it.suggestions
                }

                is W3WVoiceClient.RecognitionStatus.Error -> {
                    error = it.error
                }
            }
        }

        // when
        voiceClient.initialize(W3WRFC5646Language.EN_GB, null, audioStream)
            .openWebSocketAndStartRecognition(listener)
        mockWebSocket.send("voice1")
        mockWebSocket.send("voice2")

        // then
        assert(suggestions == null)
        assert(error != null)
        assert(voiceClient.socket == null)
    }

    @Test
    fun `autosuggest-with-coordinates returns a failure`() {
        // Arrange
        val webSocketError = "websocket error"
        mockWebSocketFailure(jsonStart, webSocketError)
        val w3WAutosuggestOptions = W3WAutosuggestOptions.Builder().includeCoordinates(true).build()
        var suggestions: List<SuggestionWithCoordinates>? = null
        var error: W3WError? = null
        val listener: (W3WVoiceClient.RecognitionStatus) -> Unit = {
            when (it) {
                is W3WVoiceClient.RecognitionStatus.Suggestions -> {
                    suggestions = it.suggestions
                }

                is W3WVoiceClient.RecognitionStatus.Error -> {
                    error = it.error
                }
            }
        }

        // Act
        voiceClient.initialize(W3WRFC5646Language.EN_GB, w3WAutosuggestOptions, audioStream)
            .openWebSocketAndStartRecognition(listener)
        mockWebSocket.send("voice1")
        mockWebSocket.send("voice2")

        // Assert
        assert(suggestions == null)
        assert(error != null)
        assert(voiceClient.socket == null)
    }

    @Test
    fun `autosuggest returns a failure when closing the socket`() {
        // Arrange
        val closeCode = 2500
        mockWebSocketClosingWithError(jsonStart, closeCode)
        var suggestions: List<SuggestionWithCoordinates>? = null
        var error: W3WError? = null
        val listener: (W3WVoiceClient.RecognitionStatus) -> Unit = {
            when (it) {
                is W3WVoiceClient.RecognitionStatus.Suggestions -> {
                    suggestions = it.suggestions
                }

                is W3WVoiceClient.RecognitionStatus.Error -> {
                    error = it.error
                }
            }
        }

        // Act
        voiceClient.initialize(W3WRFC5646Language.EN_GB, null, audioStream)
            .openWebSocketAndStartRecognition(listener)
        mockWebSocket.send("voice1")
        mockWebSocket.send("voice2")

        // Assert
        assert(suggestions == null)
        assert(error != null)
        assert(voiceClient.socket == null)
    }

    @Test
    fun `autosuggest is terminated by user`() {
        // Arrange
        mockWebSocket(jsonStart, jsonSuggestions, 1000, "Terminated by user")
        var suggestions: List<SuggestionWithCoordinates>? = null
        var error: W3WError? = null
        val listener: (W3WVoiceClient.RecognitionStatus) -> Unit = {
            when (it) {
                is W3WVoiceClient.RecognitionStatus.Suggestions -> {
                    suggestions = it.suggestions
                }

                is W3WVoiceClient.RecognitionStatus.Error -> {
                    error = it.error
                }
            }
        }

        // Act
        voiceClient.initialize(W3WRFC5646Language.EN_GB, null, audioStream)
            .openWebSocketAndStartRecognition(listener)
        mockWebSocket.send("voice1")
        mockWebSocket.send("voice2")
        mockWebSocket.send("voice3")
        voiceClient.close("Terminated by user")

        // Assert
        assert(suggestions == null)
        assert(error == null)
        assert(voiceClient.socket == null)
    }

    @Test
    fun `autosuggest returns connection error`() {
        // Arrange
        mockWebSocketApiError()
        var suggestions: List<SuggestionWithCoordinates>? = null
        var error: W3WError? = null
        val listener: (W3WVoiceClient.RecognitionStatus) -> Unit = {
            println(it)
            when (it) {
                is W3WVoiceClient.RecognitionStatus.Suggestions -> {
                    suggestions = it.suggestions
                }

                is W3WVoiceClient.RecognitionStatus.Error -> {
                    error = it.error
                }
            }
        }

        // Act
        voiceClient.initialize(W3WRFC5646Language.EN_GB, null, audioStream)
            .openWebSocketAndStartRecognition(listener)

        // Assert
        assert(suggestions == null)
        assert(error != null)
        assert(error is W3WApiVoiceError.ConnectionError)
    }

    @Test
    fun `autosuggest returns W3WError`() {
        // Arrange
        mockWebSocketW3WError()
        val options = W3WAutosuggestOptions.Builder().focus(W3WCoordinates(91.0, 181.0)).build() // This option will cause a BadFocusError in real life
        var suggestions: List<SuggestionWithCoordinates>? = null
        var error: W3WError? = null
        val listener: (W3WVoiceClient.RecognitionStatus) -> Unit = {
            when (it) {
                is W3WVoiceClient.RecognitionStatus.Suggestions -> {
                    suggestions = it.suggestions
                }

                is W3WVoiceClient.RecognitionStatus.Error -> {
                    error = it.error
                }
            }
        }

        // Act
        voiceClient.initialize(W3WRFC5646Language.EN_GB, options, audioStream)
            .openWebSocketAndStartRecognition(listener)
        mockWebSocket.send("voice1")
        mockWebSocket.send("voice2")
        mockWebSocket.send("voice3")
        mockWebSocket.send("voiceEnd")
        mockWebSocket.close(1000, "closed by server")

        // Assert
        assert(suggestions == null)
        assert(error != null)
        assert(error is BadFocusError)
    }

    @Test
    fun `createSocketUrl with AutosuggestOptions matches expected param url`() {
        // Arrange
        val apiKey = "anyKey"
        val options = W3WAutosuggestOptions.Builder()
            .focus(W3WCoordinates(51.521251, -0.203586))
            .clipToCountry(*arrayOf(W3WCountry("NZ"), W3WCountry("AU")))
            .clipToPolygon(
                W3WPolygon(
                    listOf(
                        W3WCoordinates(51.521, -0.343),
                        W3WCoordinates(52.6, 2.3324),
                        W3WCoordinates(54.234, 8.343),
                        W3WCoordinates(51.521, -0.343)

                    )
                )
            )
            .clipToCircle(
                W3WCircle(
                    W3WCoordinates(51.521, -0.343), W3WDistance(142.0)
                )
            )
            .clipToBoundingBox(
                W3WRectangle(
                    W3WCoordinates(51.521, -0.343),
                    W3WCoordinates(52.6, 2.3324)
                )
            )
            .build()

        // Act
        val url = voiceClient.buildRequest(options, W3WRFC5646Language.EN_GB, apiKey).url

        // Assert
        assert(url.queryParameter("clip-to-polygon") == "51.521,-0.343,52.6,2.3324,54.234,8.343,51.521,-0.343")
        assert(url.queryParameter("clip-to-bounding-box") == "51.521,-0.343,52.6,2.3324")
        assert(url.queryParameter("clip-to-circle") == "51.521,-0.343,142.0")
        assert(url.queryParameter("clip-to-country") == "NZ,AU")
        assert(url.queryParameter("focus") == "51.521251,-0.203586")
        assert(url.queryParameter("key") == apiKey)
        assert(url.queryParameter("voice-language") == "en")
    }

    @Test
    fun `call openWebSocketAndStartRecognition() without initializing the client throws IllegalStateException`() {
        // Arrange
        val listener: (W3WVoiceClient.RecognitionStatus) -> Unit = {}

        // Act & Assert
        Assert.assertThrows(IllegalStateException::class.java) {
            voiceClient.openWebSocketAndStartRecognition(listener)
        }
    }
}