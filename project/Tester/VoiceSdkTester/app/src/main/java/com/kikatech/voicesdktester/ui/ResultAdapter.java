package com.kikatech.voicesdktester.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kikatech.voice.core.webservice.message.AlterMessage;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voicesdktester.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ryanlin on 16/01/2018.
 */

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ResultItemViewHolder> {

    private final List<Message> mCurrentResults = new ArrayList<>();

    private final Context mContext;

    public ResultAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ResultItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result, null);
        return new ResultItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ResultItemViewHolder holder, int position) {
        if (mContext == null) {
            return;
        }
        Message message = mCurrentResults.get(position);
        if (message instanceof TextMessage) {
            holder.resultText.setText(((TextMessage) message).text[0]);
            holder.cidText.setText(
                    String.format(mContext.getString(R.string.time_text), convertCidToDate(((TextMessage) message).cid)));
        } else if (message instanceof IntermediateMessage) {
            holder.resultText.setText(((IntermediateMessage) message).text);
            holder.cidText.setText(
                    String.format(mContext.getString(R.string.time_text), convertCidToDate(((IntermediateMessage) message).cid)));
        } else if (message instanceof AlterMessage) {
            holder.resultText.setText(((AlterMessage) message).text[0]);
            holder.cidText.setText(
                    String.format(mContext.getString(R.string.time_text), convertCidToDate(((AlterMessage) message).cid)));
        }

    }

    private String convertCidToDate(long cid) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
        Date resultdate = new Date(cid);
        return sdf.format(resultdate);
    }

    @Override
    public int getItemCount() {
        return mCurrentResults.size();
    }

    public class ResultItemViewHolder extends RecyclerView.ViewHolder {

        TextView cidText;
        TextView resultText;

        public ResultItemViewHolder(View itemView) {
            super(itemView);
            cidText = (TextView) itemView.findViewById(R.id.text_cid);
            resultText = (TextView) itemView.findViewById(R.id.text_result);
        }
    }

    public void clearResults() {
        mCurrentResults.clear();
    }

    public void addResult(Message message) {
        if (message instanceof TextMessage
                || message instanceof IntermediateMessage
                || message instanceof AlterMessage) {
            mCurrentResults.add(0, message);
        }
    }
}
