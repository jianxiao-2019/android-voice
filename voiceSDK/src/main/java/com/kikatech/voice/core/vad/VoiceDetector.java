package com.kikatech.voice.core.vad;

import com.kikatech.voice.VadUtil;
import com.kikatech.voice.core.debug.DebugUtil;
import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.core.util.DataUtils;
import com.kikatech.voice.service.event.EventMsg;
import com.kikatech.voice.core.debug.ReportUtil;
import com.kikatech.voice.util.log.Logger;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by tianli on 17-10-28.
 * Update by ryanlin on 25/12/2017.
 */

public class VoiceDetector extends IDataPath {

    private static final int S_BYTE_LEN = (int) (4096 * 1.5);//vad中的输入是一段4096字节的音频

    private byte[] mBuf = new byte[S_BYTE_LEN];
    private int mBufLen = 0;

    private float mPrevProb = -1;

    public VoiceDetector(IDataPath dataPath) {
        super(dataPath);
    }

    @Override
    public void onData(byte[] data, int length) {
        mNextPath.onData(data, length);

        // TODO : do this in the other thread?
        alignmentTheBufferSize(data, length);
    }

    private void alignmentTheBufferSize(byte[] audioData, int readSize) {
        int tempLen = readSize;
        int tempIdx = 0;
        int length;
        while (tempLen + mBufLen >= S_BYTE_LEN) {
            length = S_BYTE_LEN - mBufLen;
            System.arraycopy(audioData, tempIdx, mBuf, mBufLen, length);
            tempLen -= length;
            tempIdx += length;
            mBufLen = 0;
            testVad(mBuf, S_BYTE_LEN);
        }
        System.arraycopy(audioData, tempIdx, mBuf, mBufLen, tempLen);
        mBufLen += tempLen;
    }

    private void testVad(byte[] data, int length) {
        float[] sample = DataUtils.byteToFloat(data, length / 2);
        float prob = VadUtil.speechProbability(sample, 0, sample.length, VadUtil.sConf);

        if (mPrevProb != prob) {
            EventBus.getDefault().post(new EventMsg(EventMsg.Type.VD_VAD_CHANGED, prob));
        }
        mPrevProb = prob;

        if (DebugUtil.isDebug() && prob > 0) {
            if (!ReportUtil.getInstance().isEverDetectedVad()) {
                ReportUtil.getInstance().vadDetected();
                ReportUtil.getInstance().logTimeStamp("first_vad_prob = " + String.format("%.2f", (double) prob));
            } else {
                ReportUtil.getInstance().logText("vad_prob = " + String.format("%.2f", (double) prob));
            }
        }
    }
}
