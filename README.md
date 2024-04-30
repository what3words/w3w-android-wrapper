# <img src="https://what3words.com/assets/images/w3w_square_red.png" width="64" height="64" alt="what3words">&nbsp;w3w-android-wrapper

[![Maven Central](https://img.shields.io/maven-central/v/com.what3words/w3w-android-wrapper)](https://central.sonatype.com/artifact/com.what3words/w3w-android-wrapper) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=what3words_w3w-android-wrapper&metric=alert_status)](https://sonarcloud.io/dashboard?id=what3words_w3w-android-wrapper)

An Android library to use the [what3words v3 API](https://docs.what3words.com/api/v3/).

## Table of contents
- [Table of contents](#table-of-contents)
- [Useful links](#useful-links)
- [Installation](#installation)
- [Method Overview](#method-overview)
- [Usage](#usage)
  - [W3WApiTextDataSource](#w3wapitextdatasource)
    - [Get the instance](#get-the-w3wapitextdatasource)
    - [convertTo3wa](#convertto3wa-example)
    - [convertToCoordinates](#converttocoordinates-example)
    - [AutoSuggest](#autosuggest-example)
    - [availableLanguages](#availablelanguages-example)
    - [gridSection](#gridsection-example)
  - [W3WApiVoiceDataSource](#w3wapivoicedatasource)
    - [Get the instance](#create-a-w3wapivoicedatasource-instance)
    - [Initialize the microphone](#create-a-w3wmicrophone-instance-to-handle-voice-recording)
    - [Set up microphone callbacks](#you-can-set-up-callbacks-to-receive-information-about-the-recording-progress)
    - [Perform Voice AutoSuggest](#perform-voice-autosuggest)
- [Integrate what3words to the existing textfield](#add-what3words-autosuggest-to-an-existing-autosuggest-field)
- [UX guidelines](#ux-guidelines)
- [Migration from version 3.x to 4.x](#migrate-from-version-3x-to-4x)
  - [Introduce of the core library](#introduce-of-the-core-library)
  - [Transition from What3WordsV3 to W3WApiTextDataSource and W3WApiVoiceDataSource](#transition-from-what3wordsv3-to-w3wapitextdatasource-and-w3wapivoicedatasource)
    - [convertToCoordinates](#converttocoordinates-1)
    - [convertTo3wa](#convertto3wa-1)
    - [AutoSuggest](#autosuggest-1)
    - [availableLanguages](#availablelanguages-1)
    - [gridSection](#gridsection-1)
    - [AutosuggestHelper constructor change](#autosuggesthelper-1)

## Useful Links
[what3words API documentation](https://docs.what3words.com/api/v3/)

[Sign up and select a plan](https://what3words.com/select-plan)

[Sample app](https://github.com/what3words/w3w-android-samples/tree/main/api-wrapper-sample)

## Installation

### Gradle

To integrate the library into your project, add the following dependency:
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

#### Snapshots
We deploy snapshot versions of the library to [Sonatype's snapshot repository](https://s01.oss.sonatype.org/content/repositories/snapshots/). These snapshots are generated after every merge to an Epic branch, providing an easy way to test the latest unreleased changes and upcoming updates without waiting for the next official version release.

To use snapshot versions in your project, add the snapshot repository to your Gradle script:

```
repositories {
    maven {
        url 'https://s01.oss.sonatype.org/content/repositories/snapshots/'
    }
}
```
Then, update your dependencies to use the snapshot version:

```
dependencies {
    implementation 'com.what3words:w3w-android-wrapper:4.0.0-SNAPSHOT'
}
```
The latest snapshot versions are available [here](https://s01.oss.sonatype.org/content/repositories/snapshots/com/what3words/w3w-android-wrapper/).

### Android Manifest
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.yourpackage.yourapp">

    <uses-permission android:name="android.permission.INTERNET" />

    <!-- add if using voice api autosuggest -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
</manifest>
```

## Method Overview
This table offers a succinct overview of the methods available in this library along with their descriptions.

| DataSource method     | Function                | Description                                                                                                      |
|-----------------------|-------------------------|------------------------------------------------------------------------------------------------------------------|
| W3WApiTextDataSource  | create                  | Create an instance of W3WApiTextDataSource with your API key and optional server endpoint.                       |
| W3WApiTextDataSource  | convertTo3wa            | Convert latitude and longitude to a what3words address.                                                          |
| W3WApiTextDataSource  | convertToCoordinates    | Convert a what3words address to latitude and longitude.                                                          |
| W3WApiTextDataSource  | autosuggest             | Get suggestions for a slightly incomplete what3words address.                                                    |
| W3WApiTextDataSource  | availableLanguages      | Retrieve a set of all available languages that what3words supports.                                              |
| W3WApiTextDataSource  | gridSection             | Get a section of the 3m x 3m what3words grid for a bounding box and convert it into GeoJSON format.              |
| W3WApiVoiceDataSource | create                  | Create an instance of W3WApiVoiceDataSource with your API key and optional server endpoint.                      |
| W3WApiVoiceDataSource | autosuggest             | Perform voice autosuggestion using a microphone input.                                                           |
| AutosuggestHelper     | update                  | Update autosuggestion options dynamically.                                                                       |
| AutosuggestHelper     | selected                | Retrieve the full what3words address once the user has selected a row from the RecyclerView without coordinates. |
| AutosuggestHelper     | selectedWithCoordinates | Retrieve the full what3words address with coordinates once the user has selected a row from the RecyclerView.    |

## Usage
This example demonstrates the usage of the library using Kotlin Coroutines. For a cleaner architecture or Java example, you can refer to our [sample app](https://github.com/what3words/w3w-android-samples/tree/main/api-wrapper-sample).

### W3WApiTextDataSource
The W3WApiTextDataSource class facilitates the conversion of coordinates to what3words addresses and vice versa, as well as providing suggestions for slightly incomplete what3words addresses.

#### Get the W3WApiTextDataSource

Create an instance of W3WApiTextDataSource via the factory method.
```kotlin
val textDataSource = W3WApiTextDataSource.create(context, "YOUR_API_KEY")
```

If you are running your own Enterprise Suite API Server, you can specify the URL to your server:

```kotlin
val textDataSource = W3WApiTextDataSource.create(context, "YOUR_API_KEY", "YOUR_SERVER_ENPOINT")
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
                        Log.d("MainActivity", "Coordinates: ${result.value.center.lat}, ${result.value.center.lng}")
                    }
                }
            }
        }
    }
}
```

#### AutoSuggest example
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

#### Create a W3WApiVoiceDataSource instance
```kotlin
val voiceDataSource = W3WApiVoiceDataSource("YOUR_API_KEY")
```

If you are running your own Enterprise Suite API Server, you can specify the URL to your server:

```kotlin
val voiceDataSource = W3WApiVoiceDataSource("YOUR_API_KEY", "YOUR_SERVER_ENDPOINT")
```

#### Create a W3WMicrophone instance to handle voice recording.

```kotlin
val microphone = W3WMicrophone()
```

#### You can set up callbacks to receive information about the recording progress.

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

#### Perform Voice AutoSuggest

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ...
        
        val voiceLanguage = W3WRFC5646Language.EN_GB

        CoroutineScope(Dispatchers.IO).launch { 
            // Perform the Voice AutoSuggest in Dispatchers.IO
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

### Add what3words AutoSuggest to an existing text field

#### Using AutosuggestHelper class

Add the api and helper wherever you put your class variables and be sure to use your [API key](https://what3words.com/select-plan):

```Kotlin
val dataSource = W3WApiTextDataSource.create(context, "YOUR_API_KEY_HERE")
val autosuggestOptions = W3WAutosuggestOptions.Builder().
    .focus(...)
    .clipToCountry(...)
    // others options as well
    .build()

val autosuggestHelper = AutosuggestHelper(dataSource).options(autosuggestOptions)
```
Next step is to use a TextWatcher (or doOnTextChanged EditText extension) and let **AutoSuggestHelper** know about the changed text and add what3words suggestion data to your existing RecyclerView/Adapter. (check sample for complete working example with [custom data model and RecyclerView adapter](https://github.com/what3words/w3w-android-wrapper/blob/master/sample-multi-autosuggest-providers/src/main/java/com/what3words/sample_multi_autosuggest_providers/SuggestionsAdapter.kt) to show different autosuggest sources and [EditText and RecyclerView](https://github.com/what3words/w3w-android-wrapper/blob/master/sample-multi-autosuggest-providers/src/main/java/com/what3words/sample_multi_autosuggest_providers/MainActivity.kt) setup.

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

- Once the user has entered the first letter of the 3rd word the AutoSuggest feature should be displayed
- For simplicity, we recommend only displaying 3 suggested results
- Every address should be accompanied by its nearest location.

## Migrate from version 3.x to 4.x

In version 4.0, the API of android-wrapper library changed significantly. This is a guide for gradually adapting the existing code to the new API.

### Introduce of the core library

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

#### Method changes list

---
#### <code style="color : LightSkyBlue">convertToCoordinates</code>
**In version 3.x**
<br>
```kotlin
val wrapper = What3WordsV3(API_KEY, activityContext)
val result = wrapper.convertToCoordinates(words).execute()
```
Parameters: 
- words: ```String```

Return ```ConvertToCoordinates```
<br>

**In version 4.0**
<br>
```kotlin
val textDataSource = W3WApiTextDataSource.create(context, API_KEY) 
val result = textDataSource.convertToCoordinates(words) // Must run on background thread
```
Parameters: 
- words: ```String```

Return ```W3WResult<W3WAddress>```

For more details and instructions, see [Convert to coordinates example](#converttocoordinates-example).
<br><br>

---

#### <code style="color : LightSkyBlue">convertTo3wa</code>
**In version 3.x**
<br>
```kotlin
val wrapper = What3WordsV3(API_KEY, activityContext)
val result = wrapper.convertTo3wa(coordinates).execute()
```
Parameters: 
- coordinates: ```Coordinates```

Return ```ConvertTo3WA```
<br>

**In version 4.0**
<br>
```kotlin
val textDataSource = W3WApiTextDataSource.create(context, API_KEY) 
val result = textDataSource.convertTo3wa(coordinates, language) // Must run on background thread
```
Parameters: 
- coordinates: ```W3WCoordinates```
- language: ```W3WLanguage```

Return ```W3WResult<W3WAddress>```

For more details and instructions, see [Convert to what3words address example](#convertto3wa-example).
<br><br>

---

#### <code style="color : LightSkyBlue">AutoSuggest</code>
**In version 3.x**
<br>
```kotlin
val wrapper = What3WordsV3(API_KEY, activityContext)
val result = wrapper.autosuggest(word).execute()
```
Parameters: 
- word: ```String```

Return ```Autosuggest```
<br>

**In version 4.0**
<br>
```kotlin
val textDataSource = W3WApiTextDataSource.create(context, API_KEY) 
val result = textDataSource.autosuggest(word, options) // Must run on background thread
```
Parameters: 
- word: ```String```
- options: ```W3WAutosuggestOptions?```

Return ```W3WResult<List<W3WSuggestion>>```

For more details and instructions, see [Autosuggest example](#autosuggest-example).
<br><br>

---

#### <code style="color : LightSkyBlue">availableLanguages</code>
**In version 3.x**
<br>
```kotlin
val wrapper = What3WordsV3(API_KEY, activityContext)
val result = wrapper.availableLanguages().execute()
```

Return ```AvailableLanguages```
<br>

**In version 4.0**
<br>
```kotlin
val textDataSource = W3WApiTextDataSource.create(context, API_KEY) 
val result = textDataSource.availableLanguages() // Must run on background thread
```
Return ```W3WResult<Set<W3WProprietaryLanguage>>```

For more details and instructions, see [Get available languages example](#availablelanguages-example).
<br><br>

---

#### <code style="color : LightSkyBlue">gridSection</code>
**In version 3.x**
<br>
```kotlin
val wrapper = What3WordsV3(API_KEY, activityContext)
val result = wrapper.gridSection(boudingBox).execute()
```
Parameters:
- boudingBox: ```BoundingBox```

Return ```GridSection```
<br>

**In version 4.0**
<br>
```kotlin
val textDataSource = W3WApiTextDataSource.create(context, API_KEY) 
val result = textDataSource.gridSection(boundingBox) // Must run on background thread
```
Parameters:
- boudingBox: ```W3WRectangle```

Return ```W3WResult<W3WGridSection>```

For more details and instructions, see [Grid section example](#gridsection-example).
<br><br>

---

#### <code style="color : LightSkyBlue">Voice AutoSuggest</code>
**In version 3.x**
<br>
```kotlin
val wrapper = What3WordsV3(API_KEY, activityContext)
val result = wrapper.autosuggest(microphone, voiceLanguage)
    .onSuggstions { suggestions ->
        // Handle the suggestions
    }
    .onError { error ->
        // Handle the error
    }
```
Parameters:
- mircophone: ```Microphone```
- voiceLanguage: ```String```

Return:
- suggestions: ```List<Suggestion>!```
- error: ```APIResponse.What3WordsError!```
<br>

**In version 4.0**
<br>
```kotlin
val voiceDataSource = W3WApiVoiceDataSource.create(API_KEY) 
val result = voiceDataSource.autosuggest(
        audioStream,
        voiceLanguage,
        options,
        onSpeechDetected,
    ) { result ->
        // Handle result
    }
```
Parameters:
- input: ```W3WAudioStream```
- voiceLanguage: ```W3WLanguage```,
- options: ```W3WAutosuggestOptions?```,
- onSpeechDetected: ```((String) -> Unit)?``` 

Return
- result: ```W3WResult<List<W3WSuggestion>>```

For more details and instructions, see [Voice autosugge example](#w3wapivoicedatasource).
<br><br>

---

#### <code style="color : LightSkyBlue">AutosuggestHelper</code>
In the previous version, AutosuggestHelper relied on What3WordsV3. Now, we've transitioned to using W3WApiTextDataSource. 

Therefore, update your code from:
<br>
```kotlin
val what3words = What3WordsV3("YOUR_API_KEY_HERE", this)
val autosuggestHelper = AutosuggestHelper(what3words)
```
<br>
To:
<br>

```kotlin
val dataSource = W3WApiTextDataSource.create(context, "YOUR_API_KEY_HERE")
val autosuggestHelper = AutosuggestHelper(dataSource)
```

For more details and instructions, see [AutoSuggestHelper](#using-autosuggesthelper-class).
<br><br>