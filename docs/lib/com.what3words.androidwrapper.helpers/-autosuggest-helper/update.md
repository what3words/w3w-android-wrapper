//[lib](../../../index.md)/[com.what3words.androidwrapper.helpers](../index.md)/[AutosuggestHelper](index.md)/[update](update.md)

# update

[androidJvm]\
fun [update](update.md)(searchText: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), onSuccessListener: [Consumer](https://developer.android.com/reference/kotlin/androidx/core/util/Consumer.html)<[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<Suggestion>>, onFailureListener: [Consumer](https://developer.android.com/reference/kotlin/androidx/core/util/Consumer.html)<APIResponse.What3WordsError>? = null, onDidYouMeanListener: [Consumer](https://developer.android.com/reference/kotlin/androidx/core/util/Consumer.html)<Suggestion>? = null)

Update AutosuggestHelper query and receive suggestions (strong regex applied) or a did you mean (flexible regex applied) from our Autosuggest API.

## Parameters

androidJvm

| | |
|---|---|
| searchText | the updated query. |
| onSuccessListener | the callback for suggestions. |
| onFailureListener | the callback for API errors APIResponse.What3WordsError. |
| onDidYouMeanListener | the callback for did you mean results. |
