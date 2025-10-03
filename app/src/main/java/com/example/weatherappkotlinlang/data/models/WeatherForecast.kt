package com.example.weatherappkotlinlang.data.models

data class WeatherForecast(
    val cityName: String,
    val country: String,
    val currentTemp: Int,
    val condition: String,
    val humidity: Int,
    val windSpeed: String,
    val pressure: String,
    val visibility: String,
    val feelsLike: Int,
    val uvIndex: Double,
    val date: String,
    val dayOfWeek: String,
    val isDay: Boolean
)

// Weather condition types for background selection (hardcoded as enum clss)
enum class WeatherCondition(val keywords: List<String>) {
    SUNNY(listOf("sunny", "clear", "bright")),
    RAINY(listOf("rain", "drizzle", "shower", "storm", "thunderstorm")),
    CLOUDY(listOf("cloud", "overcast", "partly", "mostly")),
    SNOWY(listOf("snow", "blizzard", "flurries"));

    companion object {
        fun fromCondition(condition: String): WeatherCondition {
            val lowerCondition = condition.lowercase()
            return values().firstOrNull { weatherCondition ->
                weatherCondition.keywords.any { keyword ->
                    lowerCondition.contains(keyword)
                }
            } ?: SUNNY
        }
    }
}