package com.kikatech.usb.util;

/**
 * Created by ryanlin on 2018/5/14.
 */

public class DataUtils {

    public static short[] byteToShort(byte[] bytes, int len) {
        short[] shorts = new short[len];
        for (int i = 0; i < len; ++i) {
            shorts[i] = (short) ((bytes[i * 2 + 1] << 8) | (bytes[i * 2] & 0xff));
        }
        return shorts;
    }

    public static float[] byteToFloat(byte[] bytes, int len) {
        float[] floats = new float[len];
        for (int i = 0; i < len; ++i) {
            floats[i] = (float) ((bytes[i * 2 + 1] << 8) | (bytes[i * 2] & 0xff));
        }
        return floats;
    }

    public static byte[] shortToByte(short[] shorts) {
        byte[] bytes = new byte[shorts.length * 2];
        for (int i = 0; i < shorts.length; i++) {
            bytes[2 * i] = (byte) (shorts[i] & 0xff);
            bytes[2 * i + 1] = (byte) ((shorts[i] >> 8) & 0xff);
        }
        return bytes;
    }
}
