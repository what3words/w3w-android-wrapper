package com.what3words.androidwrapper

import androidx.core.util.Consumer
import com.google.common.truth.Truth.assertThat
import com.what3words.androidwrapper.voice.Microphone
import com.what3words.androidwrapper.voice.VoiceApi
import com.what3words.androidwrapper.voice.VoiceApiListener
import com.what3words.androidwrapper.voice.VoiceApiListenerWithCoordinates
import com.what3words.javawrapper.response.APIError
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.WebSocket
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class VoiceBuilderTests {
    private val dispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startListening then manual stopListening`() {
        // given
        val voiceApi = mockk<VoiceApi>()
        val socket = mockk<WebSocket>()
        val microphone = mockk<Microphone>()
        val suggestionsCallback = mockk<Consumer<List<Suggestion>>>()
        val errorCallback = mockk<Consumer<APIResponse.What3WordsError>>()

        justRun {
            voiceApi.forceStop()
            voiceApi.open(any(), any(), any(), any(), any<VoiceApiListener>())
            voiceApi.open(any(), any(), any(), any(), any<VoiceApiListenerWithCoordinates>())
            microphone.startRecording(socket)
            microphone.stopRecording()
        }

        val what3WordsV3 = What3WordsV3("key", voiceApi)
        val builder = what3WordsV3.autosuggest(microphone, "en")
        builder.onSuggestions(suggestionsCallback)
        builder.onError(errorCallback)

        // when startListening and connected successfully
        builder.startListening()
        builder.connected(socket)

        // then
        assertThat(builder.isListening()).isTrue()
        verify(exactly = 1) { voiceApi.open(any(), any(), any(), false, builder) }
        verify(exactly = 1) { microphone.startRecording(socket) }

        // when forced stop
        builder.stopListening()

        // then
        assertThat(builder.isListening()).isFalse()
        verify(exactly = 1) { voiceApi.forceStop() }
        verify(exactly = 1) { microphone.stopRecording() }
        verify(exactly = 0) { suggestionsCallback.accept(any()) }
        verify(exactly = 0) { errorCallback.accept(any()) }
    }

    @Test
    fun `startListening then error occurs`() {
        // given
        val voiceApi = mockk<VoiceApi>()
        val socket = mockk<WebSocket>()
        val microphone = mockk<Microphone>()
        val suggestionsCallback = mockk<Consumer<List<Suggestion>>>()
        val errorCallback = mockk<Consumer<APIResponse.What3WordsError>>()

        justRun {
            voiceApi.forceStop()
            voiceApi.open(any(), any(), any(), any(), any<VoiceApiListener>())
            voiceApi.open(any(), any(), any(), any(), any<VoiceApiListenerWithCoordinates>())
            microphone.startRecording(socket)
            microphone.stopRecording()
            suggestionsCallback.accept(any())
            errorCallback.accept(any())
        }

        val what3WordsV3 = What3WordsV3("key", voiceApi)
        val builder = what3WordsV3.autosuggest(microphone, "en")
        builder.onSuggestions(suggestionsCallback)
        builder.onError(errorCallback)

        // when startListening and connected successfully
        builder.startListening()
        builder.connected(socket)

        // then
        assertThat(builder.isListening()).isTrue()
        verify(exactly = 1) { voiceApi.open(any(), any(), any(), false, builder) }
        verify(exactly = 1) { microphone.startRecording(socket) }

        // when
        builder.error(APIError())

        // then
        assertThat(builder.isListening()).isFalse()
        verify(exactly = 0) { voiceApi.forceStop() }
        verify(exactly = 1) { microphone.stopRecording() }
        verify(exactly = 0) { suggestionsCallback.accept(any()) }
        verify(exactly = 1) { errorCallback.accept(any()) }
    }
}
