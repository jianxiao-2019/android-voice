package com.kikatech.usb.buffer;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.kikatech.usb.nc.KikaNcBuffer;
import com.kikatech.usb.util.LogUtil;

/**
 * Created by ryanlin on 14/03/2018.
 */

public abstract class KikaBuffer {
    private static final String TAG = "KikaBuffer";
    public static int NC_VERSION_KIKAGO = 1;
    public static int NC_VERSION_DASEN = 2;

    public static int NC_VERSION = NC_VERSION_KIKAGO;

    public static final int BUFFER_SIZE_KIKAGO = 3840;
    public static final int BUFFER_SIZE_DASEN = 512;

    private static final int TYPE_NOISE_CANCELLATION = 1;
    private static final int TYPE_STEREO_TO_MONO = 2;

    @IntDef({TYPE_NOISE_CANCELLATION, TYPE_STEREO_TO_MONO})
    public @interface BufferType {
        int NOISE_CANCELLATION = TYPE_NOISE_CANCELLATION;
        int STEREO_TO_MONO = TYPE_STEREO_TO_MONO;
    }

    public static KikaBuffer getKikaBuffer(@BufferType int type) {
        switch (type) {
            case BufferType.STEREO_TO_MONO:
                if (LogUtil.DEBUG) {
                    LogUtil.logd(TAG, "BufferType.STEREO_TO_MONO");
                }
                return new KikaS2MBuff();
            default:
            case BufferType.NOISE_CANCELLATION:
                if (LogUtil.DEBUG) {
                    LogUtil.logd(TAG, "BufferType.NOISE_CANCELLATION");
                }
                return new KikaNcBuffer();
        }
    }

    public abstract void onData(byte[] data, int len);

    public abstract void create();

    public abstract void close();

    public abstract void reset();

    public abstract int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes);
}
