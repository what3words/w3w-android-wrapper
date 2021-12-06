//[lib](../../../index.md)/[com.what3words.androidwrapper.helpers](../index.md)/[AutosuggestHelper](index.md)

# AutosuggestHelper

[androidJvm]\
class [AutosuggestHelper](index.md)(**api**: [What3WordsV3](../../com.what3words.androidwrapper/-what3-words-v3/index.md), **dispatchers**: [DispatcherProvider](../-dispatcher-provider/index.md))

## Functions

| Name | Summary |
|---|---|
| [allowFlexibleDelimiters](allow-flexible-delimiters.md) | [androidJvm]<br>fun [allowFlexibleDelimiters](allow-flexible-delimiters.md)(boolean: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)): [AutosuggestHelper](index.md)<br>Flexible delimiters feature allows our regex to be less precise on delimiters, this means that "filled count soa" or "filled,count,soa" will be parsed to "filled.count.soa" and send to our autosuggest API. |
| [options](options.md) | [androidJvm]<br>fun [options](options.md)(options: AutosuggestOptions): [AutosuggestHelper](index.md)<br>Set all options at once using AutosuggestOptions |
| [selected](selected.md) | [androidJvm]<br>fun [selected](selected.md)(rawString: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), suggestion: Suggestion, onSuccessListener: [Consumer](https://developer.android.com/reference/kotlin/androidx/core/util/Consumer.html)<Suggestion>)<br>When suggestion is selected this will provide all three word address information needed (without coordinates). |
| [selectedWithCoordinates](selected-with-coordinates.md) | [androidJvm]<br>fun [selectedWithCoordinates](selected-with-coordinates.md)(rawString: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), suggestion: Suggestion, onSuccessListener: [Consumer](https://developer.android.com/reference/kotlin/androidx/core/util/Consumer.html)<SuggestionWithCoordinates>, onFailureListener: [Consumer](https://developer.android.com/reference/kotlin/androidx/core/util/Consumer.html)<APIResponse.What3WordsError>? = null)<br>When suggestion is selected this will provide all three word address information needed with coordinates. |
| [update](update.md) | [androidJvm]<br>fun [update](update.md)(searchText: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), onSuccessListener: [Consumer](https://developer.android.com/reference/kotlin/androidx/core/util/Consumer.html)<[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<Suggestion>>, onFailureListener: [Consumer](https://developer.android.com/reference/kotlin/androidx/core/util/Consumer.html)<APIResponse.What3WordsError>? = null, onDidYouMeanListener: [Consumer](https://developer.android.com/reference/kotlin/androidx/core/util/Consumer.html)<Suggestion>? = null)<br>Update AutosuggestHelper query and receive suggestions (strong regex applied) or a did you mean (flexible regex applied) from our Autosuggest API. |
