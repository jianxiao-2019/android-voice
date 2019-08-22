package com.kikatech.usb.datasource;

import android.support.annotation.NonNull;

import com.kikatech.usb.buffer.KikaBuffer;
import com.kikatech.usb.nc.KikaNcBuffer;
import com.kikatech.usb.util.DataUtil;
import com.kikatech.usb.util.DataUtils;
import com.kikatech.usb.util.DbUtil;
import com.kikatech.usb.util.LogUtil;
import com.kikatech.usb.util.debug.DebugUtil;
import com.kikatech.usb.util.debug.FileWriter;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.kikago.usb.NoiseCancellation;

/**
 * Created by tianli on 17-11-6.
 * Update by ryanlin on 25/12/2017.
 */

public class KikaGoVoiceSource {
    private static final String TAG = "KikaGoVoiceSource";

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

    private OnOpenedCallback mOnOpenedCallback;
    private SourceDataCallback mSourceDataCallback;

    private DbUtil mDbUtil;
    private FileWriter mFileWriter;

    private AtomicBoolean mIsOpened = new AtomicBoolean(false);
    private boolean mIsInversePhase = false;
    private boolean isAmplifyDB;


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

        mKikaBuffer = KikaBuffer.getKikaBuffer(KikaBuffer.BufferType.NOISE_CANCELLATION);

        mDbUtil = new DbUtil();
        mDbUtil.setDbCallback(mDbCallback);

        mFileWriter = new FileWriter("_USB");

        if (getNcVersion() == 50750) {
            isAmplifyDB = true;
        } else {
            isAmplifyDB = false;
        }
//        isAmplifyDB = true;
    }


    public void setSourceDataCallback(SourceDataCallback callback) {
        mSourceDataCallback = callback;
    }

    public void setOnOpenedCallback(OnOpenedCallback callback) {
        mOnOpenedCallback = callback;
    }


    public void setAudioFilePath(String path, String fileName) {

        DebugUtil.setAudioFilePath(String.format("%s/%s%s", path, "Kikago_", fileName));
    }


    public boolean open() {
        boolean success = mUsbDataSource.open();
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, String.format("KikaGoVoiceSource open success: %s", success));
        }
        if (success) {
            mIsOpened.set(true);
            mKikaBuffer.create();
            mIsInversePhase = isIsInversePhase(checkFwVersion());
        } else {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, "KikaGoVoiceSource open fail.");
            }
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

    public void start() {
        mDbUtil.clearData();
        mUsbDataSource.start();

        if (mFileWriter != null) {
            mFileWriter.start();
        }
    }

    public int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        int readInt = mKikaBuffer.read(audioData, offsetInBytes, sizeInBytes);

        if (isAmplifyDB) {
            short[] shorts = DataUtils.byteToShort(audioData, audioData.length / 2);
            for (int i = 0; i < shorts.length; i++) {
                shorts[i] = (short) (shorts[i] * 2);
            }
            byte[] amplifiedData = DataUtils.shortToByte(shorts);
            for (int i = 0; i < audioData.length; i++) {
                audioData[i] = amplifiedData[i];
            }
        }

        return readInt;
    }

    public void stop() {
        mUsbDataSource.stop();

        mDbUtil.clearData();
        if (mSourceDataCallback != null) {
            mSourceDataCallback.onCurrentDB(0);
        }
    }

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


    public void setKikaBuffer(int tag) {
        if (!mIsOpened()) {
            mKikaBuffer = KikaBuffer.getKikaBuffer(tag);
        } else {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, "Can't change the buffer when it has been opened.");
            }
        }
    }

    public void updateBufferType(@KikaBuffer.BufferType int type) {
        mKikaBuffer.close();
        mKikaBuffer = KikaBuffer.getKikaBuffer(type);
        mKikaBuffer.create();
    }

    public int getBufferSize() {
        return KikaNcBuffer.getNcBufferSize();
    }


    public int checkVolumeState() {
        if (!mIsOpened()) {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, "Fail operation because the Usb audio not initialized : checkVolumeState");
            }
            return ERROR_VOLUME_NOT_INITIALIZED;
        }
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, "Fail operation because the Usb audio not initialized : checkVolumeState");
        }
        int volumeState = mUsbDataSource.checkVolumeState();
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, String.format("[%s] checkVolumeState mUsbAudio checkVolumeState: %s", Thread.currentThread().getName(), volumeState));
        }
        return volumeState;
    }

    public int volumeUp() {
        if (!mIsOpened()) {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, "Fail operation because the Usb audio not initialized : volumeUp");
            }
            return ERROR_VOLUME_NOT_INITIALIZED;
        }
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, String.format("[%s] volumeUp mUsbAudio checkVolumeState: %s", Thread.currentThread().getName(), mUsbDataSource.checkVolumeState()));
        }
        return mUsbDataSource.volumeUp();
    }

    public int volumeDown() {
        if (!mIsOpened()) {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, "Fail operation because the Usb audio not initialized : volumeDown");
            }
            return ERROR_VOLUME_NOT_INITIALIZED;
        }
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, String.format("[%s] volumeDown mUsbAudio checkVolumeState: %s", Thread.currentThread().getName(), mUsbDataSource.checkVolumeState()));
        }
        return mUsbDataSource.volumeDown();
    }

    public int checkFwVersion() {
        if (!mIsOpened()) {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, "Fail operation because the Usb audio not initialized : checkFwVersion");
            }
            return ERROR_VERSION;
        }
        byte[] result = mUsbDataSource.checkFwVersion();

        return result[1] & 0xFF | (result[0] & 0xFF) << 8;
    }

    public int checkDriverVersion() {
        if (!mIsOpened()) {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, "Fail operation because the Usb audio not initialized : checkDriverVersion");
            }
            return ERROR_VERSION;
        }
        byte[] result = mUsbDataSource.checkDriverVersion();

        return result[1] & 0xFF | (result[0] & 0xFF) << 8;
    }


    private boolean isIsInversePhase(int ver) {
        for (int version : VERSIONS_TO_INVERSE) {
            if (ver == version) {
                return true;
            }
        }
        return false;
    }

    public boolean mIsOpened() {
        return mIsOpened.get();
    }


    public void setNoiseCancellationParameters(int mode, int value) {
        KikaNcBuffer.setNoiseSuppressionParameters(mode, value);
    }

    public int getNoiseSuppressionParameters(int mode) {
        return KikaNcBuffer.getNoiseSuppressionParameters(mode);
    }

    public static int getNcVersion() {
        return KikaNcBuffer.getVersion();
    }


    public interface OnOpenedCallback {
        void onOpened(int state);
    }

    public interface SourceDataCallback {
        void onSource(byte[] leftData, byte[] rightData);

        void onCurrentDB(int curDB);
    }


    public void enableWebrtc() {
        KikaNcBuffer.enableWebrtc();
    }
    public void Beamforming() {
        KikaNcBuffer.Beamforming();
    }
    public void Omlsa() {
        KikaNcBuffer.Omlsa();
    }
    public void NoiseGate() {
        KikaNcBuffer.NoiseGate();
    }
    public void enableEq() {
        KikaNcBuffer.enableEq();
    }
    public void enableAgc() {
        KikaNcBuffer.enableAgc();
    }
    public void Gain() {
        KikaNcBuffer.Gain();
    }
}
