package com.what3words.androidwrapper

import android.content.Context
import android.media.AudioFormat
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.util.Consumer
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.what3words.androidwrapper.voice.Microphone
import com.what3words.androidwrapper.voice.VoiceApi
import com.what3words.androidwrapper.voice.VoiceApi.Companion.BASE_URL
import com.what3words.androidwrapper.voice.VoiceApi.Companion.URL_WITH_COORDINATES
import com.what3words.androidwrapper.voice.VoiceApiListenerWithCoordinates
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.APIError
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.WebSocket
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@ExperimentalCoroutinesApi
class VoiceBuilderWithCoordinatesTests {
    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var socket: WebSocket

    @MockK
    private lateinit var voiceApi: VoiceApi

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var microphone: Microphone

    @MockK
    private var suggestionsCallback = mockk<Consumer<List<SuggestionWithCoordinates>>>()

    @MockK
    private var errorCallback = mockk<Consumer<APIResponse.What3WordsError>>()

    @Before
    fun setup() {
        voiceApi = mockk()
        socket = mockk()
        microphone = mockk()
        suggestionsCallback = mockk()
        errorCallback = mockk()
        context = mockk()

        justRun {
            voiceApi.forceStop()
            voiceApi.initialize(any(), any(), any(), any<VoiceApiListenerWithCoordinates>())
            microphone.startRecording(voiceApi)
            microphone.stopRecording()
            suggestionsCallback.accept(any())
            errorCallback.accept(any())
        }

        every {
            microphone.recordingRate
        } answers {
            44000
        }

        every {
            voiceApi.getBaseVoiceUrl()
        } answers {
            BASE_URL
        }

        every {
            microphone.encoding
        } answers {
            AudioFormat.ENCODING_DEFAULT
        }


        every {
            context.packageName
        } answers {
            "com.what3words.android.wrapper"
        }
    }

    @Test
    fun `startListening then force stopListening`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val what3WordsV3 = What3WordsV3("key", voiceApi, coroutinesTestRule.testDispatcherProvider)
            val builder = what3WordsV3.autosuggestWithCoordinates(microphone, "en")
            builder.onSuggestions(suggestionsCallback)
            builder.onError(errorCallback)

            // when startListening and connected successfully
            builder.startListening()
            builder.connected(voiceApi)

            // then
            assertThat(builder.isListening()).isTrue()
            verify(exactly = 1) { voiceApi.initialize(any(), any(), any(), builder) }
            verify(exactly = 1) { microphone.startRecording(voiceApi) }

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
    fun `startListening then error occurs`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val what3WordsV3 = What3WordsV3("key", voiceApi, coroutinesTestRule.testDispatcherProvider)
            val builder = what3WordsV3.autosuggestWithCoordinates(microphone, "en")
            builder.onSuggestions(suggestionsCallback)
            builder.onError(errorCallback)

            // when startListening and connected successfully
            builder.startListening()
            builder.connected(voiceApi)

            // then
            assertThat(builder.isListening()).isTrue()
            verify(exactly = 1) { voiceApi.initialize(any(), any(), any(), builder) }
            verify(exactly = 1) { microphone.startRecording(voiceApi) }

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
    fun `startListening then returns suggestions`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val what3WordsV3 = What3WordsV3("key", voiceApi, coroutinesTestRule.testDispatcherProvider)
            val builder = what3WordsV3.autosuggestWithCoordinates(microphone, "en")
            builder.onSuggestions(suggestionsCallback)
            builder.onError(errorCallback)

            // when startListening and connected successfully
            builder.startListening()
            builder.connected(voiceApi)

            // then
            assertThat(builder.isListening()).isTrue()
            verify(exactly = 1) { voiceApi.initialize(any(), any(), any(), builder) }
            verify(exactly = 1) { microphone.startRecording(voiceApi) }

            val suggestionsJson =
                ClassLoader.getSystemResource("suggestions-with-coordinates.json").readText()
            val suggestions =
                Gson().fromJson(suggestionsJson, Array<SuggestionWithCoordinates>::class.java)
                    .toList()

            // when
            builder.suggestionsWithCoordinates(suggestions)

