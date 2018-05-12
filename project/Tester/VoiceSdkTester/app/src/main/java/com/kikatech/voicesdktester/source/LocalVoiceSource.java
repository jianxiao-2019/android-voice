package com.kikatech.voicesdktester.source;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.usb.buffer.KikaBuffer;
import com.kikatech.voice.core.recorder.IVoiceSource;
import com.kikatech.voice.util.log.Logger;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ryanlin on 02/04/2018.
 */

public abstract class LocalVoiceSource implements IVoiceSource {

    private String mTargetFilePath;
    private BufferedInputStream mBuffer;

    private KikaBuffer mKikaBuffer;

    private EofListener mEofListener;

    private AtomicBoolean mIsStopped = new AtomicBoolean(false);

    public interface EofListener {
        void onEndOfFile();
    }

    public LocalVoiceSource() {
        mKikaBuffer = getKikaBuffer();
        Logger.i("r5r5 LocalVoiceSource mKikaBuffer = " + mKikaBuffer);
    }

    @Override
    public boolean open() {
        mKikaBuffer.create();
        return true;
    }

    @Override
    public void start() {
        if (TextUtils.isEmpty(mTargetFilePath)) {
            Logger.e("Please select the target file first.");
            return;
        }
        mKikaBuffer.reset();
        mIsStopped.set(false);
        new Thread(() -> {
            try {
                int bufferSize = getBufferSize();
                byte[] audioData = new byte[bufferSize];
                mBuffer = new BufferedInputStream(new FileInputStream(mTargetFilePath));
                int result;
                while (!mIsStopped.get() &&(result = mBuffer.read(audioData, 0, bufferSize)) > 0) {
                    mKikaBuffer.onData(audioData, result);
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
        }).start();

    }

    @Override
    public void stop() {
        try {
            mBuffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mIsStopped.set(true);
        mBuffer = null;
    }

    @Override
    public void close() {
        mKikaBuffer.close();
    }

    @Override
    public int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        return mKikaBuffer.read(audioData, offsetInBytes, sizeInBytes);
    }

    public void setEofListener(EofListener eofListener) {
        mEofListener = eofListener;
    }

    public void setTargetFile(String path) {
        mTargetFilePath = path;
    }

    protected abstract KikaBuffer getKikaBuffer();
}
