//[lib](../../../index.md)/[com.what3words.androidwrapper.voice](../index.md)/[VoiceBuilder](index.md)/[onSuggestions](on-suggestions.md)

# onSuggestions

[androidJvm]\
fun [onSuggestions](on-suggestions.md)(callback: [Consumer](https://developer.android.com/reference/kotlin/androidx/core/util/Consumer.html)<[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<Suggestion>>): [VoiceBuilder](index.md)

onSuggestions callback will be called when VoiceAPI returns a set of suggestion after receiving the voice data, this can be empty in case of no suggestions available for the provided voice record.

#### Return

a [VoiceBuilder](index.md) instance

## Parameters

androidJvm

| | |
|---|---|
| callback | with a list of Suggestion returned by our VoiceAPI |
