package com.zkrallah.backupandrestore

import android.app.Application

class App : Application() {
    companion object {
        lateinit var ctx: Application
    }

    override fun onCreate() {
        ctx = this
        super.onCreate()
    }
}