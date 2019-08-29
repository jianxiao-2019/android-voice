package com.kikatech.usb.nc;

import android.support.annotation.NonNull;
import android.util.Log;

import com.kikatech.usb.buffer.KikaBuffer;
import com.kikatech.usb.buffer.CircularBuffer;
import com.kikatech.usb.util.DataUtil;
import com.kikatech.usb.util.LogUtil;

import ai.kikago.usb.NoiseCancellation;

/**
 * Created by ryanlin on 15/01/2018.
 */

public class KikaNcBuffer extends KikaBuffer {
    private static final String TAG = "KikaNcBuffer";

    public static final int CONTROL_ANGLE = 0;
    public static final int CONTROL_NC = 1;
    public static final int CONTROL_MODE = 2;

    private final CircularBuffer mCircularBuffer;
    private final byte[] mAudioBytes_kikago = new byte[BUFFER_SIZE_KIKAGO];
    private final byte[] mAudioBytes_dasen = new byte[BUFFER_SIZE_DASEN];
    private int mOffset = 0;

    public KikaNcBuffer() {
        mCircularBuffer = new CircularBuffer(200000);
    }

    public static int getNcBufferSize() {
        if(NC_VERSION == NC_VERSION_KIKAGO)
            return BUFFER_SIZE_KIKAGO;
        else if(NC_VERSION == NC_VERSION_DASEN)
            return BUFFER_SIZE_DASEN;
        return BUFFER_SIZE_DASEN;
    }

    @Override
    public void onData(byte[] data, int len) {
        int tempLen = len;
        int tempIdx = 0;
        int length;

        if(NC_VERSION == NC_VERSION_KIKAGO) {
            while (tempLen + mOffset >= BUFFER_SIZE_KIKAGO) {
                length = BUFFER_SIZE_KIKAGO - mOffset;
                System.arraycopy(data, tempIdx, mAudioBytes_kikago, mOffset, length);
                tempLen -= length;
                tempIdx += length;
                mCircularBuffer.write(doNoiseCancellation(), mAudioBytes_kikago.length / 2);
                mOffset = 0;
            }
        } else {
            while (tempLen + mOffset >= BUFFER_SIZE_DASEN) {
                length = BUFFER_SIZE_DASEN - mOffset;
                System.arraycopy(data, tempIdx, mAudioBytes_dasen, mOffset, length);
                tempLen -= length;
                tempIdx += length;
                mCircularBuffer.write(doNoiseCancellation(), mAudioBytes_dasen.length / 2);
                mOffset = 0;
            }
        }

        if(NC_VERSION == NC_VERSION_KIKAGO)
            System.arraycopy(data, tempIdx, mAudioBytes_kikago, mOffset, tempLen);
        else
            System.arraycopy(data, tempIdx, mAudioBytes_dasen, mOffset, tempLen);
        mOffset += tempLen;
    }

    private byte[] doNoiseCancellation() {
        short[] outBuffs;
        if(NC_VERSION == NC_VERSION_KIKAGO) {
            outBuffs = new short[mAudioBytes_kikago.length / 2];
            ai.kikago.usb.NoiseCancellation.NoiseMask0(DataUtil.byteToShort(mAudioBytes_kikago), outBuffs);
        } else {
            outBuffs = new short[mAudioBytes_dasen.length / 2];
            lib.android.anc.NoiseCancellation.NoiseMask(DataUtil.byteToShort(mAudioBytes_dasen), outBuffs);
        }
        return DataUtil.shortToByte(outBuffs);
    }

    @Override
    public void create() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "create");
        }
        ai.kikago.usb.NoiseCancellation.Init();
        lib.android.anc.NoiseCancellation.Init();
    }

    @Override
    public void close() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "close");
        }
        ai.kikago.usb.NoiseCancellation.Destroy();
        lib.android.anc.NoiseCancellation.Destroy();
    }

    @Override
    public void reset() {
        mCircularBuffer.clear();
    }

    @Override
    public int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        return mCircularBuffer.read(audioData, offsetInBytes, sizeInBytes);
    }

    public static void setNoiseSuppressionParameters(int mode, int value) {
        if(NC_VERSION == NC_VERSION_KIKAGO) {
            ai.kikago.usb.NoiseCancellation.SetControl(mode, value);
        }else {
            lib.android.anc.NoiseCancellation.SetControl(mode, value);
        }
    }

    public static int getNoiseSuppressionParameters(int mode) {
        if(NC_VERSION == NC_VERSION_KIKAGO) {
            return ai.kikago.usb.NoiseCancellation.GetControl(mode);
        } else {
            return lib.android.anc.NoiseCancellation.GetControl(mode);
        }
    }

    public static int getVersion() {
        if(NC_VERSION == NC_VERSION_KIKAGO) {
            return ai.kikago.usb.NoiseCancellation.GetVersion();
        } else {
            return lib.android.anc.NoiseCancellation.GetVersion();
        }
    }
    public static void SetRefGain(float g) {
        if(NC_VERSION == NC_VERSION_KIKAGO) {
            ai.kikago.usb.NoiseCancellation.SetRefGain(g);
        } else {
            lib.android.anc.NoiseCancellation.SetRefGain(g);
        }
    }
    public static void enableWebrtc() {
        NoiseCancellation.enableWebrtc();
    }
    public static void enableBeamforming() {
        NoiseCancellation.enableBeamforming();
    }
    public static void enableNoiseGate() {
        NoiseCancellation.enableNoiseGate();
    }
    public static void enableEq() {
        NoiseCancellation.enableEq();
    }
    public static void enableAgc() {
        NoiseCancellation.enableAgc();
    }
    public static void disableWebrtc() {
        NoiseCancellation.disableWebrtc();
    }
    public static void disableBeamforming() {
        NoiseCancellation.disableBeamforming();
    }
    public static void disableNoiseGate() {
        NoiseCancellation.disableNoiseGate();
    }
    public static void disableEq() {
        NoiseCancellation.disableEq();
    }
    public static void disableAgc() {
        NoiseCancellation.disableAgc();
    }
    public static void setAgcMode(int value) {
        NoiseCancellation.setAgcMode(value);
    }
    public static void setAgcGaindB(int value) {
        NoiseCancellation.setAgcGaindB(value);
    }
    public static void setAgcTargetLevelDbfs(int value) {
        NoiseCancellation.setAgcTargetLevelDbfs(value);
    }
    public static void setWebRtcMode(int value) {
        NoiseCancellation.setWebRtcMode(value);
    }
    public static void setInspace(float value) {
        NoiseCancellation.setInspace(value);
    }
    public static void setNoiseGateThreshold(int value) {
        NoiseCancellation.setNoiseGateThreshold(value);
    }
}
