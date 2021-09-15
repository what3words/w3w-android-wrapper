package com.what3words.sample_multi_autosuggest_providers

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.androidwrapper.helpers.AutosuggestHelper
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.Suggestion

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val query = findViewById<EditText>(R.id.edit_query)
        val recyclerView = findViewById<RecyclerView>(R.id.list_suggestions)

        // what3words setup with static filters
        val what3words = What3WordsV3("YOUR_WHAT3WORDS_API_KEY_HERE", this)
        val autosuggestHelper = AutosuggestHelper(what3words).focus(Coordinates(51.2, 41.2))

        // google place setup
        Places.initialize(applicationContext, "YOUR_GOOGLE_PLACES_ANDROID_SDK_KEY_HERE")
        val placesClient = Places.createClient(this)

        // setup recycler view
        val suggestionList = mutableListOf<SuggestionDataModel>()
        val adapter = SuggestionsAdapter(suggestionList) { suggestion ->
            // if what3words use autosuggestHelper.selectedWithCoordinates to get coordinates for the selected suggestion
            if (suggestion.type == SuggestionDataModel.Type.What3words) {
                autosuggestHelper.selectedWithCoordinates(
                    query.text.toString(),
                    suggestion.data as Suggestion,
                    onSuccessListener = { w3wWithCoordinates ->
                        Toast.makeText(
                            this,
                            "suggestion selected from what3words, ${w3wWithCoordinates.words}, ${w3wWithCoordinates.coordinates.lat} ${w3wWithCoordinates.coordinates.lng}",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    onFailureListener = {
                        Log.e("MainActivity", it.message)
                    }
                )
            } else {
                Toast.makeText(
                    this,
                    "suggestion selected from ${suggestion.type}, ${suggestion.primaryText}, ${suggestion.secondaryText}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        query.doOnTextChanged { text, _, _, _ ->
            // empty recycler on textChanged
            suggestionList.clear()
            adapter.notifyDataSetChanged()

            // if you don't want to mix providers in the same list please use our regex, i.e:
            //
            //    if(text.toString().isPossible3wa()) {
            //        autosuggestHelper.update()
            //    } else {
            //        call others providers
            //    }
            autosuggestHelper.update(
                text.toString(),
                onSuccessListener = { suggestionResults ->
                    suggestionResults.forEach { suggestion ->
                        suggestionList.add(
                            SuggestionDataModel(
                                SuggestionDataModel.Type.What3words,
                                suggestion.words,
                                suggestion.nearestPlace,
                                suggestion
                            )
                        )
                        Log.i("MainActivity", suggestion.words)
                    }
                    adapter.notifyDataSetChanged()
                },
                onFailureListener = {
                    Log.e("MainActivity", it.message)
                }
            )

            // use other autosuggest providers, i.e: Google places.
            val request =
                FindAutocompletePredictionsRequest.builder()
                    .setSessionToken(AutocompleteSessionToken.newInstance())
                    .setQuery(text.toString())
                    .build()

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                    for (prediction in response.autocompletePredictions) {
                        suggestionList.add(
                            SuggestionDataModel(
                                SuggestionDataModel.Type.Google,
                                prediction.getPrimaryText(null).toString(),
                                prediction.getSecondaryText(null).toString(),
                                prediction
                            )
                        )
                        Log.i("MainActivity", prediction.placeId)
                        Log.i("MainActivity", prediction.getPrimaryText(null).toString())
                    }
                    adapter.notifyDataSetChanged()
                }.addOnFailureListener { exception: Exception? ->
                    if (exception is ApiException) {
                        Log.e("MainActivity", "Place not found: " + exception.statusCode)
                    }
                }
        }
    }
}
