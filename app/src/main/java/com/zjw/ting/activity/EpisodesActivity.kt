package com.zjw.ting.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.rxbus.RxBus
import com.trello.rxlifecycle3.android.lifecycle.kotlin.bindUntilEvent
import com.zjw.ting.R
import com.zjw.ting.adapter.EposodesAdapter
import com.zjw.ting.bean.AudioHistory
import com.zjw.ting.bean.AudioHistorys
import com.zjw.ting.net.TingShuUtil
import com.zjw.ting.util.ACache
import es.dmoral.toasty.Toasty
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_episodes.*
import kotlinx.android.synthetic.main.activity_episodes.rv
import kotlinx.android.synthetic.main.activity_search_result.*
import kotlinx.android.synthetic.main.activity_search_result.swipeLayout

class EpisodesActivity : AppCompatActivity(), LifecycleOwner {

    private val audioInfos = ArrayList<TingShuUtil.AudioInfo>()
    private lateinit var adapter: EposodesAdapter
    private var code = 100

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episodes)

        setHistoryUi()

        //加载数据
        refreshData()
        swipeLayout.isRefreshing = true
        swipeLayout.setOnRefreshListener {
            refreshData()
        }

        rv.layoutManager = GridLayoutManager(this@EpisodesActivity, 4) as RecyclerView.LayoutManager?
        adapter = EposodesAdapter(audioInfos, this@EpisodesActivity)
        rv.adapter = adapter
        adapter.onItemClickListener = object : EposodesAdapter.OnItemClickListener {
            override fun onItemClick(item: TingShuUtil.AudioInfo, position: Int) {
                //跳转到播放页面
                val intent = Intent(this@EpisodesActivity, AudioPlayActivity::class.java)
                intent.putExtra("url", item.url)
                intent.putExtra("position", position + 1)
                intent.putExtra("bookUrl", getIntent().getStringExtra("url"))
                intent.putExtra("info", getIntent().getStringExtra("info"))
                //startActivityForResult(intent, code)
                startActivity(intent)
            }
        }

        // 注册 String 类型事件
        RxBus.getDefault().subscribeSticky(this, object : RxBus.Callback<String>() {
            override fun onEvent(s: String) {
                setHistoryUi()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun setHistoryUi() {
        var history = ACache.get(this).getAsObject("history")
        history?.let {
            it as AudioHistorys
            val audioHistory = it.map[intent.getStringExtra("url")]
            audioHistory?.let {
                historyTv.visibility = View.VISIBLE
                line.visibility = View.VISIBLE
                historyTv.text = "播放记录： " + audioHistory.info

                historyTv.setOnClickListener {
                    //跳转到播放页面
                    val intent = Intent(this@EpisodesActivity, AudioPlayActivity::class.java)
                    intent.putExtra("url", audioHistory.episodesUrl)
                    intent.putExtra("position", audioHistory.position)
                    intent.putExtra("currentPosition", audioHistory.currentPosition)
                    intent.putExtra("bookUrl", getIntent().getStringExtra("url"))
                    intent.putExtra("info", getIntent().getStringExtra("info"))
                    //startActivityForResult(intent, code)
                    startActivity(intent)
                }
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
    private fun loadData(onSuccess: (list: ArrayList<TingShuUtil.AudioInfo>) -> Unit, onError: (e: Throwable) -> Unit) {
        Observable.create(ObservableOnSubscribe<ArrayList<TingShuUtil.AudioInfo>> {
            try {
                val urls = TingShuUtil.getEpisodesUrls(intent.getStringExtra("url"))
                urls?.let { infos ->
                    it.onNext(infos)
                }
                it.onComplete()
            } catch (e: Throwable) {
                it.onError(e)

            }
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .bindUntilEvent(this@EpisodesActivity, Lifecycle.Event.ON_DESTROY)
            .subscribe({
                onSuccess(it)
            }, {
                onError(it)
            })
    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == code) {
            val bookUrl = data?.extras?.getString("bookUrl")
            bookUrl?.let {
                if (it == intent.getStringExtra("url")) {
                    setHistoryUi()
                }
            }
        }
    }*/

    override fun onDestroy() {
        // 注销
        RxBus.getDefault().unregister(this)
        super.onDestroy()
    }
}
