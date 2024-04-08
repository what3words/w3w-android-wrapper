package com.what3words.androidwrapper.datasource.voice

import com.google.gson.Gson
import com.what3words.androidwrapper.datasource.voice.client.W3WVoiceClient
import com.what3words.androidwrapper.datasource.voice.di.MapperFactory
import com.what3words.androidwrapper.datasource.voice.error.W3WApiVoiceError
import com.what3words.androidwrapper.voice.ErrorPayload
import com.what3words.androidwrapper.voice.SuggestionsWithCoordinatesPayload
import com.what3words.core.datasource.voice.audiostream.W3WMicrophone
import com.what3words.core.types.common.W3WError
import com.what3words.core.types.common.W3WResult
import com.what3words.core.types.language.W3WRFC5646Language
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class W3WApiVoiceDataSourceTest {

    @MockK
    private lateinit var client: W3WVoiceClient

    private lateinit var dataSource: W3WApiVoiceDataSource

    private var suggestionWithCoordinatesMapper =
        MapperFactory.provideSuggestionWithCoordinatesMapper()

    private lateinit var expectedExceptions: List<SuggestionWithCoordinates>

    private lateinit var expectedError: W3WError

    private lateinit var jsonSuggestions: String

    private lateinit var jsonError: String

    @Before
    fun setUp() {
        loadAssets()
        client = mockk()
        dataSource = W3WApiVoiceDataSource(client, suggestionWithCoordinatesMapper)

        every {
            client.initialize(any(), any(), any())
        } answers {
            client
        }
    }

    private fun loadAssets() {
        jsonSuggestions = ClassLoader.getSystemResource("voice-suggestions.json").readText()
        jsonError = ClassLoader.getSystemResource("streaming-error.json").readText()

        val payload = Gson().fromJson(
            jsonSuggestions,
            SuggestionsWithCoordinatesPayload::class.java
        )
        expectedExceptions = payload.suggestions

        val result = Gson().fromJson(jsonError, ErrorPayload::class.java)
        expectedError = W3WApiVoiceError.StreamingError(
            type = result.type,
            code = result.code,
            reason = result.reason,
        )
    }

    private fun mockClientReturnSuggestions(
        suggestions: List<SuggestionWithCoordinates>
    ) {
        every {
            client.openWebSocketAndStartRecognition(any())
        } answers {
            val listener = arg<(W3WVoiceClient.RecognitionStatus) -> Unit>(0)
            listener.invoke(W3WVoiceClient.RecognitionStatus.Suggestions(suggestions))
        }
    }

    private fun mockClientReturnError(
        error: W3WError
    ) {
        every {
            client.openWebSocketAndStartRecognition(any())
        } answers {
            val listener = arg<(W3WVoiceClient.RecognitionStatus) -> Unit>(0)
            listener.invoke(W3WVoiceClient.RecognitionStatus.Error(error))
        }
    }

    @Test
    fun `test factory method create new instance`() {
        // Act
        val dataSource = W3WApiVoiceDataSource.create("anyKey", "anyEndPoint")

        // Assert
        assert(dataSource != null)
    }

    @Test
    fun `autosuggest return suggestions`() {
        // Arrange
        mockClientReturnSuggestions(expectedExceptions)

        // Act
        dataSource.autosuggest(W3WMicrophone(), W3WRFC5646Language.EN_GB, null, null) { result ->
            // Assert
            assert(result is W3WResult.Success)
            assert((result as W3WResult.Success).value.size == expectedExceptions!!.size)
        }
    }

    @Test
    fun `autosuggest return error`() {
        // Arrange
        val error = W3WError(expectedError)
        mockClientReturnError(error)

        // Act
        dataSource.autosuggest(W3WMicrophone(), W3WRFC5646Language.EN_GB, null, null) { result ->
            // Assert
            assert(result is W3WResult.Failure)
            assert((result as W3WResult.Failure).error == error)
        }
    }
}