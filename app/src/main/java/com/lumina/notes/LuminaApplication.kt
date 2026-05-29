package com.lumina.notes

import android.app.Application
import com.lumina.notes.di.AppContainer

class LuminaApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
