package com.kikatech.voice.core.speex;

import com.kikatech.androidspeex.Speex;
import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.core.util.DataUtils;

/**
 * Created by ryanlin on 2018/5/14.
 */

public class SpeexEncoder extends IDataPath {

    private static final int DEFAULT_FRAME_LENGTH = 1920;

    private final int mFrameLength = DEFAULT_FRAME_LENGTH;
    private final short[] mBuf = new short[DEFAULT_FRAME_LENGTH];
    private int mBufLen = 0;

    private Speex mSpeex;

    public SpeexEncoder(IDataPath nextPath) {
        super(nextPath);
    }

    @Override
    public void onData(byte[] data, int dataLen) {

        short[] vadData = DataUtils.byteToShort(data, dataLen / 2);
        int tempLen = vadData.length;
        int tempIdx = 0;
        int length;
        while (tempLen + mBufLen >= mFrameLength) {
            length = mFrameLength - mBufLen;
            System.arraycopy(vadData, tempIdx, mBuf, mBufLen, length);
            tempLen -= length;
            tempIdx += length;
            if (mNextPath != null) {
                byte[] bytes = speexEncodeFunc(mBuf, mFrameLength);
                mNextPath.onData(bytes, bytes.length);
            }
            mBufLen = 0;
        }
        System.arraycopy(vadData, tempIdx, mBuf, mBufLen, tempLen);
        mBufLen += tempLen;
    }

    private byte[] speexEncodeFunc(short[] buf, int len) {
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
}
