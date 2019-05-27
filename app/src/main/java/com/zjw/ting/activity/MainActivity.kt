package com.zjw.ting.activity

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.eye.cool.permission.PermissionHelper
import com.tangguna.searchbox.library.cache.HistoryCache
import com.tangguna.searchbox.library.callback.onSearchCallBackListener
import com.zjw.ting.R
import com.zjw.ting.net.TingShuUtil
import es.dmoral.toasty.Toasty
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PermissionHelper.Builder(this@MainActivity)
            .permissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE
                )
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
            override fun Search(p0: String?) {
                //跳转到搜索结果页面
                if (TextUtils.isEmpty(p0)) {
                    Toasty.warning(this@MainActivity, "亲，必须输入关键字哟~", Toast.LENGTH_SHORT, true).show()
                } else {
                    //需要异步操作
                    Observable.create(ObservableOnSubscribe<ArrayList<String>> {
                        try {
                            val searchUrls = TingShuUtil.getSearchUrls(p0, TingShuUtil.countPage)
                            it.onNext(searchUrls)
                            it.onComplete()
                        } catch (e: Error) {
                            it.onError(e)
                        }
                    }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            //跳转到搜素结果页面
                            Log.e("tag", it.toString())
                        }

                }
            }

            override fun SaveOldData(p0: ArrayList<String>?) {
                //保存所有的搜索记录
                HistoryCache.saveHistory(applicationContext, HistoryCache.toJsonArray(skillHots))
            }

            override fun Back() {
                //finish()
            }

            override fun ClearOldData() {
                //清除历史搜索记录  更新记录原始数据
                HistoryCache.clear(applicationContext)
            }
        }, 1)
    }
}
