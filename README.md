# <img src="https://what3words.com/assets/images/w3w_square_red.png" width="64" height="64" alt="what3words">&nbsp;w3w-android-wrapper

[![Maven Central](https://img.shields.io/maven-central/v/com.what3words/w3w-android-wrapper)](https://central.sonatype.com/artifact/com.what3words/w3w-android-wrapper) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=what3words_w3w-android-wrapper&metric=alert_status)](https://sonarcloud.io/dashboard?id=what3words_w3w-android-wrapper)

An Android library to use the [what3words v3 API](https://docs.what3words.com/api/v3/).

## Useful Links
[what3words API documentation](https://docs.what3words.com/api/v3/)

[Sign up and select a plan](https://what3words.com/select-plan)

[Sample app](https://github.com/what3words/w3w-android-samples/tree/main/api-wrapper-sample)

## Download

### Gradle

To integrate the library into your project, add the following dependency:
```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.what3words:w3w-android-wrapper:$latest_version")
}
```

## Example Usage
This example demonstrates the usage of the library using Kotlin Coroutines. For a cleaner architecture or Java example, you can refer to our [sample app](https://github.com/what3words/w3w-android-samples/tree/main/api-wrapper-sample).

### W3WApiTextDataSource
The W3WApiTextDataSource class facilitates the conversion of coordinates to What3words addresses and vice versa, as well as providing suggestions for slightly incomplete What3words addresses.

#### Get the W3WApiTextDataSource

Create an instance of W3WApiTextDataSource via the factory method.
```kotlin
val textDataSource = W3WApiTextDataSource.create("YOUR_API_KEY")
```

If you are running your own Enterprise Suite API Server, you can specify the URL to your server:

```kotlin
val textDataSource = W3WApiTextDataSource.create("YOUR_API_KEY", "YOUR_SERVER_ENPOINT")
```

#### convertTo3wa example
This function will convert a latitude and longitude to a what3words address, in the language of your choice.

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ...

        val coordinates = W3WCoordinates("51.2305", "-0.24123")
        val language = W3WRFC5646Language.EN_GB
        
        CoroutineScope(Dispatchers.IO).launch { 
            // Run to convert method in Dispatchers.IO   
            val result = textDataSource.convertTo3wa(coordinates, language)
            
            //Switch to Dispatcher.Main to update your views with the results if needed
            withContext(Dispatchers.Main) {
                when (result) {
                    is W3WResult.Failure -> {
                        Log.e("MainActivity", "Error: ${result.message}")
                    }
                    is W3WResult.Success -> {
                        Log.d("MainActivity", "what3words address: ${result.value.address}")
                    }
                }
            }
        
        }
    }
}
```

#### convertToCoordinates example
This function converts a what3words address to a latitude and longitude.

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ...

        val w3wAddress = "filled.count.soap"
        
        CoroutineScope(Dispatchers.IO).launch { 
            // Run to convert method in Dispatchers.IO   
            val result = textDataSource.convertToCoordinates(w3wAddress)
            
            //Switch to Dispatcher.Main to update your views with the results if needed
            withContext(Dispatchers.Main) {
                when (result) {
                    is W3WResult.Failure -> {
                        Log.e("MainActivity", "Error: ${result.message}")
                    }
                    is W3WResult.Success -> {
                        Log.d("MainActivity", "Coordinates: ${result.value.lat}, ${result.value.lng}")
                    }
                }
            }
        }
    }
}
```

#### autosuggest example
AutoSuggest can take a slightly incorrect what3words address and suggest a list of valid what3words address.

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ...

        val incompletedW3WAddress = "filled.count.so"
        
        CoroutineScope(Dispatchers.IO).launch { 
            // Run to auto suggest method in Dispatchers.IO   
            val result = textDataSource.autosuggest(incompletedW3WAddress)
            
            //Switch to Dispatcher.Main to update your views with the results if needed
            withContext(Dispatchers.Main) {
                when (result) {
                    is W3WResult.Failure -> {
                        Log.e("MainActivity", "Error: ${result.message}")
                    }
                    is W3WResult.Success -> {
                        if (result.value.isNotEmpty()) {
                            Log.d("MainActivity", "Suggestions: ${result.value.joinToString { it.w3wAddress.address }}")
                        } else {
                            Log.d("MainActivity", "No suggestions found")
                        } 
                    }
                }
            }
        }
    }
}
```

#### availableLanguages example
This method retrieves a set of all available languages that what3words is supporting.

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ...
        
        CoroutineScope(Dispatchers.IO).launch { 
            // Run to availableLanguages method in Dispatchers.IO   
            val result = textDataSource.availableLanguages()
            
            //Switch to Dispatcher.Main to update your views with the results if needed
            withContext(Dispatchers.Main) {
                when (result) {
                    is W3WResult.Failure -> {
                        Log.e("MainActivity", "Error: ${result.message}")
                    }
                    is W3WResult.Success -> { 
                        result.value.forEach {
                            Log.d("MainActivity", "languageCode: ${it.code} - locale: ${it.locale}")
                        }
                    }
                }
            }
        }
    }
}
```

#### gridSection example
Returns a section of the 3m x 3m what3words grid for a bounding box. The bounding box is specified by lat,lng,lat,lng as south,west,north,east. You then can convert the grid into GeoJSON format, making it very simple to display on a map.

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ...
        
        CoroutineScope(Dispatchers.IO).launch { 
            // Run to gridSection method in Dispatchers.IO   
            val result = w3WApiTextDataSource.gridSection(
                W3WRectangle(W3WCoordinates(51.0, 0.0), W3WCoordinates(52.0, 0.1)))
            
            //Switch to Dispatcher.Main to update your views with the results if needed
            withContext(Dispatchers.Main) {
                when (result) {
                    is W3WResult.Failure -> {
                        Log.e("MainActivity", "Error: ${result.message}")
                    }
                    is W3WResult.Success -> { 
                        val gridSection = result.value
                        // Convert to GeoJSON
                        val geoJsonString = gridSection.toGeoJSON()
                    }
                }
            }
        }
    }
}
```

### W3WApiVoiceDataSource
The W3WApiVoiceDataSource class allows searching for what3words addresses using voice input. Ensure you have a Voice API plan enabled in your account to use this feature.

Create a W3WApiVoiceDataSource instance
```kotlin
val voiceDataSource = W3WApiVoiceDataSource("YOUR_API_KEY")
```

If you are running your own Enterprise Suite API Server, you can specify the URL to your server:

```kotlin
val voiceDataSource = W3WApiVoiceDataSource("YOUR_API_KEY", "YOUR_SERVER_ENDPOINT")
```

Create a W3WMicrophone instance to handle voice recording.

```kotlin
val microphone = W3WMicrophone()
```

You can set up callbacks to receive information about the recording progress.

```kotlin
microphone.setEventsListener(object : W3WAudioStream.EventsListener {
    override fun onVolumeChange(volume: Float) {
        
    }

    override fun onError(error: W3WError) {
        
    }

    override fun onAudioStreamStateChange(state: W3WAudioStreamState) {
        
    }
})
```

Perform voice autosuggest

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ...
        
        val voiceLanguage = W3WRFC5646Language.EN_GB

        CoroutineScope(Dispatchers.IO).launch { 
            // Perform the voice autosuggest in Dispatchers.IO
            voiceDataSource.autosuggest(
                microphone,
                voiceLanguage,
                null, // W3WAutosuggestOptions
                null, // onSpeechDetected
            ) { result ->
                //Switch to Dispatcher.Main to update your views with the results if needed
                withContext(Dispatchers.IO) {
                    is W3WResult.Failure -> {
                        Log.e("MainActivity", "Error: ${result.message}")
                    }
                    is W3WResult.Success -> {
                        if (result.value.isNotEmpty()) {
                            Log.d("MainActivity", "Suggestions: ${result.value.joinToString { it.w3wAddress.address }}")
                        } else {
                            Log.d("MainActivity", "No suggestions found")
                        } 
                    }
                }
            }
        }
    }
}
```

### Add what3words autosuggest to an existing autosuggest field

#### Using AutosuggestHelper class

Add the api and helper wherever you put your class variables and be sure to use your [API key](https://what3words.com/select-plan):

```Kotlin
val dataSource = W3WApiTextDataSource.create("YOUR_API_KEY_HERE")
val autosuggestOptions = W3WAutosuggestOptions.Builder().
    .focus(...)
    .clipToCountry(...)
    // others options as well
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
    onSuccessListener = { suggestion ->
        Log.d("MainActivity", "suggestion selected from what3words: ${suggesstion.w3wAddress.address}, lat=${suggestion.w3wAddress.center?.lat} lng=${suggestion.w3wAddress.center?.lng}")
    },  
    onFailureListener = {  
	    Log.e("MainActivity", it.message)  
    }  
)
```

***Note*** *that selectedWithCoordinates() will convert the what3words address to a lat/lng which will count against your plan's quota.*

## UX Guidelines

![alt text](https://github.com/what3words/w3w-android-wrapper/blob/master/assets/autosuggest.png?raw=true "Autosuggest UX guideline")

- Once the user has entered the first letter of the 3rd word the autosuggest feature should be displayed
- For simplicity, we recommend only displaying 3 suggested results
- Every address should be accompanied by it’s nearest location.

## Migrate from version 3.x to 4.x

In version 4.0, the API of android-wrapper library changed significantly. This is a guide for gradually adapting the existing code to the new API.

### Introduce of the core libary

[Core library](https://github.com/what3words/w3w-core-library) establishes essential models and interfaces that maintain consistency across various other libraries. Within our Android-wrapper libraries, we've implemented numerous changes by substituting existing models with those from the core library. It's imperative to update your code accordingly to utilize these new models. Please consult the table below for key models and their corresponding replacements:

| Current models    | New models    |
| :------          | :----        |
| com.what3words.javawrapper.response.SuggestionWithCoordinates | com.what3words.core.types.domain.W3WSuggestion          |
| com.what3words.javawrapper.request.AutosuggestOptions         | com.what3words.core.types.options.W3WAutosuggestOptions |
| com.what3words.javawrapper.response.Coordinates               | com.what3words.core.types.geometry.W3WCoordinates       |
| com.what3words.javawrapper.response.Square                    | com.what3words.core.types.geometry.W3WRectangle         |
| com.what3words.core.domain.language.W3WLanguage               | com.what3words.core.types.language.W3WLanguage          |
| com.what3words.javawrapper.request.BoundingBox                | com.what3words.core.types.geometry.W3WRectangle         |
| com.what3words.androidwrapper.voice.Microphone                | com.what3words.core.datasource.voice.audiostream.W3WMicrophone |

Regrettably, there isn't an automated process to convert the current models to the new ones. Therefore, you'll need to manually replace the old models one by one.

### Transition from What3WordsV3 to W3WApiTextDataSource and W3WApiVoiceDataSource

We've restructured What3WordsV3 into two distinct classes, each serving specific functions. 
- W3WApiTextDataSource: Handles text-based tasks like address searching and conversions.
- W3WApiVoiceDataSource: Specializes in voice-based address suggestions.

Please refer to the sections above for usage guidelines on these two classes. The table below offers a concise overview of the methods in What3WordsV3 and their corresponding replacements.

| What3WordsV3                                               |                                                                                                        *DataSource                                                                                                       |
|------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| convertTo3wa(coordinates: Coordinates)                     | W3WApiTextDataSource.convertTo3wa(coordinates: W3WCoordinates, language: W3WLanguage)                                                                                                                                    |
| convertToCoordinates(word: String)                         | W3WApiTextDataSource.convertToCoordinates(word: String)                                                                                                                                                                  |
| autosuggest(input: String)                                 | W3WApiTextDataSource.autosuggest(input: String, options: W3WAutosuggestOptions?,)                                                                                                                                        |
| autosuggest(microphone: Microphone, voiceLanguage: String) | W3WApiVoiceDataSource.autosuggest(input: W3WAudioStream, voiceLanguage: W3WLanguage, options: W3WAutosuggestOptions?, onSpeechDetected: ((String) -> Unit)?, onResult: (result: W3WResult<List<W3WSuggestion>>) -> Unit) |

### Constructor Modifications in AutosuggestHelper

In the previous version, AutosuggestHelper relied on What3WordsV3. Now, we've transitioned to using W3WApiTextDataSource. Therefore, update your code from:

```kotlin
val what3words = What3WordsV3("YOUR_API_KEY_HERE", this)
val autosuggestHelper = AutosuggestHelper(what3words)
```

to:

```kotlin
val dataSource = W3WApiTextDataSource.create("YOUR_API_KEY_HERE")
val autosuggestHelper = AutosuggestHelper(dataSource)
```