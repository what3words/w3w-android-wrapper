package com.what3words.androidwrapper

import com.what3words.androidwrapper.voice.Microphone
import com.what3words.androidwrapper.voice.VoiceBuilder
import com.what3words.androidwrapper.voice.VoiceBuilderWithCoordinates
import com.what3words.javawrapper.request.AutosuggestRequest
import com.what3words.javawrapper.request.AutosuggestSelectionRequest
import com.what3words.javawrapper.request.AutosuggestWithCoordinatesRequest
import com.what3words.javawrapper.request.AvailableLanguagesRequest
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.ConvertTo3WARequest
import com.what3words.javawrapper.request.ConvertToCoordinatesRequest
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.request.GridSectionRequest
import com.what3words.javawrapper.request.SourceApi
import com.what3words.javawrapper.services.What3WordsV3Service

interface IWhat3WordsV3 {
    fun convertTo3wa(coordinates: Coordinates): ConvertTo3WARequest.Builder
    fun convertToCoordinates(words: String): ConvertToCoordinatesRequest.Builder
    fun autosuggest(input: String): AutosuggestRequest.Builder
    fun autosuggest(
        microphone: Microphone,
        voiceLanguage: String
    ): VoiceBuilder
    fun autosuggestWithCoordinates(input: String): AutosuggestWithCoordinatesRequest.Builder
    fun autosuggestWithCoordinates(
        microphone: Microphone,
        voiceLanguage: String
    ): VoiceBuilderWithCoordinates
    fun gridSection(boundingBox: BoundingBox): GridSectionRequest.Builder
    fun autosuggestionSelection(
        rawInput: String,
        selection: String,
        rank: Int,
        sourceApi: SourceApi
    ): AutosuggestSelectionRequest.Builder
    fun availableLanguages(): AvailableLanguagesRequest.Builder
    fun what3words(): What3WordsV3Service
}