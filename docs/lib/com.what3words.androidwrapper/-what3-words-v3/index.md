//[lib](../../../index.md)/[com.what3words.androidwrapper](../index.md)/[What3WordsV3](index.md)

# What3WordsV3

[androidJvm]\
class [What3WordsV3](index.md) : What3WordsV3

## Constructors

| | |
|---|---|
| [What3WordsV3](-what3-words-v3.md) | [androidJvm]<br>fun [What3WordsV3](-what3-words-v3.md)(apiKey: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)) |
| [What3WordsV3](-what3-words-v3.md) | [androidJvm]<br>fun [What3WordsV3](-what3-words-v3.md)(apiKey: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), headers: [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)<[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)>) |
| [What3WordsV3](-what3-words-v3.md) | [androidJvm]<br>fun [What3WordsV3](-what3-words-v3.md)(apiKey: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), endpoint: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)) |
| [What3WordsV3](-what3-words-v3.md) | [androidJvm]<br>fun [What3WordsV3](-what3-words-v3.md)(apiKey: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), endpoint: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), headers: [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)<[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)>) |

## Types

| Name | Summary |
|---|---|
| [Companion](-companion/index.md) | [androidJvm]<br>object [Companion](-companion/index.md) |

## Functions

| Name | Summary |
|---|---|
| [autosuggest](index.md#710479625%2FFunctions%2F-1973928616) | [androidJvm]<br>open fun [autosuggest](index.md#710479625%2FFunctions%2F-1973928616)(p0: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): AutosuggestRequest.Builder<br>[androidJvm]<br>fun [autosuggest](autosuggest.md)(microphone: [Microphone](../../com.what3words.androidwrapper.voice/-microphone/index.md), voiceLanguage: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [VoiceBuilder](../../com.what3words.androidwrapper.voice/-voice-builder/index.md)<br>The what3words Voice API allows a user to say three words into any application or service, with it returning a list of suggested what3words addresses, through a single API call. |
| [autosuggestionSelection](index.md#-347360360%2FFunctions%2F-1973928616) | [androidJvm]<br>open fun [autosuggestionSelection](index.md#-347360360%2FFunctions%2F-1973928616)(p0: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), p1: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), p2: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), p3: SourceApi): AutosuggestSelectionRequest.Builder |
| [autosuggestWithCoordinates](index.md#1108421370%2FFunctions%2F-1973928616) | [androidJvm]<br>open fun [autosuggestWithCoordinates](index.md#1108421370%2FFunctions%2F-1973928616)(p0: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): AutosuggestWithCoordinatesRequest.Builder<br>[androidJvm]<br>fun [autosuggestWithCoordinates](autosuggest-with-coordinates.md)(microphone: [Microphone](../../com.what3words.androidwrapper.voice/-microphone/index.md), voiceLanguage: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [VoiceBuilderWithCoordinates](../../com.what3words.androidwrapper.voice/-voice-builder-with-coordinates/index.md)<br>The what3words Voice API allows a user to say three words into any application or service, with it returning a list of suggested what3words addresses with coordinates, through a single API call. |
| [availableLanguages](index.md#-1732763176%2FFunctions%2F-1973928616) | [androidJvm]<br>open fun [availableLanguages](index.md#-1732763176%2FFunctions%2F-1973928616)(): AvailableLanguagesRequest.Builder |
| [convertTo3wa](index.md#1590651555%2FFunctions%2F-1973928616) | [androidJvm]<br>open fun [convertTo3wa](index.md#1590651555%2FFunctions%2F-1973928616)(p0: Coordinates): ConvertTo3WARequest.Builder |
| [convertToCoordinates](index.md#-336711833%2FFunctions%2F-1973928616) | [androidJvm]<br>open fun [convertToCoordinates](index.md#-336711833%2FFunctions%2F-1973928616)(p0: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): ConvertToCoordinatesRequest.Builder |
| [getRetrofitInstance](index.md#-668202990%2FFunctions%2F-1973928616) | [androidJvm]<br>open fun [getRetrofitInstance](index.md#-668202990%2FFunctions%2F-1973928616)(): Retrofit |
| [gridSection](index.md#1203675341%2FFunctions%2F-1973928616) | [androidJvm]<br>open fun [gridSection](index.md#1203675341%2FFunctions%2F-1973928616)(p0: BoundingBox): GridSectionRequest.Builder |
| [what3words](index.md#-1171094256%2FFunctions%2F-1973928616) | [androidJvm]<br>open fun [what3words](index.md#-1171094256%2FFunctions%2F-1973928616)(): What3WordsV3Service |
