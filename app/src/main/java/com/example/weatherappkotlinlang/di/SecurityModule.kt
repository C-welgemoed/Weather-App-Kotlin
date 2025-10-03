package com.example.weatherappkotlinlang.di


import android.content.Context
import com.example.weatherappkotlinlang.data.security.SecureApiKeyManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideSecureApiKeyManager(@ApplicationContext context: Context): SecureApiKeyManager {
        return SecureApiKeyManager(context)
    }
}