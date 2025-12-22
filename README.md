# Weather App - Kotlin Android Application

A modern, user-friendly Android weather application built with Kotlin that provides real-time weather information for locations worldwide.

## Features

- 🌍 **Location Search**: Search for any city worldwide with autocomplete suggestions
- 🌡️ **Current Weather**: View detailed current weather conditions including:
  - Temperature (Celsius)
  - Weather conditions with dynamic animations
  - Humidity levels
  - Wind speed and direction
  - Atmospheric pressure
  - Visibility
  - UV index
  - Sunrise and sunset times
- 🎨 **Dynamic UI**: Background changes based on weather conditions (sunny, rainy, cloudy, snowy)
- 🎭 **Lottie Animations**: Smooth weather animations that match current conditions
- ⚡ **Splash Screen**: Loading screen with data pre-fetching

## Screenshots

<div align="center">

### Splash Screen
<img src="https://github.com/user-attachments/assets/94d9c13e-c1bc-4226-857e-d814e91534d0" alt="Splash Screen" width="300"/>

*Loading screen with animated weather icon*

### Weather Display - Sunny Conditions
<img src="https://github.com/user-attachments/assets/b974390c-f2ce-4e7f-8b2e-da65d8d618ec" alt="Sunny Weather" width="300"/>


*Warm gradient background for sunny weather in Diepenbeek, Belgium*

### Weather Display - Cloudy Conditions  
<img src="https://github.com/user-attachments/assets/4bb48385-6a3c-43f0-9e82-83bae71f4eec" alt="Cloudy Weather" width="300"/>


*Gray atmospheric design for partly cloudy conditions*

### Weather Display - Rain Conditions 
<img src="https://github.com/user-attachments/assets/e93d3eb1-b7b9-485e-a5b5-030a25537e3b" alt="Rainy Weather with Search" width="300"/>

*Autocomplete search suggestions for cities worldwide*

</div>

### Key UI Features Shown:
- Dynamic backgrounds that change based on weather conditions (sunny, cloudy, rainy, snowy)
- Lottie animations for weather conditions
- Comprehensive weather metrics display including:
  - Current temperature with min/max
  - Humidity percentage
  - Wind speed and direction
  - Atmospheric pressure (Sea Level in hPa)
  - Sunrise and sunset times
  - Current conditions with icon
- Real-time location search with dropdown suggestions
- Clean, modern Material Design interface

## Technologies Used

### Core Android
- **Kotlin** - Primary programming language
- **Android SDK** (Min SDK 24, Target SDK 34)
- **AndroidX Libraries** - Modern Android components

### Architecture & DI
- **[Dagger Hilt](https://dagger.dev/hilt/)** (v2.48.1) - Dependency injection framework
- **Repository Pattern** - Clean architecture for data management
- **Coroutines** - Asynchronous programming

### Networking
- **[Retrofit](https://square.github.io/retrofit/)** (v2.9.0) - Type-safe HTTP client
- **[OkHttp](https://square.github.io/okhttp/)** (v4.12.0) - HTTP client with logging interceptor
- **[Gson](https://github.com/google/gson)** (v2.10.1) - JSON serialization/deserialization

### UI & Animations
- **[Lottie](https://airbnb.io/lottie/)** (v6.2.0) - Animation library for smooth weather animations
- **Material Design Components** (v1.11.0) - Modern UI components
- **ConstraintLayout** (v2.1.4) - Flexible layouts

### Security
- **[Android Security Crypto](https://developer.android.com/jetpack/androidx/releases/security)** (v1.1.0-alpha06) - Encrypted SharedPreferences for API key storage

### Lifecycle & Reactive
- **[Lifecycle Components](https://developer.android.com/jetpack/androidx/releases/lifecycle)** (v2.7.0) - ViewModel and LiveData
- **[Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)** (v1.7.3) - Asynchronous operations



## Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24 or higher
- Valid API key from [WeatherAPI.com](https://www.weatherapi.com/)

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone <your-repository-url>
   cd WeatherAppKotlinLang
   ```

2. **Get a Free API Key**
   - Visit [WeatherAPI.com](https://www.weatherapi.com/)
   - Sign up for a free account
   - Navigate to your dashboard to get your API key
   - Free tier includes 1,000,000 calls/month

3. **Configure API Key**

   Open `Local.properties` and place ```YOUR_API_KEY_HERE= ``` and your API key


   Open `build.gradle.kts` (app module) and add the buildConfig field if not present:
   
   ```kotlin
   buildConfigField("String", "WEATHER_API_KEY", "\"YOUR_API_KEY_HERE\"")
   ```

3. **Sync and Build**
   - Open the project in Android Studio
   - Let Gradle sync complete
   - Build the project

4. **Run the App**
   - Connect an Android device or start an emulator
   - Click Run in Android Studio

## Project Structure

```
com.example.weatherappkotlinlang/
├── data/
│   ├── api/              # Retrofit API service and client
│   ├── models/           # Data models
│   ├── repository/       # Repository layer
│   └── security/         # Secure API key management
├── di/                   # Dependency injection modules
├── MainActivity.kt       # Main weather display screen
├── SplashActivity.kt     # Splash screen with data loading
└── WeatherApplication.kt # Application class
```

## Key Components

### API Client
- Rate limiting (1 request per second)
- Automatic retry on connection failure
- Comprehensive error handling
- Safe number parsing with custom deserializers

### Repository
- Handles API calls with proper error handling
- Maps API responses to app models
- Provides Result types for success/failure handling

### Security
- Encrypted API key storage using Android Keystore
- AES256_GCM encryption for preferences

## Configuration

### Network Security
The app uses cleartext traffic for API calls. Configuration is in `network_security_config.xml`.

### Timeouts
- Connect timeout: 30 seconds
- Read timeout: 30 seconds
- Write timeout: 30 seconds

## API Usage

This app uses the [WeatherAPI.com](https://www.weatherapi.com/) service:
- **Current Weather Endpoint**: Provides real-time weather data
- **Location Search Endpoint**: Autocomplete city search




## Acknowledgments

- Weather data provided by [WeatherAPI.com](https://www.weatherapi.com/)
- Lottie animations from [LottieFiles](https://lottiefiles.com/)
 - **Photos used attribution**
-  Rain Photo by 𝗛&𝗖𝗢 : https://www.pexels.com/photo/a-gloomy-sky-4955064/ 
- Splash Screen Photo by Szabó Viktor: https://www.pexels.com/photo/photo-of-clouds-during-dawn-3227984/ 
- Cloud Photo by Matheus Potsclam Barro: https://www.pexels.com/photo/heavy-clouds-1828305/ 
- Snow Photo by Burak The Weekender: https://www.pexels.com/photo/photo-of-snow-field-near-trees-1978126/
- Sunny Photo by Min An: https://www.pexels.com/photo/scenic-view-of-the-forest-during-sunrise-1006121/




---

**Note**: Remember to never commit your API key to version control. Always use environment variables or build configuration fields.
