package com.kikatech.usb.datasource;

import android.support.annotation.NonNull;

import com.kikatech.usb.buffer.KikaBuffer;
import com.kikatech.usb.nc.KikaNcBuffer;
import com.kikatech.usb.util.DataUtil;
import com.kikatech.usb.util.DbUtil;
import com.kikatech.voice.core.debug.DebugUtil;
import com.kikatech.voice.core.debug.FileWriter;
import com.kikatech.voice.core.recorder.IVoiceSource;
import com.kikatech.voice.core.util.DataUtils;
import com.kikatech.voice.util.log.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by tianli on 17-11-6.
 * Update by ryanlin on 25/12/2017.
 */

public class KikaGoVoiceSource implements IVoiceSource {

    public static int OPEN_RESULT_FAIL = -1;
    public static int OPEN_RESULT_MONO = 1;
    public static int OPEN_RESULT_STEREO = 2;

    public static final int INIT_VOLUME = 5;

    public static final int ERROR_VOLUME_NOT_INITIALIZED = 254;
    public static final int ERROR_VOLUME_FW_NOT_SUPPORT = 255;
    public static final int ERROR_VERSION = -1;

    private static final int[] VERSIONS_TO_INVERSE = {
            0x1221,
            0x1224,
    };

    private IUsbDataSource mUsbDataSource;

    private KikaBuffer mKikaBuffer;

    private AtomicBoolean mIsOpened = new AtomicBoolean(false);

    private OnOpenedCallback mOnOpenedCallback;
    private SourceDataCallback mSourceDataCallback;

    private boolean mIsInversePhase = false;

    private DbUtil mDbUtil;

    private FileWriter mFileWriter;

    public interface OnOpenedCallback {
        void onOpened(int state);
    }

    public interface SourceDataCallback {
        void onSource(byte[] leftData, byte[] rightData);

        void onCurrentDB(int curDB);
    }

    private IUsbDataSource.OnDataListener mOnDataListener = new IUsbDataSource.OnDataListener() {
        @Override
        public void onData(byte[] data, int length) {
            byte[] leftResult = new byte[length / 2];
            byte[] rightResult = new byte[length / 2];
            DataUtil.separateChannelsToLeftAndRight(data, leftResult, rightResult);

            if (mIsInversePhase) {
                DataUtil.correctInversePhase(data, leftResult, rightResult);
            }

            if (mSourceDataCallback != null) {
                mSourceDataCallback.onSource(leftResult, rightResult);
                mDbUtil.onData(leftResult, leftResult.length);
            }

            if (mFileWriter != null) {
                mFileWriter.onData(data, length);
            }

            mKikaBuffer.onData(data, length);
        }
    };

    private DbUtil.DbCallback mDbCallback = new DbUtil.DbCallback() {
        @Override
        public void onCurrentDB(int curDB) {
            if (mSourceDataCallback != null) {
                mSourceDataCallback.onCurrentDB(curDB);
            }
        }

        @Override
        public void onLongtimeDB(int longtimeDB) {

        }

        @Override
        public void onMaxDB(int maxDB) {

        }
    };

    public KikaGoVoiceSource(IUsbDataSource source) {
        mUsbDataSource = source;
        mUsbDataSource.setOnDataListener(mOnDataListener);

        mKikaBuffer = KikaBuffer.getKikaBuffer(KikaBuffer.BufferType.STEREO_TO_MONO);

        mDbUtil = new DbUtil();
        mDbUtil.setDbCallback(mDbCallback);

        mFileWriter = DebugUtil.isDebug() ? new FileWriter("_USB", null) : null;
    }

    @Override
    public boolean open() {

        boolean success = mUsbDataSource.open();
        Logger.d("KikaGoVoiceSource open success = " + success);
        if (success) {
            mIsOpened.set(true);
            mKikaBuffer.create();

            mIsInversePhase = isIsInversePhase(checkFwVersion());
        } else {
            Logger.e("KikaGoVoiceSource open fail.");
        }

        if (mOnOpenedCallback != null) {
            if (success) {
                mOnOpenedCallback.onOpened(OPEN_RESULT_STEREO);
            } else {
                mOnOpenedCallback.onOpened(OPEN_RESULT_FAIL);
            }
        }

        return mIsOpened.get();
    }

    @Override
    public void start() {
        mDbUtil.clearData();
        mUsbDataSource.start();

        if (mFileWriter != null) {
            mFileWriter.start();
        }
    }

