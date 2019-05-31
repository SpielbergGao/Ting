package com.zjw.ting

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.tencent.bugly.Bugly
import com.tencent.bugly.beta.Beta


class TingApp : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
        // 安装tinker
        Beta.installTinker()
    }
    override fun onCreate() {
        super.onCreate()
        Bugly.init(applicationContext, "880ab32e5a", false)
    }
}