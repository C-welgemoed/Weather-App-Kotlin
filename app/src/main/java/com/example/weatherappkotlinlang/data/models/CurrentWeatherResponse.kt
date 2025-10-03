package com.example.weatherappkotlinlang.data.models

import com.google.gson.annotations.SerializedName

// Current Weather Response
data class CurrentWeatherResponse(
    @SerializedName("location")
    val location: Location,
    @SerializedName("current")
    val current: Current
)