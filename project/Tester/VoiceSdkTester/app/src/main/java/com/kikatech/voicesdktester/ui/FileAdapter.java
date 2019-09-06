package com.kikatech.voicesdktester.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kikatech.voice.core.debug.WavHeaderHelper;
import com.kikatech.voice.util.log.Logger;
import com.kikatech.voicesdktester.AudioPlayerTask;
import com.kikatech.voicesdktester.R;
import com.kikatech.voicesdktester.fragments.PlayerFragment;
import com.kikatech.voicesdktester.listeners.ItemShareClickListener;
import com.kikatech.voicesdktester.utils.FileUtil;
import com.kikatech.voicesdktester.wave.view.WaveformView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ryanlin on 04/01/2018.
 */

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private int mOpenedIndex = -1;

    private CheckBox lastChecked = null;
    private String lastCheckedStr = null;
    private final List<File> mFiles;
    private final String mFilePath;
    private boolean mEnableMultipleCheck = false;
    private FileAdapter adapter;

    private OnItemCheckedListener mListener = null;

    ArrayList<String> mCheckedList = new ArrayList<String>();

    public interface OnItemCheckedListener {
        void onItemChecked(String itemStr);
        void onItemUnchecked(String itemStr);
        void onNothingChecked();
    }

    public FileAdapter(String filePath, List<File> fileNames) {
        mFilePath = filePath;
        mFiles = fileNames;

    }

    public void updateContent(List<File> fileNames) {
        mFiles.clear();
        mFiles.addAll(fileNames);

        mCheckedList.clear();
    }


    public void refreshFiles() {

//        mFiles.clear();
//        mFiles.addAll(adapter.mFiles);

        if (FileAdapter.this != null) {
            FileAdapter.this.notifyDataSetChanged();
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
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(mCheckedList.contains(fileName));
        holder.expendedLayout_ts.setVisibility(View.GONE);
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String itemStr = buttonView.getText().toString();
                holder.expendedLayout_ts.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                if (isChecked) {
                    if (mEnableMultipleCheck) {
                        if (!mCheckedList.contains(itemStr)) {
                            mCheckedList.add(itemStr);
                        }
                    } else {
                        if (lastChecked != null && lastCheckedStr != null ) {
                            mCheckedList.remove(lastCheckedStr);
                            lastChecked.setChecked(false);
                        }
                        mCheckedList.add(itemStr);
                    }
                    lastChecked = (CheckBox) buttonView;
                    lastCheckedStr = lastChecked.getText().toString();
                    if (mListener != null) {
                        mListener.onItemChecked(itemStr);
                    }
                } else {
                    if (mEnableMultipleCheck) {
                        if (mCheckedList.contains(itemStr)) {
                            mCheckedList.remove(itemStr);
                        }
                        if (lastChecked != null && lastCheckedStr != null && lastCheckedStr.equals(itemStr)) {
                            lastChecked = null;
                            lastCheckedStr = null;
                        }
                    } else {
                        mCheckedList.remove(itemStr);
                        lastChecked = null;
                        lastCheckedStr = null;
                    }
                    if (mListener != null) {
                        mListener.onItemUnchecked(itemStr);
                        if (mCheckedList.size() == 0) {
                            mListener.onNothingChecked();
                        }
                    }
                }
            }
        });
        boolean isSourceUsb = false;
        String filePath = mFiles.get(position).getPath();
        holder.playButton.setOnClickListener(new PlayButtonClickListener(filePath));
        //load file and draw wave
        if (filePath.contains("_SRC") || filePath.contains("_NC")) {
            holder.waveView.setVisibility(View.VISIBLE);
            holder.waveView.loadFromFile(filePath, true);
        } else {
            holder.waveView.setVisibility(View.GONE);
        }

//        holder.recognizeResult_ts.setText(Html.fromHtml(recognizeResult));

//        holder.deleteItem_ts.setOnClickListener(new ItemDelete_tsClickListener(filePath));
//        holder.renameItem_ts.setOnClickListener(new ItemRename_tsListener(filePath));
        holder.shareItem_ts.setOnClickListener(new ItemShare_tsClickListener(holder.shareItem_ts.getContext(), isSourceUsb, filePath));
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    class FileViewHolder extends RecyclerView.ViewHolder {

        Button playButton;
        CheckBox checkBox;
        WaveformView waveView;

        View expendedLayout_ts;
        TextView recognizeResult_ts;

        View deleteItem_ts;
        View renameItem_ts;
        View shareItem_ts;

        FileViewHolder(View itemView) {
            super(itemView);
            playButton = (Button) itemView.findViewById(R.id.button_play);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);

            waveView = (WaveformView) itemView.findViewById(R.id.waveview);

            expendedLayout_ts = itemView.findViewById(R.id.expanded_layout_ts);
            recognizeResult_ts = (TextView) itemView.findViewById(R.id.asr_result_text_ts);

            deleteItem_ts = itemView.findViewById(R.id.item_delete_ts);
            renameItem_ts = itemView.findViewById(R.id.item_rename_ts);
            shareItem_ts = itemView.findViewById(R.id.item_share_ts);
        }
    }
