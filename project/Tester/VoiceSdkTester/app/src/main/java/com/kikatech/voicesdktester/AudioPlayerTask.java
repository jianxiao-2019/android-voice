package com.kikatech.voicesdktester;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by ryanlin on 03/01/2018.
 */

public class AudioPlayerTask extends AsyncTask<Void, Void, Void> {

    private String mFilePath;
    private AudioTrack mAudioTrack;

    private Button mTargetButton;
    private TextView mTargetTextView;
    private int mDrawablePlay;
    private int mDrawableSource;

    public AudioPlayerTask(String filePath, Button button) {
        mFilePath = filePath;
        mTargetButton = button;
    }

    public AudioPlayerTask(String filePath, TextView textView, int drawablePlay, int drawableSource) {
        mFilePath = filePath;
        mTargetTextView = textView;

        mDrawablePlay = drawablePlay;
        mDrawableSource = drawableSource;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                16000,
                getChannelConfig(),
                AudioFormat.ENCODING_PCM_16BIT,
                AudioTrack.getMinBufferSize(16000, getChannelConfig(), AudioFormat.ENCODING_PCM_16BIT),
                AudioTrack.MODE_STREAM);
        mAudioTrack.play();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        File file = new File(mFilePath);
        int size = (int) file.length();
        Log.d("Ryan", "doInBackground size = " + size + " mFilePath = " + mFilePath);
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (mAudioTrack != null) {
            mAudioTrack.write(bytes, 0, bytes.length);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void o) {
        super.onPostExecute(o);
        stop();
        if (mTargetButton != null) {
            mTargetButton.setText("PLAY");
        }
        if (mTargetTextView != null) {
            mTargetTextView.setCompoundDrawablesWithIntrinsicBounds(0, mDrawablePlay, 0, mDrawableSource);
        }
    }

    public void stop() {
        if (mAudioTrack != null) {
            if (mAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_STOPPED) {
                mAudioTrack.stop();
            }
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }

    public boolean isPlaying() {
        if (mAudioTrack != null) {
            return mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
        }
        return false;
    }

    private int getChannelConfig() {
        return mFilePath.contains("USB") ? AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO;
    }
}
