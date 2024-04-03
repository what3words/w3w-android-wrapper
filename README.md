# <img src="https://what3words.com/assets/images/w3w_square_red.png" width="64" height="64" alt="what3words">&nbsp;w3w-android-wrapper

[![Maven Central](https://img.shields.io/maven-central/v/com.what3words/w3w-android-wrapper)](https://central.sonatype.com/artifact/com.what3words/w3w-android-wrapper) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=what3words_w3w-android-wrapper&metric=alert_status)](https://sonarcloud.io/dashboard?id=what3words_w3w-android-wrapper)


An Android library to use the [what3words v3 API](https://docs.what3words.com/api/v3/).

API methods are grouped into a single service object which can be centrally managed by a What3WordsV3 instance. It will act as a factory for all of the API endpoints and will automatically initialize them with your API key.

To obtain an API key, please visit [https://what3words.com/select-plan](https://what3words.com/select-plan) and sign up for an account.


### Gradle

```
implementation 'com.what3words:w3w-android-wrapper:3.2.2'
```

## Documentation

See the what3words public API [documentation](https://docs.what3words.com/api/v3/)

## Sample using w3w-android-wrapper library

[api-wrapper-sample](https://github.com/what3words/w3w-android-samples/tree/main/api-wrapper-sample)


## Usage

### convertTo3wa example in kotlin with Coroutines.
Because it is not possible to perform a networking operation on the main application thread, API calls need to be made in a background thread, we used Coroutines in this example. *for more Kotlin examples try our **sample app** in this repo*.

```Kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val wrapper = What3WordsV3("YOUR_API_KEY_HERE", this)
        CoroutineScope(Dispatchers.IO).launch {
            //use wrapper.convertTo3wa() with Dispatcher.IO - background thread
            val result = wrapper.convertTo3wa(Coordinates("51.2305", "-0.24123")).execute()
            CoroutineScope(Dispatchers.Main).launch {
                //use Dispatcher.Main to update your views with the results if needed - Main thread
                if (result.isSuccessful) {
                    Log.i("MainActivity", "3 word address: ${result.words}")
                } else {
                    Log.e("MainActivity", result.error.message)
                }
            }
        }
    }
}
```

### convertTo3wa example in Java with RxJava
Because it is not possible to perform a networking operation on the main application thread, API calls need to be made in a background thread, we used RxJava in this example. *for more Java examples try our **sample-java app** in this repo*

```Java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        What3WordsV3 wrapper = new What3WordsV3("YOUR_API_KEY_HERE", this);
        Observable.fromCallable(() -> wrapper.convertTo3wa(new Coordinates(51.2423, -0.12423)).execute())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.isSuccessful()) {
                        Log.i("MainActivity", String.format("3 word address: %s", result.getWords()));
                    } else {
                        Log.e("MainActivity", result.getError().getMessage());
                    }
                });
    }
}
```

### voice autosuggest example in Kotlin
```Kotlin
val microphone = Microphone().onListening { volume ->
    Log.i("VoiceSample","volume: $volume")
}

wrapper.autosuggest(microphone, "en")
    .focus(Coordinates(51.423, -0.1245))
    .onSuggestions { suggestions ->
        Log.i("VoiceSample", "Suggestions: ${suggestions.joinToString { "${it.words}" }}")
    }.onError { error ->
        Log.e("VoiceSample", error.message)
    }.startListening()
```

### Other available wrapper calls and examples.

- **wrapper.convertToCoordinates()** - Convert a 3 word address to a latitude and longitude
```Kotlin
    val result = wrapper.convertToCoordinates("index.home.raft").execute()
```
- **wrapper.autosuggest()** - AutoSuggest can take a slightly incorrect 3 word address, and suggest a list of valid 3 word addresses. For more autosuggest proprieties similar to *focus* below go to our [documentation](https://developer.what3words.com/public-api/docs#autosuggest)
```Kotlin
    val result = wrapper.autosuggest("index.home.r").focus(51.502,-0.12345).execute()
```
- **wrapper.gridSection()** - Returns a section of the 3m x 3m what3words grid for a bounding box.
```Kotlin
    val result = wrapper.gridSection(BoundingBox(
        Coordinates(51.515900, -0.212517), 
        Coordinates(51.527649, -0.191746)
    )).execute()
```
- **wrapper.availableLanguages()** - Retrieves a list all available 3 word address languages.
```Kotlin
    val result = wrapper.availableLanguages().execute()
```

If you run our Enterprise Suite API Server yourself, you may specify the URL to your own server like so:

```Kotlin
    val wrapper = What3Words("YOUR_API_KEY_HERE", "https://api.yourserver.com")  
```

## Add what3words autosuggest to an existing autosuggest field

### Using AutosuggestHelper class

Add the api and helper wherever you put your class variables and be sure to use your [API key](https://what3words.com/select-plan):
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
## UX Guidelines

![alt text](https://github.com/what3words/w3w-android-wrapper/blob/master/assets/autosuggest.png?raw=true "Autosuggest UX guideline")

- Once the user has entered the first letter of the 3rd word the autosuggest feature should be displayed
- For simplicity, we recommend only displaying 3 suggested results
- Every address should be accompanied by it’s nearest location.

## Full android-wrappper documentation

| Name |
|---|
| [com.what3words.androidwrapper](docs/lib/com.what3words.androidwrapper/index.md) |
| [com.what3words.androidwrapper.helpers](docs/lib/com.what3words.androidwrapper.helpers/index.md) |
| [com.what3words.androidwrapper.voice](docs/lib/com.what3words.androidwrapper.voice/index.md) |
