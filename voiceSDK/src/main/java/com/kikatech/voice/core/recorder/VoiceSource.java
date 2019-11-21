package com.kikatech.voice.core.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.support.annotation.NonNull;

/**
 * Created by tianli on 17-10-29.
 * Update by ryanlin on 25/12/2017.
 *
 * 手机mic的声音来源
 */

public class VoiceSource implements IVoiceSource {

    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    private final static int AUDIO_SAMPLE_RATE = 16000;

    private final static int AUDIO_FORMAT_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT_ENCODING = AudioFormat.ENCODING_PCM_16BIT;


    private AudioRecord mAudioRecord;
    private final int mBufferSizeInBytes;

    private AcousticEchoCanceler mCanceler;

    public VoiceSource() {
        mBufferSizeInBytes = AudioRecord.getMinBufferSize(
                AUDIO_SAMPLE_RATE, AUDIO_FORMAT_CHANNEL, AUDIO_FORMAT_ENCODING);
    }

    @Override
    public boolean open() {
        return true;
    }

    @Override
    public void start() {
        if (mAudioRecord != null) {
            releaseAec();
            mAudioRecord.release();
        }
        mAudioRecord = createAudioRecord();
        if (mAudioRecord == null || mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            return;
        }
        initAec(mAudioRecord);
        mAudioRecord.startRecording();
    }

    @Override
    public void stop() {
        releaseAec();
        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

    @Override
    public void close() {
    }



    @Override
    public int getBufferSize() {
        return mBufferSizeInBytes;
    }

    @Override
    public int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        if (mAudioRecord == null) {
            return -1;
        }
        return mAudioRecord.read(audioData, offsetInBytes, sizeInBytes);
    }


    private AudioRecord createAudioRecord() {
        if (mBufferSizeInBytes <= 0) {
            return null;
        }
        try {
            return new AudioRecord(AUDIO_INPUT, AUDIO_SAMPLE_RATE, AUDIO_FORMAT_CHANNEL,
                    AUDIO_FORMAT_ENCODING, mBufferSizeInBytes);
        } catch (Exception e) {
            return null;
        }
    }

    private void initAec(AudioRecord audioRecord) {
        if (AcousticEchoCanceler.isAvailable()) {
            mCanceler = AcousticEchoCanceler.create(audioRecord.getAudioSessionId());
            if (mCanceler != null) {
                mCanceler.setEnabled(true);
            }
        }
    }

    private void releaseAec() {
        if (mCanceler != null) {
            mCanceler.setEnabled(false);
            mCanceler.release();
            mCanceler = null;
        }
    }
}
