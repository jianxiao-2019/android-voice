package com.kikatech.go.services.presenter;

import android.support.annotation.NonNull;

import com.kikatech.usb.buffer.KikaBuffer;
import com.kikatech.usb.datasource.KikaGoVoiceSource;
import com.kikatech.voice.core.recorder.IVoiceSource;

/**
 * @author SkeeterWang Created on 2018/6/29.
 */
public class KikaGoUsbVoiceSourceWrapper implements IVoiceSource {

    private final KikaGoVoiceSource mVoiceSource;

    KikaGoUsbVoiceSourceWrapper(@NonNull KikaGoVoiceSource source) {
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


    public void setOnOpenedCallback(KikaGoVoiceSource.OnOpenedCallback callback) {
        mVoiceSource.setOnOpenedCallback(callback);
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
}
