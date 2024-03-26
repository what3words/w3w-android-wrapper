package com.what3words.androidwrapper.voice

@Deprecated("This class is deprecated, please use new W3WMicroPhone instead")
object VoiceSignalParser {

    private const val MIN_SIGNAL_LEVEL = 20
    private const val MAX_SIGNAL_LEVEL = 80

    private const val MIN_SCALED_LEVEL = 0f
    private const val MAX_SCALED_LEVEL = 1f

    /**
     * The function scales the signal to an interval between 0.0 and 1.0
     * [MIN_SIGNAL_LEVEL] maps to 0.0, and [MAX_SIGNAL_LEVEL] to 1.0
     */
    private fun getScaledSignal(
        valueIn: Float
    ): Float {
        return (MAX_SCALED_LEVEL - MIN_SCALED_LEVEL) * (valueIn - MIN_SIGNAL_LEVEL) / (MAX_SIGNAL_LEVEL - MIN_SIGNAL_LEVEL) + MIN_SCALED_LEVEL
    }

    /**
     * Transform the raw Signal into dB and then to a scaling factor
     * @param rawValue the raw volume
     */

    fun transform(rawValue: Double): Float {
        return when {
            rawValue < MIN_SIGNAL_LEVEL -> MIN_SCALED_LEVEL
            rawValue > MAX_SIGNAL_LEVEL -> MAX_SCALED_LEVEL
            getScaledSignal(
                rawValue.toFloat()
            ) < MIN_SCALED_LEVEL -> MIN_SCALED_LEVEL
            getScaledSignal(
                rawValue.toFloat()
            ) > MAX_SCALED_LEVEL -> MAX_SCALED_LEVEL
            else -> getScaledSignal(
                rawValue.toFloat()
            )
        }
    }
}
