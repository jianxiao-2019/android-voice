package com.kikatech.voice.core.hotword;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;

import com.kikatech.voice.core.recorder.IVoiceSource;

/**
 * @author SkeeterWang Created on 2017/12/6.
 */

public class HotWordVoiceSource implements IVoiceSource {
    private static final String TAG = "HotWordVoiceSource";

    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.DEFAULT;
    private final static int AUDIO_SAMPLE_RATE = 16000;
    private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private final static int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private final int mBufferSizeInBytes;

    private AudioRecord mAudioRecord;

    public HotWordVoiceSource() {
        mBufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_FORMAT);
    }

    @Override
    public void start() {
        if (mAudioRecord != null) {
            if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                mAudioRecord.stop();
            }
            mAudioRecord.release();
        }
        mAudioRecord = createAudioRecord();
        if (mAudioRecord == null || mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            return;
        }
        mAudioRecord.startRecording();
    }

    @Override
    public void stop() {
        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

    @Override
    public int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        if (mAudioRecord == null) {
            return -1;
        }
        return mAudioRecord.read(audioData, offsetInBytes, sizeInBytes);
    }

    @Override
    public int getBufferSize() {
        return mBufferSizeInBytes;
    }

    @Override
    public boolean isStereo() {
        return false;
    }

    private AudioRecord createAudioRecord() {
        if (mBufferSizeInBytes <= 0) {
            return null;
        }
        try {
            return new AudioRecord(AUDIO_INPUT, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_FORMAT, mBufferSizeInBytes);
        } catch (Exception e) {
            return null;
        }
    }
}
