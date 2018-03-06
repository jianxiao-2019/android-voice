package com.kikatech.voice.core.vad;

import com.kikatech.androidspeex.Speex;
import com.kikatech.voice.VadUtil;
import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.service.EventMsg;
import com.kikatech.voice.util.ReportUtil;
import com.kikatech.voice.util.log.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by tianli on 17-10-28.
 * Update by ryanlin on 25/12/2017.
 */

public class VoiceDetector extends IDataPath {

    private static final int DEFAULT_FRAME_LENGTH = 6400;

    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private AtomicBoolean mStopped = new AtomicBoolean(false);

    private final int mFrameLength = DEFAULT_FRAME_LENGTH;
    private final short[] mBuf = new short[DEFAULT_FRAME_LENGTH];
    private int mBufLen = 0;

    private float mPrevProb = -1;
    private Speex mSpeex;

    public VoiceDetector(IDataPath dataPath) {
        super(dataPath);
    }

    @Override
    public void start() {
        super.start();
        Logger.d("VoiceDetector startDetecting");
        mStopped.set(false);
    }

    @Override
    public void stop() {
        super.stop();
        Logger.d("VoiceDetector stopDetecting");
        mStopped.set(true);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                while (mBufLen > 0) {
                    if (mNextPath != null) {
                        mNextPath.onData(Speex_Encode_Func(mBuf, mBufLen));
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
            float prob = VadUtil.speechProbability(sample, 0, sample.length, VadUtil.sConf);
            Logger.d("VoiceDetector prob = " + prob);
            if (prob > 0) {
                if (ReportUtil.getInstance().isEverDetectedVad() == false) {
                    ReportUtil.getInstance().vadDetected();
                    ReportUtil.getInstance().logTimeStamp("first_vad_prob = "+String.valueOf(prob));
                }
            }
            if (mPrevProb != prob) {
                EventBus.getDefault().post(new EventMsg(EventMsg.Type.VD_VAD_CHANGED, prob));
            }
            mPrevProb = prob;

            handleVoiceData(data);
        }

        private void handleVoiceData(byte[] data) {
            short[] vadData = ByteToShort(data, data.length / 2);
            int tempLen = vadData.length;
            int tempIdx = 0;
            int length;
            while (tempLen + mBufLen >= mFrameLength) {
                length = mFrameLength - mBufLen;
                System.arraycopy(vadData, tempIdx, mBuf, mBufLen, length);
                tempLen -= length;
                tempIdx += length;
                if (mNextPath != null) {
                    mNextPath.onData(Speex_Encode_Func(mBuf, mFrameLength));
                }
                mBufLen = 0;
            }
            System.arraycopy(vadData, tempIdx, mBuf, mBufLen, tempLen);
            mBufLen += tempLen;
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
