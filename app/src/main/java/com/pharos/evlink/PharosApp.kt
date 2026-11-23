package com.pharos.evlink

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. Hilt builds the DI graph from here.
 */
@HiltAndroidApp
class PharosApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PharosLog.i("App", "Pharos ${BuildConfig.VERSION_NAME} starting")
    }
}
