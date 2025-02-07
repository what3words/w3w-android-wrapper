package com.what3words.androidwrapper.datasource.text

import android.content.Context
import com.what3words.androidwrapper.BuildConfig
import com.what3words.androidwrapper.common.Mapper
import com.what3words.androidwrapper.common.extensions.W3WDomainToApiStringExtensions.toAPIString
import com.what3words.androidwrapper.common.extensions.W3WDomainToApiStringExtensions.toQueryMap
import com.what3words.androidwrapper.datasource.text.api.What3WordsV3Service
import com.what3words.androidwrapper.datasource.text.api.di.MappersFactory
import com.what3words.androidwrapper.datasource.text.api.response.AutosuggestResponse
import com.what3words.androidwrapper.datasource.text.api.response.AvailableLanguagesResponse
import com.what3words.androidwrapper.datasource.text.api.response.ConvertTo3waResponse
import com.what3words.androidwrapper.datasource.text.api.response.ConvertToCoordinatesResponse
import com.what3words.androidwrapper.datasource.text.api.response.GridSectionResponse
import com.what3words.androidwrapper.datasource.text.api.retrofit.W3WV3RetrofitApiClient
import com.what3words.androidwrapper.datasource.text.api.retrofit.W3WV3RetrofitApiClient.executeApiRequestAndHandleResponse
import com.what3words.androidwrapper.helpers.PackageManagerHelper.getPackageSignature
import com.what3words.core.datasource.text.W3WTextDataSource
import com.what3words.core.types.common.W3WResult
import com.what3words.core.types.domain.W3WAddress
import com.what3words.core.types.domain.W3WSuggestion
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WGridSection
import com.what3words.core.types.geometry.W3WRectangle
import com.what3words.core.types.language.W3WLanguage
import com.what3words.core.types.language.W3WProprietaryLanguage
import com.what3words.core.types.options.W3WAutosuggestOptions
import com.what3words.javawrapper.What3WordsV3
import com.what3words.javawrapper.request.SourceApi
import org.jetbrains.annotations.ApiStatus.*

/**
 * Rest API implementation of the [com.what3words.core.datasource.text.W3WTextDataSource] interface.
 * All functions in this class are blocking, therefore should be called from a background thread.
 * Clients must declare necessary internet permissions in their AndroidManifest files before invoking any function in this class.
 *
 * @property what3WordsV3Service The Retrofit service interface for making API requests.
 * @property convertTo3waDtoToDomainMapper Mapper for converting ConvertTo3waResponse DTOs to domain objects.
 * @property convertToCoordinatesResponseMapper Mapper for converting ConvertToCoordinatesResponse DTOs to domain objects.
 * @property autosuggestResponseMapper Mapper for converting AutosuggestResponse DTOs to domain objects.
 * @property availableLanguagesResponseMapper Mapper for converting AvailableLanguagesResponse DTOs to domain objects.
 * @property gridSectionResponseMapper Mapper for converting GridSectionResponse DTOs to domain objects.
 */
