package com.kikatech.voice.core.hotword;

import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.util.log.LogUtil;
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
    private String activeModel = strEnvWorkSpace + ACTIVE_UMDL;
    private String commonRes = strEnvWorkSpace + ACTIVE_RES;

    private SnowboyDetect mSnowboyDetect;
    private OnHotWordDetectListener mListener;

    private boolean isAwake;

    public SnowBoyDetector(OnHotWordDetectListener listener, IDataPath dataPath) {
        super(dataPath);
        mListener = listener;
        Logger.d("SnowBoyDetector before new SnowboyDetect");
        mSnowboyDetect = new SnowboyDetect(commonRes, activeModel);
        Logger.d("SnowBoyDetector after new SnowboyDetect");
        mSnowboyDetect.SetSensitivity("0.6");
        //-detector.SetAudioGain(1);
        mSnowboyDetect.ApplyFrontend(true);
    }

    @Override
    protected void checkWakeUpCommand(byte[] data) {
        byte[] monoData = stereoToMono(data);
        short[] audioData = new short[monoData.length / 2];
        ByteBuffer.wrap(monoData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(audioData);

        // Snowboy hotword detection.
        int result = mSnowboyDetect.RunDetection(audioData, audioData.length);

        Logger.d("checkWakeUpCommand result = " + result);
        LogUtil.logw("KikaLaunchActivity", String.format("result: %s", result));

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
                    Logger.d("checkWakeUpCommand result wake up");
                    isAwake = true;
                    if (mListener != null) {
                        mListener.onDetected();
                    }
                }
                break;

        }
    }

    private byte[] stereoToMono(byte[] stereoData) {
        byte[] monoResult = new byte[stereoData.length / 2];
        for (int i = 0; i < monoResult.length; i += 2) {
            monoResult[i] = stereoData[i * 2];
            monoResult[i + 1] = stereoData[i * 2 + 1];
        }

        return monoResult;
    }

    @Override
    protected boolean isAwake() {
        return isAwake;
    }

    @Override
    public void goSleep() {
        mSnowboyDetect.Reset();
        isAwake = false;
    }
}
