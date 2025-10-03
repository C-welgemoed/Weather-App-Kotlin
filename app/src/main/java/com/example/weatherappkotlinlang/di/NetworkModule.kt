package com.example.weatherappkotlinlang.di

import android.content.Context
import com.example.weatherappkotlinlang.data.api.WeatherApiService
import com.example.weatherappkotlinlang.data.api.ApiClient
import com.example.weatherappkotlinlang.data.repository.WeatherRepository
import com.example.weatherappkotlinlang.data.security.ApiKeyProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideWeatherApiService(): WeatherApiService {
        return ApiClient.weatherApiService
    }

    @Provides
    @Singleton
    fun provideApiKeyProvider(@ApplicationContext context: Context): ApiKeyProvider {
        return ApiKeyProvider(context)
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(
        @ApplicationContext context: Context,
        weatherApiService: WeatherApiService,
        apiKeyProvider: ApiKeyProvider
    ): WeatherRepository {
        return WeatherRepository(context, weatherApiService, apiKeyProvider)
    }
}