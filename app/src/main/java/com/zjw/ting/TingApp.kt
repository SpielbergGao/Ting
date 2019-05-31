package com.zjw.ting

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.tencent.bugly.Bugly


class TingApp : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
    override fun onCreate() {
        super.onCreate()
        Bugly.init(applicationContext, "880ab32e5a", false)
    }
}