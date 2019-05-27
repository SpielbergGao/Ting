package com.zjw.ting.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zjw.ting.R;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ResultViewHolder> {
    private final Context context;
    private List<String> items;

    public SearchResultAdapter(List<String> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @Override
    public ResultViewHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ResultViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ResultViewHolder holder, int position) {
        String item = items.get(position);
        holder.set(item);
    }

    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }
        return items.size();
    }

    public class ResultViewHolder extends RecyclerView.ViewHolder {

        public ResultViewHolder(View itemView) {
            super(itemView);
        }

        public void set(String item) {
            //UI setting code
        }
    }
}