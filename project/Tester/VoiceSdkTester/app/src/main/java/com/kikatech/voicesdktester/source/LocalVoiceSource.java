package com.kikatech.voicesdktester.source;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.usb.buffer.KikaBuffer;
import com.kikatech.usb.nc.KikaNcBuffer;
import com.kikatech.voice.core.recorder.IVoiceSource;
import com.kikatech.voice.core.util.DataUtils;
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

    private static final int FRAME_LENGTH = 16;

    private String mTargetFilePath;
    private BufferedInputStream mBuffer;

    private KikaBuffer mKikaBuffer;

    private EofListener mEofListener;

    private AtomicBoolean mIsStopped = new AtomicBoolean(false);

    private boolean isAmplifyDB = false;

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
        if (mKikaBuffer instanceof KikaNcBuffer) {
            if (KikaNcBuffer.getVersion() == 50750) {
                isAmplifyDB = true;
            } else {
                isAmplifyDB = false;
            }
        } else {
            isAmplifyDB = false;
        }
//        isAmplifyDB = true;
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
                while (!mIsStopped.get()) {
                    long begin = System.currentTimeMillis();
                    int result = mBuffer.read(audioData, 0, bufferSize);
                    if (result <= 0) {
                        break;
                    }

                    if (isAmplifyDB) {
                        short[] shorts = DataUtils.byteToShort(audioData, audioData.length / 2);
                        for (int i = 0; i < shorts.length; i++) {
                            shorts[i] = (short) (shorts[i] * 2);
                        }
                        byte[] amplifiedData = DataUtils.shortToByte(shorts);
                        for (int i = 0; i < audioData.length; i ++) {
                            audioData[i] = amplifiedData[i];
                        }
                    }

                    mKikaBuffer.onData(audioData, result);
                    Logger.v("LocalVoiceSource read from local (d) result = " + result);
                    long sleepTime = FRAME_LENGTH - (System.currentTimeMillis() - begin);
                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime);
                    }
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
