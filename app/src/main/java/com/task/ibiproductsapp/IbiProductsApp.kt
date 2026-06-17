package com.task.ibiproductsapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class IbiProductsApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}