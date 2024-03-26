package com.what3words.androidwrapper.voice.error

import com.what3words.core.types.common.W3WError

class W3WApiVoiceError(code: String, message: String) : W3WError(message = "$code: $message")