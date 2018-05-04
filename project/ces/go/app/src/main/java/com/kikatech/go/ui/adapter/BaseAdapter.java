package com.kikatech.go.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SkeeterWang Created on 2017/12/19.
 */

public abstract class BaseAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public abstract void resetHolder(@NonNull RecyclerView.ViewHolder holder, int position);

    protected Context mContext;
    protected List<T> mList = new ArrayList<>();

    protected BaseAdapter(Context context) {
        this.mContext = context;
    }

    protected BaseAdapter(Context context, List<T> list) {
        this.mContext = context;
        if (list != null && !list.isEmpty()) {
            mList.addAll(list);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder == null) {
            return;
        }
        resetHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }
}