package com.kikatech.voicesdktester.source;

import android.support.annotation.NonNull;

import com.kikatech.usb.buffer.KikaBuffer;
import com.kikatech.usb.datasource.KikaGoVoiceSource;
import com.kikatech.usb.nc.KikaNcBuffer;
import com.kikatech.voice.core.recorder.IVoiceSource;

/**
 * @author SkeeterWang Created on 2018/6/29.
 */
public class KikaGoUsbVoiceSourceWrapper implements IVoiceSource {

    private final KikaGoVoiceSource mVoiceSource;

    public KikaGoUsbVoiceSourceWrapper(@NonNull KikaGoVoiceSource source) {
        this.mVoiceSource = source;
    }

    public void setAudioFilePath(String path, String fileName) {
        mVoiceSource.setAudioFilePath(path, fileName);
    }

    @Override
    public boolean open() {
        return mVoiceSource.open();
    }

    @Override
    public void start() {
        mVoiceSource.start();
    }

    @Override
    public void stop() {
        mVoiceSource.stop();
    }

    @Override
    public void close() {
        mVoiceSource.close();
    }

    @Override
    public int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        return mVoiceSource.read(audioData, offsetInBytes, sizeInBytes);
    }

    @Override
    public int getBufferSize() {
        return mVoiceSource.getBufferSize();
    }


    public void setSourceDataCallback(KikaGoVoiceSource.SourceDataCallback callback) {
        mVoiceSource.setSourceDataCallback(callback);
    }

    public void setOnOpenedCallback(KikaGoVoiceSource.OnOpenedCallback callback) {
        mVoiceSource.setOnOpenedCallback(callback);
    }

    public void setKikaBuffer(@KikaBuffer.BufferType int type) {
        mVoiceSource.setKikaBuffer(type);
    }

    public void updateBufferType(@KikaBuffer.BufferType int type) {
        mVoiceSource.updateBufferType(type);
    }

    public int checkVolumeState() {
        return mVoiceSource.checkVolumeState();
    }

    public int volumeUp() {
        return mVoiceSource.volumeUp();
    }

    public int volumeDown() {
        return mVoiceSource.volumeDown();
    }

    public int checkFwVersion() {
        return mVoiceSource.checkFwVersion();
    }

    public int checkDriverVersion() {
        return mVoiceSource.checkDriverVersion();
    }

    public String getSn() {
        return mVoiceSource.getSn();
    }

    public boolean mIsOpened() {
        return mVoiceSource.mIsOpened();
    }

    public int getNcVersion() {
        return mVoiceSource.getNcVersion();
    }

    public void setNoiseCancellationParameters(int mode, int value) {
        mVoiceSource.setNoiseCancellationParameters(mode, value);
    }

    public int getNoiseSuppressionParameters(int mode) {
        return mVoiceSource.getNoiseSuppressionParameters(mode);
    }

    public void enableWebrtc() {
        mVoiceSource.enableWebrtc();
    }
    public void enableBeamforming() {
        mVoiceSource.enableBeamforming();
    }
    public void SetRefGain(float g) {
        mVoiceSource.SetRefGain(g);
    }
    public void enableNoiseGate() {
        mVoiceSource.enableNoiseGate();
    }
    public void enableEq() {
        mVoiceSource.enableEq();
    }
    public void enableAgc() {
        mVoiceSource.enableAgc();
    }
    public void setAgcMode(int value) {
        mVoiceSource.setAgcMode(value);
    }
    public void setAgcGaindB(int value) {
        mVoiceSource.setAgcGaindB(value);
    }
    public void setAgcTargetLevelDbfs(int value) {
        mVoiceSource.setAgcTargetLevelDbfs(value);
    }
    public void setWebRtcMode(int value) {
        mVoiceSource.setWebRtcMode(value);
    }
    public void setInspace(float value) {
        mVoiceSource.setInspace(value);
    }
}