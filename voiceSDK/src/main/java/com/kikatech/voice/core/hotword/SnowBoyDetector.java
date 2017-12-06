package com.kikatech.voice.core.hotword;

import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.util.log.LogUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ai.kitt.snowboy.Constants;
import ai.kitt.snowboy.SnowboyDetect;

/**
 * @author SkeeterWang Created on 2017/12/6.
 */

public class SnowBoyDetector implements IDataPath {
    static {
        System.loadLibrary("snowboy-detect-android");
    }

    private static final String ACTIVE_RES = Constants.ACTIVE_RES;
    private static final String ACTIVE_UMDL = Constants.ACTIVE_UMDL;

    private static String strEnvWorkSpace = Constants.DEFAULT_WORK_SPACE;
    private String activeModel = strEnvWorkSpace + ACTIVE_UMDL;
    private String commonRes = strEnvWorkSpace + ACTIVE_RES;

    private IDataPath mDataPath;

    private SnowboyDetect mSnowboyDetect;
    private OnHotWordDetectListener mListener;

    public interface OnHotWordDetectListener {
        void onDetected();
    }

    public SnowBoyDetector(OnHotWordDetectListener listener) {
        mListener = listener;
        mSnowboyDetect = new SnowboyDetect(commonRes, activeModel);
        mSnowboyDetect.SetSensitivity("0.6");
        //-detector.SetAudioGain(1);
        mSnowboyDetect.ApplyFrontend(true);
    }

    public void reset() {
        mSnowboyDetect.Reset();
    }

    @Override
    public void onData(byte[] data) {

        short[] audioData = new short[data.length / 2];
        ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(audioData);

        // Snowboy hotword detection.
        int result = mSnowboyDetect.RunDetection(audioData, audioData.length);

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
                    if (mListener != null) {
                        mListener.onDetected();
                    }
                }
                break;

        }
    }
}
