package com.what3words.androidwrapper

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import com.google.common.io.BaseEncoding
import com.what3words.androidwrapper.helpers.DefaultDispatcherProvider
import com.what3words.androidwrapper.helpers.DispatcherProvider
import com.what3words.androidwrapper.voice.Microphone
import com.what3words.androidwrapper.voice.VoiceApi
import com.what3words.androidwrapper.voice.VoiceBuilder
import com.what3words.androidwrapper.voice.VoiceBuilderWithCoordinates
import com.what3words.androidwrapper.voice.VoiceProvider
import java.security.MessageDigest

interface What3WordsWrapper : com.what3words.javawrapper.What3WordsWrapper {
    fun autosuggest(
        microphone: Microphone,
        voiceLanguage: String
    ) : VoiceBuilder

    fun autosuggestWithCoordinates(
        microphone: Microphone,
        voiceLanguage: String
    ) : VoiceBuilderWithCoordinates

    fun getVoiceProvider() : VoiceProvider
}

class What3WordsV3 : com.what3words.javawrapper.What3WordsV3, What3WordsWrapper {
    internal var voiceApi: VoiceProvider
    internal var dispatchers: DispatcherProvider

    constructor(apiKey: String, context: Context) : super(
        apiKey,
        context.packageName,
        getSignature(context),
        null
    ) {
        dispatchers = DefaultDispatcherProvider()
        voiceApi = VoiceApi(apiKey)
    }

    internal constructor(
        apiKey: String,
        voiceApi: VoiceApi,
        dispatchers: DispatcherProvider = DefaultDispatcherProvider()
    ) : super(
        apiKey,
        "com.what3words.androidwrapper",
        "",
        emptyMap()
    ) {
        this.dispatchers = dispatchers
        this@What3WordsV3.voiceApi = voiceApi
    }

    constructor(apiKey: String, context: Context, headers: Map<String, String>) : super(
        apiKey,
        context.packageName,
        getSignature(context),
        headers
    ) {
        dispatchers = DefaultDispatcherProvider()
        voiceApi = VoiceApi(apiKey)
    }

    constructor(apiKey: String, endpoint: String, context: Context) : super(
        apiKey,
        endpoint,
        context.packageName,
        getSignature(context),
        null
    ) {
        dispatchers = DefaultDispatcherProvider()
        voiceApi = VoiceApi(apiKey)
    }

    constructor(apiKey: String, endpoint: String, voiceEndpoint: String, context: Context) : super(
        apiKey,
        endpoint,
        context.packageName,
        getSignature(context),
        null
    ) {
        dispatchers = DefaultDispatcherProvider()
        voiceApi = VoiceApi(apiKey, voiceEndpoint)
    }

    constructor(
        apiKey: String,
        endpoint: String,
        context: Context,
        headers: Map<String, String>
    ) : super(apiKey, endpoint, context.packageName, getSignature(context), headers) {
        dispatchers = DefaultDispatcherProvider()
        voiceApi = VoiceApi(apiKey)
    }

    constructor(
        apiKey: String,
        endpoint: String,
        voiceEndpoint: String,
        context: Context,
        headers: Map<String, String>
    ) : super(
        apiKey,
        endpoint,
        context.packageName,
        getSignature(context),
        headers
    ) {
        dispatchers = DefaultDispatcherProvider()
        voiceApi = VoiceApi(apiKey, voiceEndpoint)
    }

    constructor(
        apiKey: String,
        endpoint: String,
        voiceProvider: VoiceProvider,
        context: Context,
        headers: Map<String, String>
    ) : super(
        apiKey,
        endpoint,
        context.packageName,
        getSignature(context),
        headers
    ) {
        dispatchers = DefaultDispatcherProvider()
        voiceApi = voiceProvider
    }

    companion object {
        private fun getSignature(context: Context?): String? {
            return if (context == null) {
                null
            } else try {
                val packageManager: PackageManager = context.packageManager
                val packageName: String = context.packageName
                val packageInfo =
                    packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                if (packageInfo?.signatures == null || packageInfo.signatures.isEmpty() || packageInfo.signatures[0] == null
                ) {
                    null
                } else signatureDigest(packageInfo.signatures[0])
            } catch (e: Exception) {
                null
            }
        }

        private fun signatureDigest(sig: Signature?): String? {
            return if (sig == null) {
                null
            } else try {
                val signature: ByteArray = sig.toByteArray()
                val md: MessageDigest = MessageDigest.getInstance("SHA1")
                val digest: ByteArray = md.digest(signature)
                BaseEncoding.base16().lowerCase().encode(digest)
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * The what3words Voice API allows a user to say three words into any application or service, with it returning a list of suggested what3words addresses, through a single API call.
     * Utilising WebSockets for realtime audio steaming, and powered by the Speechmatics WebSocket Speech API, the fast and simple interface provides a powerful AutoSuggest function, which can validate and autocorrect user input and limit it to certain geographic areas.
     *
     * @param microphone with a [Microphone] where developer can subscribe to [Microphone.onListening] and get microphone volume while recording, allowing custom inputs too as recording rates and encodings.
     * @param voiceLanguage  request parameter is mandatory, and must always be specified. The language code provided is used to configure both the Speechmatics ASR, and the what3words AutoSuggest algorithm. Please provide one of the following voice-language codes: ar, cmn, de, en, es, hi, ja, ko.
     * @return a [VoiceBuilder] instance, use [VoiceBuilder.startListening] to start recording and sending voice data to our API.
     */
    override fun autosuggest(
        microphone: Microphone,
        voiceLanguage: String
    ): VoiceBuilder {
        return VoiceBuilder(this, microphone, voiceLanguage)
    }

    internal fun autosuggest(
        microphone: Microphone,
        voiceLanguage: String,
        dispatchers: DispatcherProvider = DefaultDispatcherProvider()
    ): VoiceBuilder {
        return VoiceBuilder(this, microphone, voiceLanguage, dispatchers)
    }

    /**
     * The what3words Voice API allows a user to say three words into any application or service, with it returning a list of suggested what3words addresses with coordinates, through a single API call.
     * Utilising WebSockets for realtime audio steaming, and powered by the Speechmatics WebSocket Speech API, the fast and simple interface provides a powerful AutoSuggest function, which can validate and autocorrect user input and limit it to certain geographic areas.
     *
     * @param microphone with a [Microphone] where developer can subscribe to [Microphone.onListening] and get microphone volume while recording, allowing custom inputs too as recording rates and encodings.
     * @param voiceLanguage request parameter is mandatory, and must always be specified. The language code provided is used to configure both the Speechmatics ASR, and the what3words AutoSuggest algorithm. Please provide one of the following voice-language codes: ar, cmn, de, en, es, hi, ja, ko.
     * @return a [VoiceBuilder] instance, use [VoiceBuilder.startListening] to start recording and sending voice data to our API.
     */
    override fun autosuggestWithCoordinates(
        microphone: Microphone,
        voiceLanguage: String
    ): VoiceBuilderWithCoordinates {
        return VoiceBuilderWithCoordinates(this, microphone, voiceLanguage, dispatchers)
    }

    override fun getVoiceProvider(): VoiceProvider {
        return voiceApi
    }
}
