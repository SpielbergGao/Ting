package com.zjw.ting.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
import android.text.TextUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.eye.cool.permission.PermissionHelper
import com.lxj.xpopup.XPopup
import com.tangguna.searchbox.library.cache.HistoryCache
import com.tangguna.searchbox.library.callback.onSearchCallBackListener
import com.zjw.ting.R
import com.zjw.ting.net.TingShuUtil
import com.zjw.ting.net.TingShuUtil2
import com.zjw.ting.util.ACache
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*
import top.defaults.drawabletoolbox.DrawableBuilder


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        onCheckPermission()

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

        val sourceBtDrawable = DrawableBuilder()
            .rectangle()
            .rounded()
            .solidColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .solidColorPressed(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            .build()
        sourceBt.background = sourceBtDrawable
        sourceBt.setOnClickListener {
            //切换播放源
            val source = ACache.get(this@MainActivity).getAsString("source")
            XPopup.Builder(this@MainActivity)
                .maxWidth(600)
                .asCenterList(
                    "当前host:${if (!TextUtils.isEmpty(source)) source.split("://")[1] else ""} ",
                    arrayOf(TingShuUtil.httpHost, TingShuUtil2.httpHost)
                ) { _, text ->
                    if (text == source) {
                        Toasty.warning(this@MainActivity, "切换源与当前一致~").show()
                        return@asCenterList
                    }
                    if (TextUtils.isEmpty(source) || text != source) {
                        Toasty.success(this@MainActivity, "切源为$text~，即将自动重启app，请稍后").show()
                        ACache[this@MainActivity].onStringPutCache("source", text)
                        //自动重启
                        val restartIntent = packageManager.getLaunchIntentForPackage(application.packageName)
                        val mPendingIntentId = 123456
                        val mPendingIntent =
                            PendingIntent.getActivity(
                                this@MainActivity,
                                mPendingIntentId,
                                restartIntent,
                                PendingIntent.FLAG_CANCEL_CURRENT
                            )
                        val mgr = this@MainActivity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, mPendingIntent)
                        System.exit(0)
                    }
                }
                .show()
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun isHasInstallPermissionWithO(context: Context): Boolean {
        return context.packageManager.canRequestPackageInstalls()
    }


    /**
     * 开启设置安装未知来源应用权限界面
     * @param context
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun startInstallPermissionSettingActivity(context: Context?) {
        if (context == null) {
            return
        }
        val intent = Intent(ACTION_MANAGE_UNKNOWN_APP_SOURCES)
        (context as Activity).startActivityForResult(intent, REQUEST_CODE_APP_INSTALL)
    }

    fun onCheckPermission(){

        // Toasty.info(this@MainActivity, "版本号:${BuildConfig.VERSION_CODE}", Toast.LENGTH_SHORT, true).show()
        val permissions = arrayListOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE)
        }

        PermissionHelper.Builder(this@MainActivity)
            .permissions(
                permissions.toTypedArray()
            ).permissionCallback {
                if (it) {
                    // 请求权限成功
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (isHasInstallPermissionWithO(this@MainActivity).not()) {
                            Toasty.success(this@MainActivity, "需要应用安装权限，前往设置~~", Toast.LENGTH_SHORT, true).show()
                            startInstallPermissionSettingActivity(this@MainActivity)
                        }
                    }
                } else {
                    // 请求权限失败
                    Toasty.warning(this@MainActivity, "亲，缺少必要的权限，打扰了~", Toast.LENGTH_SHORT, true).show()
                    finish()
                }
            }.build()
            .request()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


    }

    companion object {
        private const val REQUEST_CODE_APP_INSTALL: Int = 100
    }

}
