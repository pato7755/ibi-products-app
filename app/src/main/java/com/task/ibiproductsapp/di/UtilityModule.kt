package com.task.ibiproductsapp.di

import android.content.Context
import com.task.ibiproductsapp.util.AppBiometricManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilityModule {

    @Provides
    @Singleton
    fun provideBiometricManager(
        @ApplicationContext context: Context
    ): AppBiometricManager = AppBiometricManager(context)
}