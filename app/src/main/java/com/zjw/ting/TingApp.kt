package com.zjw.ting

import android.app.Application
import android.content.Context
import android.text.TextUtils
import androidx.multidex.MultiDex
import com.tencent.bugly.Bugly
import com.tencent.bugly.beta.Beta
import com.zjw.ting.net.TingShuUtil
import com.zjw.ting.util.ACache


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
        val source = ACache.get(this).getAsString("source")
        if (TextUtils.isEmpty(source)) {
            TingShuUtil.sourceHost = TingShuUtil.httpHost
            ACache.get(this).put("source", TingShuUtil.sourceHost)
        } else {
            TingShuUtil.sourceHost = source
        }
    }
}