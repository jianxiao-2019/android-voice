package com.kikatech.go.engine.vad;

import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by xm009 on 17/6/13.
 */

@JsonObject(fieldDetectionPolicy = JsonObject.FieldDetectionPolicy.NONPRIVATE_FIELDS)
public class FilterFrameInfo {
    public int filterWindowMs = 20;  // 过滤窗长ms
    public int shortPauseLenMs = 1000;  // 短暂停时间ms
    public int silenceFilterLenMs = 100;  // 长静音时间ms
    public int framePerMs = 16;  // 每毫秒帧数
    public int windowStepMs = 5;  // 移窗速率ms
    public int minPhoneLenMs = 40; // 最小音素长ms

    public float minSilenceAmplitude = 260;  // 最小静音振幅
    public float maxSilenceAmplitude = 1000;  // 最大静音振幅
    public float autoSilenceMultiple = 3.0f;  // 自动环境音量倍数
    public float minSpeechFrequency = 60.0f;  // 基频最低语音频率
    public float maxSpeechFrequency = 800.0f;  // 基频最高语音频率
    public float minSpeechFrequencyWeight = 110.0f;  // 最低基频语音强度
    public float minSpeechFrequencyWeightPercent = 0.3f;  // 最低基频语音强度比例
    public float speechJudgementProbability = 0.3f;  // 语音评判概率
    public float totalFrequencyZero = 40;
}
