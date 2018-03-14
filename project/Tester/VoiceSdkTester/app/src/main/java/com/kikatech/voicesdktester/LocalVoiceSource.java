package com.kikatech.voicesdktester;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.usb.nc.KikaNcBuffer;
import com.kikatech.voice.core.recorder.IVoiceSource;
import com.kikatech.voice.util.log.Logger;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by ryanlin on 03/01/2018.
 */

public class LocalVoiceSource implements IVoiceSource {

    private int SIZE = KikaNcBuffer.BUFFER_SIZE;

    private String mTargetFilePath;
    private BufferedInputStream mBuffer;

    private KikaNcBuffer mKikaNcBuffer;

    private EofListener mEofListener;

    public interface EofListener {
        void onEndOfFile();
    }

    public LocalVoiceSource() {
        mKikaNcBuffer = new KikaNcBuffer();
    }

    @Override
    public void open() {
        mKikaNcBuffer.create();
    }

    @Override
    public void start() {
        if (TextUtils.isEmpty(mTargetFilePath)) {
            Logger.e("Please select the target file first.");
            return;
        }
        mKikaNcBuffer.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] audioData = new byte[SIZE];
                    mBuffer = new BufferedInputStream(new FileInputStream(mTargetFilePath));
                    int result;
                    while ((result = mBuffer.read(audioData, 0, SIZE)) > 0) {
                        mKikaNcBuffer.onData(audioData, result);
                        Logger.i("LocalVoiceSource read from local (d) result = " + result);
                        Thread.sleep(20);
                    }
                    if (mEofListener != null) {
                        mEofListener.onEndOfFile();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public void stop() {
        try {
            mBuffer.close();
            mKikaNcBuffer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mBuffer = null;
    }

    @Override
    public void close() {
        mKikaNcBuffer.close();
    }

    @Override
    public int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        return mKikaNcBuffer.read(audioData, offsetInBytes, sizeInBytes);
    }

    @Override
    public int getBufferSize() {
        return SIZE;
    }

    public void setEofListener(EofListener eofListener) {
        mEofListener = eofListener;
    }

    public void selectFile(String path) {
        mTargetFilePath = path;
    }
}
