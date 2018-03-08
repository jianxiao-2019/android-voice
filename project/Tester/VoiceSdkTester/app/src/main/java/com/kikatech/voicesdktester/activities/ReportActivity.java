package com.kikatech.voicesdktester.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kikatech.voice.core.debug.ReportUtil;
import com.kikatech.voicesdktester.R;

import java.util.ArrayList;

/**
 * Created by ian.fan on 2018/2/27.
 */

public class ReportActivity extends AppCompatActivity {

    private ArrayList<String> mDataList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        mDataList = ReportUtil.getInstance().getTsList();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_report);

        RecyclerView.Adapter myAdapter = new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
                ViewHolder viewholder = new ViewHolder(view);
                return viewholder;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                ViewHolder viewHolder = (ViewHolder)holder;
                viewHolder.textView.setText(mDataList.get(position));
            }

            @Override
            public int getItemCount() {
                return mDataList.size();
            }

            class ViewHolder extends RecyclerView.ViewHolder
            {
                public TextView textView;
                public ViewHolder(View v)
                {
                    super(v);
                    textView = (TextView) v.findViewById(R.id.text_report);
                }
            }
        };
        
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
