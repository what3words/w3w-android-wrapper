package com.what3words.androidwrapper.datasource.voice

import com.what3words.androidwrapper.BuildConfig
import com.what3words.androidwrapper.datasource.voice.W3WApiVoiceDataSource.Companion.create
import com.what3words.androidwrapper.datasource.voice.client.W3WVoiceClient
import com.what3words.androidwrapper.datasource.voice.di.MapperFactory
import com.what3words.androidwrapper.datasource.voice.mappers.SuggestionWithCoordinatesMapper
import com.what3words.core.datasource.voice.W3WVoiceDataSource
import com.what3words.core.datasource.voice.audiostream.W3WAudioStream
import com.what3words.core.types.common.W3WResult
import com.what3words.core.types.domain.W3WSuggestion
import com.what3words.core.types.language.W3WLanguage
import com.what3words.core.types.language.W3WRFC5646Language
import com.what3words.core.types.options.W3WAutosuggestOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Websocket implementation of the [com.what3words.core.datasource.voice.W3WVoiceDataSource] interface.
 * Clients must declare necessary RECORD_AUDIO permissions in their AndroidManifest files before
 * invoking any function in this class.
 *
 * Use the factory method [create] to create a new instance of this class.
 *
 * @property client Client to interact with the what3words Voice API
 * @property suggestionWithCoordinatesMapper Mapper to map SuggestionWithCoordinates to domain object
 */
class W3WApiVoiceDataSource internal constructor(
    private val client: W3WVoiceClient,
    private val suggestionWithCoordinatesMapper: SuggestionWithCoordinatesMapper
) : W3WVoiceDataSource {

    /**
     * Set of languages supported by the what3words Voice API. Each language code is followed by its corresponding English name for clarity and reference.
     * */
    private val supportedLanguages = setOf(
        W3WRFC5646Language.AR, // AR: Arabic
        W3WRFC5646Language.ZH_HANS, // ZH_HANS: Chinese (Simplified)
        W3WRFC5646Language.DE, // DE: German
        W3WRFC5646Language.EN_CA, // EN_CA: English (Canada)
        W3WRFC5646Language.EN_AU, // EN_AU: English (Australia)
        W3WRFC5646Language.EN_GB, // EN_GB: English (United Kingdom)
        W3WRFC5646Language.EN_IN, // EN_IN: English (India)
        W3WRFC5646Language.EN_US, // EN_US: English (United States)
        W3WRFC5646Language.ES_MX, // ES_MX: Spanish (Mexico)
        W3WRFC5646Language.ES_ES, // ES_ES: Spanish (Spain)
        W3WRFC5646Language.HI, // HI: Hindi
        W3WRFC5646Language.JA, // JA: Japanese
        W3WRFC5646Language.KO // KO: Korean
    )

    override fun autosuggest(
        input: W3WAudioStream,
        voiceLanguage: W3WLanguage,
        options: W3WAutosuggestOptions?,
        onRawResult: ((String) -> Unit)?,
        onResult: (result: W3WResult<List<W3WSuggestion>>) -> Unit
    ) {
        client.initialize(voiceLanguage, options, input)
            .openWebSocketAndStartRecognition { status ->
                CoroutineScope(Dispatchers.Main).launch {
                    when (status) {
                        is W3WVoiceClient.RecognitionStatus.Suggestions -> {
                            val suggestions = status.suggestions.map {
                                suggestionWithCoordinatesMapper.mapFrom(it)
                            }
                            onResult(W3WResult.Success(suggestions))
                        }

                        is W3WVoiceClient.RecognitionStatus.Error -> {
                            val voiceError = status.error
                            onResult(W3WResult.Failure(voiceError, voiceError.message))
                        }
                    }
                }
            }
    }

    override fun terminate() {
        client.close("Terminated by user")
    }

    override fun version(version: W3WVoiceDataSource.Version): String {
        return when (version) {
            W3WVoiceDataSource.Version.Library -> BuildConfig.VERSION_NAME
            W3WVoiceDataSource.Version.DataSource -> BuildConfig.VOICE_API_VERSION
        }
    }

    override fun availableLanguages(): Set<W3WRFC5646Language> {
        return supportedLanguages
    }

    companion object {
        /**
         * Factory method to create a new instance of [W3WApiVoiceDataSource].
         *
         * @param apiKey Your what3words API key obtained from https://accounts.what3words.com
         * @param endPoint Override the default public API endpoint.
         * @return A new instance of [W3WApiVoiceDataSource].
         */
        @JvmStatic
        fun create(
            apiKey: String,
            endPoint: String? = null,
        ): W3WApiVoiceDataSource {
            return W3WApiVoiceDataSource(
                W3WVoiceClient(apiKey, endPoint),
                MapperFactory.provideSuggestionWithCoordinatesMapper()
            )
        }
    }
}