class W3WApiTextDataSource internal constructor(
    private val what3WordsV3Service: What3WordsV3Service,
    private val convertTo3waDtoToDomainMapper: Mapper<ConvertTo3waResponse, W3WAddress>,
    private val convertToCoordinatesResponseMapper: Mapper<ConvertToCoordinatesResponse, W3WAddress>,
    private val autosuggestResponseMapper: Mapper<AutosuggestResponse, List<W3WSuggestion>>,
    private val availableLanguagesResponseMapper: Mapper<AvailableLanguagesResponse, Set<W3WProprietaryLanguage>>,
    private val gridSectionResponseMapper: Mapper<GridSectionResponse, W3WGridSection>
) : W3WTextDataSource {

    /**
     * Converts a latitude and longitude to a 3 word address.
     * Additionally provides country information, grid square bounds, nearest place, and a map link.
     *
     * **This is a blocking I/O method and should only be called from a background thread.**
     *
     * @param coordinates The latitude and longitude of the location to convert to a 3 word address.
     * @param language The language in which the 3 word address should be provided.
     * @return A [W3WResult] instance containing the what3words address.
     */
    @Throws(InterruptedException::class)
    override fun convertTo3wa(
        coordinates: W3WCoordinates, language: W3WLanguage
    ): W3WResult<W3WAddress> {
        return convertTo3wa(coordinates, language, emptyMap())
    }

    /**
     * Converts a latitude and longitude to a 3 word address with specified headers.
     * Additionally provides country information, grid square bounds, nearest place, and a map link.
     *
     * **This is a blocking I/O method and should only be called from a background thread.**
     *
     * @param coordinates The latitude and longitude of the location to convert to a 3 word address.
     * @param language The language in which the 3 word address should be provided.
     * @param headers Additional headers to be included in the request.
     * @return A [W3WResult] instance containing the what3words address.
     */
    fun convertTo3wa(
        coordinates: W3WCoordinates, language: W3WLanguage,
        headers: Map<String, String>
    ): W3WResult<W3WAddress> {
        return executeApiRequestAndHandleResponse(convertTo3waDtoToDomainMapper) {
            what3WordsV3Service.convertTo3wa(
                coordinates = coordinates.toAPIString(),
                language = language.w3wLocale ?: language.w3wCode,
                headers = headers
            )
        }
    }

    /**
     * Converts a 3 word address to coordinates.
     *
     * **This is a blocking I/O method and should only be called from a background thread.**
     *
     * @param words A 3 word address as a string.
     * @return A [W3WResult] instance containing the what3words coordinates.
     */
    @Throws(InterruptedException::class)
    override fun convertToCoordinates(words: String): W3WResult<W3WAddress> {
        return convertToCoordinates(words = words, headers = emptyMap())
    }

    /**
     * Converts a 3 word address to coordinates with specified headers.
     *
     * **This is a blocking I/O method and should only be called from a background thread.**
     *
     * @param words A 3 word address as a string.
     * @param headers Additional headers to be included in the request.
     * @return A [W3WResult] instance containing the what3words coordinates.
     */
    fun convertToCoordinates(
        words: String,        
        headers: Map<String, String>
    ): W3WResult<W3WAddress> {
        return executeApiRequestAndHandleResponse(convertToCoordinatesResponseMapper) {
            what3WordsV3Service.convertToCoordinates(
                address = words,
                headers = headers
            )
        }
    }

    /**
     * [autosuggest] can take a slightly incorrect 3 word address and suggest a list of valid 3 word addresses.
     * It can optionally limit results to a country or area and prefer results near the user.
     *
     * **This is a blocking I/O method and should only be called from a background thread.**
     *
     * @param input The full or partial 3 word address to obtain suggestions for.
     * @param options Additional options for auto suggestion.
     * @return A [W3WResult] instance containing a list of what3words address suggestions.
     */
    override fun autosuggest(
        input: String, options: W3WAutosuggestOptions?
    ): W3WResult<List<W3WSuggestion>> {
        return autosuggest(input, options, emptyMap())
    }

    /**
     * [autosuggest] can take a slightly incorrect 3 word address and suggest a list of valid 3 word addresses.
     * It can optionally limit results to a country or area and prefer results near the user.
     *
     * **This is a blocking I/O method and should only be called from a background thread.**
     *
     * @param input The full or partial 3 word address to obtain suggestions for.
     * @param options Additional options for auto suggestion.
     * @param headers Additional headers to be included in the request.
     * @return A [W3WResult] instance containing a list of what3words address suggestions.
     */
    fun autosuggest(
        input: String, options: W3WAutosuggestOptions?,
        headers: Map<String, String>
    ): W3WResult<List<W3WSuggestion>> {
        return executeApiRequestAndHandleResponse(autosuggestResponseMapper) {
            if (options?.includeCoordinates == true) {
                what3WordsV3Service.autosuggestWithCoordinates(
                    input = input,
                    options = options.toQueryMap(),
                    headers = headers
                )
            } else {
                what3WordsV3Service.autosuggest(
                    input = input,
                    options = options?.toQueryMap() ?: emptyMap(),
                    headers = headers
                )
            }
        }
    }

    /**
     * Returns a section of the what3words grid for a bounding box.
     *
     * **This is a blocking I/O method and should only be called from a background thread.**
     *
     * @param boundingBox The bounding box for which the grid section should be returned.
     * @return A [W3WResult] instance representing the requested what3words grid section.
     */
    @Throws(InterruptedException::class)
    override fun gridSection(boundingBox: W3WRectangle): W3WResult<W3WGridSection> {
        return gridSection(boundingBox, emptyMap())
    }

    /**
     * Returns a section of the what3words grid for a bounding box with specified headers.
     *
     * **This is a blocking I/O method and should only be called from a background thread.**
     *
     * @param boundingBox The bounding box for which the grid section should be returned.
     * @param headers Additional headers to be included in the request.
     * @return A [W3WResult] instance representing the requested what3words grid section.
     */
    fun gridSection(
        boundingBox: W3WRectangle,
        headers: Map<String, String>
    ): W3WResult<W3WGridSection> {
        return executeApiRequestAndHandleResponse(gridSectionResponseMapper) {
            what3WordsV3Service.gridSection(
                bbox = boundingBox.toAPIString(),
                headers = headers
            )
        }
    }

    /**
     * Checks if a given what3words address is valid.
     *
     * **This is a blocking I/O method and should only be called from a background thread.**
     *
     * @param words The what3words address to validate.
     * @return A [W3WResult] instance containing a boolean value indicating whether the what3words address is valid.
     */
    @Throws(InterruptedException::class)
    override fun isValid3wa(words: String): W3WResult<Boolean> {
        return isValid3wa(words, emptyMap())
    }

    /**
     * Checks if a given what3words address is valid.
     *
     * **This is a blocking I/O method and should only be called from a background thread.**
     *
     * @param words The what3words address to validate.
     * @param headers Additional headers to be included in the request.
     * @return A [W3WResult] instance containing a boolean value indicating whether the what3words address is valid.
     */
    fun isValid3wa(
        words: String,
        headers: Map<String, String>
    ): W3WResult<Boolean> {
        if (!What3WordsV3.isPossible3wa(words)) {
            return W3WResult.Success(false)
        }
        val response = executeApiRequestAndHandleResponse(autosuggestResponseMapper) {
            what3WordsV3Service.autosuggest(
                words,
                headers
            )
        }
        return when (response) {
            is W3WResult.Success -> W3WResult.Success(response.value.any {
                it.w3wAddress.words.replace("/", "")
                    .equals(words.replace("/", ""), ignoreCase = true)
            })

            is W3WResult.Failure -> W3WResult.Failure(response.error)
        }
    }

    override fun version(version: W3WTextDataSource.Version): String? {
        return when (version) {
            W3WTextDataSource.Version.Library -> BuildConfig.LIBRARY_VERSION
            W3WTextDataSource.Version.DataSource -> BuildConfig.TEXT_API_VERSION
            W3WTextDataSource.Version.Data -> null
        }
    }

    /**
     * Retrieves a set of all available what3words languages.
     *
     * **This is a blocking I/O method and should only be called from a background thread.**
     *
     * @return A [W3WResult] instance containing a set of available what3words languages.
     */
    @Throws(InterruptedException::class)
    override fun availableLanguages(): W3WResult<Set<W3WProprietaryLanguage>> {
        return availableLanguages(emptyMap())
    }

    /**
     * Retrieves a set of all available what3words languages with specified headers.
     *
     * **This is a blocking I/O method and should only be called from a background thread.**
     *
     * @param headers Additional headers to be included in the request.
     * @return A [W3WResult] instance containing a set of available what3words languages.
     */
    fun availableLanguages(headers: Map<String, String>): W3WResult<Set<W3WProprietaryLanguage>> {
        return executeApiRequestAndHandleResponse(availableLanguagesResponseMapper) {
            what3WordsV3Service.availableLanguages(headers)
        }
    }

    internal fun autosuggestionSelection(
        rawInput: String,
        selection: String,
        rank: Int,
        sourceApi: SourceApi,
        options: W3WAutosuggestOptions? = null
    ) {
        return what3WordsV3Service.autosuggestSelection(
            rawInput,
            selection,
            rank.toString(),
            sourceApi.toString().lowercase(),
            options?.toQueryMap() ?: emptyMap()
        )
    }


    companion object {
        /**
         * Creates a new [W3WApiTextDataSource] instance.
         *
         * If you want to restrict the API key usage to just your application only, ensure that the provided [apiKey] has API restrictions enabled.
         * For detailed instructions, refer to the API Key Restriction section of your [what3words developer account](https://accounts.what3words.com/overview).
         *
         * @param context The context of the application.
         * @param apiKey Your what3words API key obtained from your [what3words developer account](https://accounts.what3words.com).
         * @param endPoint (Optional) Override the default public API endpoint.
         * @param headers (Optional) Additional custom HTTP headers to include in each request.
         * @return A new instance of [W3WApiTextDataSource].
         */
        @JvmStatic
        fun create(
            context: Context,
            apiKey: String,
            endPoint: String? = null,
            headers: Map<String, String> = mapOf()
        ): W3WApiTextDataSource {
            return create(
                what3WordsV3Service = W3WV3RetrofitApiClient.createW3WV3Service(
                    apiKey, endPoint, context.packageName, context.getPackageSignature(), headers
                )
            )
        }

        /**
         * Creates a new [W3WApiTextDataSource] instance for internal purposes only.
         *
         * @param apiKey Your what3words API key obtained from your [what3words developer account](https://accounts.what3words.com).
         * @param endPoint (Optional) Override the default public API endpoint.
         * @param headers (Optional) Additional custom HTTP headers to include in each request.
         * @return A new instance of [W3WApiTextDataSource].
         */
        @Internal
        internal fun create(
            apiKey: String,
            endPoint: String? = null,
            headers: Map<String, String> = mapOf()
        ): W3WApiTextDataSource {
            return create(
                what3WordsV3Service = W3WV3RetrofitApiClient.createW3WV3Service(
                    apiKey, endPoint, null, null, headers
                )
            )
        }

        /**
         * Creates a new instance of [W3WApiTextDataSource] using the provided [What3WordsV3Service].
         *
         * @param what3WordsV3Service The What3WordsV3Service instance used for API communication.
         * @return A new instance of W3WApiTextDataSource.
         */
        private fun create(
            what3WordsV3Service: What3WordsV3Service
        ): W3WApiTextDataSource {
            return W3WApiTextDataSource(
                what3WordsV3Service = what3WordsV3Service,
                convertTo3waDtoToDomainMapper = MappersFactory.providesConvertTo3waDtoToDomainMapper(),
                convertToCoordinatesResponseMapper = MappersFactory.providesConvertToCoordinatesResponseMapper(),
                autosuggestResponseMapper = MappersFactory.providesAutosuggestResponseMapper(),
                availableLanguagesResponseMapper = MappersFactory.providesAvailableLanguagesResponseMapper(),
                gridSectionResponseMapper = MappersFactory.providesGridSectionResponseMapper()
            )
        }
    }
}
