package com.what3words.androidwrapper.datasource.text

import com.what3words.androidwrapper.common.Mapper
import com.what3words.androidwrapper.datasource.text.api.utils.executeApiRequestAndHandleResponse
import com.what3words.androidwrapper.datasource.text.api.What3WordsV3Service
import com.what3words.androidwrapper.datasource.text.api.di.MappersFactory
import com.what3words.androidwrapper.datasource.text.api.extensions.W3WDomainToApiStringExtensions.toAPIString
import com.what3words.androidwrapper.datasource.text.api.response.AutosuggestResponse
import com.what3words.androidwrapper.datasource.text.api.response.AvailableLanguagesResponse
import com.what3words.androidwrapper.datasource.text.api.response.ConvertTo3waResponse
import com.what3words.androidwrapper.datasource.text.api.response.ConvertToCoordinatesResponse
import com.what3words.androidwrapper.datasource.text.api.response.GridSectionResponse
import com.what3words.androidwrapper.datasource.text.api.retrofit.W3WV3RetrofitApiClient
import com.what3words.core.datasource.W3WTextDatasource
import com.what3words.core.types.common.W3WResult
import com.what3words.core.types.domain.W3WAddress
import com.what3words.core.types.domain.W3WSuggestion
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WGridSection
import com.what3words.core.types.geometry.W3WRectangle
import com.what3words.core.types.language.W3WLanguage
import com.what3words.core.types.language.W3WLanguageRCF5646
import com.what3words.core.types.options.W3WAutosuggestOptions

/**
 * Rest API implementation of the [com.what3words.core.datasource.W3WTextDatasource] interface.
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
class W3WApiTextDatasource internal constructor(
    private val what3WordsV3Service: What3WordsV3Service,
    private val convertTo3waDtoToDomainMapper: Mapper<ConvertTo3waResponse, W3WAddress>,
    private val convertToCoordinatesResponseMapper: Mapper<ConvertToCoordinatesResponse, W3WCoordinates>,
    private val autosuggestResponseMapper: Mapper<AutosuggestResponse, List<W3WSuggestion>>,
    private val availableLanguagesResponseMapper: Mapper<AvailableLanguagesResponse, Set<W3WLanguage>>,
    private val gridSectionResponseMapper: Mapper<GridSectionResponse, W3WGridSection>
) : W3WTextDatasource {

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
        return executeApiRequestAndHandleResponse(convertTo3waDtoToDomainMapper) {
            what3WordsV3Service.convertTo3wa(
                coordinates = coordinates.toAPIString(),
                language = language.code,
                locale = language.locale
            )
        }
    }

    /**
     * Converts a latitude and longitude to a 3 word address.
     * Additionally provides country information, grid square bounds, nearest place, and a map link.
     *
     * **This is a blocking I/O method and should only be called from a background thread.**
     *
     * @param coordinates The latitude and longitude of the location to convert to a 3 word address.
     * @param language The language in which the 3 word address should be provided, specified by RFC 5646.
     * @return A [W3WResult] instance containing the what3words address.
     */
    @Throws(InterruptedException::class)
    override fun convertTo3wa(
        coordinates: W3WCoordinates, language: W3WLanguageRCF5646
    ): W3WResult<W3WAddress> {
        return executeApiRequestAndHandleResponse(convertTo3waDtoToDomainMapper) {
            what3WordsV3Service.convertTo3wa(
                coordinates = coordinates.toAPIString(),
                language = language.getLanguageCode(),
                locale = language.getRegionCode()
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
    override fun convertToCoordinates(words: String): W3WResult<W3WCoordinates> {
        return executeApiRequestAndHandleResponse(convertToCoordinatesResponseMapper) {
            what3WordsV3Service.convertToCoordinates(
                address = words
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
        return executeApiRequestAndHandleResponse(autosuggestResponseMapper) {
            val requestLanguage: String? =
                options?.language?.code ?: options?.languageRCF5646?.getLanguageCode()
            val requestLocale: String? =
                options?.language?.locale ?: options?.languageRCF5646?.getRegionCode()

            if (options?.includeCoordinates == true) {
                what3WordsV3Service.autosuggestWithCoordinates(
                    input = input,
                    nResults = options.nResults.toString(),
                    focus = options.focus?.toString(),
                    nFocusResults = options.nFocusResults?.toString(),
                    clipToCountry = options.clipToCountry.toAPIString(),
                    clipToBoundingBox = options.clipToBoundingBox?.toAPIString(),
                    clipToCircle = options.clipToCircle?.toAPIString(),
                    clipToPolygon = options.clipToPolygon?.toAPIString(),
                    inputType = options.inputType?.value,
                    lang = requestLanguage,
                    locale = requestLocale,
                    preferLand = options.preferLand.toString()
                )
            } else {
                what3WordsV3Service.autosuggest(
                    input = input,
                    nResults = options?.nResults?.toString(),
                    focus = options?.focus?.toString(),
                    nFocusResults = options?.nFocusResults?.toString(),
                    clipToCountry = options?.clipToCountry?.toAPIString(),
                    clipToBoundingBox = options?.clipToBoundingBox?.toAPIString(),
                    clipToCircle = options?.clipToCircle?.toAPIString(),
                    clipToPolygon = options?.clipToPolygon?.toAPIString(),
                    inputType = options?.inputType?.value,
                    lang = requestLanguage,
                    locale = requestLocale,
                    preferLand = options?.preferLand?.toString()
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
        return executeApiRequestAndHandleResponse(gridSectionResponseMapper) {
            what3WordsV3Service.gridSection(
                bbox = boundingBox.toAPIString()
            )
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
    override fun availableLanguages(): W3WResult<Set<W3WLanguage>> {
        return executeApiRequestAndHandleResponse(availableLanguagesResponseMapper) {
            what3WordsV3Service.availableLanguages()
        }
    }

    companion object {
        /**
         * Creates a new [W3WApiTextDatasource] instance.
         *
         * @param apiKey Your what3words API key obtained from https://accounts.what3words.com
         * @param endPoint Override the default public API endpoint.
         * @param packageName For use within Android applications to provide the application package name as part of API key restriction.
         * @param signature For use within Android applications to provide the application SHA1 signature as part of API key restriction.
         * @param headers Add any custom HTTP headers to send in each request.
         */
        fun create(
            apiKey: String,
            endPoint: String? = null,
            packageName: String? = null,
            signature: String? = null,
            headers: Map<String, String> = mapOf()
        ): W3WApiTextDatasource {
            return W3WApiTextDatasource(
                what3WordsV3Service = W3WV3RetrofitApiClient.createW3WV3Service(
                    apiKey, endPoint, packageName, signature, headers
                ),
                convertTo3waDtoToDomainMapper = MappersFactory.providesConvertTo3waDtoToDomainMapper(),
                convertToCoordinatesResponseMapper = MappersFactory.providesConvertToCoordinatesResponseMapper(),
                autosuggestResponseMapper = MappersFactory.providesAutosuggestResponseMapper(),
                availableLanguagesResponseMapper = MappersFactory.providesAvailableLanguagesResponseMapper(),
                gridSectionResponseMapper = MappersFactory.providesGridSectionResponseMapper()
            )
        }
    }
}