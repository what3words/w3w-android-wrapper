package com.what3words.androidwrapper

import com.what3words.androidwrapper.voice.VoiceSignalParser
import org.junit.Test

class VoiceSignalParserTests {
    @Test
    fun `voiceSignalParser above MAX_SIGNAL_LEVEL`() {
        // given
        val parsedValue = VoiceSignalParser.transform(90.0)
        assert(parsedValue == 1.0f)
    }

    @Test
    fun `voiceSignalParser exact MAX_SIGNAL_LEVEL`() {
        // given
        val parsedValue = VoiceSignalParser.transform(80.0)
        assert(parsedValue == 1.0f)
    }

    @Test
    fun `voiceSignalParser exact MIN_SIGNAL_LEVEL`() {
        // given
        val parsedValue = VoiceSignalParser.transform(20.0)
        assert(parsedValue == 0.0f)
    }

    @Test
    fun `voiceSignalParser below MIN_SIGNAL_LEVEL`() {
        // given
        val parsedValue = VoiceSignalParser.transform(10.0)
        assert(parsedValue == 0.0f)
    }

    @Test
    fun `voiceSignalParser mid level`() {
        // given
        val parsedValue = VoiceSignalParser.transform(50.0)
        assert(parsedValue == 0.5f)
    }
}