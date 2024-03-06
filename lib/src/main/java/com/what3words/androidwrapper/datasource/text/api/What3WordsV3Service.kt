package com.what3words.androidwrapper.datasource.text.api

import com.what3words.androidwrapper.datasource.text.api.response.AutosuggestResponse
import com.what3words.androidwrapper.datasource.text.api.response.AvailableLanguagesResponse
import com.what3words.androidwrapper.datasource.text.api.response.ConvertTo3waResponse
import com.what3words.androidwrapper.datasource.text.api.response.ConvertToCoordinatesResponse
import com.what3words.androidwrapper.datasource.text.api.response.GridSectionResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Query

internal interface What3WordsV3Service {
    @GET("convert-to-3wa")
    suspend fun convertTo3wa(
        @Query("coordinates") coordinates: String?,
        @Query("language") language: String?,
        @Query("locale") locale: String?,
        @HeaderMap headers: Map<String, String> = emptyMap()
    ): Response<ConvertTo3waResponse>

    @GET("convert-to-coordinates")
    suspend fun convertToCoordinates(
        @Query("words") address: String?,
        @HeaderMap headers: Map<String, String> = emptyMap()
    ): Response<ConvertToCoordinatesResponse>

    @GET("autosuggest")
    suspend fun autosuggest(
        @Query("input") input: String?,
        @Query("n-results") nResults: String?,
        @Query("focus") focus: String?,
        @Query("n-focus-results") nFocusResults: String?,
        @Query("clip-to-country") clipToCountry: String?,
        @Query("clip-to-bounding-box") clipToBoundingBox: String?,
        @Query("clip-to-circle") clipToCircle: String?,
        @Query("clip-to-polygon") clipToPolygon: String?,
        @Query("input-type") inputType: String?,
        @Query("language") lang: String?,
        @Query("locale") locale: String?,
        @Query("prefer-land") preferLand: String?,
        @HeaderMap headers: Map<String, String> = emptyMap()
    ): Response<AutosuggestResponse>

    @GET("autosuggest-with-coordinates")
    suspend fun autosuggestWithCoordinates(
        @Query("input") input: String?,
        @Query("n-results") nResults: String?,
        @Query("focus") focus: String?,
        @Query("n-focus-results") nFocusResults: String?,
        @Query("clip-to-country") clipToCountry: String?,
        @Query("clip-to-bounding-box") clipToBoundingBox: String?,
        @Query("clip-to-circle") clipToCircle: String?,
        @Query("clip-to-polygon") clipToPolygon: String?,
        @Query("input-type") inputType: String?,
        @Query("language") lang: String?,
        @Query("locale") locale: String?,
        @Query("prefer-land") preferLand: String?,
        @HeaderMap headers: Map<String, String> = emptyMap()
    ): Response<AutosuggestResponse>

    @GET("grid-section")
    suspend fun gridSection(
        @Query("bounding-box") bbox: String?,
        @HeaderMap headers: Map<String, String> = emptyMap()
    ): Response<GridSectionResponse>


    @GET("available-languages")
    suspend fun availableLanguages(
        @HeaderMap headers: Map<String, String> = emptyMap()
    ): Response<AvailableLanguagesResponse>
}