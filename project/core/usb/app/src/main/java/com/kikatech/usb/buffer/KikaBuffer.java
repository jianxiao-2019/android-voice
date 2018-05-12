package com.kikatech.usb.buffer;

import android.support.annotation.NonNull;

import com.kikatech.usb.nc.KikaNcBuffer;

/**
 * Created by ryanlin on 14/03/2018.
 */

public abstract class KikaBuffer {

    public static final int TYPE_NOISC_CANCELLATION = 1;
    public static final int TYPE_STEREO_TO_MONO = 2;

    public static KikaBuffer getKikaBuffer(int type) {
        if (type == TYPE_STEREO_TO_MONO) {
            return new KikaS2MBuff();
        }
        return new KikaNcBuffer();
    }

    public abstract void onData(byte[] data, int len);
    public abstract void create();
    public abstract void close();
    public abstract void reset();
    public abstract int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes);
}
