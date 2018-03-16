package com.kika.usbasrtester;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.FileProvider;
import android.view.View;

import com.kikatech.voice.core.debug.WavHeaderHelper;
import com.kikatech.voice.util.log.Logger;

import java.io.File;

/**
 * Created by ryanlin on 06/02/2018.
 */

public class ItemShareClickListener implements View.OnClickListener {

    private Context mContext;
    private boolean mIsSourceUsb;
    private String mFileSimplePath;
    private int mWhich;

    public ItemShareClickListener(Context context, boolean isSourceUsb, String path) {
        mContext = context;
        mIsSourceUsb = isSourceUsb;
        mFileSimplePath = path;
    }

    @Override
    public void onClick(View v) {
        if (mIsSourceUsb) {
            new android.support.v7.app.AlertDialog.Builder(mContext)
                    .setSingleChoiceItems(new String[]{"Raw", "Noise Cancellation"}, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mWhich = which;
                        }
                    })
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            shareAudio(mFileSimplePath + (mWhich == 0 ? "_USB" : "_NC"));
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
            shareAudio(mFileSimplePath + "_SRC");
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
        share.setType("audio/wav");
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


