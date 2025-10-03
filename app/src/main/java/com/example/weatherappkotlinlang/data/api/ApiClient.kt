package com.example.weatherappkotlinlang.data.api

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "https://api.weatherapi.com/"

    // Rate limiting interceptor
    private class RateLimitInterceptor : Interceptor {
        private var lastRequestTime = 0L
        private val minInterval = 1000L // 1 second between requests

        override fun intercept(chain: Interceptor.Chain): Response {
            val currentTime = System.currentTimeMillis()
            val timeSinceLastRequest = currentTime - lastRequestTime

            if (timeSinceLastRequest < minInterval) {
                Thread.sleep(minInterval - timeSinceLastRequest)
            }

            lastRequestTime = System.currentTimeMillis()
            return chain.proceed(chain.request())
        }
    }

    // Custom deserializer for handling number parsing errors
    private class SafeDoubleDeserializer : JsonDeserializer<Double> {
        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Double {
            return try {
                json?.asDouble ?: 0.0
            } catch (e: Exception) {
                0.0
            }
        }
    }

    // Custom deserializer for handling integer parsing errors
    private class SafeIntDeserializer : JsonDeserializer<Int> {
        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Int {
            return try {
                when {
                    json?.isJsonNull == true -> 0
                    json?.isJsonPrimitive == true -> {
                        val primitive = json.asJsonPrimitive
                        when {
                            primitive.isNumber -> primitive.asInt
                            primitive.isString -> primitive.asString.toIntOrNull() ?: 0
                            else -> 0
                        }
                    }
                    else -> 0
                }
            } catch (e: Exception) {
                0
            }
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)  // Enable for debugging API calls
        .addInterceptor(RateLimitInterceptor())
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(Double::class.java, SafeDoubleDeserializer())
        .registerTypeAdapter(Int::class.java, SafeIntDeserializer())
        .registerTypeAdapter(object : TypeToken<Double?>() {}.type, SafeDoubleDeserializer())
        .registerTypeAdapter(object : TypeToken<Int?>() {}.type, SafeIntDeserializer())
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val weatherApiService: WeatherApiService = retrofit.create(WeatherApiService::class.java)
}