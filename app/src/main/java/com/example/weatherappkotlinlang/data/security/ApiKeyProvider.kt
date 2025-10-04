package com.example.weatherappkotlinlang.data.security

import android.content.Context
import android.util.Log
import com.example.weatherappkotlinlang.BuildConfig

class ApiKeyProvider(private val context: Context) {

    private val secureApiKeyManager = SecureApiKeyManager(context)

    private val defaultApiKey = BuildConfig.WEATHER_API_KEY

    fun getApiKey(): String {
        secureApiKeyManager.clearApiKey()
        secureApiKeyManager.storeApiKey(defaultApiKey)

        Log.d("ApiKeyProvider", "API key loaded")

        return defaultApiKey
    }

    fun updateApiKey(newApiKey: String) {
        secureApiKeyManager.storeApiKey(newApiKey)
    }

    fun clearApiKey() {
        secureApiKeyManager.clearApiKey()
    }
}