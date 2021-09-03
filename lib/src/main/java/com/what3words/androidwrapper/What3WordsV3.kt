package com.what3words.androidwrapper

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import com.google.common.io.BaseEncoding
import com.what3words.androidwrapper.voice.Microphone
import com.what3words.androidwrapper.voice.VoiceApi
import com.what3words.androidwrapper.voice.VoiceBuilder
import com.what3words.androidwrapper.voice.VoiceBuilderWithCoordinates
import java.security.MessageDigest

class What3WordsV3 : com.what3words.javawrapper.What3WordsV3 {
    internal var voiceApi: VoiceApi

    constructor(apiKey: String, context: Context) : super(
        apiKey,
        context.packageName,
        getSignature(context),
        null
    ) {
        voiceApi = VoiceApi(apiKey)
    }

    internal constructor(apiKey: String, voiceApi: VoiceApi) : super(
        apiKey,
        "com.what3words.androidwrapper",
        "",
        emptyMap()
    ) {
        this@What3WordsV3.voiceApi = voiceApi
    }

    constructor(apiKey: String, context: Context, headers: Map<String, String>) : super(
        apiKey,
        context.packageName,
        getSignature(context),
        headers
    ) {
        voiceApi = VoiceApi(apiKey)
    }

    constructor(apiKey: String, endpoint: String, context: Context) : super(
        apiKey,
        endpoint,
        context.packageName,
        getSignature(context),
        null
    ) {
        voiceApi = VoiceApi(apiKey)
    }

    constructor(
        apiKey: String,
        endpoint: String,
        context: Context,
        headers: Map<String, String>
    ) : super(apiKey, endpoint, context.packageName, getSignature(context), headers) {
        voiceApi = VoiceApi(apiKey)
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

    fun autosuggest(
        microphone: Microphone,
        voiceLanguage: String
    ): VoiceBuilder {
        return VoiceBuilder(this, microphone, voiceLanguage)
    }

    fun autosuggestWithCoordinates(
        microphone: Microphone,
        voiceLanguage: String
    ): VoiceBuilderWithCoordinates {
        return VoiceBuilderWithCoordinates(this, microphone, voiceLanguage)
    }
}