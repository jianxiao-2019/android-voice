package com.kikatech.voicesdktester.ui;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.kikatech.voicesdktester.AudioPlayerTask;
import com.kikatech.voicesdktester.R;
import com.kikatech.voicesdktester.wave.view.WaveformView;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ryanlin on 04/01/2018.
 */

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private CheckBox lastChecked = null;
    private final List<File> mFiles;
    private final String mFilePath;

    private OnItemCheckedListener mListener = null;

    public interface OnItemCheckedListener {
        void onItemChecked(String itemStr);
        void onNothingChecked();
    }

    public FileAdapter(String filePath, List<File> fileNames) {
        mFilePath = filePath;
        mFiles = fileNames;

        sortFiles();
    }

    public void updateContent(List<File> fileNames) {
        mFiles.clear();
        mFiles.addAll(fileNames);

        sortFiles();
    }

    private void sortFiles() {
        if (mFiles != null) {
            Collections.sort(mFiles, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return (int) (o2.lastModified() - o1.lastModified());
                }
            });
        }
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {
        String fileName = mFiles.get(position).getName();
        holder.checkBox.setText(fileName);
        holder.checkBox.setTextColor(fileName.contains("_s") ? Color.GREEN : Color.LTGRAY);
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
                        mListener.onNothingChecked();
                    }
                }
            }
        });

        String filePath = mFiles.get(position).getPath();
        holder.playButton.setOnClickListener(new PlayButtonClickListener(filePath));
        //load file and draw wave
        if (filePath.contains("_SRC") || filePath.contains("_NC")) {
            holder.waveView.setVisibility(View.VISIBLE);
            holder.waveView.loadFromFile(filePath);
        } else {
            holder.waveView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    class FileViewHolder extends RecyclerView.ViewHolder {

        Button playButton;
        CheckBox checkBox;
        WaveformView waveView;

        FileViewHolder(View itemView) {
            super(itemView);
            playButton = (Button) itemView.findViewById(R.id.button_play);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);

            waveView = (WaveformView) itemView.findViewById(R.id.waveview);
        }
    }

    private class PlayButtonClickListener implements View.OnClickListener {

        private String mFilePath;
        private AudioPlayerTask mTask;

        PlayButtonClickListener(String filePath) {
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
