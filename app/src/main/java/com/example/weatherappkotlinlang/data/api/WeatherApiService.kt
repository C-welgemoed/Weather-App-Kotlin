package com.example.weatherappkotlinlang.data.api

import com.example.weatherappkotlinlang.data.models.CurrentWeatherResponse
import com.example.weatherappkotlinlang.data.models.LocationSearchResult
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("v1/current.json")
    suspend fun getCurrentWeather(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("aqi") aqi: String = "no"
    ): Response<CurrentWeatherResponse>

    @GET("v1/search.json")
    suspend fun searchLocations(
        @Query("key") apiKey: String,
        @Query("q") query: String
    ): Response<List<LocationSearchResult>>
}