package com.example.weatherappkotlinlang.data.repository

import android.content.Context
import com.example.weatherappkotlinlang.data.api.WeatherApiService
import com.example.weatherappkotlinlang.data.models.CurrentWeatherResponse
import com.example.weatherappkotlinlang.data.models.LocationSearchResult
import com.example.weatherappkotlinlang.data.models.WeatherForecast
import com.example.weatherappkotlinlang.data.security.ApiKeyProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class WeatherRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: WeatherApiService,
    private val apiKeyProvider: ApiKeyProvider
) {

    suspend fun searchLocations(query: String): Result<List<LocationSearchResult>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchLocations(
                apiKey = apiKeyProvider.getApiKey(),
                query = query
            )

            when {
                response.isSuccessful -> {
                    response.body()?.let { locations ->
                        Result.success(locations)
                    } ?: Result.failure(Exception("No locations found"))
                }
                response.code() == 401 -> {
                    Result.failure(Exception("Invalid API key. Please check your WeatherAPI key."))
                }
                response.code() == 403 -> {
                    Result.failure(Exception("API quota exceeded or access forbidden."))
                }
                response.code() == 429 -> {
                    Result.failure(Exception("Too many requests. Please wait and try again."))
                }
                else -> {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception("API Error: ${response.code()} - ${response.message()}\nDetails: $errorBody"))
                }
            }
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("Request timeout. Please check your internet connection."))
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("No internet connection. Please check your network."))
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun getCurrentWeather(locationQuery: String): Result<WeatherForecast> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCurrentWeather(
                apiKey = apiKeyProvider.getApiKey(),
                query = locationQuery
            )

            when {
                response.isSuccessful -> {
                    response.body()?.let { weatherResponse ->
                        val forecast = mapToWeatherForecast(weatherResponse)
                        Result.success(forecast)
                    } ?: Result.failure(Exception("No weather data found"))
                }
                response.code() == 401 -> {
                    Result.failure(Exception("Invalid API key. Please check your WeatherAPI key."))
                }
                response.code() == 403 -> {
                    Result.failure(Exception("API quota exceeded or access forbidden."))
                }
                response.code() == 429 -> {
                    Result.failure(Exception("Too many requests. Please wait and try again."))
                }
                response.code() == 400 -> {
                    Result.failure(Exception("Invalid location. Please try a different search term."))
                }
                else -> {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception("API Error: ${response.code()} - ${response.message()}\nDetails: $errorBody"))
                }
            }
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("Request timeout. Please check your internet connection."))
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("No internet connection. Please check your network."))
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    private fun mapToWeatherForecast(response: CurrentWeatherResponse): WeatherForecast {
        val currentDate = Date()
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        return WeatherForecast(
            cityName = response.location.name,
            country = response.location.country,
            currentTemp = response.current.tempC.roundToInt(),
            condition = response.current.condition.text,
            humidity = response.current.humidity,
            windSpeed = "${response.current.windKph.roundToInt()} km/h ${response.current.windDir}",
            pressure = "${response.current.pressureMb.roundToInt()} hPa",
            visibility = "${response.current.visKm.roundToInt()} km",
            feelsLike = response.current.feelslikeC.roundToInt(),
            uvIndex = response.current.uv,
            date = dateFormat.format(currentDate),
            dayOfWeek = dayFormat.format(currentDate),
            isDay = response.current.isDay == 1
        )
    }

    // Test API key validity
    suspend fun testApiKey(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCurrentWeather(
                apiKey = apiKeyProvider.getApiKey(),
                query = "London"
            )

            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}