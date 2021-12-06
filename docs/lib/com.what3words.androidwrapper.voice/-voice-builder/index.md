//[lib](../../../index.md)/[com.what3words.androidwrapper.voice](../index.md)/[VoiceBuilder](index.md)

# VoiceBuilder

[androidJvm]\
class [VoiceBuilder](index.md)(**api**: [What3WordsV3](../../com.what3words.androidwrapper/-what3-words-v3/index.md), **mic**: [Microphone](../-microphone/index.md), **voiceLanguage**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **dispatchers**: [DispatcherProvider](../../com.what3words.androidwrapper.helpers/-dispatcher-provider/index.md)) : [VoiceApiListener](../-voice-api-listener/index.md)

## Functions

| Name | Summary |
|---|---|
| [clipToBoundingBox](clip-to-bounding-box.md) | [androidJvm]<br>fun [clipToBoundingBox](clip-to-bounding-box.md)(boundingBox: BoundingBox?): [VoiceBuilder](index.md)<br>Restrict autosuggest results to a BoundingBox. |
| [clipToCircle](clip-to-circle.md) | [androidJvm]<br>fun [clipToCircle](clip-to-circle.md)(centre: Coordinates?, radius: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)? = 1.0): [VoiceBuilder](index.md)<br>Restrict autosuggest results to a circle, specified by Coordinates representing the [centre](clip-to-circle.md) of the circle, plus the [radius](clip-to-circle.md) in kilometres. |
| [clipToCountry](clip-to-country.md) | [androidJvm]<br>fun [clipToCountry](clip-to-country.md)(countryCodes: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)>): [VoiceBuilder](index.md)<br>Restricts autosuggest to only return results inside the countries specified by comma-separated list of uppercase ISO 3166-1 alpha-2 country codes (for example, to restrict to Belgium and the UK, use clipToCountry("GB", "BE"). |
| [clipToPolygon](clip-to-polygon.md) | [androidJvm]<br>fun [clipToPolygon](clip-to-polygon.md)(polygon: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<Coordinates>): [VoiceBuilder](index.md)<br>Restrict autosuggest results to a [polygon](clip-to-polygon.md), specified by a collection of Coordinates. |
| [connected](connected.md) | [androidJvm]<br>open override fun [connected](connected.md)(socket: WebSocket)<br>When WebSocket successfully does the handshake with VoiceAPI |
| [error](error.md) | [androidJvm]<br>open override fun [error](error.md)(message: APIError)<br>When there's an error with the VoiceAPI connection, please find all errors at: https://developer.what3words. |
| [focus](focus.md) | [androidJvm]<br>fun [focus](focus.md)(coordinates: Coordinates?): [VoiceBuilder](index.md)<br>This is a location, specified as a latitude (often where the user making the query is). |
| [isListening](is-listening.md) | [androidJvm]<br>fun [isListening](is-listening.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>isListening() can be used to check if is currently in recording state. |
| [nFocusResults](n-focus-results.md) | [androidJvm]<br>fun [nFocusResults](n-focus-results.md)(n: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)?): [VoiceBuilder](index.md)<br>Specifies the number of results (must be &lt;= nResults) within the results set which will have a focus. |
| [nResults](n-results.md) | [androidJvm]<br>fun [nResults](n-results.md)(n: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)?): [VoiceBuilder](index.md)<br>Set the number of AutoSuggest results to return. |
| [onError](on-error.md) | [androidJvm]<br>fun [onError](on-error.md)(callback: [Consumer](https://developer.android.com/reference/kotlin/androidx/core/util/Consumer.html)<APIResponse.What3WordsError>): [VoiceBuilder](index.md)<br>onError callback will be called when some API error occurs on the VoiceAPI |
| [onSuggestions](on-suggestions.md) | [androidJvm]<br>fun [onSuggestions](on-suggestions.md)(callback: [Consumer](https://developer.android.com/reference/kotlin/androidx/core/util/Consumer.html)<[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<Suggestion>>): [VoiceBuilder](index.md)<br>onSuggestions callback will be called when VoiceAPI returns a set of suggestion after receiving the voice data, this can be empty in case of no suggestions available for the provided voice record. |
| [startListening](start-listening.md) | [androidJvm]<br>fun [startListening](start-listening.md)(): [VoiceBuilder](index.md)<br>startListening() starts the [Microphone](../-microphone/index.md) recording and starts sending voice data to our VoiceAPI. |
| [stopListening](stop-listening.md) | [androidJvm]<br>fun [stopListening](stop-listening.md)()<br>stopListening() forces the [Microphone](../-microphone/index.md) to stop recording and closes the socket with our VoiceAPI. |
| [suggestions](suggestions.md) | [androidJvm]<br>open override fun [suggestions](suggestions.md)(suggestions: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<Suggestion>)<br>When VoiceAPI receive the recording, processed it and retrieved what3word addresses |
