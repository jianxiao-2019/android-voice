package com.kikatech.voice;

import com.kikatech.vkb.engine.interfaces.IVoiceManager;

public class VadUtil {
    static {
        System.loadLibrary("voice-vadutil");

        if (IVoiceManager.DEBUG) {
            VadUtil.switchJNILog(false);
        }
    }

    public static native void switchJNILog(boolean logSwitch);

    public static native float speechProbability(float[] data, int startFrame, int endFrame, Object filterFrameInfo);

    public static FilterFrameInfo sFilterFrameInfo = new FilterFrameInfo();

    private static final VadUtil sInstance = new VadUtil();

    private VadUtil() {
    }

    public static VadUtil getInstance() {
        return sInstance;
    }

    public void updateConfig(FilterFrameInfo filterFrameInfo) {
        sFilterFrameInfo.filterWindowMs = filterFrameInfo.filterWindowMs;
        sFilterFrameInfo.shortPauseLenMs = filterFrameInfo.shortPauseLenMs;
        sFilterFrameInfo.silenceFilterLenMs = filterFrameInfo.silenceFilterLenMs;
        sFilterFrameInfo.framePerMs = filterFrameInfo.framePerMs;
        sFilterFrameInfo.windowStepMs = filterFrameInfo.windowStepMs;
        sFilterFrameInfo.minPhoneLenMs = filterFrameInfo.minPhoneLenMs;

        sFilterFrameInfo.minSilenceAmplitude = filterFrameInfo.minSilenceAmplitude;
        sFilterFrameInfo.maxSilenceAmplitude = filterFrameInfo.maxSilenceAmplitude;
        sFilterFrameInfo.autoSilenceMultiple = filterFrameInfo.autoSilenceMultiple;
        sFilterFrameInfo.minSpeechFrequency = filterFrameInfo.minSpeechFrequency;
        sFilterFrameInfo.maxSpeechFrequency = filterFrameInfo.maxSpeechFrequency;
        sFilterFrameInfo.minSpeechFrequencyWeight = filterFrameInfo.minSpeechFrequencyWeight;
        sFilterFrameInfo.minSpeechFrequencyWeightPercent = filterFrameInfo.minSpeechFrequencyWeightPercent;
        sFilterFrameInfo.speechJudgementProbability = filterFrameInfo.speechJudgementProbability;
        sFilterFrameInfo.totalFrequencyZero = filterFrameInfo.totalFrequencyZero;
    }

//    public static class FilterFrameInfo {
//        public int FILTER_WINDOW_MS = 20;  // 过滤窗长ms
//        public int SHORT_PAUSE_LEN_MS = 1000;  // 短暂停时间ms
//        public int SILENCE_FILETER_LEN_MS = 100;  // 长静音时间ms
//        public int FRAME_PER_MS = 16;  // 每毫秒帧数
//        public int WINDOW_STEP_MS = 5;  // 移窗速率ms
//        public int MIN_PHONE_LEN_MS = 40; // 最小音素长ms
//
//        public float MIN_SILENCE_AMPLITUDE = 200;  // 最小静音振幅
//        public float MAX_SILENCE_AMPLITUDE = 1500;  // 最大静音振幅
//        public float AUTO_SILENCE_MULTIPLE = 3;  // 自动环境音量倍数
//        public float MIN_SPEECH_FREQUENCY = 60;  // 基频最低语音频率
//        public float MAX_SPEECH_FREQUENCY = 800;  // 基频最高语音频率
//        public float MIN_SPEECH_FREQUENCY_WEIGHT = 80;  // 最低基频语音强度
//        public float MIN_SPEECH_FREQUENCY_WEIGHT_PERCENT = (float) 0.20;  // 最低基频语音强度比例
//        public float SPEECH_JUDGEMENT_PROBABILITY = (float) 0.3;  // 语音评判概率
//    }
}


