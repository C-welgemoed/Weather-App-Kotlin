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
- 🔒 **Secure API Key Storage**: Encrypted storage using Android Security Crypto
- ⚡ **Splash Screen**: Loading screen with data pre-fetching

## Screenshots

<div align="center">

### Splash Screen
<img src="https://github.com/user-attachments/assets/fcd283bc-06f5-46be-a4c2-9cc68091436a" width="250" alt="Splash Screen"/>

*Loading screen with animated weather icon*

### Weather Display - Sunny Conditions
<img src="https://github.com/user-attachments/assets/3b17b9c9-2d79-45b2-9ed7-730d2f482dc0" width="250" alt="Sunny Weather in Diepenbeek"/>

*Warm gradient background for sunny weather in Diepenbeek, Belgium*

### Weather Display - Cloudy Conditions  
<img src="https://github.com/user-attachments/assets/fc7781c0-4c28-4069-aa68-3356aa4f4293" width="250" alt="Cloudy Weather"/>

*Gray atmospheric design for partly cloudy conditions*

### Location Search
<img src="https://github.com/user-attachments/assets/abcb413c-b2e2-4ffa-9c66-e520743ac7b3" width="250" alt="Location Search"/>

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

### Testing
- **JUnit** (v4.13.2) - Unit testing framework
- **Mockito** (v5.8.0) - Mocking framework
- **Espresso** (v3.5.1) - UI testing

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
   
   Open `build.gradle.kts` (app module) and replace the empty API key:
   
   ```kotlin
   buildConfigField("String", "WEATHER_API_KEY", "\"YOUR_API_KEY_HERE\"")
   ```

4. **Sync and Build**
   - Open the project in Android Studio
   - Let Gradle sync complete
   - Build the project

5. **Run the App**
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

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

[Add your license here]

## Acknowledgments

- Weather data provided by [WeatherAPI.com](https://www.weatherapi.com/)
- Lottie animations from [LottieFiles](https://lottiefiles.com/)
- Icons and design inspired by Material Design guidelines


For issues, questions, or suggestions, please open an issue in the repository.

---

**Note**: Remember to never commit your API key to version control. Always use environment variables or build configuration fields.
