package com.kikatech.usb.util;

/**
 * Created by ryanlin on 2018/5/12.
 */

public class DataUtil {

    public static void correctInversePhase(byte[] data, byte[] leftResult, byte[] rightResult) {
        short[] shorts = byteToShort(leftResult);
        for (int i = 0; i < shorts.length; i += 1) {
            shorts[i] = (short) (shorts[i] * -1); //inverse the phase
        }
        shortToByteWithBytes(leftResult, shorts);
        combineLeftAndRightChannels(data, leftResult, rightResult);
    }

    public static void separateChannelsToLeftAndRight(byte[] data, byte[] leftResult, byte[] rightResult) {
        for (int i = 0; i < leftResult.length; i += 2) {
            leftResult[i] = data[i * 2];
            leftResult[i + 1] = data[i * 2 + 1];
            rightResult[i] = data[i * 2 + 2];
            rightResult[i + 1] = data[i * 2 + 3];
        }
    }

    private static void combineLeftAndRightChannels(byte[] data, byte[] leftResult, byte[] rightResult) {
        for (int i = 0; i < leftResult.length; i += 2) {
            data[i * 2] = leftResult[i];
            data[i * 2 + 1] = leftResult[i + 1];
        }
        for (int i = 0; i < rightResult.length; i += 2) {
            data[i * 2 + 2] = rightResult[i];
            data[i * 2 + 3] = rightResult[i + 1];
        }
    }

    public static short[] byteToShort(byte[] bytes) {
        int len = bytes.length / 2;
        short[] shorts = new short[len];
        for (int i = 0; i < len; ++i) {
            shorts[i] = (short) ((bytes[i * 2 + 1] << 8) | (bytes[i * 2] & 0xff));
        }
        return shorts;
    }

    private static void shortToByteWithBytes(byte[] bytes, short[] shorts) {
        for (int i = 0; i < shorts.length; i++) {
            bytes[2 * i] = (byte) (shorts[i] & 0xff);
            bytes[2 * i + 1] = (byte) ((shorts[i] >> 8) & 0xff);
        }
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
