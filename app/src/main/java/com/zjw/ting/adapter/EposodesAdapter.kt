package com.zjw.ting.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zjw.ting.R
import com.zjw.ting.bean.AudioInfo
import kotlinx.android.synthetic.main.item_episodes.view.*

class EposodesAdapter(var items: ArrayList<AudioInfo>?, private val context: Context) :
    RecyclerView.Adapter<EposodesAdapter.ResultViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ResultViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_episodes, parent, false)
        return ResultViewHolder(v)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val item = items!![position]
        holder.set(item)
        holder.itemView.setOnClickListener {
            onItemClickListener?.let { that ->
                that.onItemClick(item, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    inner class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun set(item: AudioInfo) {
            //UI setting code
            itemView.episodesTv.text = item.info
        }
    }


    interface OnItemClickListener {
        fun onItemClick(item: AudioInfo, position: Int)
    }
}