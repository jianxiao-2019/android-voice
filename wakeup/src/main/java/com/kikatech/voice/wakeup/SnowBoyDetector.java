package com.kikatech.voice.wakeup;

import android.content.Context;
import android.util.SparseIntArray;

import com.kikatech.voice.core.hotword.WakeUpDetector;
import com.kikatech.voice.util.CustomConfig;
import com.kikatech.voice.util.log.Logger;
import com.kikatech.voice.util.request.MD5;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ai.kitt.snowboy.AppResCopy;
import ai.kitt.snowboy.Constants;
import ai.kitt.snowboy.SnowboyDetect;

/**
 * @author SkeeterWang Created on 2017/12/6.
 */

public class SnowBoyDetector extends WakeUpDetector {
    static {
        System.loadLibrary("snowboy-detect-android");
    }

    private static final String TAG = "SnowBoyDetector";

    private static final String ACTIVE_RES = Constants.ACTIVE_RES;
    private static final String ACTIVE_UMDL = Constants.ACTIVE_UMDL;

    private static String strEnvWorkSpace = Constants.DEFAULT_WORK_SPACE;
    private final String activeModel = strEnvWorkSpace + ACTIVE_UMDL;
    private final String commonRes = strEnvWorkSpace + ACTIVE_RES;

    private SnowboyDetect mSnowboyDetect;

    private boolean isAwake;

    private SparseIntArray mSnowbpoyLog = new SparseIntArray();
    private long logTime = 0;

    private short[] audioDataBuffer;
    private byte[] monoResultBuffer;

    private boolean mEnableDetection = true;

    public SnowBoyDetector(Context context) {
        long t = System.currentTimeMillis();
        AppResCopy.copyResFromAssetsToSD(context);
        String sensitivity = CustomConfig.getSnowboySensitivity();

        if (Logger.DEBUG) {
            Logger.i(TAG, "SnowBoyDetector before new SnowBoyDetector:" + activeModel);
            String md5 = MD5.getFileMD5(new File(activeModel));
            Logger.i(TAG, "model:" + activeModel + ", md5:" + md5);
            Logger.i(TAG, "sensitivity:" + sensitivity);
        }

        mSnowboyDetect = new SnowboyDetect(commonRes, activeModel);
        mSnowboyDetect.SetSensitivity(sensitivity);
        //-detector.SetAudioGain(1);
        mSnowboyDetect.ApplyFrontend(true);

        if (Logger.DEBUG) {
            Logger.i(TAG, "init done, spend :" + (System.currentTimeMillis() - t) + " ms");
            Logger.i(TAG, "hotwords:" + mSnowboyDetect.NumHotwords() + ", Sensitivity:" + mSnowboyDetect.GetSensitivity() +
                    ", BitsPerSample:" + mSnowboyDetect.BitsPerSample() + ", NumChannels:" + mSnowboyDetect.NumChannels() +
                    ", SampleRate:" + mSnowboyDetect.SampleRate());
        }
    }

    @Override
    protected synchronized void checkWakeUpCommand(byte[] data) {
        //if(Logger.DEBUG) Logger.i(TAG, "checkWakeUpCommand data len = " + data.length);
        if (!mEnableDetection) {
            return;
        }
        if (mSnowboyDetect == null) {
            if (Logger.DEBUG) Logger.i(TAG, "Err, mSnowboyDetect is null");
            return;
        }

        if (audioDataBuffer == null || audioDataBuffer.length != data.length / 2) {
            audioDataBuffer = new short[data.length / 2];
        }
        ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(audioDataBuffer);

//        FileLoggerUtil.getIns().asyncWriteToFile(data, dbgPath);

        // Snowboy hotword detection.
        int result = mSnowboyDetect.RunDetection(audioDataBuffer, audioDataBuffer.length);

        //if(Logger.DEBUG) Logger.i(TAG, "checkWakeUpCommand result = " + result);

        switch (result) {
            case -2:
                // post a higher CPU usage:
                // sendMessage(MsgEnum.MSG_VAD_NOSPEECH, null);
                break;
            case -1:
                // Unknown Detection Error
                break;
            case 0:
                // post a higher CPU usage:
                // sendMessage(MsgEnum.MSG_VAD_SPEECH, null);
                break;
            default:
                if (result > 0) {
                    // sendMessage(MsgEnum.MSG_ACTIVE, null);
                    // Log.i("Snowboy: ", "Hotword " + Integer.toString(result) + " detected!");
                    // player.start();
                    if (Logger.DEBUG) {
                        Logger.i(TAG, "checkWakeUpCommand result wake up");
                    }
                    isAwake = true;
                    if (mListener != null) {
                        mListener.onDetected();
                    } else {
                        if (Logger.DEBUG) {
                            Logger.w(TAG, "mListener::OnHotWordDetectListener is null");
                        }
                    }
                }
                break;
        }

        debugDetectResult(result);
    }

    private void debugDetectResult(int result) {
        if (Logger.DEBUG) {
            Integer v = mSnowbpoyLog.get(result);
            mSnowbpoyLog.put(result, v + 1);
            long lll = System.currentTimeMillis() - logTime;
            if (lll > 2000 || result > 0) {
                int detectCount = 0;
                StringBuilder log = new StringBuilder("{");
                for (int i = 0; i < mSnowbpoyLog.size(); i++) {
                    log.append(mSnowbpoyLog.keyAt(i)).append(":").append(mSnowbpoyLog.valueAt(i)).append(", ");
                    detectCount += mSnowbpoyLog.valueAt(i);
                }
                log.append("}, Detection count : ").append(detectCount);
                if (Logger.DEBUG) Logger.i(TAG, log.toString());
                logTime = System.currentTimeMillis();
                mSnowbpoyLog.clear();
            }
        }
    }

    private byte[] stereoToMono(byte[] stereoData) {
        if (monoResultBuffer == null || monoResultBuffer.length != stereoData.length / 2) {
            monoResultBuffer = new byte[stereoData.length / 2];
        }
        for (int i = 0; i < monoResultBuffer.length; i += 2) {
            monoResultBuffer[i] = stereoData[i * 2];
            monoResultBuffer[i + 1] = stereoData[i * 2 + 1];
        }

        return monoResultBuffer;
    }

    @Override
    public boolean isAwake() {
        return isAwake;
    }

    @Override
    public void reset() {
        if (mSnowboyDetect != null) {
            mSnowboyDetect.Reset();
        }
    }

    @Override
    public void goSleep() {
        if (Logger.DEBUG) {
            Logger.d(TAG, "goSleep");
        }
        isAwake = false;
    }

    @Override
    public void wakeUp() {
        if (Logger.DEBUG) {
            Logger.d(TAG, "wakeUp");
        }
        isAwake = true;
    }

    @Override
    public synchronized void close() {
        if (Logger.DEBUG) {
            Logger.i(TAG, "close, mSnowboyDetect:" + mSnowboyDetect);
        }
        if (mSnowboyDetect != null) {
            mSnowboyDetect.delete();
            mSnowboyDetect = null;
        }
        if (audioDataBuffer != null) {
            audioDataBuffer = null;
        }
        if (monoResultBuffer != null) {
            monoResultBuffer = null;
        }
    }

    @Override
    public synchronized void enableDetector(boolean enable) {
        if (Logger.DEBUG) {
            Logger.i(TAG, "enableDetector: " + enable);
        }
        mEnableDetection = enable;
    }

    @Override
    public boolean isEnabled() {
        return mEnableDetection;
    }
}