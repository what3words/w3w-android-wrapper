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
        const val DEFAULT_ENCODING = "pcm_s16le"
        const val CHANNEL = AudioFormat.CHANNEL_IN_DEFAULT
        const val FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    constructor() {
        recordingRate = DEFAULT_RECORDING_RATE
        encoding = DEFAULT_ENCODING
        channel = CHANNEL
        format = FORMAT
        bufferSize = AudioRecord.getMinBufferSize(
            recordingRate, channel, format
        )
    }

    constructor(recordingRate: Int, encoding: String, channel: Int, format: Int) {
        this.recordingRate = recordingRate
        this.encoding = encoding
        this.channel = channel
        this.format = format
        bufferSize = AudioRecord.getMinBufferSize(
            recordingRate, channel, format
        )
    }

    private var format: Int = FORMAT
    private var channel: Int = CHANNEL
    private var onListeningCallback: Consumer<Float?>? = null
    private var onErrorCallback: Consumer<String>? = null
    private var recordingRate: Int = DEFAULT_RECORDING_RATE
    private var encoding: String = DEFAULT_ENCODING

    private var bufferSize: Int = 0

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
                format,
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
