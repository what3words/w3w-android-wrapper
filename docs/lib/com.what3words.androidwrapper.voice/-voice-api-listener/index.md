//[lib](../../../index.md)/[com.what3words.androidwrapper.voice](../index.md)/[VoiceApiListener](index.md)

# VoiceApiListener

[androidJvm]\
interface [VoiceApiListener](index.md)

Implement this listener to receive the callbacks from VoiceApi

## Functions

| Name | Summary |
|---|---|
| [connected](connected.md) | [androidJvm]<br>abstract fun [connected](connected.md)(socket: WebSocket)<br>When WebSocket successfully does the handshake with VoiceAPI |
| [error](error.md) | [androidJvm]<br>abstract fun [error](error.md)(message: APIError)<br>When there's an error with the VoiceAPI connection, please find all errors at: https://developer.what3words. |
| [suggestions](suggestions.md) | [androidJvm]<br>abstract fun [suggestions](suggestions.md)(suggestions: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<Suggestion>)<br>When VoiceAPI receive the recording, processed it and retrieved what3word addresses |

## Inheritors

| Name |
|---|
| [VoiceBuilder](../-voice-builder/index.md) |
