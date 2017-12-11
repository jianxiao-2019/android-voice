package com.kikatech.voice.core.vad;

import com.kikatech.androidspeex.Speex;
import com.kikatech.voice.VadUtil;
import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.util.log.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by tianli on 17-10-28.
 */

public class VoiceDetector implements IDataPath {

    private static final int DEFAULT_FRAME_LENGTH = 6400;

    private IDataPath mDataPath = null;

    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private AtomicBoolean mStopped = new AtomicBoolean(false);

    private OnVadProbabilityChangeListener mListener;

    private int mFrameLength = DEFAULT_FRAME_LENGTH;
    private short[] mBuf = new short[DEFAULT_FRAME_LENGTH];
    private int mBufLen = 0;

    private float mPrevProb = -1;
    private Speex mSpeex;

    public interface OnVadProbabilityChangeListener {
        void onSpeechProbabilityChanged(float speechProbability);
    }

    public VoiceDetector(IDataPath dataPath, OnVadProbabilityChangeListener listener) {
        mDataPath = dataPath;
        mListener = listener;
    }

    public void startDetecting() {
        Logger.d("VoiceDetector startDetecting");
        mStopped.set(false);
    }

    public void stopDetecting() {
        Logger.d("VoiceDetector stopDetecting");
        mStopped.set(true);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                while (mBufLen > 0) {
                    if (mDataPath != null) {
                        mDataPath.onData(Speex_Encode_Func(mBuf, mBufLen));
                    }
                    mBufLen = 0;
                }
            }
        });
    }

    @Override
    public void onData(byte[] data) {
        if (mStopped.get()) {
            Logger.e("Can't add data while VoiceDetector is stopped.");
            return;
        }
        byte[] tempData = new byte[data.length];
        System.arraycopy(data, 0, tempData, 0, data.length);
        mExecutor.execute(new DetectorTask(tempData));
    }

    public void updatePacketInterval(int packetInterval) {
        Logger.d("updatePacketInterval packetInterval = " + packetInterval + " buff size = " + getFrameLength(packetInterval));
        int newFrameLength = getFrameLength(packetInterval);
        if (newFrameLength != mFrameLength) {
            mExecutor.execute(new UpdateBuffSizeTask(newFrameLength));
        }
    }

    private int getFrameLength(int packetInterval) {
        return DEFAULT_FRAME_LENGTH * packetInterval / 500;
    }

    private class UpdateBuffSizeTask implements Runnable {

        private int mBuffSize;
        UpdateBuffSizeTask(int size) {
            mBuffSize = size;
        }

        @Override
        public void run() {
            mBuf = new short[mBuffSize];
            mBufLen = 0;
            mFrameLength = mBuffSize;
        }
    }

    private class DetectorTask implements Runnable {

        private byte[] mData;

        public DetectorTask(byte[] data) {
            mData = data;
        }

        @Override
        public void run() {
            if (mStopped.get()) {
                return;
            }
            final byte[] data = mData;
            float[] sample = ByteToFloat(data, data.length / 2);
            float prob = VadUtil.speechProbability(sample, 0, sample.length,
                    VadUtil.sConf);
            if (mListener != null && mPrevProb != prob) {
                mListener.onSpeechProbabilityChanged(prob);
            }
            mPrevProb = prob;

            handleVoiceData(data);
        }

        private void handleVoiceData(byte[] data) {
            short[] vadData = ByteToShort(data, data.length / 2);
            int vadSize = vadData.length;
            if (vadSize + mBufLen < mFrameLength) {
                System.arraycopy(vadData, 0, mBuf, mBufLen, vadSize);
                mBufLen += vadSize;
            } else {
                int temp_len = vadSize + mBufLen - mFrameLength;
                if (temp_len > 0) {
                    System.arraycopy(vadData, 0, mBuf, mBufLen, mFrameLength - mBufLen);
                    short[] temp_data = new short[temp_len];
                    System.arraycopy(vadData, mFrameLength - mBufLen, temp_data, 0, temp_len);
                    if (mDataPath != null) {
                        mDataPath.onData(Speex_Encode_Func(mBuf, mFrameLength));
                    }
                    System.arraycopy(temp_data, 0, mBuf, 0, temp_len);
                }
                mBufLen = temp_len;
            }
        }
    }

    private synchronized byte[] Speex_Encode_Func(short[] buf, int len) {
        if (mSpeex == null) {
            mSpeex = new Speex();
            mSpeex.init();
        }
        int speex_bytes = 0, curr_bytes;
        int tot_bytes, offset = 0;
        int enc_frame_size = mSpeex.getFrameSize();
        byte[] data = new byte[mFrameLength];
        byte[] encoded = new byte[enc_frame_size];
        while (offset < len) {
            if (len - offset < enc_frame_size) {
                curr_bytes = len - offset;
            } else {
                curr_bytes = enc_frame_size;
            }
            short[] sample = new short[curr_bytes];
            System.arraycopy(buf, offset, sample, 0, curr_bytes);
            offset += curr_bytes;
            tot_bytes = mSpeex.encode(sample, 0, encoded, curr_bytes);
            data[speex_bytes++] = (byte) tot_bytes;
            System.arraycopy(encoded, 0, data, speex_bytes, tot_bytes);
            speex_bytes += tot_bytes;
        }

        byte[] tempData = new byte[speex_bytes];
        if (data.length < speex_bytes) {
            speex_bytes = data.length;
        }
        System.arraycopy(data, 0, tempData, 0, speex_bytes);
        return tempData;
    }

    private short[] ByteToShort(byte[] bytes, int len) {
        short[] shorts = new short[len];
        for (int i = 0; i < len; ++i) {
            shorts[i] = (short) ((bytes[i * 2 + 1] << 8) | (bytes[i * 2] & 0xff));
        }
        return shorts;
    }

    private float[] ByteToFloat(byte[] bytes, int len) {
        float[] floats = new float[len];
        for (int i = 0; i < len; ++i) {
            floats[i] = (float) ((bytes[i * 2 + 1] << 8) | (bytes[i * 2] & 0xff));
        }
        return floats;
    }

}
