package com.what3words.androidwrapper.datasource.text.api.dto

data class SuggestionDto(
    val words: String,
    val country: String,
    val nearestPlace: String,
    val rank: Int,
    val language: String,
    val locale: String?,
    val distanceToFocusKm: Int?,
    val square: SquareDto?,
    val coordinates: CoordinatesDto?
)