package com.what3words.androidwrapper

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.util.Consumer
import com.google.gson.Gson
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.androidwrapper.datasource.text.api.error.BadWordsError
import com.what3words.androidwrapper.datasource.text.api.error.InvalidKeyError
import com.what3words.androidwrapper.datasource.voice.mappers.SuggestionMapper
import com.what3words.androidwrapper.helpers.AutosuggestHelper
import com.what3words.core.types.common.W3WError
import com.what3words.core.types.common.W3WResult
import com.what3words.core.types.domain.W3WAddress
import com.what3words.core.types.domain.W3WCountry
import com.what3words.core.types.domain.W3WSuggestion
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WPolygon
import com.what3words.core.types.geometry.W3WRectangle
import com.what3words.core.types.options.W3WAutosuggestOptions
import com.what3words.javawrapper.request.SourceApi
import com.what3words.javawrapper.response.Suggestion
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@ExperimentalCoroutinesApi
class AutosuggestHelperTests {
    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    private lateinit var expectedSuggestions: List<W3WSuggestion>

    private lateinit var helper: AutosuggestHelper

    private val suggestionMapper = SuggestionMapper()

    @MockK
    private var dataSource: W3WApiTextDataSource = mockk()

    @MockK
    private var suggestionsCallback = mockk<Consumer<List<W3WSuggestion>>>()

    @MockK
    private var suggestionCallback = mockk<Consumer<W3WSuggestion>>()

    @MockK
    private var convertCallback = mockk<Consumer<W3WSuggestion>>()

    @MockK
    private var errorCallback = mockk<Consumer<W3WError>>()

    @MockK
    private var didYouMeanCallback = mockk<Consumer<W3WSuggestion>>()

    @Before
    fun setup() {
        suggestionsCallback = mockk()
        errorCallback = mockk()
        helper = AutosuggestHelper(dataSource, coroutinesTestRule.testDispatcherProvider)

        loadAsset()

        justRun {
            suggestionsCallback.accept(any())
            errorCallback.accept(any())
            suggestionCallback.accept(any())
            convertCallback.accept(any())
            didYouMeanCallback.accept(any())
        }
    }

    private fun loadAsset() {
        val suggestionsJson =
            ClassLoader.getSystemResource("suggestions.json").readText()

        expectedSuggestions =
            Gson().fromJson(suggestionsJson, Array<Suggestion>::class.java).toList().map {
                suggestionMapper.mapFrom(it)
            }
    }

    @Test
    fun `invalid 3wa return empty list`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        // given
        val list = emptyList<W3WSuggestion>()

        // when
        helper.update("index", suggestionsCallback, errorCallback)

