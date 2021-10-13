package com.what3words.androidwrapper

import androidx.core.util.Consumer
import com.google.gson.Gson
import com.what3words.androidwrapper.helpers.AutosuggestHelper
import com.what3words.javawrapper.request.AutosuggestRequest
import com.what3words.javawrapper.request.AutosuggestSelectionRequest
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.ConvertToCoordinatesRequest
import com.what3words.javawrapper.request.SourceApi
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Autosuggest
import com.what3words.javawrapper.response.ConvertToCoordinates
import com.what3words.javawrapper.response.Coordinates
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class AutosuggestHelperTests {
    private val dispatcher = TestCoroutineDispatcher()

    @MockK
    private var api: What3WordsV3 = mockk()

    @MockK
    private var suggestionsCallback = mockk<Consumer<List<Suggestion>>>()

    @MockK
    private var suggestionCallback = mockk<Consumer<Suggestion>>()

    @MockK
    private var convertCallback = mockk<Consumer<SuggestionWithCoordinates>>()

    @MockK
    private var errorCallback = mockk<Consumer<APIResponse.What3WordsError>>()

    @MockK
    private var didYouMeanCallback = mockk<Consumer<Suggestion>>()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        suggestionsCallback = mockk()
        errorCallback = mockk()

        justRun {
            suggestionsCallback.accept(any())
            errorCallback.accept(any())
            suggestionCallback.accept(any())
            convertCallback.accept(any())
            didYouMeanCallback.accept(any())
            api.autosuggestionSelection(any(), any(), any(), any()).execute()
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invalid 3wa return empty list`() {
        // given
        val helper = AutosuggestHelper(api)

        // when
        helper.update("index", suggestionsCallback, errorCallback)

        // then
        verify(exactly = 1) { suggestionsCallback.accept(emptyList()) }
        verify(exactly = 0) { errorCallback.accept(any()) }
    }

    @Test
    fun `valid 3wa returns suggestions`() {
        // given
        val helper = AutosuggestHelper(api)
        val suggestionsJson =
            ClassLoader.getSystemResource("suggestions.json").readText()
        val suggestions =
            Gson().fromJson(suggestionsJson, Array<Suggestion>::class.java).toList()
        val autosuggest = mockk<Autosuggest>()

        every {
            autosuggest.isSuccessful
        } answers {
            true
        }

        every {
            autosuggest.suggestions
        } answers {
            suggestions
        }

        every {
            api.autosuggest("index.home.r").execute()
        } answers {
            autosuggest
        }

        every {
            api.autosuggest("index.home.ra").execute()
        } answers {
            autosuggest
        }

        // when
        runBlocking {
            helper.update("index", suggestionsCallback, errorCallback, didYouMeanCallback)
            helper.update("index.home", suggestionsCallback, errorCallback, didYouMeanCallback)
            helper.update("index.home.r", suggestionsCallback, errorCallback, didYouMeanCallback)
            delay(150)
            helper.update("index.home.ra", suggestionsCallback, errorCallback, didYouMeanCallback)
            delay(500)
        }

        // then
        verify(exactly = 2) { suggestionsCallback.accept(emptyList()) }
        verify(exactly = 1) { suggestionsCallback.accept(suggestions) }
        verify(exactly = 0) { didYouMeanCallback.accept(any()) }
        verify(exactly = 0) { errorCallback.accept(any()) }
    }

    @Test
    fun `did you mean 3wa returns suggestions`() {
        // given
        val helper = AutosuggestHelper(api)
        val suggestionsJson =
            ClassLoader.getSystemResource("suggestions.json").readText()
        val suggestions =
            Gson().fromJson(suggestionsJson, Array<Suggestion>::class.java).toList()
        val autosuggest = mockk<Autosuggest>()

        every {
            autosuggest.isSuccessful
        } answers {
            true
        }

        every {
            autosuggest.suggestions
        } answers {
            suggestions
        }

        every {
            api.autosuggest("star.words.f").execute()
        } answers {
            autosuggest
        }

        every {
            api.autosuggest("star.words.forced").execute()
        } answers {
            autosuggest
        }

        // when
        runBlocking {
            helper.update("star", suggestionsCallback, errorCallback, didYouMeanCallback)
            helper.update("star words", suggestionsCallback, errorCallback, didYouMeanCallback)
            helper.update("star words f", suggestionsCallback, errorCallback, didYouMeanCallback)
            delay(150)
            helper.update("star words forced", suggestionsCallback, errorCallback, didYouMeanCallback)
            delay(500)
        }

        // then
        verify(exactly = 2) { suggestionsCallback.accept(emptyList()) }
        verify(exactly = 1) { didYouMeanCallback.accept(suggestions.first()) }
        verify(exactly = 0) { errorCallback.accept(any()) }
    }

    @Test
    fun `allowFlexibleDelimiters is true returns suggestions`() {
        // given
        val helper = AutosuggestHelper(api).allowFlexibleDelimiters(true)
        val suggestionsJson =
            ClassLoader.getSystemResource("suggestions.json").readText()
        val suggestions =
            Gson().fromJson(suggestionsJson, Array<Suggestion>::class.java).toList()
        val autosuggest = mockk<Autosuggest>()

        every {
            autosuggest.isSuccessful
        } answers {
            true
        }

        every {
            autosuggest.suggestions
        } answers {
            suggestions
        }

        every {
            api.autosuggest("star.words.f").execute()
        } answers {
            autosuggest
        }

        every {
            api.autosuggest("star.words.forced").execute()
        } answers {
            autosuggest
        }

        // when
        runBlocking {
            helper.update("star", suggestionsCallback, errorCallback, didYouMeanCallback)
            helper.update("star words", suggestionsCallback, errorCallback, didYouMeanCallback)
            helper.update("star words f", suggestionsCallback, errorCallback, didYouMeanCallback)
            delay(150)
            helper.update("star words forced", suggestionsCallback, errorCallback, didYouMeanCallback)
            delay(500)
        }

        // then
        verify(exactly = 2) { suggestionsCallback.accept(emptyList()) }
        verify(exactly = 1) { suggestionsCallback.accept(suggestions) }
        verify(exactly = 0) { didYouMeanCallback.accept(any()) }
        verify(exactly = 0) { errorCallback.accept(any()) }
    }

    @Test
    fun `valid 3wa returns ApiError and callback is set`() {
        // given
        val helper = AutosuggestHelper(api)
        val autosuggest = mockk<Autosuggest>()

        every {
            autosuggest.isSuccessful
        } answers {
            false
        }

        every {
            autosuggest.error
        } answers {
            APIResponse.What3WordsError.INVALID_KEY
        }

        every {
            api.autosuggest("index.home.r").execute()
        } answers {
            autosuggest
        }

        every {
            api.autosuggest("index.home.ra").execute()
        } answers {
            autosuggest
        }

        // when
        runBlocking {
            helper.update("index", suggestionsCallback, errorCallback)
            helper.update("index.home", suggestionsCallback, errorCallback)
            helper.update("index.home.r", suggestionsCallback, errorCallback)
            delay(150)
            helper.update("index.home.ra", suggestionsCallback, errorCallback)
            delay(500)
        }

        // then
        verify(exactly = 2) { suggestionsCallback.accept(any()) }
        verify(exactly = 1) { errorCallback.accept(APIResponse.What3WordsError.INVALID_KEY) }
    }

    @Test
    fun `valid 3wa returns ApiError and callback is not set`() {
        // given
        val helper = AutosuggestHelper(api)
        val autosuggest = mockk<Autosuggest>()

        every {
            autosuggest.isSuccessful
        } answers {
            false
        }

        every {
            autosuggest.error
        } answers {
            APIResponse.What3WordsError.INVALID_KEY
        }

        every {
            api.autosuggest("index.home.r").execute()
        } answers {
            autosuggest
        }

        every {
            api.autosuggest("index.home.ra").execute()
        } answers {
            autosuggest
        }

        // when
        runBlocking {
            helper.update("index", suggestionsCallback, null)
            helper.update("index.home", suggestionsCallback, null)
            helper.update("index.home.r", suggestionsCallback, null)
            delay(150)
            helper.update("index.home.ra", suggestionsCallback, null)
            delay(500)
        }

        // then
        verify(exactly = 2) { suggestionsCallback.accept(any()) }
        verify(exactly = 0) { errorCallback.accept(APIResponse.What3WordsError.INVALID_KEY) }
    }

    @Test
    fun `selected suggestion without coordinates`() {
        // given
        val helper = AutosuggestHelper(api)
        val suggestion = mockk<Suggestion>()
        val selectionBuilder = mockk<AutosuggestSelectionRequest.Builder>()

        every {
            suggestion.words
        } answers {
            "index.home.raft"
        }

        every {
            suggestion.rank
        } answers {
            1
        }

        every {
            api.autosuggestionSelection(any(), any(), any(), any())
        } answers {
            selectionBuilder
        }

        every {
            selectionBuilder.execute()
        } answers {
            mockk()
        }

        // when
        runBlocking {
            helper.selected("index.home.r", suggestion, suggestionCallback)
        }

        // then
        verify(exactly = 1) {
            api.autosuggestionSelection(
                "index.home.r",
                "index.home.raft",
                1,
                SourceApi.TEXT
            )
        }
        verify(exactly = 1) { suggestionCallback.accept(suggestion) }
    }

//TODO: flaky    @Test
//    fun `selected suggestion with coordinates`() {
//        // given
//        val helper = AutosuggestHelper(api)
//        val suggestion = mockk<Suggestion>()
//        val convert = mockk<ConvertToCoordinates>()
//        val coordinates = mockk<Coordinates>()
//        val selectionBuilder = mockk<AutosuggestSelectionRequest.Builder>()
//        val convertBuilder = mockk<ConvertToCoordinatesRequest.Builder>()
//
//        every {
//            suggestion.words
//        } answers {
//            "index.home.raft"
//        }
//
//        every {
//            suggestion.country
//        } answers {
//            "UK"
//        }
//
//        every {
//            suggestion.distanceToFocusKm
//        } answers {
//            1
//        }
//
//        every {
//            suggestion.nearestPlace
//        } answers {
//            "Bayswater, London"
//        }
//
//        every {
//            suggestion.language
//        } answers {
//            "en-GB"
//        }
//
//        every {
//            suggestion.rank
//        } answers {
//            1
//        }
//
//        every {
//            coordinates.lat
//        } answers {
//            51.2
//        }
//
//        every {
//            coordinates.lng
//        } answers {
//            -0.15
//        }
//
//        every {
//            convert.coordinates
//        } answers {
//            coordinates
//        }
//
//        every {
//            convert.isSuccessful
//        } answers {
//            true
//        }
//
//        every {
//            convert.map
//        } answers {
//            "map"
//        }
//
//        every {
//            convert.square
//        } answers {
//            Square()
//        }
//
//        every {
//            api.autosuggestionSelection(any(), any(), any(), any())
//        } answers {
//            selectionBuilder
//        }
//
//        every {
//            api.convertToCoordinates("index.home.raft")
//        } answers {
//            convertBuilder
//        }
//
//        every {
//            convertBuilder.execute()
//        } answers {
//            convert
//        }
//
//        every {
//            selectionBuilder.execute()
//        } answers {
//            mockk()
//        }
//
//        // when
//        runBlocking {
//            helper.selectedWithCoordinates(
//                "index.home.r",
//                suggestion,
//                convertCallback,
//                errorCallback
//            )
//        }
//
//        // then
//        verify(exactly = 1) {
//            api.autosuggestionSelection(
//                "index.home.r",
//                "index.home.raft",
//                1,
//                SourceApi.TEXT
//            )
//        }
//        verify(exactly = 1) {
//            api.convertToCoordinates(
//                "index.home.raft",
//            )
//        }
//        verify(exactly = 1) { convertCallback.accept(any()) }
//        verify(exactly = 0) { errorCallback.accept(any()) }
//    }

    @Test
    fun `selectedWithCoordinates returns error`() {
        // given
        val helper = AutosuggestHelper(api)
        val suggestion = mockk<Suggestion>()
        val convert = mockk<ConvertToCoordinates>()
        val coordinates = mockk<Coordinates>()
        val selectionBuilder = mockk<AutosuggestSelectionRequest.Builder>()
        val convertBuilder = mockk<ConvertToCoordinatesRequest.Builder>()

        every {
            suggestion.words
        } answers {
            "index.home.raft"
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
            convert.error
        } answers {
            APIResponse.What3WordsError.BAD_WORDS
        }

        every {
            convert.isSuccessful
        } answers {
            false
        }

        every {
            api.autosuggestionSelection(any(), any(), any(), any())
        } answers {
            selectionBuilder
        }

        every {
            api.convertToCoordinates("index.home.raft")
        } answers {
            convertBuilder
        }

        every {
            convertBuilder.execute()
        } answers {
            convert
        }

        every {
            selectionBuilder.execute()
        } answers {
            mockk()
        }

        runBlocking {
            helper.selectedWithCoordinates(
                "index.home.r",
                suggestion,
                convertCallback,
                errorCallback
            )
        }

        // then
        verify(exactly = 1) {
            api.convertToCoordinates(
                "index.home.raft",
            )
        }
        verify(exactly = 1) {
            api.autosuggestionSelection(
                "index.home.r",
                "index.home.raft",
                1,
                SourceApi.TEXT
            )
        }
        verify(exactly = 0) { convertCallback.accept(any()) }
        verify(exactly = 1) { errorCallback.accept(APIResponse.What3WordsError.BAD_WORDS) }
    }

    @Test
    fun `filters are set expect autosuggestBuilder filters to be called`() {
        val helper = AutosuggestHelper(api)
        val suggestionsJson =
            ClassLoader.getSystemResource("suggestions.json").readText()
        val suggestions =
            Gson().fromJson(suggestionsJson, Array<Suggestion>::class.java).toList()
        val autosuggest = mockk<Autosuggest>()
        val autosuggestRequestBuilder = mockk<AutosuggestRequest.Builder>()
        val focus = com.what3words.javawrapper.request.Coordinates(51.2, 0.234)
        val countries = listOf("GB", "FR")
        val boundingBox = BoundingBox(focus, focus)
        val polygon = listOf(focus, focus)

        every {
            autosuggest.isSuccessful
        } answers {
            true
        }

        every {
            autosuggest.suggestions
        } answers {
            suggestions
        }

        every {
            autosuggestRequestBuilder.focus(any())
        } answers {
            autosuggestRequestBuilder
        }

        every {
            autosuggestRequestBuilder.nFocusResults(any())
        } answers {
            autosuggestRequestBuilder
        }

        every {
            autosuggestRequestBuilder.nResults(any())
        } answers {
            autosuggestRequestBuilder
        }

        every {
            autosuggestRequestBuilder.clipToCircle(any(), any())
        } answers {
            autosuggestRequestBuilder
        }

        every {
            autosuggestRequestBuilder.clipToCountry(*countries.toTypedArray())
        } answers {
            autosuggestRequestBuilder
        }

        every {
            autosuggestRequestBuilder.clipToBoundingBox(boundingBox)
        } answers {
            autosuggestRequestBuilder
        }

        every {
            autosuggestRequestBuilder.clipToPolygon(*polygon.toTypedArray())
        } answers {
            autosuggestRequestBuilder
        }

        every {
            api.autosuggest("index.home.ra")
        } answers {
            autosuggestRequestBuilder
        }

        every {
            autosuggestRequestBuilder.execute()
        } answers {
            autosuggest
        }

        // when
        runBlocking {
            helper.focus(focus).nFocusResults(5).nResults(3).clipToCountry(countries).clipToCircle(
                focus
            ).clipToBoundingBox(BoundingBox(focus, focus)).clipToPolygon(listOf(focus, focus))

            helper.update("index", suggestionsCallback, errorCallback)
            helper.update("index.home", suggestionsCallback, errorCallback)
            helper.update("index.home.ra", suggestionsCallback, errorCallback)
            delay(500)
        }

        // then
        verify(exactly = 2) { suggestionsCallback.accept(emptyList()) }
        verify(exactly = 1) { suggestionsCallback.accept(suggestions) }
        verify(exactly = 1) { autosuggestRequestBuilder.focus(focus) }
        verify(exactly = 1) { autosuggestRequestBuilder.nResults(3) }
        verify(exactly = 1) { autosuggestRequestBuilder.nFocusResults(5) }
        verify(exactly = 1) { autosuggestRequestBuilder.clipToCountry(*countries.toTypedArray()) }
        verify(exactly = 1) { autosuggestRequestBuilder.clipToCircle(focus, 1.0) }
        verify(exactly = 1) { autosuggestRequestBuilder.clipToBoundingBox(boundingBox) }
        verify(exactly = 1) { autosuggestRequestBuilder.clipToPolygon(*polygon.toTypedArray()) }
        verify(exactly = 0) { errorCallback.accept(any()) }
    }
}
