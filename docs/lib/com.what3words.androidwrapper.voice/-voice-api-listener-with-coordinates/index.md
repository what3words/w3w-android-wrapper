//[lib](../../../index.md)/[com.what3words.androidwrapper.voice](../index.md)/[VoiceApiListenerWithCoordinates](index.md)

# VoiceApiListenerWithCoordinates

[androidJvm]\
interface [VoiceApiListenerWithCoordinates](index.md)

## Functions

| Name | Summary |
|---|---|
| [connected](connected.md) | [androidJvm]<br>abstract fun [connected](connected.md)(socket: WebSocket)<br>When WebSocket successfully does the handshake with VoiceAPI |
| [error](error.md) | [androidJvm]<br>abstract fun [error](error.md)(message: APIError)<br>When there's an error with the VoiceAPI connection, please find all errors at: https://developer.what3words. |
| [suggestionsWithCoordinates](suggestions-with-coordinates.md) | [androidJvm]<br>abstract fun [suggestionsWithCoordinates](suggestions-with-coordinates.md)(suggestions: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<SuggestionWithCoordinates>)<br>When VoiceAPI receive the recording, processed it and retrieved what3word addresses with coordinates |

## Inheritors

| Name |
|---|
| [VoiceBuilderWithCoordinates](../-voice-builder-with-coordinates/index.md) |
