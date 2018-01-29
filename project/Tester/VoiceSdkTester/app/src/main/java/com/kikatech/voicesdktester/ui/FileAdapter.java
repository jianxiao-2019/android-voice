package com.kikatech.voicesdktester.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.kikatech.voicesdktester.AudioPlayerTask;
import com.kikatech.voicesdktester.R;

import java.util.List;

/**
 * Created by ryanlin on 04/01/2018.
 */

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private CheckBox lastChecked = null;
    private final List<String> mFileNames;
    private final String mFilePath;

    private OnItemCheckedListener mListener = null;

    public interface OnItemCheckedListener {
        void onItemChecked(String itemStr);
        void onNothongChecked();
    }

    public FileAdapter(String filePath, List<String> fileName) {
        mFilePath = filePath;
        mFileNames = fileName;
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {
        holder.checkBox.setText(mFileNames.get(position));
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (lastChecked != null) {
                        lastChecked.setChecked(false);
                    }
                    lastChecked = (CheckBox) buttonView;
                    if (mListener != null) {
                        mListener.onItemChecked(buttonView.getText().toString());
                    }
                } else {
                    lastChecked = null;
                    if (mListener != null) {
                        mListener.onNothongChecked();
                    }
                }
            }
        });
        holder.playButton.setOnClickListener(new PlayButtonClickListener(mFilePath + mFileNames.get(position)));
    }

    @Override
    public int getItemCount() {
        return mFileNames.size();
    }

    public class FileViewHolder extends RecyclerView.ViewHolder {

        Button playButton;
        CheckBox checkBox;

        public FileViewHolder(View itemView) {
            super(itemView);
            playButton = (Button) itemView.findViewById(R.id.button_play);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
        }
    }

    private class PlayButtonClickListener implements View.OnClickListener {

        private String mFilePath;
        private AudioPlayerTask mTask;

        public PlayButtonClickListener(String filePath) {
            mFilePath = filePath;
        }

        @Override
        public void onClick(View v) {
            if (mTask == null || !mTask.isPlaying()) {
                mTask = new AudioPlayerTask(mFilePath, (Button) v);
                mTask.execute();
                ((Button) v).setText("STOP");
            } else {
                if (mTask.isPlaying()) {
                    mTask.stop();
                    mTask = null;
                }
                ((Button) v).setText("PLAY");
            }
        }
    }

    public void setOnItemCheckedListener(OnItemCheckedListener listener) {
        mListener = listener;
    }
}
