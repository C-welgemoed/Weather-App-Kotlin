package com.example.weatherappkotlinlang

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.weatherappkotlinlang.data.repository.WeatherRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    companion object {
        private const val SPLASH_MIN_DELAY = 2000L // Minimum 2 seconds
        private const val DATA_TIMEOUT = 10000L // 10 seconds timeout
        private const val DEBUG_MODE = false // Set to false for APK build
        private const val TAG = "SplashActivity"
    }

    @Inject
    lateinit var weatherRepository: WeatherRepository

    private lateinit var loadingText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var startTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        initializeViews()
        startTime = System.currentTimeMillis()

        if (DEBUG_MODE) {
            // Debug mode: Quick splash without data loading
            loadingText.text = "Debug Mode - Quick Start"
            startQuickSplash()
        } else {
            // Production mode: Load actual data
            loadingText.text = "Loading Weather Data..."
            startDataLoadingSplash()
        }
    }

    private fun initializeViews() {
        loadingText = findViewById(R.id.loadingText) ?: TextView(this)
        progressBar = findViewById(R.id.progressBar) ?: ProgressBar(this)
        errorText = findViewById(R.id.errorText) ?: TextView(this)

        progressBar.isVisible = true
        errorText.isVisible = false
    }

    private fun startQuickSplash() {
        scope.launch {
            delay(SPLASH_MIN_DELAY)
            navigateToMainActivity()
        }
    }

    private fun startDataLoadingSplash() {
        scope.launch {
            try {
                // Start data loading with timeout
                val dataLoadJob = async {
                    loadInitialWeatherData()
                }

                // Wait for either data loading or timeout
                withTimeout(DATA_TIMEOUT) {
                    val success = dataLoadJob.await()

                    if (success) {
                        updateStatus("Data loaded successfully!")
                        Log.d(TAG, "Weather data loaded successfully")
                    } else {
                        updateStatus("Using default settings...")
                        Log.w(TAG, "Failed to load weather data, using defaults")
                    }
                }

                // Ensure minimum splash time
                val elapsedTime = System.currentTimeMillis() - startTime
                if (elapsedTime < SPLASH_MIN_DELAY) {
                    delay(SPLASH_MIN_DELAY - elapsedTime)
                }

                navigateToMainActivity()

            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Data loading timeout")
                showError("Connection timeout. Starting with offline mode...")

                delay(2000) // Show error for 2 seconds
                navigateToMainActivity()

            } catch (e: Exception) {
                Log.e(TAG, "Error during splash data loading", e)
                showError("Failed to load data. Starting with offline mode...")

                delay(2000) // Show error for 2 seconds
                navigateToMainActivity()
            }
        }
    }

    private suspend fun loadInitialWeatherData(): Boolean = withContext(Dispatchers.IO) {
        try {
            updateStatus("Testing API connection...")

            // Test API key validity first
            val apiTestResult = weatherRepository.testApiKey()
            if (apiTestResult.isFailure) {
                Log.e(TAG, "API key test failed: ${apiTestResult.exceptionOrNull()?.message}")
                return@withContext false
            }

            if (!apiTestResult.getOrDefault(false)) {
                Log.e(TAG, "API key is invalid")
                return@withContext false
            }

            updateStatus("Loading default location...")

            // Try to load default weather data (Pretoria)
            val weatherResult = weatherRepository.getCurrentWeather("Pretoria")
            weatherResult.onSuccess { forecast ->
                Log.d(TAG, "Successfully loaded weather for ${forecast.cityName}: ${forecast.condition}")
                updateStatus("Weather data ready!")
                return@withContext true
            }.onFailure { exception ->
                Log.e(TAG, "Failed to load default weather data: ${exception.message}")
                return@withContext false
            }

            return@withContext false

        } catch (e: Exception) {
            Log.e(TAG, "Exception during data loading", e)
            return@withContext false
        }
    }

    private fun updateStatus(message: String) {
        scope.launch(Dispatchers.Main) {
            loadingText.text = message
            Log.d(TAG, "Status: $message")
        }
    }

    private fun showError(message: String) {
        scope.launch(Dispatchers.Main) {
            loadingText.isVisible = false
            progressBar.isVisible = false
            errorText.text = message
            errorText.isVisible = true
            Log.e(TAG, "Error: $message")
        }
    }

    private fun navigateToMainActivity() {
        scope.launch(Dispatchers.Main) {
            try {
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to navigate to MainActivity", e)
                // Fallback: try again after a short delay
                delay(1000)
                try {
                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } catch (e2: Exception) {
                    Log.e(TAG, "Critical error: Cannot start MainActivity", e2)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}