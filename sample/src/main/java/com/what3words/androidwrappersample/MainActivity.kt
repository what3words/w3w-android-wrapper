package com.what3words.androidwrappersample

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.androidwrapper.voice.VoiceBuilder
import com.what3words.javawrapper.request.Coordinates
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private var builder: VoiceBuilder? = null
    private val wrapper by lazy {
        What3WordsV3("YOUR_API_KEY_HERE", this)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //convert-to-3wa sample
        buttonConvertTo3wa.setOnClickListener {
            val latLong = textInputConvertTo3wa.text?.replace("\\s".toRegex(), "")?.split(",")
                ?.filter { it.isNotEmpty() }
            val lat = latLong?.getOrNull(0)?.toDoubleOrNull()
            val long = latLong?.getOrNull(1)?.toDoubleOrNull()
            if (lat != null && long != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    //use wrapper.convertTo3wa() with Dispatcher.IO - background thread
                    val result = wrapper.convertTo3wa(Coordinates(lat, long)).execute()
                    CoroutineScope(Dispatchers.Main).launch {
                        //use Dispatcher.Main to update your views with the results - Main thread
                        if (result.isSuccessful) {
                            resultConvertTo3wa.text = "3 word address: ${result.words}"
                        } else {
                            resultConvertTo3wa.text = result.error.message
                        }
                    }
                }
            } else {
                resultConvertTo3wa.text = "invalid lat,long"
            }
        }

        //convert-to-coordinates sample
        buttonConvertToCoordinates.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                //use wrapper.convertToCoordinates() with Dispatcher.IO - background thread
                val result =
                    wrapper.convertToCoordinates(textInputConvertToCoordinates.text.toString())
                        .execute()
                CoroutineScope(Dispatchers.Main).launch {
                    //use Dispatcher.Main to update your views with the results - Main thread
                    if (result.isSuccessful) {
                        resultConvertToCoordinates.text =
                            "Coordinates: ${result.coordinates.lat}, ${result.coordinates.lng}"
                    } else {
                        resultConvertToCoordinates.text = result.error.message
                    }
                }
            }
        }

        //text autosuggest sample
        buttonAutoSuggest.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                //use wrapper.autosuggest() with Dispatcher.IO - background thread
                val result = wrapper.autosuggest(textInputAutoSuggest.text.toString()).execute()
                CoroutineScope(Dispatchers.Main).launch {
                    //use Dispatcher.Main to update your views with the results - Main thread
                    if (result.isSuccessful) {
                        resultAutoSuggest.text = if (result.suggestions.count() != 0)
                            "Suggestions: ${result.suggestions.joinToString { it.words }}"
                        else "No suggestions available"
                    } else {
                        resultAutoSuggest.text = result.error.message
                    }
                }
            }
        }

        val microphone = VoiceBuilder.Microphone().onListening {
            it?.let { volume ->
                volumeAutoSuggestVoice.text =
                    "volume: ${(volume.times(100).roundToInt())}"
            }
        }

        //voice autosuggest sample
        builder = wrapper.autosuggest(microphone, "en")
            .onSuggestions { suggestions ->
                buttonAutoSuggestVoice.setIconResource(R.drawable.ic_record)
                resultAutoSuggestVoice.text =
                    "Suggestions: ${suggestions.joinToString { it.words }}"
            }.onError { error ->
                buttonAutoSuggestVoice.setIconResource(R.drawable.ic_record)
                resultAutoSuggestVoice.text = error
            }

        buttonAutoSuggestVoice.setOnClickListener {
            if (builder?.isListening() == true) {
                buttonAutoSuggestVoice.setIconResource(R.drawable.ic_record)
                builder?.stopListening()
            } else {
                //Check if RECORD_AUDIO permission is granted
                val permission =
                    PermissionChecker.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)

                if (permission == PermissionChecker.PERMISSION_GRANTED) {
                    buttonAutoSuggestVoice.setIconResource(R.drawable.ic_stop)
                    builder?.startListening()
                } else {
                    //request RECORD_AUDIO permission
                    ActivityCompat.requestPermissions(this,  arrayOf(Manifest.permission.RECORD_AUDIO), 1)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.count() == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            buttonAutoSuggestVoice.setIconResource(R.drawable.ic_stop)
            builder?.startListening()
        } else {
            resultAutoSuggestVoice.text = "record audio permission required"
        }
    }
}