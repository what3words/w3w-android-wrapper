//[lib](../../../index.md)/[com.what3words.androidwrapper.helpers](../index.md)/[AutosuggestHelper](index.md)/[selectedWithCoordinates](selected-with-coordinates.md)

# selectedWithCoordinates

[androidJvm]\
fun [selectedWithCoordinates](selected-with-coordinates.md)(rawString: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), suggestion: Suggestion, onSuccessListener: [Consumer](https://developer.android.com/reference/kotlin/androidx/core/util/Consumer.html)<SuggestionWithCoordinates>, onFailureListener: [Consumer](https://developer.android.com/reference/kotlin/androidx/core/util/Consumer.html)<APIResponse.What3WordsError>? = null)

When suggestion is selected this will provide all three word address information needed with coordinates. Note that selectedWithCoordinates() will convert the three word address to a lat/lng which will count against your plan's quota.

## Parameters

androidJvm

| | |
|---|---|
| rawString | the updated raw query. |
| suggestion | the selected suggestion. |
| onSuccessListener | the callback for the full suggestion information with coordinates SuggestionWithCoordinates. |
| onFailureListener | the callback for API errors APIResponse.What3WordsError. |
