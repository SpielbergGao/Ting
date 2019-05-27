package com.zjw.ting.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.zjw.ting.R
import com.zjw.ting.net.TingShuUtil
import kotlinx.android.synthetic.main.item_search_result.view.*

class SearchResultAdapter(var items: ArrayList<TingShuUtil.AudioInfo>?, private val context: Context) :
    RecyclerView.Adapter<SearchResultAdapter.ResultViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ResultViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
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

        fun set(item: TingShuUtil.AudioInfo) {
            //UI setting code
            val split = item.info.trim().split(" ")
            itemView.titleTv.text = split[0]
            itemView.titleInfoTv.text = split[1]
        }
    }


    interface OnItemClickListener {
        fun onItemClick(item: TingShuUtil.AudioInfo, position: Int)
    }
}