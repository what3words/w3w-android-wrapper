//[lib](../../../index.md)/[com.what3words.androidwrapper.helpers](../index.md)/[AutosuggestHelper](index.md)/[selected](selected.md)

# selected

[androidJvm]\
fun [selected](selected.md)(rawString: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), suggestion: Suggestion, onSuccessListener: [Consumer](https://developer.android.com/reference/kotlin/androidx/core/util/Consumer.html)<Suggestion>)

When suggestion is selected this will provide all three word address information needed (without coordinates).

## Parameters

androidJvm

| | |
|---|---|
| rawString | the updated raw query. |
| suggestion | the selected suggestion. |
| onSuccessListener | the callback for the full suggestion information (without coordinates) Suggestion. |
