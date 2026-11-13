package com.example.notelite3

import android.app.Application
import com.example.notelite3.di.AppContainer

class NoteliteApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}