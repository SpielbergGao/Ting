package com.zjw.ting.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import com.zjw.ting.R
import com.zjw.ting.adapter.EposodesAdapter
import com.zjw.ting.net.TingShuUtil
import es.dmoral.toasty.Toasty
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_episodes.rv
import kotlinx.android.synthetic.main.activity_search_result.*

class EpisodesActivity : AppCompatActivity() {

    private val audioInfos = ArrayList<TingShuUtil.AudioInfo>()
    private lateinit var adapter: EposodesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episodes)

        //加载数据
        refreshData()
        swipeLayout.isRefreshing = true
        swipeLayout.setOnRefreshListener {
            refreshData()
        }

        rv.layoutManager = GridLayoutManager(this@EpisodesActivity, 4)
        adapter = EposodesAdapter(audioInfos, this@EpisodesActivity)
        rv.adapter = adapter
        adapter.onItemClickListener = object : EposodesAdapter.OnItemClickListener {
            override fun onItemClick(item: TingShuUtil.AudioInfo, position: Int) {
                //跳转到播放页面
                val intent = Intent(this@EpisodesActivity, AudioActivity::class.java)
                intent.putExtra("url", item.url)
                startActivity(intent)
            }
        }
    }

    private fun refreshData() {
        loadData(onSuccess = {
            swipeLayout.isRefreshing = false
            adapter.items?.clear()
            adapter.items = it
            //展示搜素结果页面
            rv.adapter?.notifyDataSetChanged()
        }, onError = {
            it.message?.let { msg -> Toasty.error(this@EpisodesActivity, msg).show() }
            swipeLayout.isRefreshing = false
        }
        )
    }

    @SuppressLint("CheckResult")
    private fun loadData(onSuccess: (list: ArrayList<TingShuUtil.AudioInfo>) -> Unit, onError: (e: Error) -> Unit) {
        Observable.create(ObservableOnSubscribe<ArrayList<TingShuUtil.AudioInfo>> {
            try {
                val urls = TingShuUtil.getEpisodesUrls(intent.getStringExtra("url"))
                it.onNext(urls)
                it.onComplete()
            } catch (e: Error) {
                it.onError(e)
                onError(e)
            }
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                onSuccess(it)
            }
    }
}