    @Override
    public void stop() {
        mUsbDataSource.stop();

        mDbUtil.clearData();
        if (mSourceDataCallback != null) {
            mSourceDataCallback.onCurrentDB(0);
        }
    }

    @Override
    public void close() {
        if (mIsOpened.compareAndSet(true, false)) {
            mUsbDataSource.close();
            mKikaBuffer.close();
        }

        mDbUtil.clearData();
        if (mSourceDataCallback != null) {
            mSourceDataCallback.onCurrentDB(0);
        }
    }

    @Override
    public int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        int readInt = mKikaBuffer.read(audioData, offsetInBytes, sizeInBytes);
        short[] shorts = DataUtils.byteToShort(audioData, audioData.length / 2);
        for (int i = 0; i < shorts.length; i++) {
            shorts[i] = (short) (shorts[i] * 2);
        }
        byte[] amplifiedData = DataUtils.shortToByte(shorts);
        for (int i = 0; i < audioData.length; i++) {
            audioData[i] = amplifiedData[i];
        }
        return readInt;
    }

    @Override
    public int getBufferSize() {
        return KikaNcBuffer.BUFFER_SIZE;
    }

    public void updateBufferType(@KikaBuffer.BufferType int type) {
        mKikaBuffer.close();
        mKikaBuffer = KikaBuffer.getKikaBuffer(type);
        mKikaBuffer.create();
    }

    public void setNoiseCancellationParameters(int mode, int value) {
        KikaNcBuffer.setNoiseSuppressionParameters(mode, value);
    }

    public int getNoiseSuppressionParameters(int mode) {
        return KikaNcBuffer.getNoiseSuppressionParameters(mode);
    }

    public boolean mIsOpened() {
        return mIsOpened.get();
    }

    public int checkVolumeState() {
        if (!mIsOpened()) {
            Logger.w("Fail operation because the Usb audio not initialized : checkVolumeState");
            return ERROR_VOLUME_NOT_INITIALIZED;
        }
        Logger.d("[" + Thread.currentThread().getName() + "] checkVolumeState mUsbAudio checkVolumeState = " + mUsbDataSource.checkVolumeState());
        return mUsbDataSource.checkVolumeState();
    }

    public int volumeUp() {
        if (!mIsOpened()) {
            Logger.w("Fail operation because the Usb audio not initialized : volumeUp");
            return ERROR_VOLUME_NOT_INITIALIZED;
        }
        Logger.d("[" + Thread.currentThread().getName() + "] volumeUp mUsbAudio checkVolumeState = " + mUsbDataSource.checkVolumeState());
        return mUsbDataSource.volumeUp();
    }

    public int volumeDown() {
        if (!mIsOpened()) {
            Logger.w("Fail operation because the Usb audio not initialized : volumeDown");
            return ERROR_VOLUME_NOT_INITIALIZED;
        }
        Logger.d("[" + Thread.currentThread().getName() + "] volumeDown mUsbAudio checkVolumeState = " + mUsbDataSource.checkVolumeState());
        return mUsbDataSource.volumeDown();
    }

    public int checkFwVersion() {
        if (!mIsOpened()) {
            Logger.w("Fail operation because the Usb audio not initialized : checkFwVersion");
            return ERROR_VERSION;
        }
        byte[] result = mUsbDataSource.checkFwVersion();

        return result[1] & 0xFF |
                (result[0] & 0xFF) << 8;
    }

    public int checkDriverVersion() {
        if (!mIsOpened()) {
            Logger.w("Fail operation because the Usb audio not initialized : checkDriverVersion");
            return ERROR_VERSION;
        }
        byte[] result = mUsbDataSource.checkDriverVersion();

        return result[1] & 0xFF |
                (result[0] & 0xFF) << 8;
    }

    public void setKikaBuffer(int tag) {
        if (!mIsOpened()) {
            mKikaBuffer = KikaBuffer.getKikaBuffer(tag);
        } else {
            Logger.e("Can't change the buffer when it has been opened.");
        }
    }

    public void setSourceDataCallback(SourceDataCallback callback) {
        mSourceDataCallback = callback;
    }

    public void setOnOpenedCallback(OnOpenedCallback callback) {
        mOnOpenedCallback = callback;
    }

    public int getNcVersion() {
        return KikaNcBuffer.getVersion();
    }

    private boolean isIsInversePhase(int ver) {
        for (int version : VERSIONS_TO_INVERSE) {
            if (ver == version) {
                return true;
            }
        }
        return false;
    }
}
