package com.kikatech.usb.util;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ian.fan on 19/04/2018.
 */

public class DbUtil {
//    private static final DbUtil sInstance = new DbUtil();

    private DbCallback mDbCallback;

    private ArrayList<Short> mDbBuf = new ArrayList<Short>();
    private ArrayList<Double> mSamples = new ArrayList<Double>();

    final private int rateX = 100; //every 1 sec get 160 samples, 16000/rateX
    final private int latestSamplesCount = 32; //the amount of samples in the latest 0.2 sec, (16000/rateX)*(db_count_time/1000)
    final private int longtimeSamplesCount = 1600; // the amount of samples in 10 secs, (16000/rateX)*10
    final private long db_count_time = 200; //update info every 0.2 sec
    private long db_c_time;
    private int mMaxDB = 0;

    public interface DbCallback {
        void onCurrentDB(int curDB);

        void onLongtimeDB(int longtimeDB);

        void onMaxDB(int maxDB);
    }

    public void setDbCallback(DbCallback callback) {
        mDbCallback = callback;
    }

    public void clearData() {
        mSamples.clear();
        mMaxDB = 0;
    }

    public void onData(byte[] data, int readSize) {
        if (mDbCallback == null) {
            return;
        }

        try {
            short[] buffer = ByteToShort(data);
            synchronized (mDbBuf) {
                for (int i = 0; i < readSize; i += rateX) {
                    mDbBuf.add(buffer[i]);
                }
            }
        } catch (Throwable t) {
        }

        long time = new Date().getTime();
        if (time - db_c_time >= db_count_time) {
            ArrayList<Short> buf = new ArrayList<Short>();
            synchronized (mDbBuf) {
                if (mDbBuf.size() == 0) {
                    return;
                } else {
                    while (mDbBuf.size() > longtimeSamplesCount) {
                        mDbBuf.remove(0);
                    }
                }
                buf = (ArrayList<Short>) mDbBuf.clone();
            }
            calculateDB(buf, buf.size());
            db_c_time = new Date().getTime();
        }

    }

    private void calculateDB(ArrayList<Short> buffer, int readSize) {
        if (readSize == 0) {
            return;
        }

        final int latestSize = buffer.size() - latestSamplesCount;
        double latestMaxSample = 0;

        for (int i = 0; i < buffer.size(); i++) {
            Short sample = buffer.get(i);
            double absSample = Math.abs(sample);
            if (i >= latestSize) {
                latestMaxSample = Math.max(latestMaxSample, absSample);
            }
        }

        // current DB
        final int currentDB = (int) SampleToDb(latestMaxSample);
//        Logger.d("[db]currentDB_" + String.valueOf(currentDB));
        if (mDbCallback != null) {
            mDbCallback.onCurrentDB(currentDB);
        }

        // long time DB
        mSamples.add(latestMaxSample);
        double sum = 0;
        for (double sample : mSamples) {
            sum += sample;
        }
        double rms = sum / mSamples.size();
        final int longtimeDB = (int) SampleToDb(rms);
//        Logger.d("[db]avgDB" + String.valueOf(avgDB));
        if (mDbCallback != null) {
            mDbCallback.onLongtimeDB(longtimeDB);
        }

        // max DB
        if (latestMaxSample > mMaxDB) {
            mMaxDB = (int) latestMaxSample;
//            Logger.d("[db]maxDB_" + String.valueOf(maxDB));
            if (mDbCallback != null) {
                mDbCallback.onMaxDB(mMaxDB);
            }
        }
    }

    private double SampleToDb(double sample) {
        return 20 * Math.log10(sample + 1); // "+ 1" for prevent infinite number
    }

    private short[] ByteToShort(byte[] bytes) {
        int len = bytes.length / 2;
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
