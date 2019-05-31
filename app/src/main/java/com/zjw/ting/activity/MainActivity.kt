package com.zjw.ting.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.eye.cool.permission.PermissionHelper
import com.tangguna.searchbox.library.cache.HistoryCache
import com.tangguna.searchbox.library.callback.onSearchCallBackListener
import com.zjw.ting.R
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*
import top.defaults.drawabletoolbox.DrawableBuilder

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var permissions = arrayListOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            //Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.READ_LOGS
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissions.add(Manifest.permission.REQUEST_INSTALL_PACKAGES)
        }

        PermissionHelper.Builder(this@MainActivity)
            .permissions(
                permissions.toTypedArray()
            ).permissionCallback {
                if (it) {
                    // 请求权限成功
                } else {
                    // 请求权限失败
                    Toasty.warning(this@MainActivity, "亲，缺少必要的权限，打扰了~", Toast.LENGTH_SHORT, true).show()
                    finish()
                }
            }.build()
            .request()

        val skills = HistoryCache.toArray(applicationContext)
        val skillHots = arrayListOf<String>("我当算命先生那几年", "官方救世主", "我有一座恐怖屋", "剑来")
        searchLayout.initData(skills, skillHots, object : onSearchCallBackListener {
            @SuppressLint("CheckResult")
            override fun Search(keyWord: String?) {
                //跳转到搜索结果页面
                if (TextUtils.isEmpty(keyWord)) {
                    Toasty.warning(this@MainActivity, "亲，必须输入关键字哟~", Toast.LENGTH_SHORT, true).show()
                } else {
                    //跳转页面
                    val intent = Intent(this@MainActivity, SearchResultActivity::class.java)
                    intent.putExtra("keyWord", keyWord)
                    startActivity(intent)
                }
            }

            override fun SaveOldData(alloldDataList: ArrayList<String>?) {
                //保存所有的搜索记录
                HistoryCache.saveHistory(applicationContext, HistoryCache.toJsonArray(alloldDataList))
            }

            override fun Back() {
                //finish()
            }

            override fun ClearOldData() {
                //清除历史搜索记录  更新记录原始数据
                HistoryCache.clear(applicationContext)
            }
        }, 1)


        val nextBtDrawable = DrawableBuilder()
            .rectangle()
            .rounded()
            .solidColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .solidColorPressed(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            .build()
        bt.background = nextBtDrawable
        bt.setOnClickListener {
            //跳转页面
            val intent = Intent(this@MainActivity, HistoryActivity::class.java)
            startActivity(intent)
        }
    }
}