            // then
            assertThat(builder.isListening()).isFalse()
            verify(exactly = 0) { voiceApi.forceStop() }
            verify(exactly = 1) { microphone.stopRecording() }
            verify(exactly = 1) { suggestionsCallback.accept(suggestions) }
            verify(exactly = 0) { errorCallback.accept(any()) }
        }

    @Test
    fun `focus is set expect param url`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val expectedUrl = "${BASE_URL}${URL_WITH_COORDINATES}?voice-language=en&focus=51.1,-0.152"
            val what3WordsV3 = What3WordsV3("key", voiceApi, coroutinesTestRule.testDispatcherProvider)
            val builder = what3WordsV3.autosuggestWithCoordinates(microphone, "en")
                .onSuggestions(suggestionsCallback)
                .onError(errorCallback)

            // when
            builder.focus(Coordinates(51.1, -0.152))
                .startListening()

            // then
            assertThat(builder.isListening()).isTrue()
            verify(exactly = 1) { voiceApi.initialize(any(), any(), expectedUrl, builder) }
        }

    @Test
    fun `focus is set with nFocusResults expect param url`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val expectedUrl =
                "${BASE_URL}${URL_WITH_COORDINATES}?voice-language=en&focus=51.1,-0.152&n-focus-results=3"
            val what3WordsV3 = What3WordsV3("key", voiceApi, coroutinesTestRule.testDispatcherProvider)
            val builder = what3WordsV3.autosuggestWithCoordinates(microphone, "en")
                .onSuggestions(suggestionsCallback)
                .onError(errorCallback)

            // when
            builder.focus(Coordinates(51.1, -0.152)).nFocusResults(3)
                .startListening()

            // then
            assertThat(builder.isListening()).isTrue()
            verify(exactly = 1) { voiceApi.initialize(any(), any(), expectedUrl, builder) }
        }

    @Test
    fun `nResults is set expect param url`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val expectedUrl = "${BASE_URL}${URL_WITH_COORDINATES}?voice-language=en&n-results=3"
            val what3WordsV3 = What3WordsV3("key", voiceApi, coroutinesTestRule.testDispatcherProvider)
            val builder = what3WordsV3.autosuggestWithCoordinates(microphone, "en")
                .onSuggestions(suggestionsCallback)
                .onError(errorCallback)

            // when
            builder.nResults(3).startListening()

            // then
            assertThat(builder.isListening()).isTrue()
            verify(exactly = 1) { voiceApi.initialize(any(), any(), expectedUrl, builder) }
        }

    @Test
    fun `clipToCountry is set expect param url`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val expectedUrl = "${BASE_URL}${URL_WITH_COORDINATES}?voice-language=en&clip-to-country=GB,FR"
            val what3WordsV3 = What3WordsV3("key", voiceApi, coroutinesTestRule.testDispatcherProvider)
            val builder = what3WordsV3.autosuggestWithCoordinates(microphone, "en")
                .onSuggestions(suggestionsCallback)
                .onError(errorCallback)

            // when
            builder.clipToCountry(listOf("GB", "FR")).startListening()

            // then
            assertThat(builder.isListening()).isTrue()
            verify(exactly = 1) { voiceApi.initialize(any(), any(), expectedUrl, builder) }
        }

    @Test
    fun `clipToCircle is set without radius expect param url`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val expectedUrl =
                "${BASE_URL}${URL_WITH_COORDINATES}?voice-language=en&clip-to-circle=51.1,-0.152,1.0"
            val what3WordsV3 = What3WordsV3("key", voiceApi, coroutinesTestRule.testDispatcherProvider)
            val builder = what3WordsV3.autosuggestWithCoordinates(microphone, "en")
                .onSuggestions(suggestionsCallback)
                .onError(errorCallback)

            // when
            builder.clipToCircle(Coordinates(51.1, -0.152)).startListening()

            // then
            assertThat(builder.isListening()).isTrue()
            verify(exactly = 1) { voiceApi.initialize(any(), any(), expectedUrl, builder) }
        }

    @Test
    fun `clipToCircle is set with radius expect param url`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val expectedUrl =
                "${BASE_URL}${URL_WITH_COORDINATES}?voice-language=en&clip-to-circle=51.1,-0.152,100.0"
            val what3WordsV3 = What3WordsV3("key", voiceApi, coroutinesTestRule.testDispatcherProvider)
            val builder = what3WordsV3.autosuggestWithCoordinates(microphone, "en")
                .onSuggestions(suggestionsCallback)
                .onError(errorCallback)

            // when
            builder.clipToCircle(Coordinates(51.1, -0.152), 100.0).startListening()

            // then
            assertThat(builder.isListening()).isTrue()
            verify(exactly = 1) { voiceApi.initialize(any(), any(), expectedUrl, builder) }
        }

    @Test
    fun `clipToBoundingBox is set expect param url`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val expectedUrl =
                "${BASE_URL}${URL_WITH_COORDINATES}?voice-language=en&clip-to-bounding-box=51.1,-0.152,51.1,-0.152"
            val what3WordsV3 = What3WordsV3("key", voiceApi, coroutinesTestRule.testDispatcherProvider)
            val builder = what3WordsV3.autosuggestWithCoordinates(microphone, "en")
                .onSuggestions(suggestionsCallback)
                .onError(errorCallback)

            // when
            builder.clipToBoundingBox(
                BoundingBox(Coordinates(51.1, -0.152), Coordinates(51.1, -0.152))
            ).startListening()

            // then
            assertThat(builder.isListening()).isTrue()
            verify(exactly = 1) { voiceApi.initialize(any(), any(), expectedUrl, builder) }
        }

    @Test
    fun `clipToPolygon is set expect param url`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val expectedUrl =
                "${BASE_URL}${URL_WITH_COORDINATES}?voice-language=en&clip-to-polygon=51.1,-0.152,51.1,-0.152,51.1,-0.152"
            val what3WordsV3 = What3WordsV3("key", voiceApi, coroutinesTestRule.testDispatcherProvider)
            val builder = what3WordsV3.autosuggestWithCoordinates(microphone, "en")
                .onSuggestions(suggestionsCallback)
                .onError(errorCallback)

            // when
            builder.clipToPolygon(
                listOf(
                    Coordinates(51.1, -0.152),
                    Coordinates(51.1, -0.152),
                    Coordinates(51.1, -0.152)
                )
            ).startListening()

            // then
            assertThat(builder.isListening()).isTrue()
            verify(exactly = 1) { voiceApi.initialize(any(), any(), expectedUrl, builder) }
        }

    @Test
    fun `custom VoiceApi URL`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val textCustomUrl = "http://custom.text.url/"
            val voiceCustomUrl = "wss://custom.voice.url/"
            val what3WordsV3 =
                What3WordsV3("key", textCustomUrl, voiceCustomUrl, context, emptyMap())

            // when
            val builder = what3WordsV3.autosuggestWithCoordinates(microphone, "en")
            val finalURL = builder.createSocketUrlWithCoordinates(what3WordsV3.voiceApi.getBaseVoiceUrl())

            // then
            assertThat(finalURL.contains(voiceCustomUrl)).isTrue()
        }
}
