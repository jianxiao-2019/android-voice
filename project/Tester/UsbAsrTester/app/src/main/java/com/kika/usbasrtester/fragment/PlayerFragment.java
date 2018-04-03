package com.kika.usbasrtester.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kika.usbasrtester.AudioPlayerTask;
import com.kika.usbasrtester.R;
import com.kika.usbasrtester.listeners.ItemShareClickListener;
import com.kikatech.voice.core.debug.DebugUtil;
import com.kikatech.voice.util.log.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ryanlin on 23/01/2018.
 */

public class PlayerFragment extends PageFragment {

    private final List<RecognizeItem> mFileNames = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private FileAdapter mAdapter;
    private int mOpenedIndex = -1;

    private TextView mPrevPlayingView = null;
    private AudioPlayerTask mTask;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new FileAdapter();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(3));
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mTask != null && mTask.isPlaying()) {
            mTask.stop();
        }
    }

    public void refreshFiles() {
        mFileNames.clear();
        mFileNames.addAll(scanRecognizeResultFiles(DebugUtil.getDebugFolderPath()));

        mOpenedIndex = -1;
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPagePause() {
        if (mTask != null && mTask.isPlaying()) {
            mTask.stop();
        }
    }

    @Override
    public void onPageResume() {

    }

    private class FileAdapter extends RecyclerView.Adapter<FileViewHolder> {

        @Override
        public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new FileViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_recorded, parent, false));
        }

        @Override
        public void onBindViewHolder(FileViewHolder holder, int position) {
            RecognizeItem item = mFileNames.get(position);
            boolean isOpenedItem = position == mOpenedIndex;

            holder.fileName.setText(item.fileName);
//            holder.controlNc.setVisibility(item.isSourceUsb ? View.VISIBLE : View.INVISIBLE);
            holder.controlNc.getLayoutParams().width = item.isSourceUsb ? ViewGroup.LayoutParams.WRAP_CONTENT : 0;
            holder.controlNc.setOnClickListener(
                    new PlayButtonClickListener(
                            item.filePath + "_NC",
                            R.drawable.ic_source_nc_play,
                            R.drawable.ic_source_nc_pause,
                            R.drawable.ic_source_tag_nc));
            holder.controlRaw.setOnClickListener(
                    new PlayButtonClickListener(
                            item.filePath + (item.isSourceUsb ? "_USB" : "_SRC"),
                            R.drawable.ic_source_raw_play,
                            R.drawable.ic_source_raw_pause,
                            R.drawable.ic_source_tag_raw));

            holder.expendedLayout.setVisibility(isOpenedItem ? View.VISIBLE : View.GONE);
            holder.itemView.setOnClickListener(new ItemClickListener(position));
            holder.itemView.setBackgroundColor(isOpenedItem ? 0xFF3B475D : 0xFF2F3A4F);
            holder.recognizeResult.setText(Html.fromHtml(item.recognizeResult));

            holder.shareItem.setOnClickListener(new ItemShareClickListener(
                    getContext(), item.isSourceUsb, item.filePath));


            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd hh:mm");
            long duration = item.file.length() / 2 / 16000;
            Logger.d("[" + holder.fileName + "] duration = " + duration + " date = " + sdf.format(item.file.lastModified()));
            holder.fileTime.setText(sdf.format(item.file.lastModified()) + " | " + String.format("%02d:%02d", duration / 60, duration % 60));
        }

        @Override
        public int getItemCount() {
            return mFileNames.size();
        }
    }

    private class FileViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        TextView fileName;
        TextView fileTime;
        TextView controlRaw;
        TextView controlNc;
        View expendedLayout;
        TextView recognizeResult;
        View shareItem;

        public FileViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            fileName = (TextView) itemView.findViewById(R.id.file_name);
            fileTime = (TextView) itemView.findViewById(R.id.file_time);
            controlRaw = (TextView) itemView.findViewById(R.id.control_raw);
            controlNc = (TextView) itemView.findViewById(R.id.control_nc);

            expendedLayout = itemView.findViewById(R.id.expanded_layout);
            recognizeResult = (TextView) itemView.findViewById(R.id.asr_result_text);

            shareItem = itemView.findViewById(R.id.button_share);
        }
    }

    private List<RecognizeItem> scanRecognizeResultFiles(String path) {
        List<RecognizeItem> items = new ArrayList<>();
        if (TextUtils.isEmpty(path)) {
            return items;
        }
        File folder = new File(path);
        if (!folder.exists() || !folder.isDirectory() || folder.listFiles() == null) {
            return items;
        }

        for (final File file : folder.listFiles()) {
            if (file.isDirectory()) {
                continue;
            }
            if (file.getName().contains("_NC") && !file.getName().contains(".wav")) {
                String simpleFileName = file.getPath().substring(0, file.getPath().lastIndexOf("_"));
                RecognizeItem item = new RecognizeItem();
                item.file = file;
                item.filePath = simpleFileName;
                item.fileName = simpleFileName.substring(simpleFileName.lastIndexOf("/") + 1);
                item.isSourceUsb = true;
                item.recognizeResult = getRecognizeResult(simpleFileName + ".txt");

                items.add(item);

            } else if (file.getName().contains("_SRC") && !file.getName().contains(".wav")) {
                String simpleFileName = file.getPath().substring(0, file.getPath().lastIndexOf("_"));
                RecognizeItem item = new RecognizeItem();
                item.file = file;
                item.filePath = simpleFileName;
                item.fileName = simpleFileName.substring(simpleFileName.lastIndexOf("/") + 1);
                item.isSourceUsb = false;
                item.recognizeResult = getRecognizeResult(simpleFileName + ".txt");

                items.add(item);
            }
        }

        Collections.sort(items);
        return items;
    }

    private String getRecognizeResult(String path) {
        StringBuilder sb = new StringBuilder();
        try {
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("result:")) {
                    sb.append(line.substring(7)).append("<br>").append("\n");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String result = sb.toString();
        if (TextUtils.isEmpty(result)) {
            result = "<i>No voice recognition results</i>";
        }

        Logger.d("getRecognizeResult result = " + result);
        return result;
    }

    private class ItemClickListener implements View.OnClickListener {

        final int index;

        public ItemClickListener(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View v) {
            if (mOpenedIndex != index) {
                mOpenedIndex = index;
            } else {
                mOpenedIndex = -1;
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private class PlayButtonClickListener implements View.OnClickListener {

        private String mFilePath;

        private int mDrawablePlay;
        private int mDrawableStop;
        private int mDrawableSource;

        public PlayButtonClickListener(String filePath,
                                       int drawablePlay, int drawableStop, int drawableSource) {
            mFilePath = filePath;

            mDrawablePlay = drawablePlay;
            mDrawableStop = drawableStop;
            mDrawableSource = drawableSource;
        }

        @Override
        public void onClick(View v) {
            if (!(v instanceof TextView)) {
                return;
            }
            TextView currentView = (TextView) v;
            if (v == mPrevPlayingView) {
                if (mTask == null || !mTask.isPlaying()) {
                    mTask = new AudioPlayerTask(mFilePath, currentView, mDrawablePlay, mDrawableSource);
                    mTask.execute();
                    currentView.setCompoundDrawablesWithIntrinsicBounds(
                            0, mDrawableStop, 0, mDrawableSource);
                } else {
                    if (mTask.isPlaying()) {
                        mTask.stop();
                        mTask = null;
                    }
                    currentView.setCompoundDrawablesWithIntrinsicBounds(
                            0, mDrawablePlay, 0, mDrawableSource);
                }
            } else {
                if (mTask != null && mTask.isPlaying()) {
                    mTask.stop();
//                    mPrevPlayingView.setCompoundDrawablesWithIntrinsicBounds(
//                            0, mDrawablePlay, 0, mDrawableSource);
                    mTask = new AudioPlayerTask(mFilePath, (TextView) v, mDrawablePlay, mDrawableSource);
                    mTask.execute();
                    currentView.setCompoundDrawablesWithIntrinsicBounds(
                            0, mDrawableStop, 0, mDrawableSource);
                } else {
                    mTask = new AudioPlayerTask(mFilePath, (TextView) v, mDrawablePlay, mDrawableSource);
                    mTask.execute();
                    currentView.setCompoundDrawablesWithIntrinsicBounds(
                            0, mDrawableStop, 0, mDrawableSource);
                }

                mPrevPlayingView = (TextView) v;
            }
        }
    }

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildLayoutPosition(view) == 0) {
                outRect.top = space;
            } else {
                outRect.top = 0;
            }
        }
    }

    private class RecognizeItem implements Comparable<RecognizeItem>{
        File file;
        String fileName;
        String filePath;
        boolean isSourceUsb;
        String recognizeResult;

        @Override
        public int compareTo(@NonNull RecognizeItem another) {
            return Long.compare(another.file.lastModified(), file.lastModified());
        }
    }
}