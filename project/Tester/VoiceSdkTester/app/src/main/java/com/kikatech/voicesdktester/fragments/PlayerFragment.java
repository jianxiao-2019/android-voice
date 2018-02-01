package com.kikatech.voicesdktester.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.kikatech.voice.util.log.Logger;
import com.kikatech.voicesdktester.AudioPlayerTask;
import com.kikatech.voicesdktester.R;

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

public class PlayerFragment extends Fragment {

    private final List<RecognizeItem> mFileNames = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private FileAdapter mAdapter;
    private int mOpenedIndex = -1;

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

        refreshFiles();
    }

    public void refreshFiles() {
        mFileNames.clear();
        mFileNames.addAll(scanRecognizeResultFiles(RecorderFragment.PATH_FROM_MIC));

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
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
            holder.sourceImage.setImageResource(item.isSourceUsb ?
                    isOpenedItem ? R.drawable.ic_list_usbcable_select : R.drawable.ic_list_usbcable :
                    isOpenedItem ? R.drawable.ic_list_phone_select : R.drawable.ic_list_phone);
            holder.controlNc.setVisibility(item.isSourceUsb ? View.VISIBLE : View.INVISIBLE);
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
            holder.recognizeResult.setText(item.recognizeResult);

            holder.deleteItem.setOnClickListener(new ItemDeleteClickListener(item.filePath));
            holder.renameItem.setOnClickListener(new ItemRenameListener(item.filePath));
//            holder.shareItem.setOnClickListener();

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/ hh:mm");
            long duration = item.file.length() / 2 / 16000;
            Logger.d("[" + holder.fileName + "] duration = " + duration + " date = " + sdf.format(item.file.lastModified()));
            holder.fileTime.setText(sdf.format(item.file.lastModified()) + " (" + String.format("%02d:%02d", duration / 60, duration % 60) + ")");
        }

        @Override
        public int getItemCount() {
            return mFileNames.size();
        }
    }

    private class FileViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        ImageView sourceImage;
        TextView fileName;
        TextView fileTime;
        TextView controlRaw;
        TextView controlNc;
        View expendedLayout;
        TextView recognizeResult;

        View deleteItem;
        View renameItem;
        View shareItem;

        public FileViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            sourceImage = (ImageView) itemView.findViewById(R.id.source_icon);
            fileName = (TextView) itemView.findViewById(R.id.file_name);
            fileTime = (TextView) itemView.findViewById(R.id.file_time);
            controlRaw = (TextView) itemView.findViewById(R.id.control_raw);
            controlNc = (TextView) itemView.findViewById(R.id.control_nc);

            expendedLayout = itemView.findViewById(R.id.expanded_layout);
            recognizeResult = (TextView) itemView.findViewById(R.id.asr_result_text);

            deleteItem = itemView.findViewById(R.id.item_delete);
            renameItem = itemView.findViewById(R.id.item_rename);
            shareItem = itemView.findViewById(R.id.item_share);
        }
    }

    private List<RecognizeItem> scanRecognizeResultFiles(String path) {
        List<RecognizeItem> items = new ArrayList<>();
        File folder = new File(path);
        if (!folder.exists() || !folder.isDirectory() || folder.listFiles() == null) {
            return items;
        }

        for (final File file : folder.listFiles()) {
            if (file.isDirectory()) {
                continue;
            }
            if (file.getName().contains("_NC")) {
                String simpleFileName = file.getPath().substring(0, file.getPath().lastIndexOf("_"));
                RecognizeItem item = new RecognizeItem();
                item.file = file;
                item.filePath = simpleFileName;
                item.fileName = simpleFileName.substring(simpleFileName.lastIndexOf("/") + 1);
                item.isSourceUsb = true;
                item.recognizeResult = getRecognizeResult(simpleFileName + ".txt");

                items.add(item);

            } else if (file.getName().contains("_SRC")) {
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
                    sb.append(line.substring(7)).append(" ");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    private class ItemDeleteClickListener implements View.OnClickListener {

        private String fileSimplePath;

        public ItemDeleteClickListener(String path) {
            this.fileSimplePath = path;
        }

        @Override
        public void onClick(View v) {
            deleteFile(new File(fileSimplePath + "_USB"));
            deleteFile(new File(fileSimplePath + ".txt"));
            deleteFile(new File(fileSimplePath + "_SRC"));
            deleteFile(new File(fileSimplePath + "_NC"));
            deleteFile(new File(fileSimplePath + "_speex"));
            refreshFiles();
        }

        private boolean deleteFile(File file) {
            Logger.d("deleteFile file = " + file.getPath());
            return file.delete();
        }
    }

    private class ItemRenameListener implements View.OnClickListener {

        private String fileSimplePath;

        public ItemRenameListener(String path) {
            this.fileSimplePath = path;
        }

        @Override
        public void onClick(View v) {
            final AlertDialog.Builder editDialog = new AlertDialog.Builder(getContext());
            editDialog.setTitle("Enter new file name.");

            final EditText editText = new EditText(getContext());
            editDialog.setView(editText);

            editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                // do something when the button is clicked
                public void onClick(DialogInterface dialog, int which) {
                    String newName = editText.getText().toString();
                    renameFile(new File(fileSimplePath + "_USB"),
                            new File(fileSimplePath.substring(0, fileSimplePath.lastIndexOf("/") + 1) + newName + "_USB"));
                    renameFile(new File(fileSimplePath + ".txt"),
                            new File(fileSimplePath.substring(0, fileSimplePath.lastIndexOf("/") + 1) + newName + ".txt"));
                    renameFile(new File(fileSimplePath + "_SRC"),
                            new File(fileSimplePath.substring(0, fileSimplePath.lastIndexOf("/") + 1) + newName + "_SRC"));
                    renameFile(new File(fileSimplePath + "_NC"),
                            new File(fileSimplePath.substring(0, fileSimplePath.lastIndexOf("/") + 1) + newName + "_NC"));
                    renameFile(new File(fileSimplePath + "_speex"),
                            new File(fileSimplePath.substring(0, fileSimplePath.lastIndexOf("/") + 1) + newName + "_speex"));

                    refreshFiles();
                }
            });
            editDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                // do something when the button is clicked
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            editDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    }
                }
            });
            editDialog.show();
        }

        private boolean renameFile(File origin, File newFile) {
            Logger.d("renameFile origin = " + origin.getPath() + " new = " + newFile.getPath());
            if (origin.exists() && !newFile.exists()) {
                return origin.renameTo(newFile);
            }
            return false;
        }
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
        private AudioPlayerTask mTask;

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
            if (mTask == null || !mTask.isPlaying()) {
                mTask = new AudioPlayerTask(mFilePath, (TextView) v, mDrawablePlay, mDrawableSource);
                mTask.execute();
                ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(
                        0, mDrawableStop, 0, mDrawableSource);
            } else {
                if (mTask.isPlaying()) {
                    mTask.stop();
                    mTask = null;
                }
                ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(
                        0, mDrawablePlay, 0, mDrawableSource);
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
