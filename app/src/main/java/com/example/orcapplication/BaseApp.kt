package com.example.orcapplication

import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication

class BaseApp : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
    }
}