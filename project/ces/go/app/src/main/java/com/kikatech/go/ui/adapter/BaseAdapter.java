package com.kikatech.go.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

/**
 * @author SkeeterWang Created on 2017/12/19.
 */

public abstract class BaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public abstract void resetHolder(@NonNull RecyclerView.ViewHolder holder, int position);

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder == null) {
            return;
        }
        resetHolder(holder, position);
    }
}