        // then
        verify(exactly = 1) { suggestionsCallback.accept(list) }
        verify(exactly = 0) { errorCallback.accept(any()) }
    }

    @Test
    fun `valid 3wa returns suggestions`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        // given
        val helper = AutosuggestHelper(dataSource, coroutinesTestRule.testDispatcherProvider)
        val suggestionsJson =
            ClassLoader.getSystemResource("suggestions.json").readText()
        val suggestions =
            Gson().fromJson(suggestionsJson, Array<Suggestion>::class.java).toList().map {
                suggestionMapper.mapFrom(it)
            }

        every {
            dataSource.autosuggest("index.home.r", null)
        } answers {
            W3WResult.Success(suggestions)
        }

        every {
            dataSource.autosuggest("index.home.ra", null)
        } answers {
            W3WResult.Success(suggestions)
        }

        // when
        helper.update("index", suggestionsCallback, errorCallback, didYouMeanCallback)
        helper.update("index.home", suggestionsCallback, errorCallback, didYouMeanCallback)
        helper.update("index.home.r", suggestionsCallback, errorCallback, didYouMeanCallback)
        delay(150)
        helper.update("index.home.ra", suggestionsCallback, errorCallback, didYouMeanCallback)
        delay(500)

        // then
        verify(exactly = 2) { suggestionsCallback.accept(emptyList()) }
        verify(exactly = 1) { suggestionsCallback.accept(suggestions) }
        verify(exactly = 0) { didYouMeanCallback.accept(any()) }
        verify(exactly = 0) { errorCallback.accept(any()) }
    }

    @Test
    fun `did you mean 3wa returns suggestions`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given

            every {
                dataSource.autosuggest("star.words.f", null)
            } answers {
                W3WResult.Success(expectedSuggestions)
            }

            every {
                dataSource.autosuggest("star.words.forced", null)
            } answers {
                W3WResult.Success(expectedSuggestions)
            }

            // when
            helper.update("star", suggestionsCallback, errorCallback, didYouMeanCallback)
            helper.update("star words", suggestionsCallback, errorCallback, didYouMeanCallback)
            helper.update("star words f", suggestionsCallback, errorCallback, didYouMeanCallback)
            delay(150)
            helper.update(
                "star words forced",
                suggestionsCallback,
                errorCallback,
                didYouMeanCallback
            )
            delay(500)

            // then
            verify(exactly = 2) { suggestionsCallback.accept(emptyList()) }
            verify(exactly = 1) { didYouMeanCallback.accept(expectedSuggestions.first()) }
            verify(exactly = 0) { errorCallback.accept(any()) }
        }

    @Test
    fun `did you mean 3wa returns suggestions with capital letters`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            every {
                dataSource.autosuggest("star.words.f", null)
            } answers {
                W3WResult.Success(expectedSuggestions)
            }

            every {
                dataSource.autosuggest("Star.words.forced", null)
            } answers {
                W3WResult.Success(expectedSuggestions)
            }

            // when
            helper.update("Star", suggestionsCallback, errorCallback, didYouMeanCallback)
            helper.update("Star words", suggestionsCallback, errorCallback, didYouMeanCallback)
            helper.update("Star words f", suggestionsCallback, errorCallback, didYouMeanCallback)
            delay(150)
            helper.update(
                "Star words forced",
                suggestionsCallback,
                errorCallback,
                didYouMeanCallback
            )
            delay(500)

            // then
            verify(exactly = 2) { suggestionsCallback.accept(emptyList()) }
            verify(exactly = 1) { didYouMeanCallback.accept(expectedSuggestions.first()) }
            verify(exactly = 0) { errorCallback.accept(any()) }
        }

    @Test
    fun `allowFlexibleDelimiters is true returns suggestions`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            helper.allowFlexibleDelimiters(true)

            every {
                dataSource.autosuggest("star.words.f", null)
            } answers {
                W3WResult.Success(expectedSuggestions)
            }

            every {
                dataSource.autosuggest("star.words.forced", null)
            } answers {
                W3WResult.Success(expectedSuggestions)
            }

            // when
            helper.update("star", suggestionsCallback, errorCallback, didYouMeanCallback)
            helper.update("star words", suggestionsCallback, errorCallback, didYouMeanCallback)
            helper.update("star words f", suggestionsCallback, errorCallback, didYouMeanCallback)
            delay(150)
            helper.update(
                "star words forced",
                suggestionsCallback,
                errorCallback,
                didYouMeanCallback
            )
            delay(300)

            // then
            verify(exactly = 2) { suggestionsCallback.accept(emptyList()) }
            verify(exactly = 1) { suggestionsCallback.accept(expectedSuggestions) }
            verify(exactly = 0) { didYouMeanCallback.accept(any()) }
            verify(exactly = 0) { errorCallback.accept(any()) }
        }

    @Test
    fun `valid 3wa returns ApiError and callback is set`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given

            val error = InvalidKeyError("invalid_key", "Invalid key")
            val failureResult =
                W3WResult.Failure<List<W3WSuggestion>>(
                    "Error",
                    error
                )

            every {
                dataSource.autosuggest("index.home.r", null)
            } answers {
                failureResult
            }

            every {
                dataSource.autosuggest("index.home.ra", null)
            } answers {
                failureResult
            }

            // when
            helper.update("index", suggestionsCallback, errorCallback)
            helper.update("index.home", suggestionsCallback, errorCallback)
            helper.update("index.home.r", suggestionsCallback, errorCallback)
            delay(150)
            helper.update("index.home.ra", suggestionsCallback, errorCallback)
            delay(300)

            // then
            verify(exactly = 2) { suggestionsCallback.accept(any()) }
            verify(exactly = 1) { errorCallback.accept(error) }
        }

    @Test
    fun `valid 3wa returns ApiError and callback is not set`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val error = InvalidKeyError("invalid_key", "Invalid key")
            val failureResult =
                W3WResult.Failure<List<W3WSuggestion>>(
                    "Error",
                    error
                )

            every {
                dataSource.autosuggest("index.home.r", null)
            } answers {
                failureResult
            }

            every {
                dataSource.autosuggest("index.home.ra", null)
            } answers {
                failureResult
            }

            // when
            helper.update("index", suggestionsCallback, null)
            helper.update("index.home", suggestionsCallback, null)
            helper.update("index.home.r", suggestionsCallback, null)
            delay(150)
            helper.update("index.home.ra", suggestionsCallback, null)
            delay(500)

            // then
            verify(exactly = 2) { suggestionsCallback.accept(any()) }
            verify(exactly = 0) { errorCallback.accept(error) }
        }

    @Test
    fun `selected suggestion without coordinates`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val suggestion = mockk<W3WSuggestion>()

            every {
                suggestion.w3wAddress.address
            } answers {
                "///index.home.raft"
            }

            every {
                suggestion.rank
            } answers {
                1
            }

            every {
                dataSource.autosuggestionSelection(any(), any(), any(), any())
            } answers {
                mockk()
            }

            // when
            helper.selected("index.home.r", suggestion, suggestionCallback)

            // then
            verify(exactly = 1) {
                dataSource.autosuggestionSelection(
                    "index.home.r",
                    "index.home.raft",
                    1,
                    SourceApi.TEXT
                )
            }
            verify(exactly = 1) { suggestionCallback.accept(suggestion) }
        }

    @Test
    fun `selected suggestion with coordinates`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val suggestion = mockk<W3WSuggestion>()
            val coordinates = mockk<W3WCoordinates>()
            val address = mockk<W3WAddress>()

            every {
                suggestion.w3wAddress.address
            } answers {
                "///index.home.raft"
            }

            every {
                address.center
            } answers {
                coordinates
            }

            every {
                suggestion.w3wAddress.square
            } answers {
                W3WRectangle(W3WCoordinates(51.2, 0.234), W3WCoordinates(51.2, 0.234))
            }

            every {
                suggestion.w3wAddress.country.twoLetterCode
            } answers {
                "UK"
            }

            every {
                suggestion.distanceToFocus?.km()
            } answers {
                1.0
            }

            every {
                suggestion.w3wAddress.nearestPlace
            } answers {
                "Bayswater, London"
            }

            every {
                suggestion.w3wAddress.language.w3wCode
            } answers {
                "en"
            }

            every {
                suggestion.w3wAddress.language.w3wLocale
            } answers {
                "en-GB"
            }

            every {
                suggestion.rank
            } answers {
                1
            }

            every {
                coordinates.lat
            } answers {
                51.2
            }

            every {
                coordinates.lng
            } answers {
                -0.15
            }

            every {
                dataSource.convertToCoordinates(any())
            } answers {
                W3WResult.Success(address)
            }


            every {
                dataSource.autosuggestionSelection(any(), any(), any(), any())
            } answers {
                mockk()
            }

            // when
            helper.selectedWithCoordinates(
                "index.home.r",
                suggestion,
                convertCallback,
                errorCallback
            )
            delay(300)

            // then
            verify(exactly = 1) {
                dataSource.autosuggestionSelection(
                    "index.home.r",
                    "index.home.raft",
                    1,
                    SourceApi.TEXT
                )
            }
            verify(exactly = 1) {
                dataSource.convertToCoordinates(
                    "index.home.raft",
                )
            }
            verify(exactly = 1) { convertCallback.accept(any()) }
            verify(exactly = 0) { errorCallback.accept(any()) }
        }

    @Test
    fun `selectedWithCoordinates returns error`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // given
            val suggestion = mockk<W3WSuggestion>()
            val coordinates = mockk<W3WCoordinates>()
            val error = BadWordsError("bad_words", "Bad words")

            every {
                suggestion.w3wAddress.address
            } answers {
                "///index.home.raft"
            }

            every {
                suggestion.rank
            } answers {
                1
            }

            every {
                coordinates.lat
            } answers {
                51.2
            }

            every {
                coordinates.lng
            } answers {
                -0.15
            }

            every {
                dataSource.convertToCoordinates("index.home.raft")
            } answers {
                W3WResult.Failure("Bad word", error)
            }

            every {
                dataSource.autosuggestionSelection(any(), any(), any(), any())
            } answers {
                mockk()
            }

            helper.selectedWithCoordinates(
                "index.home.r",
                suggestion,
                convertCallback,
                errorCallback
            )

            // then
            verify(exactly = 1) {
                dataSource.convertToCoordinates(
                    "index.home.raft",
                )
            }
            verify(exactly = 1) {
                dataSource.autosuggestionSelection(
                    "index.home.r",
                    "index.home.raft",
                    1,
                    SourceApi.TEXT
                )
            }
            verify(exactly = 0) { convertCallback.accept(any()) }
            verify(exactly = 1) { errorCallback.accept(error) }
        }

    @Test
    fun `filters are set expect autosuggestBuilder filters to be called`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            val focus = W3WCoordinates(51.2, 0.234)
            val boundingBox = W3WRectangle(focus, focus)
            val polygon = W3WPolygon(listOf(focus, focus, focus))
            val autosuggestOptions = W3WAutosuggestOptions.Builder()
                .focus(focus)
                .clipToCountry(*arrayOf(W3WCountry("GB"), W3WCountry("FR")))
                .clipToBoundingBox(boundingBox)
                .clipToPolygon(polygon)
                .build()

            every {
                dataSource.autosuggest("index.home.ra", options = autosuggestOptions)
            } answers {
                W3WResult.Success(expectedSuggestions)
            }

            // when
            helper.options(autosuggestOptions)
            helper.update("index", suggestionsCallback, errorCallback)
            helper.update("index.home", suggestionsCallback, errorCallback)
            helper.update("index.home.ra", suggestionsCallback, errorCallback)
            delay(500)

            // then
            verify(exactly = 2) { suggestionsCallback.accept(emptyList()) }
            verify(exactly = 1) { suggestionsCallback.accept(expectedSuggestions) }
            verify(exactly = 0) { errorCallback.accept(any()) }
        }
}
