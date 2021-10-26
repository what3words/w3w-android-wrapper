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
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.log10

class Microphone {
    companion object {
        const val DEFAULT_RECORDING_RATE = 44100
        const val CHANNEL = AudioFormat.CHANNEL_IN_DEFAULT
        const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
    }

    constructor() {
        recordingRate =
            getSupportedSampleRates().maxOrNull() ?: -1
        channel = CHANNEL
        encoding = ENCODING
        bufferSize = AudioRecord.getMinBufferSize(
            recordingRate, channel, encoding
        )
    }

    private fun getSupportedSampleRates(): List<Int> {
        val validSampleRates = intArrayOf(
            8000, 11025, 16000, 22050, 44100, 48000
        )
        val list = mutableListOf<Int>()
        validSampleRates.forEach {
            val result = AudioRecord.getMinBufferSize(
                it,
                AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (result != AudioRecord.ERROR && result != AudioRecord.ERROR_BAD_VALUE && result > 0) {
                list.add(it)
            }
        }
        // Log.i("VoiceFlow", "supportedRates: ${list.joinToString(",") { it.toString() }}")
        return list
    }

    private fun isSampleRateValid(sampleRate: Int): Boolean {
        return getSupportedSampleRates().contains(sampleRate)
    }

    constructor(recordingRate: Int, encoding: Int, channel: Int) {
        this.recordingRate = recordingRate
        this.encoding = encoding
        this.channel = channel
        bufferSize = AudioRecord.getMinBufferSize(
            recordingRate, channel, encoding
        )
        Timber.i(
            "VoiceFlow",
            "custom constructor, recording: $recordingRate, channel: $channel, encoding: $encoding, bufferSize: $bufferSize"
        )
    }

    internal var recordingRate: Int = DEFAULT_RECORDING_RATE
    internal var encoding: Int = ENCODING
    private var bufferSize: Int = 0
    private var channel: Int = CHANNEL

    private var onListeningCallback: Consumer<Float?>? = null
    private var onErrorCallback: Consumer<String>? = null
    private var recorder: AudioRecord? = null
    var isListening: Boolean = false

    /**
     * onListening() callback will return the volume of the microphone while recording from 0.0-1.0, i.e: 0.5, 50% (0.0 min, 1.0 max volume)
     *
     * @param callback with a float 0.0-1.0 with the microphone volume, useful for animations, etc.
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
        isListening = false
        recorder?.release()
    }

    internal fun startRecording(socket: WebSocket) {
        if (!isSampleRateValid(recordingRate)) {
            onErrorCallback?.accept("Invalid sample rate, please use one of the following: ${getSupportedSampleRates().joinToString { it.toString() }}")
            return
        }
        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            recordingRate,
            channel,
            encoding,
            bufferSize
        ).also { audioRecord ->
            if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                isListening = true
                CoroutineScope(Dispatchers.IO).launch {
                    val buffer = ShortArray(bufferSize)
                    var oldTimestamp = System.currentTimeMillis()
                    audioRecord.startRecording()
                    while (isListening) {
                        val readCount = audioRecord.read(buffer, 0, buffer.size)
                        sendData(readCount, buffer, socket)
                        if ((System.currentTimeMillis() - oldTimestamp) > 100) {
                            oldTimestamp = System.currentTimeMillis()
                            val dB =
                                VoiceSignalParser.transform(
                                    calculateVolume(readCount, buffer)
                                )
                            CoroutineScope(Dispatchers.Main).launch {
                                onListeningCallback?.accept(dB)
                            }
                        }
                    }
                }
            } else {
                Log.e(
                    "VoiceFlow",
                    "Failed to initialize AudioRecord, please request AUDIO_RECORD permission."
                )
                CoroutineScope(Dispatchers.Main).launch {
                    onErrorCallback?.accept("Failed to initialize AudioRecord, please request AUDIO_RECORD permission.")
                }
            }
        }
    }

    private fun sendData(readCount: Int, buffer: ShortArray, socket: WebSocket) {
        val bufferBytes: ByteBuffer =
            ByteBuffer.allocate(readCount * 2) // 2 bytes per short
        bufferBytes.order(ByteOrder.LITTLE_ENDIAN) // save little-endian byte from short buffer
        bufferBytes.asShortBuffer().put(buffer, 0, readCount)
        socket.send(ByteString.of(*bufferBytes.array()))
    }

    internal fun calculateVolume(readCount: Int, buffer: ShortArray): Double {
        var v: Long = 0
        for (i in 0 until readCount) {
            v += buffer[i] * buffer[i]
        }
        val amplitude =
            if (readCount != 0) (v / readCount).toDouble() else 0.0
        var volume = 0.0
        if (amplitude > 0) {
            volume = 10 * log10(amplitude)
        }
        return volume
    }
}
