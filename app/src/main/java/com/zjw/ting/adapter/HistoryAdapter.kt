package com.zjw.ting.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zjw.ting.R
import com.zjw.ting.bean.AudioHistory
import kotlinx.android.synthetic.main.item_history.view.*

class HistoryAdapter(var items: List<AudioHistory>?, private val context: Context) :
    RecyclerView.Adapter<HistoryAdapter.HistoryHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryHolder(v)
    }

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        val item = items!![position]
        holder.set(item)
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    inner class HistoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun set(item: AudioHistory) {
            //UI setting code
            itemView.historyTv.text = item.info
        }
    }
}