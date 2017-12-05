package com.kikatech.voice.core.recorder;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.support.annotation.NonNull;

import com.kikatech.voice.util.log.Logger;

/**
 * Created by tianli on 17-10-29.
 */

public class VoiceSource implements IVoiceSource {

    public final static int AUDIO_INPUT = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
    public final static int AUDIO_SAMPLE_RATE = 16000;

    private AudioRecord mAudioRecord;
    private final int mBufferSizeInBytes;

    private AcousticEchoCanceler mCanceler;

    public VoiceSource() {
        mBufferSizeInBytes = AudioRecord.getMinBufferSize(
                AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
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
//            return STATUS_RECORDER_INIT_FAIL;
            return;
        }
        initAec(mAudioRecord);
        mAudioRecord.startRecording();
//        return STATUS_SUCCESS;
    }

    @Override
    public void stop() {
        if (mCanceler != null) {
            mCanceler.setEnabled(false);
            mCanceler.release();
            mCanceler = null;
        }
        if (mAudioRecord != null) {
//            if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
//                mAudioRecord.stop();
//            }
            mAudioRecord.release();
            mAudioRecord = null;
        }
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
        Logger.d("createAudioRecord mBufferSizeInBytes = " + mBufferSizeInBytes);
        if (mBufferSizeInBytes <= 0) {
            return null;
        }
        try {
            return new AudioRecord(AUDIO_INPUT, AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_IN_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT, mBufferSizeInBytes);
        } catch (Exception e) {
            return null;
        }
    }

    private void initAec(AudioRecord audioRecord) {
        if (AcousticEchoCanceler.isAvailable()) {
            mCanceler = AcousticEchoCanceler.create(audioRecord.getAudioSessionId());
            if (mCanceler != null) {
                mCanceler.setEnabled(true);
                Logger.d("DefaultVoiceSource initAec mCanceler.enable = " + mCanceler.getEnabled());
            }
        }
    }

}
