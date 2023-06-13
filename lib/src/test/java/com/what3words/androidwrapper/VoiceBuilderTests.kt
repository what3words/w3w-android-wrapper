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
import com.what3words.androidwrapper.voice.VoiceApi.Companion.URL_WITHOUT_COORDINATES
import com.what3words.androidwrapper.voice.VoiceApiListener
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.APIError
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
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
class VoiceBuilderTests {
    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var socket: WebSocket

    @MockK
    private lateinit var voiceApi: VoiceApi

    @MockK
    private lateinit var microphone: Microphone

    @MockK
    private lateinit var context: Context

    @MockK
    private var suggestionsCallback = mockk<Consumer<List<Suggestion>>>()

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
            voiceApi.initialize(any(), any(), any(), any(), any<VoiceApiListener>())
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
            voiceApi.baseUrl
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
            val what3WordsV3 =
                What3WordsV3("key", voiceApi, coroutinesTestRule.testDispatcherProvider)
            val builder = what3WordsV3.autosuggest(microphone, "en")
            builder.onSuggestions(suggestionsCallback)
            builder.onError(errorCallback)

            // when startListening and connected successfully
            builder.startListening()
            builder.connected(voiceApi)

            // then
            assertThat(builder.isListening()).isTrue()
            verify(exactly = 1) { voiceApi.initialize(any(), any(), any(), any(), builder) }
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
    fun `startListening then error occurs`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        // given
        val what3WordsV3 = What3WordsV3("key", voiceApi, coroutinesTestRule.testDispatcherProvider)
        val builder = what3WordsV3.autosuggest(microphone, "en")
        builder.onSuggestions(suggestionsCallback)
        builder.onError(errorCallback)

        // when startListening and connected successfully
        builder.startListening()
        builder.connected(voiceApi)

        // then
        assertThat(builder.isListening()).isTrue()
        verify(exactly = 1) { voiceApi.initialize(any(), any(), any(), any(), builder) }
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
            val what3WordsV3 =
                What3WordsV3("key", voiceApi, coroutinesTestRule.testDispatcherProvider)
            val builder = what3WordsV3.autosuggest(microphone, "en")
            builder.onSuggestions(suggestionsCallback)
            builder.onError(errorCallback)

            // when startListening and connected successfully
            builder.startListening()
            builder.connected(voiceApi)

            // then
            assertThat(builder.isListening()).isTrue()
            verify(exactly = 1) { voiceApi.initialize(any(), any(), any(), any(), builder) }
            verify(exactly = 1) { microphone.startRecording(voiceApi) }

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
    fun `focus is set to VoiceBuilder autosuggestOptions`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val what3WordsV3 =
                What3WordsV3("key", voiceApi, coroutinesTestRule.testDispatcherProvider)
            val builder = what3WordsV3.autosuggest(microphone, "en")
                .onSuggestions(suggestionsCallback)
                .onError(errorCallback)

            // when
            builder.focus(Coordinates(51.1, -0.152))
                .startListening()

