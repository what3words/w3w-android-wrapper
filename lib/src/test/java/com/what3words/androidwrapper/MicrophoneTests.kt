package com.what3words.androidwrapper

import com.what3words.androidwrapper.voice.Microphone
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import okhttp3.WebSocket
import okio.ByteString
import org.junit.Before
import org.junit.Test

class MicrophoneTests {

    @MockK
    private lateinit var socket: WebSocket

    @Before
    fun setup() {
        socket = mockk()
        every {
            socket.send(any<ByteString>())
        } answers {
            true
        }
    }

    @Test
    fun `microphone calculateVolume no sound`() {
        val microphone = Microphone()
        val volume = microphone.calculateVolume(
            5,
            shortArrayOf(
                0,
                0,
                0,
                0,
                0
            )
        )
        assert(volume == 0.0)
    }

    @Test
    fun `microphone calculateVolume low volume`() {
        val microphone = Microphone()
        val volume = microphone.calculateVolume(
            5,
            shortArrayOf(
                250,
                250,
                250,
                250,
                250
            )
        )
        assert(volume < 50.0)
    }

    @Test
    fun `microphone calculateVolume high volume`() {
        val microphone = Microphone()
        val volume = microphone.calculateVolume(
            5,
            shortArrayOf(
                500,
                500,
                500,
                500,
                500
            )
        )
        assert(volume > 50.0)
    }
}
