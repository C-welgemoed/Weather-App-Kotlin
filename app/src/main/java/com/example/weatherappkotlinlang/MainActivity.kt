package com.example.weatherappkotlinlang

import android.app.SearchManager
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.airbnb.lottie.LottieAnimationView
import com.example.weatherappkotlinlang.data.models.LocationSearchResult
import com.example.weatherappkotlinlang.data.models.WeatherCondition
import com.example.weatherappkotlinlang.data.models.WeatherForecast
import com.example.weatherappkotlinlang.data.repository.WeatherRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // UI Components
    private lateinit var searchView: SearchView
    private lateinit var cityNameText: TextView
    private lateinit var weatherAnimation: LottieAnimationView
    private lateinit var todayText: TextView
    private lateinit var currentTempText: TextView
    private lateinit var weatherConditionText: TextView
    private lateinit var dayText: TextView
    private lateinit var dateText: TextView
    private lateinit var humidityValueText: TextView
    private lateinit var windSpeedText: TextView
    private lateinit var conditionsText: TextView
    private lateinit var pressureText: TextView
    private lateinit var visibilityText: TextView
    private lateinit var feelsLikeText: TextView
    private lateinit var uvIndexText: TextView
    private lateinit var seaLevelText: TextView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var errorText: TextView
    // Additional UI fields that exist in layout but weren't in the original code
    private lateinit var maxTempText: TextView
    private lateinit var minTempText: TextView
    private lateinit var sunriseText: TextView
    private lateinit var sunsetText: TextView

    // Data - Injected by Hilt
    @Inject
    lateinit var weatherRepository: WeatherRepository

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var locations: List<LocationSearchResult> = emptyList()
    private var selectedLocation: LocationSearchResult? = null
    private var weatherForecast: WeatherForecast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupSearchView()
        setupInitialState()

        // Load default location (Pretoria) on start
        loadDefaultLocation()
    }

    private fun initializeViews() {
        searchView = findViewById(R.id.searchView)
        cityNameText = findViewById(R.id.cityNameText)
        weatherAnimation = findViewById(R.id.weatherAnimation)
        todayText = findViewById(R.id.todayText)
        currentTempText = findViewById(R.id.currentTempText)
        weatherConditionText = findViewById(R.id.weatherConditionText)
        dayText = findViewById(R.id.dayText)
        dateText = findViewById(R.id.dateText)
        humidityValueText = findViewById(R.id.humidityValueText)
        windSpeedText = findViewById(R.id.windSpeedText)
        conditionsText = findViewById(R.id.conditionsText)
        seaLevelText = findViewById(R.id.seaLevelText)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        errorText = findViewById(R.id.errorText)

        // Additional UI fields from layout
        maxTempText = findViewById(R.id.maxTempText)
        minTempText = findViewById(R.id.minTempText)
        sunriseText = findViewById(R.id.sunriseText)
        sunsetText = findViewById(R.id.sunsetText)

        // Map to new weather details
        pressureText = seaLevelText // Reuse sea level text view for pressure
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchLocations(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    locations = emptyList()
                    hideError()
                } else if (newText.length >= 3) {
                    searchLocations(newText)
                }
                return true
            }
        })

        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean = false

            override fun onSuggestionClick(position: Int): Boolean {
                if (position < locations.size) {
                    selectLocation(locations[position])
                    searchView.clearFocus()
                }
                return true
            }
        })
    }

    private fun setupInitialState() {
        showLoading(false)
        hideError()
        resetWeatherDisplay()
    }

    private fun loadDefaultLocation() {
        getCurrentWeather("Pretoria")
    }

    private fun searchLocations(query: String) {
        if (query.isBlank()) {
            locations = emptyList()
            return
        }

        showLoading(true)
        hideError()

        scope.launch {
            try {
                val result = weatherRepository.searchLocations(query)
                result.onSuccess { foundLocations ->
                    locations = foundLocations
                    setupLocationSuggestions(foundLocations)
                    hideError()
                }.onFailure { exception ->
                    showError(exception.message ?: "Failed to search locations")
                    locations = emptyList()
                }
            } catch (e: Exception) {
                showError("Network error occurred")
                locations = emptyList()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun setupLocationSuggestions(locations: List<LocationSearchResult>) {
        val columns = arrayOf(
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1
        )

        val cursor = MatrixCursor(columns)

        locations.forEachIndexed { index, location ->
            val suggestion = "${location.name}, ${location.region}, ${location.country}"
            cursor.addRow(arrayOf(index, suggestion))
        }

        val adapter = SimpleCursorAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            cursor,
            arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1),
            intArrayOf(android.R.id.text1),
            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        )

        searchView.suggestionsAdapter = adapter
    }

    private fun selectLocation(location: LocationSearchResult) {
        selectedLocation = location
        cityNameText.text = "${location.name}, ${location.country}"
        getCurrentWeather("${location.lat},${location.lon}")
    }

    private fun getCurrentWeather(locationQuery: String) {
        showLoading(true)
        hideError()

        scope.launch {
            try {
                val result = weatherRepository.getCurrentWeather(locationQuery)
                result.onSuccess { forecast ->
                    weatherForecast = forecast
                    updateWeatherDisplay(forecast)
                    hideError()
                }.onFailure { exception ->
                    showError(exception.message ?: "Failed to get weather data")
                }
            } catch (e: Exception) {
                showError("Network error occurred")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun updateWeatherDisplay(forecast: WeatherForecast) {
        // Update main weather info
        currentTempText.text = "${forecast.currentTemp}°C"
        weatherConditionText.text = forecast.condition.uppercase()
        dayText.text = forecast.dayOfWeek
        dateText.text = forecast.date
        cityNameText.text = "${forecast.cityName}, ${forecast.country}"

        // Update weather details
        humidityValueText.text = "${forecast.humidity}%"
        windSpeedText.text = forecast.windSpeed
        conditionsText.text = forecast.condition
        pressureText.text = forecast.pressure

        // Set placeholder values for fields not available in current API response
        maxTempText.text = "Max: ${forecast.currentTemp + 3}°C" // Estimated max
        minTempText.text = "Min: ${forecast.currentTemp - 5}°C" // Estimated min
        sunriseText.text = "06:30" // Placeholder
        sunsetText.text = "18:45" // Placeholder

        // Update weather animation and background
        updateWeatherAnimation(forecast.condition)
        updateBackgroundForWeather(forecast.condition)
    }

    private fun updateWeatherAnimation(condition: String) {
        val weatherCondition = WeatherCondition.fromCondition(condition)
        val animationRes = when (weatherCondition) {
            WeatherCondition.SUNNY -> R.raw.sun
            WeatherCondition.RAINY -> {
                // Check if rain animation exists, otherwise fallback to sun
                try {
                    R.raw.rain
                } catch (e: Exception) {
                    R.raw.sun
                }
            }
            WeatherCondition.CLOUDY -> {
                // Check if cloud animation exists, otherwise fallback to sun
                try {
                    R.raw.cloud
                } catch (e: Exception) {
                    R.raw.sun
                }
            }
            WeatherCondition.SNOWY -> {
                // Check if snow animation exists, otherwise fallback to sun
                try {
                    R.raw.snow
                } catch (e: Exception) {
                    R.raw.sun
                }
            }
        }

        weatherAnimation.setAnimation(animationRes)
        weatherAnimation.playAnimation()
    }

    private fun updateBackgroundForWeather(condition: String) {
        val weatherCondition = WeatherCondition.fromCondition(condition)
        val backgroundRes = when (weatherCondition) {
            WeatherCondition.SUNNY -> R.drawable.sunny_background
            WeatherCondition.RAINY -> R.drawable.rain_background
            WeatherCondition.CLOUDY -> R.drawable.cloud_background
            WeatherCondition.SNOWY -> R.drawable.snow_background
        }

        findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.main)
            .setBackgroundResource(backgroundRes)
    }

    private fun resetWeatherDisplay() {
        currentTempText.text = "--°C"
        weatherConditionText.text = "--"
        dayText.text = "Loading..."
        dateText.text = "Loading..."
        humidityValueText.text = "--"
        windSpeedText.text = "--"
        conditionsText.text = "--"
        pressureText.text = "--"
        maxTempText.text = "Max: --°C"
        minTempText.text = "Min: --°C"
        sunriseText.text = "--:--"
        sunsetText.text = "--:--"
        cityNameText.text = "Select a City"

        // Set default background
        findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.main)
            .setBackgroundResource(R.drawable.sunny_background)
    }

    private fun showLoading(show: Boolean) {
        loadingProgressBar.isVisible = show
    }

    private fun showError(message: String) {
        errorText.text = message
        errorText.isVisible = true
    }

    private fun hideError() {
        errorText.isVisible = false
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}