//[lib](../../index.md)/[com.what3words.androidwrapper.voice](index.md)

# Package com.what3words.androidwrapper.voice

## Types

| Name | Summary |
|---|---|
| [BaseVoiceMessagePayload](-base-voice-message-payload/index.md) | [androidJvm]<br>open class [BaseVoiceMessagePayload](-base-voice-message-payload/index.md) |
| [ErrorPayload](-error-payload/index.md) | [androidJvm]<br>data class [ErrorPayload](-error-payload/index.md)(**type**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **code**: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)?, **reason**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) : [BaseVoiceMessagePayload](-base-voice-message-payload/index.md) |
| [Microphone](-microphone/index.md) | [androidJvm]<br>class [Microphone](-microphone/index.md) |
| [SuggestionsPayload](-suggestions-payload/index.md) | [androidJvm]<br>data class [SuggestionsPayload](-suggestions-payload/index.md)(**suggestions**: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<Suggestion>) : [BaseVoiceMessagePayload](-base-voice-message-payload/index.md) |
| [SuggestionsWithCoordinatesPayload](-suggestions-with-coordinates-payload/index.md) | [androidJvm]<br>data class [SuggestionsWithCoordinatesPayload](-suggestions-with-coordinates-payload/index.md)(**suggestions**: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<SuggestionWithCoordinates>) : [BaseVoiceMessagePayload](-base-voice-message-payload/index.md) |
| [VoiceApiListener](-voice-api-listener/index.md) | [androidJvm]<br>interface [VoiceApiListener](-voice-api-listener/index.md)<br>Implement this listener to receive the callbacks from VoiceApi |
| [VoiceApiListenerWithCoordinates](-voice-api-listener-with-coordinates/index.md) | [androidJvm]<br>interface [VoiceApiListenerWithCoordinates](-voice-api-listener-with-coordinates/index.md) |
| [VoiceBuilder](-voice-builder/index.md) | [androidJvm]<br>class [VoiceBuilder](-voice-builder/index.md)(**api**: [What3WordsV3](../com.what3words.androidwrapper/-what3-words-v3/index.md), **mic**: [Microphone](-microphone/index.md), **voiceLanguage**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **dispatchers**: [DispatcherProvider](../com.what3words.androidwrapper.helpers/-dispatcher-provider/index.md)) : [VoiceApiListener](-voice-api-listener/index.md) |
| [VoiceBuilderWithCoordinates](-voice-builder-with-coordinates/index.md) | [androidJvm]<br>class [VoiceBuilderWithCoordinates](-voice-builder-with-coordinates/index.md)(**api**: [What3WordsV3](../com.what3words.androidwrapper/-what3-words-v3/index.md), **mic**: [Microphone](-microphone/index.md), **voiceLanguage**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **dispatchers**: [DispatcherProvider](../com.what3words.androidwrapper.helpers/-dispatcher-provider/index.md)) : [VoiceApiListenerWithCoordinates](-voice-api-listener-with-coordinates/index.md) |
| [VoiceSignalParser](-voice-signal-parser/index.md) | [androidJvm]<br>object [VoiceSignalParser](-voice-signal-parser/index.md) |
| [W3WError](-w3-w-error/index.md) | [androidJvm]<br>data class [W3WError](-w3-w-error/index.md)(**code**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **message**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) |
| [W3WErrorPayload](-w3-w-error-payload/index.md) | [androidJvm]<br>data class [W3WErrorPayload](-w3-w-error-payload/index.md)(**error**: [W3WError](-w3-w-error/index.md)) : [BaseVoiceMessagePayload](-base-voice-message-payload/index.md) |
