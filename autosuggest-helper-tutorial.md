
# <img src="https://what3words.com/assets/images/w3w_square_red.png" width="64" height="64" alt="what3words">&nbsp;AutosuggestHelper tutorial

## Audience

This tutorial is intended for anyone that already has an autocomplete UI element in their app showing possible addresses in a AutoCompleteTextView/EditText/RecyclerView. It explains a method of adding what3words suggestions alongside your existing address results.

## Example

There is a sample provided called [sample-multi-autosuggest-providers](https://github.com/what3words/w3w-android-wrapper/tree/master/sample-multi-autosuggest-providers) on our GitHub repo folder of the API wrapper repository. 
The example uses a EditText and a RecyclerView with two different providers for location autosuggest, **Google Places API** and **what3words API** using our AutosuggestHelper to show the 3 words addresses in the manner described below.  

## Usage

### Gradle

```
implementation 'com.what3words:w3w-android-wrapper:3.1.15'
```

AndroidManifest.xml
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.yourpackage.yourapp">

    <uses-permission android:name="android.permission.INTERNET" />
```

build.gradle (app level)
```gradle
android {
    ...
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
```

add the following ProGuard rules
```
-keep class com.what3words.javawrapper.request.* { *; }
-keep class com.what3words.javawrapper.response.* { *; }
```

### Using AutosuggestHelper class

Add the api and helper wherever you put your class variables and be sure to use your API key:
```Kotlin
val what3words = What3WordsV3("YOUR_API_KEY_HERE", this)
val autosuggestOptions = AutosuggestOptions().apply {
	// apply all clippings here (focus, clipToCountry, clipToCircle, etc.)
	focus = Coordinates(51.5209433, -0.1962334)
}

val autosuggestHelper = AutosuggestHelper(what3words).options(autosuggestOptions)
```
Next step is to use a TextWatcher (or doOnTextChanged EditText extension) and let **autosuggestHelper** know about the changed text and add what3words suggestion data to your existing RecyclerView/Adapter. (check sample for complete working example with [custom data model and RecyclerView adapter](https://github.com/what3words/w3w-android-wrapper/blob/master/sample-multi-autosuggest-providers/src/main/java/com/what3words/sample_multi_autosuggest_providers/SuggestionsAdapter.kt) to show different autosuggest sources and [EditText and RecyclerView](https://github.com/what3words/w3w-android-wrapper/blob/master/sample-multi-autosuggest-providers/src/main/java/com/what3words/sample_multi_autosuggest_providers/MainActivity.kt) setup.

```Kotlin
editText.doOnTextChanged { text, _, _, _ -> 
	// update options in case of new clippings applying/changing dynamically i.e: Location.  
	autosuggestHelper.options(autosuggestOptions).update(  
	    text.toString(),  
	    onSuccessListener = { suggestionResults ->  
			suggestionResults.forEach { suggestion ->  
			    //Add suggestion to existing RecyclerView adapter
			    list.add(suggestion)
	            Log.i("MainActivity", suggestion.words)  
	        } 
	        //notify adapter that there's changes on the data. 
			adapter.notifyDataSetChanged()  
	    },  
		onFailureListener = {  
		  //log any errors returned by what3words API.
		  Log.e("MainActivity", it.message)  
		}  
	)
}
```

### Get the full three word address once the user has selected a row

When user selects a row from the RecyclerView *autosuggestHelper.selected()* or *autosuggestHelper.selectedWithCoordinates()* should be called to retrieve the full three word address with or without coordinates. 

```Kotlin
autosuggestHelper.selectedWithCoordinates(  
	query.text.toString(),  
    selectedSuggestion,  
    onSuccessListener = { w3wWithCoordinates ->
		Toast.makeText(this,"suggestion selected from what3words, ${w3wWithCoordinates.words}, ${w3wWithCoordinates.coordinates.lat} ${w3wWithCoordinates.coordinates.lng}", Toast.LENGTH_LONG).show()  
    },  
    onFailureListener = {  
	    Log.e("MainActivity", it.message)  
    }  
)
```

***Note*** *that selectedWithCoordinates() will convert the three word address to a lat/lng which will count against your plan's quota.*