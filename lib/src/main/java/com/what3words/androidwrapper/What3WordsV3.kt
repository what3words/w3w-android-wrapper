package com.what3words.androidwrapper

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.androidwrapper.helpers.AutosuggestHelper
import com.what3words.androidwrapper.helpers.DefaultDispatcherProvider
import com.what3words.androidwrapper.helpers.DispatcherProvider
import com.what3words.androidwrapper.helpers.IAutosuggestHelper
import com.what3words.androidwrapper.helpers.PackageManagerHelper
import com.what3words.androidwrapper.helpers.PackageManagerHelper.getPackageInfoCompat
import com.what3words.androidwrapper.helpers.PackageManagerHelper.getSignaturesCompat
import com.what3words.androidwrapper.voice.Microphone
import com.what3words.androidwrapper.voice.VoiceApi
import com.what3words.androidwrapper.voice.VoiceBuilder
import com.what3words.androidwrapper.voice.VoiceBuilderWithCoordinates
import com.what3words.androidwrapper.voice.VoiceProvider
import java.security.MessageDigest

interface What3WordsAndroidWrapper : com.what3words.javawrapper.What3WordsJavaWrapper {
    fun autosuggest(
        microphone: Microphone,
        voiceLanguage: String
    ): VoiceBuilder
    fun autosuggestWithCoordinates(
        microphone: Microphone,
        voiceLanguage: String
    ): VoiceBuilderWithCoordinates

    val voiceProvider: VoiceProvider
    val helper: IAutosuggestHelper
    val dataProvider: DataProvider

    enum class DataProvider {
        API,
        SDK
    }
}

class What3WordsV3 : com.what3words.javawrapper.What3WordsV3, What3WordsAndroidWrapper {
    override val voiceProvider: VoiceProvider
    override val helper: IAutosuggestHelper
    override val dataProvider: What3WordsAndroidWrapper.DataProvider
        get() = What3WordsAndroidWrapper.DataProvider.API

    internal var dispatchers: DispatcherProvider

    constructor(apiKey: String, context: Context) : super(
        apiKey,
        context.packageName,
        getSignature(context),
        null
    ) {
        dispatchers = DefaultDispatcherProvider()
        voiceProvider = VoiceApi(apiKey)
        helper = AutosuggestHelper(W3WApiTextDataSource.create(context, apiKey), dispatchers)
    }

    constructor(
        apiKey: String,
        voiceProvider: VoiceProvider,
        context: Context,
        headers: Map<String, String> = emptyMap()
    ) : super(
        apiKey,
        context.packageName,
        getSignature(context),
        headers
    ) {
        dispatchers = DefaultDispatcherProvider()
        this.voiceProvider = voiceProvider
        helper = AutosuggestHelper(W3WApiTextDataSource.create(context, apiKey), dispatchers)
    }

    internal constructor(
        apiKey: String,
        voiceProvider: VoiceProvider,
        dispatchers: DispatcherProvider = DefaultDispatcherProvider()
    ) : super(
        apiKey,
        "com.what3words.androidwrapper",
        "",
        emptyMap()
    ) {
        this.dispatchers = dispatchers
        this.voiceProvider = voiceProvider
        helper = AutosuggestHelper(W3WApiTextDataSource.create(apiKey), dispatchers)
    }

    constructor(apiKey: String, context: Context, headers: Map<String, String>) : super(
        apiKey,
        context.packageName,
        getSignature(context),
        headers
    ) {
        dispatchers = DefaultDispatcherProvider()
        voiceProvider = VoiceApi(apiKey)
        helper = AutosuggestHelper(W3WApiTextDataSource.create(context, apiKey), dispatchers)
    }

    constructor(apiKey: String, endpoint: String, context: Context) : super(
        apiKey,
        endpoint,
        context.packageName,
        getSignature(context),
        null
    ) {
        dispatchers = DefaultDispatcherProvider()
        voiceProvider = VoiceApi(apiKey)
        helper = AutosuggestHelper(W3WApiTextDataSource.create(context, apiKey), dispatchers)
    }

    constructor(apiKey: String, endpoint: String, voiceEndpoint: String, context: Context) : super(
        apiKey,
        endpoint,
        context.packageName,
        getSignature(context),
        null
    ) {
        dispatchers = DefaultDispatcherProvider()
        voiceProvider = VoiceApi(apiKey, voiceEndpoint)
        helper = AutosuggestHelper(W3WApiTextDataSource.create(context, apiKey), dispatchers)
    }

    constructor(
        apiKey: String,
        endpoint: String,
        context: Context,
        headers: Map<String, String>
    ) : super(apiKey, endpoint, context.packageName, getSignature(context), headers) {
        dispatchers = DefaultDispatcherProvider()
        voiceProvider = VoiceApi(apiKey)
        helper = AutosuggestHelper(W3WApiTextDataSource.create(context, apiKey), dispatchers)
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
        voiceProvider = VoiceApi(apiKey, voiceEndpoint)
        helper = AutosuggestHelper(W3WApiTextDataSource.create(context, apiKey), dispatchers)
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
        this.voiceProvider = voiceProvider
        helper = AutosuggestHelper(W3WApiTextDataSource.create(context, apiKey), dispatchers)
    }

    companion object {
        private fun getSignature(context: Context?): String? {
            return if (context == null) {
                null
            } else try {
                val packageManager: PackageManager = context.packageManager
                val packageName: String = context.packageName
                val packageInfo: PackageInfo? =
                    packageManager.getPackageInfoCompat(
                        packageName,
                        PackageManagerHelper.getSigningFlagsCompat()
                    )
                val packageSignatures: Array<Signature?>? = packageInfo?.getSignaturesCompat()
                if (packageSignatures == null || packageSignatures.isEmpty() || packageSignatures[0] == null
                ) {
                    null
                } else signatureDigest(packageSignatures[0])
            } catch (e: Exception) {
                null
            }
        }

        private fun signatureDigest(sig: Signature?): String? {
            return if (sig == null) {
                null
            } else try {
                val md = MessageDigest.getInstance("SHA1")
                val sha1Hash = md.digest(sig.toByteArray())

                val result = StringBuilder()
                for (index in sha1Hash.indices) {
                    val byte = sha1Hash[index]
                    val formatResult = if (index != sha1Hash.size - 1) {
                        String.format("%02X:", byte)
                    } else {
                        String.format("%02X", byte) // Don't append : to the last entry
                    }
                    result.append(formatResult)
                }
                result.toString()
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
}
