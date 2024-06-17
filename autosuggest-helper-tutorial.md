
# <img src="https://what3words.com/assets/images/w3w_square_red.png" width="64" height="64" alt="what3words">&nbsp;AutosuggestHelper tutorial

## Audience

This tutorial is intended for anyone that already has an autocomplete UI element in their app showing possible addresses in a AutoCompleteTextView/EditText/RecyclerView. It explains a method of adding what3words suggestions alongside your existing address results.

## Installation

### Gradle

```kotlin
android {
    ...
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.what3words:w3w-android-wrapper:$latest_version")
}
```

AndroidManifest.xml
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.yourpackage.yourapp">

    <uses-permission android:name="android.permission.INTERNET" />
    
</manifest>
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

## Using AutosuggestHelper class

Add the api and helper wherever you put your class variables and be sure to use your [API key](https://what3words.com/select-plan):

```Kotlin
val dataSource = W3WApiTextDataSource.create("YOUR_API_KEY_HERE")
val autosuggestOptions = W3WAutosuggestOptions.Builder().
    .focus(...)
    .clipToCountry(...)
    ...
    .build()

val autosuggestHelper = AutosuggestHelper(dataSource).options(autosuggestOptions)
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
	            Log.i("MainActivity", suggestion.w3wAddress.address)  
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

### Get the full what3words address once the user has selected a row

When user selects a row from the RecyclerView *autosuggestHelper.selected()* or *autosuggestHelper.selectedWithCoordinates()* should be called to retrieve the full what3words address with or without coordinates. 

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

***Note*** *that selectedWithCoordinates() will convert the what3words address to a lat/lng which will count against your plan's quota.*
