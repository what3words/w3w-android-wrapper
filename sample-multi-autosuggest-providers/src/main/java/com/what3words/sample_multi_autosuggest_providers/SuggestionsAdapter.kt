package com.what3words.sample_multi_autosuggest_providers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SuggestionDataModel(
    val type: Type,
    val primaryText: String,
    val secondaryText: String?,
    val data: Any
) {
    enum class Type {
        Google,
        What3words,
    }
}

internal class SuggestionsAdapter(
    private val suggestions: List<SuggestionDataModel>,
    private val callback: ((SuggestionDataModel) -> Unit)?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount() = suggestions.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> {
                val view = layoutInflater.inflate(R.layout.item_google_suggestion, parent, false)
                GooglePlacesViewHolder(view)
            }
            else -> {
                val view = layoutInflater.inflate(R.layout.item_w3w_suggestion, parent, false)
                W3WLocationViewHolder(view)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return suggestions[position].type.ordinal
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? GooglePlacesViewHolder)?.bind(suggestions[position]) { suggestion ->
            callback?.let {
                it(suggestion)
            }
        }
        (holder as? W3WLocationViewHolder)?.bind(suggestions[position]) { suggestion ->
            callback?.let {
                it(suggestion)
            }
        }
    }

    class GooglePlacesViewHolder(
        private val view: View
    ) : RecyclerView.ViewHolder(view) {
        fun bind(
            suggestion: SuggestionDataModel,
            onSuggestionClicked: (SuggestionDataModel) -> Unit
        ) {
            val primaryText = view.findViewById<TextView>(R.id.googlePrimaryText)
            primaryText.text = suggestion.primaryText

            val secondaryText = view.findViewById<TextView>(R.id.googleSecondaryText)
            suggestion.secondaryText?.let {
                secondaryText.text = suggestion.secondaryText
            }
            view.setOnClickListener {
                onSuggestionClicked(suggestion)
            }
        }
    }

    class W3WLocationViewHolder(
        private val view: View
    ) :
        RecyclerView.ViewHolder(view) {
        fun bind(
            suggestion: SuggestionDataModel,
            onSuggestionClicked: (SuggestionDataModel) -> Unit
        ) {
            val w3wAddressLabel = view.findViewById<TextView>(R.id.w3wAddressLabel)
            w3wAddressLabel.text = suggestion.primaryText

            val w3wNearestPlaceLabel = view.findViewById<TextView>(R.id.w3wNearestPlaceLabel)
            suggestion.secondaryText?.let {
                w3wNearestPlaceLabel.text = suggestion.secondaryText
            }
            view.setOnClickListener {
                onSuggestionClicked(suggestion)
            }
        }
    }
}
