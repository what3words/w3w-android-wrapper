package com.what3words.androidwrapper.voice.cerence

import com.what3words.androidwrapper.voice.VoiceApiListener
import com.what3words.androidwrapper.voice.VoiceApiListenerWithCoordinates
import com.what3words.androidwrapper.voice.VoiceProvider

/** Cerence implementation of the [VoiceProvider] interface for offline voice recognition support.
 * **/
class CerenceVoiceProvider private constructor(private val pointerToCPPCerenceManager: Long) :
    VoiceProvider {
    external override fun initialize(
        sampleRate: Int,
        encoding: Int,
        url: String,
        listener: VoiceApiListener
    )

    external override fun initialize(
        sampleRate: Int,
        encoding: Int,
        url: String,
        listener: VoiceApiListenerWithCoordinates
    )

    external override fun sendData(readCount: Int, buffer: ShortArray)

    external override fun forceStop()

    override var baseUrl: String = "N/A"

    /** Builder class for [CerenceVoiceProvider]
     *
     *  @property fcfPath path to the fcf files
     *  @property dataPath path to the data files
     * **/
    class Builder(private val fcfPath: String, private val dataPath: String) {
        fun build(): CerenceVoiceProvider {
            return newCerenceManger(fcfPath = fcfPath, dataPath = dataPath)
        }

        private external fun newCerenceManger(
            fcfPath: String,
            dataPath: String
        ): CerenceVoiceProvider
    }
}