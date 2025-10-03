package com.example.weatherappkotlinlang.data.security
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import android.util.Base64

class SecureApiKeyManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "encrypted_weather_prefs"
        private const val API_KEY_PREF = "api_key"
        private const val ALGORITHM = "AES/CBC/PKCS7Padding"
        private const val KEY_ALIAS = "weather_app_key"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun storeApiKey(apiKey: String) {
        try {
            encryptedPrefs.edit()
                .putString(API_KEY_PREF, apiKey)
                .apply()
        } catch (e: Exception) {
            throw SecurityException("Failed to store API key securely", e)
        }
    }

    fun getApiKey(): String? {
        return try {
            encryptedPrefs.getString(API_KEY_PREF, null)
        } catch (e: Exception) {
            null
        }
    }

    fun clearApiKey() {
        encryptedPrefs.edit()
            .remove(API_KEY_PREF)
            .apply()
    }

    fun hasApiKey(): Boolean {
        return getApiKey() != null
    }
}