package com.kikatech.voice.core.ns;

import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.util.log.Logger;

import java.util.Arrays;

import lib.android.anc.NoiseCancellation;

/**
 * Created by tianli on 17-10-30.
 */

public class NoiseSuppression implements IDataPath {

    private IDataPath mDataPath;

    public NoiseSuppression(IDataPath dataPath) {

        mDataPath = dataPath;

        NoiseCancellation.Init();
        NoiseCancellation.SetVAD(4000);
    }

    @Override
    public void onData(byte[] data) {
        short[] allOfInBufs = ByteToShort(data, data.length / 2);
//        short[] allOfOutBufs = new short[allOfInBufs.length];

        int loops = data.length / 512;
        int remainder = allOfInBufs.length % 256;

        short[] outBufs = new short[256];
        Logger.d("NoiseSuppression onData data.length 2 = " + data.length);
        Logger.d("NoiseSuppression onData allOfInBufs.length = " + allOfInBufs.length + " loops = " + loops + " remainder = " + remainder);
        for (int i = 0; i < loops; i++) {
            short[] inBufs = Arrays.copyOfRange(allOfInBufs, i * 256, (i + 1) * 256);
             NoiseCancellation.NoiseMask(inBufs, outBufs);
            // Logger.d("NoiseSuppression onData inBufs.length = " + inBufs.length + " outBufs.length = " + outBufs.length);

//            for (int j = 0; j < outBufs.length; j++) {
//                allOfOutBufs[i * 128 + j] = outBufs[j];
//            }
            //short[] postfix = Arrays.copyOfRange(outBufs, 128, 256);
            if (mDataPath != null) {
                mDataPath.onData(ShortToByte(outBufs));
            }
        }

//        byte[] outData = ShortToByte(allOfOutBufs);
//        Logger.d("NoiseSuppression onData outData.length = " + outData.length);
//        if (mDataPath != null) {
//            mDataPath.onData(outData);
//        }
    }

    public void close() {
        NoiseCancellation.Destroy();
    }

    private short[] ByteToShort(byte[] bytes, int len) {
        short[] shorts = new short[len];
        for (int i = 0; i < len; ++i) {
            shorts[i] = (short) ((bytes[i * 2 + 1] << 8) | (bytes[i * 2] & 0xff));
        }
        return shorts;
    }

    private byte[] ShortToByte(short[] shorts) {
        byte[] bytes = new byte[shorts.length * 2];
        for (int i = 0; i < shorts.length; i++) {
            bytes[2 * i] = (byte) (shorts[i] & 0xff);
            bytes[2 * i + 1] = (byte) ((shorts[i] >> 8) & 0xff);
        }

        return bytes;
    }
}
