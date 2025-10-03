package com.example.weatherappkotlinlang.data.security

import android.content.Context
import android.util.Log
import com.example.weatherappkotlinlang.BuildConfig

class ApiKeyProvider(private val context: Context) {

    private val secureApiKeyManager = SecureApiKeyManager(context)

    // Using the API key from the WeatherAPI documentation
    private val defaultApiKey = BuildConfig.WEATHER_API_KEY
    fun getApiKey(): String {
        // Clear any old cached keys and always use the correct key
        secureApiKeyManager.clearApiKey()
        secureApiKeyManager.storeApiKey(defaultApiKey)

        // Log the API key being used (remove this in production)
        Log.d("ApiKeyProvider", "Using API key: $defaultApiKey")

        return defaultApiKey
    }

    fun updateApiKey(newApiKey: String) {
        secureApiKeyManager.storeApiKey(newApiKey)
    }

    fun clearApiKey() {
        secureApiKeyManager.clearApiKey()
    }
}