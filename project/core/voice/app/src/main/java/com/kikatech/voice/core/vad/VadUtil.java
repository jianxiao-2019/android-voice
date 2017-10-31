package com.kikatech.voice.core.vad;

import com.kikatech.voice.engine.interfaces.IVoiceManager;

public class VadUtil {
    static {
        System.loadLibrary("voice-vadutil");

        if (IVoiceManager.DEBUG) {
            VadUtil.switchJNILog(false);
        }
    }

    public static native void switchJNILog(boolean logSwitch);

    public static native float speechProbability(float[] data, int startFrame, int endFrame, Object conf);

    public static VadConfiguration sConf = new VadConfiguration();

    private static final VadUtil sInstance = new VadUtil();

    private VadUtil() {
    }

    public static VadUtil getInstance() {
        return sInstance;
    }

    public void updateConfig(VadConfiguration conf) {
        sConf.filterWindowMs = conf.filterWindowMs;
        sConf.shortPauseLenMs = conf.shortPauseLenMs;
        sConf.silenceFilterLenMs = conf.silenceFilterLenMs;
        sConf.framePerMs = conf.framePerMs;
        sConf.windowStepMs = conf.windowStepMs;
        sConf.minPhoneLenMs = conf.minPhoneLenMs;

        sConf.minSilenceAmplitude = conf.minSilenceAmplitude;
        sConf.maxSilenceAmplitude = conf.maxSilenceAmplitude;
        sConf.autoSilenceMultiple = conf.autoSilenceMultiple;
        sConf.minSpeechFrequency = conf.minSpeechFrequency;
        sConf.maxSpeechFrequency = conf.maxSpeechFrequency;
        sConf.minSpeechFrequencyWeight = conf.minSpeechFrequencyWeight;
        sConf.minSpeechFrequencyWeightPercent = conf.minSpeechFrequencyWeightPercent;
        sConf.speechJudgementProbability = conf.speechJudgementProbability;
        sConf.totalFrequencyZero = conf.totalFrequencyZero;
    }

//    public static class conf {
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


