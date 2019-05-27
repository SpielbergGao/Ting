package com.zjw.ting.net

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitHelper private constructor() {

    private val basApi = "https://www.ysts8.com/"

    fun <T> createApi(clazz: Class<T>): T {
        val retrofit = Retrofit.Builder()
            .baseUrl(basApi)
            .client(sOkHttpClient!!)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(clazz)
    }

    companion object {
        @Volatile
        private var sRetrofitHelper: RetrofitHelper? = null
        private var sOkHttpClient: OkHttpClient? = null

        fun get(): RetrofitHelper? {
            if (sRetrofitHelper == null) {
                synchronized(RetrofitHelper::class.java) {
                    if (sRetrofitHelper == null) {
                        initializeRetrofit()
                    }
                }
            }
            return sRetrofitHelper
        }

        private fun initializeRetrofit() {
            if (sOkHttpClient == null) {
                synchronized(RetrofitHelper::class.java) {
                    if (sOkHttpClient == null) {
                        sOkHttpClient = OkHttpClient.Builder()
                            .retryOnConnectionFailure(true)
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .build()
                    }
                }
            }
            sRetrofitHelper = RetrofitHelper()
        }
    }
}