package com.what3words.androidwrapper.voice

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.util.Consumer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import okio.ByteString
import kotlin.math.abs

class Microphone {
    companion object {
        const val DEFAULT_RECORDING_RATE = 44100
        const val CHANNEL = AudioFormat.CHANNEL_IN_DEFAULT
        const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
    }

    constructor() {
        recordingRate = getMinSupportedSampleRate()
        channel = CHANNEL
        encoding = ENCODING
        bufferSize = AudioRecord.getMinBufferSize(
            recordingRate, channel, encoding
        )
    }

    private fun getMinSupportedSampleRate(): Int {
        /*
     * Valid Audio Sample rates
     *
     * @see <a
     * href="http://en.wikipedia.org/wiki/Sampling_%28signal_processing%29"
     * >Wikipedia</a>
     */
        val validSampleRates = intArrayOf(
            8000, 11025, 16000, 22050, 44100, 48000
        )
        val list = mutableListOf<Int>()
        /*
     * Selecting default audio input source for recording since
     * AudioFormat.CHANNEL_CONFIGURATION_DEFAULT is deprecated and selecting
     * default encoding format.
     */for (i in validSampleRates.indices) {
            val result = AudioRecord.getMinBufferSize(
                validSampleRates[i],
                AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (result != AudioRecord.ERROR && result != AudioRecord.ERROR_BAD_VALUE && result > 0) {
                // return the mininum supported audio sample rate
                list.add(validSampleRates[i])
            }
        }
        // If none of the sample rates are supported return -1 handle it in
        // calling method
        return if (list.isEmpty())
            -1
        else list.maxOrNull()!!
    }

    constructor(recordingRate: Int, encoding: Int, channel: Int) {
        this.recordingRate = recordingRate
        this.encoding = encoding
        this.channel = channel
        bufferSize = AudioRecord.getMinBufferSize(
            recordingRate, channel, encoding
        )
    }

    internal var recordingRate: Int = DEFAULT_RECORDING_RATE
    internal var encoding: Int = ENCODING
    private var bufferSize: Int = 0
    private var channel: Int = CHANNEL


    private var onListeningCallback: Consumer<Float?>? = null
    private var onErrorCallback: Consumer<String>? = null
    private var recorder: AudioRecord? = null
    private var continueRecording: Boolean = false

    /**
     * onListening() callback will return the volume of the microphone while recording from 0-100 (0 min, 100 max volume)
     *
     * @param callback with a float 0.0-100.0 with the microphone volume, useful for animations, etc.
     * @return a {@link Microphone} instance
     */
    fun onListening(callback: Consumer<Float?>): Microphone {
        this.onListeningCallback = callback
        return this
    }

    /**
     * onError() callback will be called if there's some issue starting the microphone, i.e: Permissions
     *
     * @param callback with a error message.
     * @return a {@link Microphone} instance
     */
    fun onError(callback: Consumer<String>): Microphone {
        this.onErrorCallback = callback
        return this
    }

    internal fun stopRecording() {
        continueRecording = false
        recorder?.release()
    }

    internal fun startRecording(socket: WebSocket) {
        try {
            recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                recordingRate,
                channel,
                encoding,
                bufferSize
            ).also { audioRecord ->
                try {
                    if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                        continueRecording = true
                        CoroutineScope(Dispatchers.IO).launch {
                            val buffer = ByteArray(bufferSize)
                            var oldTimestamp = System.currentTimeMillis()
                            audioRecord.startRecording()
                            while (continueRecording) {
                                audioRecord.read(buffer, 0, buffer.size)
                                if ((System.currentTimeMillis() - oldTimestamp) > 100) {
                                    oldTimestamp = System.currentTimeMillis()
                                    val dB =
                                        VoiceSignalParser.transform(
                                            buffer.map { abs(it.toDouble()) }
                                                .sum()
                                        )
                                    CoroutineScope(Dispatchers.Main).launch {
                                        onListeningCallback?.accept(dB)
                                    }
                                }
                                socket.send(ByteString.of(*buffer))
                            }
                        }
                    } else {
                        Log.e(
                            "VoiceBuilder",
                            "Failed to initialize AudioRecord, please request AUDIO_RECORD permission."
                        )
                        CoroutineScope(Dispatchers.Main).launch {
                            onErrorCallback?.accept("Failed to initialize AudioRecord, please request AUDIO_RECORD permission.")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(
                        "VoiceBuilder",
                        e.message.toString()
                    )
                    CoroutineScope(Dispatchers.Main).launch {
                        onErrorCallback?.accept(e.message)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(
                "VoiceBuilder",
                e.message.toString()
            )
            CoroutineScope(Dispatchers.Main).launch {
                onErrorCallback?.accept(e.message)
            }
        }
    }
}
