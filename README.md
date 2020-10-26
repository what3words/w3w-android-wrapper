# <img src="https://what3words.com/assets/images/w3w_square_red.png" width="64" height="64" alt="what3words">&nbsp;w3w-android-wrapper

An Android library to use the [what3words v3 API](https://docs.what3words.com/api/v3/).

API methods are grouped into a single service object which can be centrally managed by a What3WordsV3 instance. It will act as a factory for all of the API endpoints and will automatically initialize them with your API key.

To obtain an API key, please visit [https://what3words.com/select-plan](https://what3words.com/select-plan) and sign up for an account.

## Installation

The artifact is available through <a href="https://search.maven.org/search?q=g:com.what3words">Maven Central</a>.

### Gradle

```
implementation 'com.what3words:w3w-android-wrapper:3.1.4'
```

## Documentation

See the what3words public API [documentation](https://docs.what3words.com/api/v3/)

## Usage

AndroidManifest.xml
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.yourpackage.yourapp">

    <uses-permission android:name="android.permission.INTERNET" />

    <!-- add if using voice api autosuggest -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
```

build.gradle (app level)
```gradle
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    dependencies {
        ...
        // we are going to use coroutines for kotlin examples, feel free to use any other library of your choice.
        implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7"
        implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.7"

        // we are going to use rxjava for java examples, feel free to use any other library of your choice.
        implementation 'io.reactivex.rxjava3:rxjava:3.0.7'
        implementation 'io.reactivex.rxjava3:rxandroid:3.0.0'
    }
```

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
                }));
    }
}
```

### voice autosuggest example in Kotlin
```Kotlin
val microphone = VoiceBuilder.Microphone().onListening { volume ->
    Log.i("VoiceSample","volume: $volume")
}

wrapper.autosuggest(microphone, "en")
    .focus(51.423, -0.1245)
    .onSuggestions { suggestions ->
        Log.i("VoiceSample","Suggestions: ${suggestions.joinToString { it.words }}")
    }.onError { error ->
        Log.e("VoiceSample", error)
    }.startListening()
```
*Note: You will need AUDIO_RECORD permission to use our Suggestion Voice API, for examples how to handle the permission request check our sample and sample-java apps in this repo.*

### Other available wrapper calls and examples.

- **wrapper.convertToCoordinates()** - Convert a 3 word address to a latitude and longitude
```Kotlin
    val result = wrapper.convertToCoordinates("index.home.raft").execute()
```
- **wrapper.autosuggest()** - AutoSuggest can take a slightly incorrect 3 word address, and suggest a list of valid 3 word addresses. For autosuggest filters like *focus* below go to :LINK DEV HERE:
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