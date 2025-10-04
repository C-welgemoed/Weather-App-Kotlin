package com.example.weatherappkotlinlang

import android.app.SearchManager
import android.content.Context
import android.database.MatrixCursor
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.provider.BaseColumns
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
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
    private var internetCheckInProgress = false

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

        // Check real internet connectivity before loading data
        checkRealInternetAndLoadData()
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
        maxTempText = findViewById(R.id.maxTempText)
        minTempText = findViewById(R.id.minTempText)
        sunriseText = findViewById(R.id.sunriseText)
        sunsetText = findViewById(R.id.sunsetText)
        pressureText = seaLevelText
    }

    private fun checkRealInternetAndLoadData() {
        if (internetCheckInProgress) return

        internetCheckInProgress = true
        showLoading(true)

        scope.launch {
            val hasInternet = withContext(Dispatchers.IO) {
                hasRealInternetConnection()
            }

            showLoading(false)
            internetCheckInProgress = false

            if (!hasInternet) {
                showInternetRequiredDialog()
            } else {
                loadDefaultLocation()
            }
        }
    }

    private fun hasRealInternetConnection(): Boolean {
        // First check if network is available
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        val hasTransport = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)

        if (!hasTransport) return false

        // Now check if we can actually reach the internet
        return try {
            val socket = Socket()
            val socketAddress = InetSocketAddress("8.8.8.8", 53) // Google DNS
            socket.connect(socketAddress, 3000) // 3 second timeout
            socket.close()
            true
        } catch (e: IOException) {
            false
        }
    }

    private fun showInternetRequiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("No Internet Connection")
            .setMessage("This app requires an active internet connection to fetch weather data.\n\nPlease check:\n• WiFi or mobile data is enabled\n• You have internet access (not just connected to network)\n• DNS settings are correct")
            .setPositiveButton("Retry") { _, _ ->
                checkRealInternetAndLoadData()
            }
            .setNegativeButton("Exit") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchLocations(it)
                }
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
                    handleNetworkError(exception)
                    locations = emptyList()
                }
            } catch (e: Exception) {
                handleNetworkError(e)
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
                    handleNetworkError(exception)
                }
            } catch (e: Exception) {
                handleNetworkError(e)
            } finally {
                showLoading(false)
            }
        }
    }

    private fun handleNetworkError(exception: Throwable) {
        val message = when {
            exception.message?.contains("UnknownHostException") == true ||
                    exception.message?.contains("Unable to resolve host") == true -> {
                "No internet connection. Please check your network settings."
            }
            exception.message?.contains("timeout") == true -> {
                "Connection timeout. Please try again."
            }
            else -> exception.message ?: "Network error occurred"
        }
        showError(message)
    }

    private fun updateWeatherDisplay(forecast: WeatherForecast) {
        currentTempText.text = "${forecast.currentTemp}°C"
        weatherConditionText.text = forecast.condition.uppercase()
        dayText.text = forecast.dayOfWeek
        dateText.text = forecast.date
        cityNameText.text = "${forecast.cityName}, ${forecast.country}"

        humidityValueText.text = "${forecast.humidity}%"
        windSpeedText.text = forecast.windSpeed
        conditionsText.text = forecast.condition
        pressureText.text = forecast.pressure

        maxTempText.text = "Max: ${forecast.currentTemp + 3}°C"
        minTempText.text = "Min: ${forecast.currentTemp - 5}°C"
        sunriseText.text = "06:30"
        sunsetText.text = "18:45"

        updateWeatherAnimation(forecast.condition)
        updateBackgroundForWeather(forecast.condition)
    }

    private fun updateWeatherAnimation(condition: String) {
        val weatherCondition = WeatherCondition.fromCondition(condition)
        val animationRes = when (weatherCondition) {
            WeatherCondition.SUNNY -> R.raw.sun
            WeatherCondition.RAINY -> {
                try {
                    R.raw.rain
                } catch (e: Exception) {
                    R.raw.sun
                }
            }
            WeatherCondition.CLOUDY -> {
                try {
                    R.raw.cloud
                } catch (e: Exception) {
                    R.raw.sun
                }
            }
            WeatherCondition.SNOWY -> {
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