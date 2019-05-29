package com.zjw.ting.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.blankj.rxbus.RxBus
import com.yanzhenjie.recyclerview.touch.OnItemMoveListener
import com.zjw.ting.R
import com.zjw.ting.adapter.HistoryAdapter
import com.zjw.ting.bean.AudioHistory
import com.zjw.ting.bean.AudioHistorys
import com.zjw.ting.util.ACache
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_history.*
import java.util.*
import kotlin.collections.ArrayList


class HistoryActivity : AppCompatActivity() {

    private lateinit var audioHistoryList: ArrayList<AudioHistory>
    private var history: Any? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        audioHistoryList = ArrayList<AudioHistory>()
        history = ACache.get(this).getAsObject("history")
        if (history == null) {
            Toasty.info(this, "暂无听书记录，快去听一听~").show()
            finish()
        }
        history?.let {
            it as AudioHistorys
            val values = it.map.values
            audioHistoryList.addAll(values)

            rv.isItemViewSwipeEnabled = true
            val adapter = HistoryAdapter(audioHistoryList, this)
            rv.setOnItemMoveListener(object : OnItemMoveListener {
                override fun onItemDismiss(srcHolder: RecyclerView.ViewHolder?) {
                    // 此方法在Item在侧滑删除时被调用。
                    // 从数据源移除该Item对应的数据，并刷新Adapter。
                    val position = srcHolder?.adapterPosition
                    position?.let { pos ->
                        it.map.remove(audioHistoryList[pos].bookUrl)
                        ACache.get(this@HistoryActivity).put("history", it)
                        // 发送 String 类型事件
                        RxBus.getDefault().post("update history")
                        audioHistoryList.removeAt(pos)
                        adapter.notifyItemRemoved(pos)
                    }
                }

                override fun onItemMove(srcHolder: RecyclerView.ViewHolder?, targetHolder: RecyclerView.ViewHolder?): Boolean {
                    // 此方法在Item拖拽交换位置时被调用。
                    // 第一个参数是要交换为之的Item，第二个是目标位置的Item。

                    // 交换数据，并更新adapter。
                    val fromPosition = srcHolder!!.adapterPosition
                    val toPosition = targetHolder!!.adapterPosition
                    Collections.swap(audioHistoryList, fromPosition, toPosition)
                    adapter.notifyItemMoved(fromPosition, toPosition)

                    // 返回true，表示数据交换成功，ItemView可以交换位置。
                    return true
                }
            })

            rv.setOnItemClickListener { _, adapterPosition ->
                //跳转到集数页面
                val intent = Intent(this@HistoryActivity, EpisodesActivity::class.java)
                intent.putExtra("url", audioHistoryList[adapterPosition].bookUrl)
                intent.putExtra("info", audioHistoryList[adapterPosition].info.split("===")[0])
                startActivity(intent)
            }
            rv.adapter = adapter


            // 注册 String 类型事件
            RxBus.getDefault().subscribeSticky(this, object : RxBus.Callback<String>() {
                override fun onEvent(s: String) {
                    if (s == "update history") {
                        return
                    }
                    history = ACache.get(this@HistoryActivity).getAsObject("history")
                    history?.let {
                        it as AudioHistorys
                        val values = it.map.values
                        audioHistoryList = ArrayList<AudioHistory>()
                        audioHistoryList.addAll(values)
                        adapter.items = audioHistoryList
                        adapter.notifyDataSetChanged()
                    }
                }
            })
        }
    }


    override fun onDestroy() {
        // 注销
        RxBus.getDefault().unregister(this)
        super.onDestroy()
    }
}
