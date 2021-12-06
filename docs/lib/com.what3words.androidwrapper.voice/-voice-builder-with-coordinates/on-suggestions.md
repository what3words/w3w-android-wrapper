//[lib](../../../index.md)/[com.what3words.androidwrapper.voice](../index.md)/[VoiceBuilderWithCoordinates](index.md)/[onSuggestions](on-suggestions.md)

# onSuggestions

[androidJvm]\
fun [onSuggestions](on-suggestions.md)(callback: [Consumer](https://developer.android.com/reference/kotlin/androidx/core/util/Consumer.html)<[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<SuggestionWithCoordinates>>): [VoiceBuilderWithCoordinates](index.md)

onSuggestions callback will be called when VoiceAPI returns a set of SuggestionWithCoordinates with coordinates after receiving the voice data, this can be empty in case of no suggestions available for the provided voice record.

#### Return

a [VoiceBuilder](../-voice-builder/index.md) instance

## Parameters

androidJvm

| | |
|---|---|
| callback | with a list of SuggestionWithCoordinates returned by our VoiceAPI |