/*    暂未实现删除,修改文件
    private class ItemRename_tsListener implements View.OnClickListener {

        private String fileSimplePath;

        public ItemRename_tsListener(String path) {
            this.fileSimplePath = path;
        }

        @Override
        public void onClick(View v) {
            final AlertDialog.Builder editDialog = new AlertDialog.Builder(v.getContext());
            editDialog.setTitle("Rename");

            final EditText editText = new EditText(v.getContext());
            editDialog.setView(editText);
            editText.setText(fileSimplePath.substring(fileSimplePath.lastIndexOf("/") + 1));
            editText.setSelection(0, editText.getText().length());

            editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                // do something when the button is clicked
                public void onClick(DialogInterface dialog, int which) {
                    String newName = editText.getText().toString();
                    renameFile(new File(fileSimplePath),
                            new File(fileSimplePath.substring(0, fileSimplePath.lastIndexOf("/") + 1) + newName));

                    mOpenedIndex = -1;
                    refreshFiles();
                    dialog.dismiss();
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
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    }
                }
            });
            AlertDialog dialog = editDialog.create();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

            dialog.show();
        }

        private boolean renameFile(File origin, File newFile) {
            Logger.d("renameFile origin = " + origin.getPath() + " new = " + newFile.getPath());
            if (origin.exists() && !newFile.exists()) {
                return origin.renameTo(newFile);
            }
            return false;
        }
    }

    private class ItemDelete_tsClickListener implements View.OnClickListener {

        private String fileSimplePath;

        public ItemDelete_tsClickListener(String path) {
            this.fileSimplePath = path;
        }

        @Override
        public void onClick(View v) {
            final AlertDialog.Builder editDialog = new AlertDialog.Builder(v.getContext());
            editDialog.setTitle("Delete file");
            editDialog.setMessage("Do you want to delete this record file?");
            editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                // do something when the button is clicked
                public void onClick(DialogInterface dialog, int which) {
                    deleteFile(new File(fileSimplePath));
                    deleteFile(new File(fileSimplePath + ".wav"));
                    deleteFile(new File(fileSimplePath + ".txt"));

                    Log.e("delyang","-------------------------------------------------1");
                    mOpenedIndex = -1;
                    refreshFiles();
                    dialog.dismiss();
                    Log.e("delyang","-------------------------------------------------2");
                }
            });
            editDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                // do something when the button is clicked
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Log.e("delyang","-------------------------------------------------3");
                }
            });
            editDialog.show();
            Log.e("delyang","-------------------------------------------------4");
        }

        private boolean deleteFile(File file) {
            Logger.d("deleteFile file = " + file.getPath());
            return file.delete();
        }
    }
*/
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

    public void setEnableMultipleCheck(boolean enable) {
        mEnableMultipleCheck = enable;
    }

    public ArrayList<String> getCheckedList() {
        return mCheckedList;
    }



    private class ItemShare_tsClickListener implements View.OnClickListener {

        private Context mContext;
        private boolean mIsSourceUsb;
        private String mFileSimplePath;
        private int mWhich;

        public ItemShare_tsClickListener(Context context, boolean isSourceUsb, String path) {
            mContext = context;
            mIsSourceUsb = isSourceUsb;
            mFileSimplePath = path;
        }

        @Override
        public void onClick(View v) {
            if (!mIsSourceUsb) {
                new android.support.v7.app.AlertDialog.Builder(mContext)
                        .setSingleChoiceItems(new String[]{"Raw"}, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mWhich = which;
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                shareAudio(mFileSimplePath);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            } else {
                new android.support.v7.app.AlertDialog.Builder(mContext)
                        .setSingleChoiceItems(new String[]{"Raw"}, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mWhich = which;
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                shareAudio(mFileSimplePath);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        }

        private void shareAudio(String shareFilePath) {
            File file = new File(shareFilePath + ".wav");
            Logger.d("shareAudio file = " + file.getPath());
            if (!file.exists()) {
                new ConvertWavAndShare().execute(shareFilePath);
            } else {
                shareFile(file.getPath());
            }
        }

        private void shareFile(String filePath) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("*/*");
            Uri uri = FileProvider.getUriForFile(
                    mContext,
                    mContext.getPackageName() + ".provider",
                    new File(filePath));
            share.putExtra(Intent.EXTRA_STREAM, uri);
            mContext.startActivity(Intent.createChooser(share, "Share audio File"));
        }

        private class ConvertWavAndShare extends AsyncTask<String, Void, String> {

            private Dialog mProgressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = ProgressDialog.show(mContext,
                        null, "Adding the wav header...",true);
            }

            @Override
            protected String doInBackground(String... strings) {
                File file = new File(strings[0]);
                WavHeaderHelper.addWavHeader(file, !file.getName().contains("USB"));
                return strings[0] + ".wav";
            }

            @Override
            protected void onPostExecute(String filePath) {
                shareFile(filePath);
                mProgressDialog.dismiss();
            }
        }
    }
}