            // then
            assertThat(builder.isListening()).isTrue()
            assertThat(builder.autosuggestOptions.focus).isEqualTo(Coordinates(51.1, -0.152))
        }

    @Test
    fun `focus is set with nFocusResults to VoiceBuilder autosuggestOptions`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val what3WordsV3 =
                What3WordsV3("key", voiceApi, coroutinesTestRule.testDispatcherProvider)
            val builder = what3WordsV3.autosuggest(microphone, "en")
                .onSuggestions(suggestionsCallback)
                .onError(errorCallback)

            // when
            builder.focus(Coordinates(51.1, -0.152)).nFocusResults(3)
                .startListening()

            // then
            assertThat(builder.isListening()).isTrue()
            assertThat(builder.autosuggestOptions.focus).isEqualTo(Coordinates(51.1, -0.152))
            assertThat(builder.autosuggestOptions.nFocusResults).isEqualTo(3)
        }

    @Test
    fun `nResults is set to VoiceBuilder autosuggestOptions`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val what3WordsV3 =
                What3WordsV3("key", voiceApi, coroutinesTestRule.testDispatcherProvider)
            val builder = what3WordsV3.autosuggest(microphone, "en")
                .onSuggestions(suggestionsCallback)
                .onError(errorCallback)

            // when
            builder.nResults(3).startListening()

            // then
            assertThat(builder.isListening()).isTrue()
            assertThat(builder.autosuggestOptions.nResults).isEqualTo(3)
        }

    @Test
    fun `clipToCountry is set to VoiceBuilder autosuggestOptions`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val what3WordsV3 =
                What3WordsV3("key", voiceApi, coroutinesTestRule.testDispatcherProvider)
            val builder = what3WordsV3.autosuggest(microphone, "en")
                .onSuggestions(suggestionsCallback)
                .onError(errorCallback)

            // when
            builder.clipToCountry(listOf("GB", "FR")).startListening()

            // then
            assertThat(builder.isListening()).isTrue()
            assertThat(builder.autosuggestOptions.clipToCountry).isEqualTo(listOf("GB", "FR"))
        }

    @Test
    fun `clipToCircle is set without radius to VoiceBuilder autosuggestOptions`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val what3WordsV3 =
                What3WordsV3("key", voiceApi, coroutinesTestRule.testDispatcherProvider)
            val builder = what3WordsV3.autosuggest(microphone, "en")
                .onSuggestions(suggestionsCallback)
                .onError(errorCallback)

            // when
            builder.clipToCircle(Coordinates(51.1, -0.152)).startListening()

            // then
            assertThat(builder.isListening()).isTrue()
            assertThat(builder.autosuggestOptions.clipToCircle).isEqualTo(Coordinates(51.1, -0.152))
            assertThat(builder.autosuggestOptions.clipToCircleRadius).isEqualTo(1.0)
        }

    @Test
    fun `clipToCircle is set with radius to VoiceBuilder autosuggestOptions`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val what3WordsV3 =
                What3WordsV3("key", voiceApi, coroutinesTestRule.testDispatcherProvider)
            val builder = what3WordsV3.autosuggest(microphone, "en")
                .onSuggestions(suggestionsCallback)
                .onError(errorCallback)

            // when
            builder.clipToCircle(Coordinates(51.1, -0.152), 100.0).startListening()

            // then
            assertThat(builder.isListening()).isTrue()
            assertThat(builder.autosuggestOptions.clipToCircle).isEqualTo(Coordinates(51.1, -0.152))
            assertThat(builder.autosuggestOptions.clipToCircleRadius).isEqualTo(100.0)
        }

    @Test
    fun `clipToBoundingBox is set to VoiceBuilder autosuggestOptions`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val expectedUrl =
                "${BASE_URL}${URL_WITHOUT_COORDINATES}?voice-language=en&clip-to-bounding-box=51.1,-0.152,51.1,-0.152"
            val what3WordsV3 =
                What3WordsV3("key", voiceApi, coroutinesTestRule.testDispatcherProvider)
            val builder = what3WordsV3.autosuggest(microphone, "en")
                .onSuggestions(suggestionsCallback)
                .onError(errorCallback)

            // when
            builder.clipToBoundingBox(
                BoundingBox(Coordinates(51.1, -0.152), Coordinates(51.1, -0.152))
            ).startListening()

            // then
            assertThat(builder.isListening()).isTrue()
            assertThat(builder.autosuggestOptions.clipToBoundingBox).isEqualTo(
                BoundingBox(
                    Coordinates(51.1, -0.152),
                    Coordinates(51.1, -0.152)
                )
            )
        }

    @Test
    fun `clipToPolygon is set to VoiceBuilder autosuggestOptions`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val what3WordsV3 =
                What3WordsV3("key", voiceApi, coroutinesTestRule.testDispatcherProvider)
            val builder = what3WordsV3.autosuggest(microphone, "en")
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
            assertThat(builder.autosuggestOptions.clipToPolygon).isEqualTo(
                listOf(
                    Coordinates(51.1, -0.152),
                    Coordinates(51.1, -0.152),
                    Coordinates(51.1, -0.152)
                )
            )
        }

    @Test
    fun `updateAutosuggestOptions in VoiceBuilder`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            val what3WordsV3 =
                What3WordsV3("key", voiceApi, coroutinesTestRule.testDispatcherProvider)
            val builder = what3WordsV3.autosuggest(microphone, "en")
                .onSuggestions(suggestionsCallback)
                .onError(errorCallback)

            // when
            builder.updateAutosuggestOptions(
                options = AutosuggestOptions().apply {
                    clipToBoundingBox =
                        BoundingBox(Coordinates(51.1, -0.152), Coordinates(51.1, -0.152))
                    clipToPolygon = listOf(
                        Coordinates(51.1, -0.152),
                        Coordinates(51.1, -0.152),
                        Coordinates(51.1, -0.152)
                    )
                }
            ).startListening()

            // then
            assertThat(builder.isListening()).isTrue()
            assertThat(builder.autosuggestOptions.clipToPolygon).isEqualTo(
                listOf(
                    Coordinates(51.1, -0.152),
                    Coordinates(51.1, -0.152),
                    Coordinates(51.1, -0.152)
                )
            )
            assertThat(builder.autosuggestOptions.clipToBoundingBox).isEqualTo(
                BoundingBox(Coordinates(51.1, -0.152), Coordinates(51.1, -0.152))
            )
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
            val builder = what3WordsV3.autosuggest(microphone, "en")
            val finalURL = (what3WordsV3.voiceProvider as VoiceApi).createSocketUrl(
                "${what3WordsV3.voiceProvider.baseUrl}${URL_WITHOUT_COORDINATES}",
                "en",
                builder.autosuggestOptions
            )

            // then
            assertThat(finalURL.contains(voiceCustomUrl)).isTrue()
        }
}
