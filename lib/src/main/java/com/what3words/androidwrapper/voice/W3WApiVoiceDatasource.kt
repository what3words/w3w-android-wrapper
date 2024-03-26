package com.what3words.androidwrapper.voice

import com.what3words.androidwrapper.voice.di.MapperFactory
import com.what3words.androidwrapper.voice.mappers.SuggestionWithCoordinatesMapper
import com.what3words.androidwrapper.voice.client.W3WVoiceClient
import com.what3words.core.datasource.voice.W3WVoiceDataSource
import com.what3words.core.datasource.voice.audiostream.W3WAudioStream
import com.what3words.core.types.common.W3WResult
import com.what3words.core.types.domain.W3WSuggestion
import com.what3words.core.types.language.W3WLanguage
import com.what3words.core.types.options.W3WAutosuggestOptions

class W3WApiVoiceDatasource internal constructor(
    private val client: W3WVoiceClient,
    private val suggestionWithCoordinatesMapper: SuggestionWithCoordinatesMapper
) : W3WVoiceDataSource {

    override fun autosuggest(
        input: W3WAudioStream,
        voiceLanguage: W3WLanguage,
        options: W3WAutosuggestOptions?,
        onSpeechDetected: ((String) -> Unit)?,
        onResult: (result: W3WResult<List<W3WSuggestion>>) -> Unit
    ) {
        client.initialize(voiceLanguage.w3wCode, options, input)
            .openWebSocketAndStartRecognition { status ->
                when (status) {
                    is W3WVoiceClient.Status.Suggestions -> {
                        val suggestions = status.suggestions.map {
                            suggestionWithCoordinatesMapper.mapFrom(it)
                        }
                        onResult(W3WResult.Success(suggestions))
                    }

                    is W3WVoiceClient.Status.Error -> {
                        val voiceError = status.error
                        onResult(W3WResult.Failure(voiceError.message, voiceError))
                    }
                }
            }
    }

    override fun terminate() {
        client.close("Terminated by user")
    }

    companion object {
        fun create(
            apiKey: String,
            endPoint: String? = null,
        ): W3WApiVoiceDatasource {
            return W3WApiVoiceDatasource(
                W3WVoiceClient(apiKey, endPoint),
                MapperFactory.provideSuggestionWithCoordinatesMapper()
            )
        }
    }
}