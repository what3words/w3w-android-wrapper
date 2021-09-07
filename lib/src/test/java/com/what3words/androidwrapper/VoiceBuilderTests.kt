package com.what3words.androidwrapper

import androidx.core.util.Consumer
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.what3words.androidwrapper.voice.*
import com.what3words.androidwrapper.voice.VoiceApi.Companion.BASE_URL
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.APIError
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import io.mockk.impl.annotations.MockK
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
import java.util.*

@ExperimentalCoroutinesApi
class VoiceBuilderTests {
    private val dispatcher = TestCoroutineDispatcher()

    @MockK
    private lateinit var socket: WebSocket

    @MockK
    private lateinit var voiceApi: VoiceApi

    @MockK
    private lateinit var microphone: Microphone

    @MockK
    private var suggestionsCallback = mockk<Consumer<List<Suggestion>>>()

    @MockK
    private var errorCallback = mockk<Consumer<APIResponse.What3WordsError>>()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        voiceApi = mockk()
        socket = mockk()
        microphone = mockk()
        suggestionsCallback = mockk()
        errorCallback = mockk()

        justRun {
            voiceApi.forceStop()
            voiceApi.open(any(), any(), any(), any<VoiceApiListener>())
            microphone.startRecording(socket)
            microphone.stopRecording()
            suggestionsCallback.accept(any())
            errorCallback.accept(any())
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startListening then force stopListening`() {
        // given
        val what3WordsV3 = What3WordsV3("key", voiceApi)
        val builder = what3WordsV3.autosuggest(microphone, "en")
        builder.onSuggestions(suggestionsCallback)
        builder.onError(errorCallback)

        // when startListening and connected successfully
        builder.startListening()
        builder.connected(socket)

        // then
        assertThat(builder.isListening()).isTrue()
        verify(exactly = 1) { voiceApi.open(any(), any(), any(), builder) }
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
        val what3WordsV3 = What3WordsV3("key", voiceApi)
        val builder = what3WordsV3.autosuggest(microphone, "en")
        builder.onSuggestions(suggestionsCallback)
        builder.onError(errorCallback)

        // when startListening and connected successfully
        builder.startListening()
        builder.connected(socket)

        // then
        assertThat(builder.isListening()).isTrue()
        verify(exactly = 1) { voiceApi.open(any(), any(), any(), builder) }
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

    @Test
    fun `startListening then returns suggestions`() {
        // given
        val what3WordsV3 = What3WordsV3("key", voiceApi)
        val builder = what3WordsV3.autosuggest(microphone, "en")
        builder.onSuggestions(suggestionsCallback)
        builder.onError(errorCallback)

        // when startListening and connected successfully
        builder.startListening()
        builder.connected(socket)

        // then
        assertThat(builder.isListening()).isTrue()
        verify(exactly = 1) { voiceApi.open(any(), any(), any(), builder) }
        verify(exactly = 1) { microphone.startRecording(socket) }

        val suggestionsJson =
            ClassLoader.getSystemResource("suggestions.json").readText()
        val suggestions =
            Gson().fromJson(suggestionsJson, Array<Suggestion>::class.java).toList()

        // when
        builder.suggestions(suggestions)

        // then
        assertThat(builder.isListening()).isFalse()
        verify(exactly = 0) { voiceApi.forceStop() }
        verify(exactly = 1) { microphone.stopRecording() }
        verify(exactly = 1) { suggestionsCallback.accept(suggestions) }
        verify(exactly = 0) { errorCallback.accept(any()) }
    }

    @Test
    fun `focus is set expect param url`() {
        // given
        val expectedUrl = "$BASE_URL?voice-language=en&focus=51.1,-0.152"
        val what3WordsV3 = What3WordsV3("key", voiceApi)
        val builder = what3WordsV3.autosuggest(microphone, "en")
            .onSuggestions(suggestionsCallback)
            .onError(errorCallback)

        // when
        builder.focus(Coordinates(51.1, -0.152))
            .startListening()

        // then
        assertThat(builder.isListening()).isTrue()
        verify(exactly = 1) { voiceApi.open(any(), any(), expectedUrl, builder) }
    }

    fun `focus is set with nFocusResults expect param url`() {
        // given
        val expectedUrl = "$BASE_URL?voice-language=en&focus=51.1,-0.152&n-focus-results=3"
        val what3WordsV3 = What3WordsV3("key", voiceApi)
        val builder = what3WordsV3.autosuggest(microphone, "en")
            .onSuggestions(suggestionsCallback)
            .onError(errorCallback)

        // when
        builder.focus(Coordinates(51.1, -0.152)).nFocusResults(3)
            .startListening()

        // then
        assertThat(builder.isListening()).isTrue()
        verify(exactly = 1) { voiceApi.open(any(), any(), expectedUrl, builder) }
    }

    @Test
    fun `nResults is set expect param url`() {
        // given
        val expectedUrl = "$BASE_URL?voice-language=en&n-results=3"
        val what3WordsV3 = What3WordsV3("key", voiceApi)
        val builder = what3WordsV3.autosuggest(microphone, "en")
            .onSuggestions(suggestionsCallback)
            .onError(errorCallback)

        // when
        builder.nResults(3).startListening()

        // then
        assertThat(builder.isListening()).isTrue()
        verify(exactly = 1) { voiceApi.open(any(), any(), expectedUrl, builder) }
    }

    @Test
    fun `clipToCountry is set expect param url`() {
        // given
        val expectedUrl = "$BASE_URL?voice-language=en&clip-to-country=GB,FR"
        val what3WordsV3 = What3WordsV3("key", voiceApi)
        val builder = what3WordsV3.autosuggest(microphone, "en")
            .onSuggestions(suggestionsCallback)
            .onError(errorCallback)

        // when
        builder.clipToCountry(listOf("GB", "FR")).startListening()

        // then
        assertThat(builder.isListening()).isTrue()
        verify(exactly = 1) { voiceApi.open(any(), any(), expectedUrl, builder) }
    }

    @Test
    fun `clipToCircle is set without radius expect param url`() {
        // given
        val expectedUrl = "$BASE_URL?voice-language=en&clip-to-circle=51.1,-0.152,1.0"
        val what3WordsV3 = What3WordsV3("key", voiceApi)
        val builder = what3WordsV3.autosuggest(microphone, "en")
            .onSuggestions(suggestionsCallback)
            .onError(errorCallback)

        // when
        builder.clipToCircle(Coordinates(51.1,-0.152)).startListening()

        // then
        assertThat(builder.isListening()).isTrue()
        verify(exactly = 1) { voiceApi.open(any(), any(), expectedUrl, builder) }
    }

    @Test
    fun `clipToCircle is set with radius expect param url`() {
        // given
        val expectedUrl = "$BASE_URL?voice-language=en&clip-to-circle=51.1,-0.152,100.0"
        val what3WordsV3 = What3WordsV3("key", voiceApi)
        val builder = what3WordsV3.autosuggest(microphone, "en")
            .onSuggestions(suggestionsCallback)
            .onError(errorCallback)

        // when
        builder.clipToCircle(Coordinates(51.1,-0.152), 100.0).startListening()

        // then
        assertThat(builder.isListening()).isTrue()
        verify(exactly = 1) { voiceApi.open(any(), any(), expectedUrl, builder) }
    }
}
