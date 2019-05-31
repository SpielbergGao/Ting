package com.zjw.ting

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex


class TingApp : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
    }
}