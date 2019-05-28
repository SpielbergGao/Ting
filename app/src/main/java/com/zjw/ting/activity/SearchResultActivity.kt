package com.zjw.ting.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.trello.rxlifecycle3.android.lifecycle.kotlin.bindUntilEvent
import com.zjw.ting.R
import com.zjw.ting.adapter.SearchResultAdapter
import com.zjw.ting.net.TingShuUtil
import es.dmoral.toasty.Toasty
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_search_result.*

class SearchResultActivity : AppCompatActivity(), LifecycleOwner {

    private val audioInfos = ArrayList<TingShuUtil.AudioInfo>()
    private lateinit var adapter: SearchResultAdapter
    private var pageIndex = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result)

        refreshData()
        swipeLayout.isRefreshing = true
        swipeLayout.setOnRefreshListener {
            refreshData()
        }


        adapter = SearchResultAdapter(audioInfos, this@SearchResultActivity)
        rv.adapter = adapter
        rv.useDefaultLoadMore()
        rv.setAutoLoadMore(true)
        rv.loadMoreFinish(false, true)
        rv.setLoadMoreListener {
            loadData(page = pageIndex, onSuccess = {
                rv.loadMoreFinish(false, true)
                pageIndex++
                adapter.items?.addAll(it)
                //展示搜素结果页面
                adapter.notifyDataSetChanged()
            }, onError = {
                rv.loadMoreFinish(false, true)
                it.message?.let { msg -> Toasty.error(this@SearchResultActivity, msg) }
            }
            )
        }
        adapter.onItemClickListener = object : SearchResultAdapter.OnItemClickListener {
            override fun onItemClick(item: TingShuUtil.AudioInfo, position: Int) {
                //跳转到集数页面
                val intent = Intent(this@SearchResultActivity, EpisodesActivity::class.java)
                intent.putExtra("url", item.url)
                intent.putExtra("info", item.info)
                startActivity(intent)
            }
        }
    }

    private fun refreshData() {
        pageIndex = 0
        loadData(page = TingShuUtil.countPage, onSuccess = {
            swipeLayout.isRefreshing = false
            adapter.items?.clear()
            adapter.items = it
            //展示搜素结果页面
            rv.adapter?.notifyDataSetChanged()
        }, onError = {
            it.message?.let { msg -> Toasty.error(this@SearchResultActivity, msg) }
            swipeLayout.isRefreshing = false
        }
        )
    }

    @SuppressLint("CheckResult")
    private fun loadData(page: Long, onSuccess: (list: ArrayList<TingShuUtil.AudioInfo>) -> Unit, onError: (e: Throwable) -> Unit) {
        Observable.create(ObservableOnSubscribe<ArrayList<TingShuUtil.AudioInfo>> {
            try {
                val urls = TingShuUtil.getSearchUrls(intent.getStringExtra("keyWord"), page)
                it.onNext(urls)
                it.onComplete()
            } catch (e: Throwable) {
                it.onError(e)
            }
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .bindUntilEvent(this@SearchResultActivity, Lifecycle.Event.ON_DESTROY)
            .subscribe({
                onSuccess(it)
            }, {
                onError(it)
            })
    }
}
