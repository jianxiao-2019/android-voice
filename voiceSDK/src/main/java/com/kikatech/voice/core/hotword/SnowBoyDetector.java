package com.kikatech.voice.core.hotword;

import android.util.SparseIntArray;

import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.util.CustomConfig;
import com.kikatech.voice.util.log.FileLoggerUtil;
import com.kikatech.voice.util.log.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ai.kitt.snowboy.Constants;
import ai.kitt.snowboy.SnowboyDetect;

/**
 * @author SkeeterWang Created on 2017/12/6.
 */

public class SnowBoyDetector extends WakeUpDetector {
    static {
        System.loadLibrary("snowboy-detect-android");
    }

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

    SnowBoyDetector(OnHotWordDetectListener listener) {
        super(listener);
        mListener = listener;
        long t = System.currentTimeMillis();
        Logger.d("[sboy]SnowBoyDetector before new SnowboyDetect:" + activeModel);
        mSnowboyDetect = new SnowboyDetect(commonRes, activeModel);
        Logger.d("[sboy]SnowBoyDetector after new SnowboyDetect");
        mSnowboyDetect.SetSensitivity(CustomConfig.getSnowboySensitivity());
        //-detector.SetAudioGain(1);
        mSnowboyDetect.ApplyFrontend(true);
        Logger.d("[sboy] init done, spend :" + (System.currentTimeMillis() - t) + " ms");
        Logger.d("[sboy] hotwords:" + mSnowboyDetect.NumHotwords() + ", Sensitivity:" + mSnowboyDetect.GetSensitivity() +
                ", BitsPerSample:" + mSnowboyDetect.BitsPerSample() + ", NumChannels:" + mSnowboyDetect.NumChannels() +
                ", SampleRate:" + mSnowboyDetect.SampleRate());
    }

    @Override
    protected synchronized void checkWakeUpCommand(byte[] data) {
        //Logger.d("[sboy]checkWakeUpCommand data len = " + data.length);
        if(mSnowboyDetect == null) {
            Logger.d("[sboy]Err, mSnowboyDetect is null");
            return;
        }

        if (audioDataBuffer == null || audioDataBuffer.length != data.length / 2) {
            audioDataBuffer = new short[data.length / 2];
        }
        ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(audioDataBuffer);

//        FileLoggerUtil.getIns().asyncWriteToFile(data, dbgPath);

        // Snowboy hotword detection.
        int result = mSnowboyDetect.RunDetection(audioDataBuffer, audioDataBuffer.length);

        //Logger.d("[sboy]checkWakeUpCommand result = " + result);

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
                    Logger.d("[sboy]checkWakeUpCommand result wake up");
                    isAwake = true;
                    if (mListener != null) {
                        mListener.onDetected();
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
                StringBuilder log = new StringBuilder("[sboy] {");
                for (int i = 0; i < mSnowbpoyLog.size(); i++) {
                    log.append(mSnowbpoyLog.keyAt(i)).append(":").append(mSnowbpoyLog.valueAt(i)).append(", ");
                    detectCount += mSnowbpoyLog.valueAt(i);
                }
                log.append("}, Detection count : ").append(detectCount);
                Logger.d(log.toString());
                logTime = System.currentTimeMillis();
                mSnowbpoyLog.clear();
            }
        }
    }

    private byte[] stereoToMono(byte[] stereoData) {
        if(monoResultBuffer == null || monoResultBuffer.length != stereoData.length / 2) {
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
        isAwake = false;
    }

    @Override
    public void wakeUp() {
        isAwake = true;
    }

    @Override
    public synchronized void close() {
        Logger.d("[sboy] close, mSnowboyDetect:" + mSnowboyDetect);
        if (mSnowboyDetect != null) {
            mSnowboyDetect.delete();
            mSnowboyDetect = null;
        }
        if(audioDataBuffer != null) {
            audioDataBuffer = null;
        }
        if(monoResultBuffer != null) {
            monoResultBuffer = null;
        }
    }
}