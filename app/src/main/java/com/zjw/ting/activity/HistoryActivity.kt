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

class HistoryActivity : AppCompatActivity() {

    private var history: Any? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        history = ACache.get(this).getAsObject("history")
        if (history == null) {
            Toasty.info(this, "暂无听书记录，快去听一听~").show()
            finish()
        }
        history?.let {
            it as AudioHistorys
            val values = it.map.values
            var audioHistoryList = ArrayList<AudioHistory>()
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
                        audioHistoryList.removeAt(pos)
                        adapter.notifyItemRemoved(pos)
                    }
                }

                override fun onItemMove(p0: RecyclerView.ViewHolder?, p1: RecyclerView.ViewHolder?): Boolean {
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
                    history = ACache.get(this@HistoryActivity).getAsObject("history")
                    history?.let {
                        it as AudioHistorys
                        val values = it.map.values
                        var audioHistoryList = ArrayList<AudioHistory>()
                        audioHistoryList.addAll(values)
                        adapter.items = audioHistoryList
                        adapter.notifyDataSetChanged()
                    }
                }
            })
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // 注销
        RxBus.getDefault().unregister(this)
    }
}
