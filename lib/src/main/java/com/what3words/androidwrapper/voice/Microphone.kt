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
        const val RECORDING_RATE = 44100
        const val CHANNEL = AudioFormat.CHANNEL_IN_MONO
        const val FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var onListeningCallback: Consumer<Float?>? = null
    private var onErrorCallback: Consumer<String>? = null

    private val bufferSize = AudioRecord.getMinBufferSize(
        RECORDING_RATE, CHANNEL, FORMAT
    )

    private var recorder: AudioRecord? = null
    private var continueRecording: Boolean = false

    fun onListening(callback: Consumer<Float?>): Microphone {
        this.onListeningCallback = callback
        return this
    }

    fun onError(callback: Consumer<String>): Microphone {
        this.onErrorCallback = callback
        return this
    }

    internal fun stopRecording() {
        continueRecording = false
        recorder?.release()
    }

    internal fun startRecording(socket: WebSocket) {
        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            RECORDING_RATE,
            CHANNEL,
            FORMAT,
            bufferSize
        ).also { audioRecord ->
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
        }
    }
}
