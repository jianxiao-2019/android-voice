package com.kikatech.usb;

import android.support.annotation.NonNull;

import com.kikatech.usb.driver.UsbAudioDriver;
import com.kikatech.usb.nc.KikaNcBuffer;
import com.kikatech.usb.util.DbUtil;
import com.kikatech.voice.core.recorder.IVoiceSource;
import com.kikatech.voice.util.log.Logger;
import com.xiao.usbaudio.AudioPlayBack;
import com.xiao.usbaudio.UsbAudio;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by tianli on 17-11-6.
 * Update by ryanlin on 25/12/2017.
 */

public class UsbAudioSource implements IVoiceSource {

    public static int OPEN_RESULT_FAIL = -1;
    public static int OPEN_RESULT_MONO = 1;
    public static int OPEN_RESULT_STEREO = 2;

    private static final int INIT_VOLUME = 4;

    public static final int ERROR_VOLUME_NOT_INITIALIZED = 254;
    public static final int ERROR_VOLUME_FW_NOT_SUPPORT = 255;
    public static final int ERROR_VERSION = -1;

    private UsbAudioDriver mAudioDriver;

    private UsbAudio mUsbAudio = new UsbAudio();
    private KikaBuffer mKikaBuffer;

    private AtomicBoolean mIsOpened = new AtomicBoolean(false);

    private SourceDataCallback mSourceDataCallback;

    private DbUtil mDbUtil;

    public interface SourceDataCallback {
        void onSource(byte[] leftData, byte[] rightData);

        void onCurrentDB(int curDB);
    }

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

    public UsbAudioSource(UsbAudioDriver driver) {
        mAudioDriver = driver;
        mKikaBuffer = KikaBuffer.getKikaBuffer(KikaBuffer.TYPE_NOISC_CANCELLATION);
        mDbUtil = new DbUtil();
        mDbUtil.setDbCallback(mDbCallback);
    }

    @Override
    public boolean open() {
        Logger.d("KikaAudioDriver open openConnection  device name = " + mAudioDriver.getDeviceName()
                + " mConnectionFileDes = " + mAudioDriver.getFileDescriptor()
                + " productId = " + mAudioDriver.getProductId()
                + " vendorId = " + mAudioDriver.getVendorId());
        boolean success = mUsbAudio.setup(
                mAudioDriver.getDeviceName(),
                mAudioDriver.getFileDescriptor(),
                mAudioDriver.getProductId(),
                mAudioDriver.getVendorId());
        Logger.d("KikaAudioDriver open success = " + success);
        if (success) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    Logger.v("KikaAudioDriver start loop");
                    mUsbAudio.loop();
                    Logger.v("KikaAudioDriver stop loop");
                }
            }).start();
            setToDefaultVolume();

            mIsOpened.set(true);
            mKikaBuffer.create();
        } else {
            Logger.e("UsbAudioSource open fail.");
        }

        return mIsOpened.get();
    }

    private void setToDefaultVolume() {
        int volume = mUsbAudio.checkVolumeState();
        while (volume != ERROR_VOLUME_FW_NOT_SUPPORT && volume != INIT_VOLUME) {
            if (volume > INIT_VOLUME) {
                volume = mUsbAudio.volumeDown();
            } else if (volume < INIT_VOLUME) {
                volume = mUsbAudio.volumeUp();
            }
        }
    }

    @Override
    public void start() {
        mDbUtil.clearData();
        mUsbAudio.start();
        AudioPlayBack.setup(this);
    }

    @Override
    public void stop() {
        mUsbAudio.stop();
        AudioPlayBack.stop();

        mDbUtil.clearData();
        if (mSourceDataCallback != null) {
            mSourceDataCallback.onCurrentDB(0);
        }
    }

    @Override
    public void close() {
        if (mIsOpened.compareAndSet(true, false)) {
            mUsbAudio.close();
            mKikaBuffer.close();
        }

        mDbUtil.clearData();
        if (mSourceDataCallback != null) {
            mSourceDataCallback.onCurrentDB(0);
        }
    }

    @Override
    public int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        return mKikaBuffer.read(audioData, offsetInBytes, sizeInBytes);
    }

    @Override
    public int getBufferSize() {
        return KikaNcBuffer.BUFFER_SIZE;
    }

    public void setNoiseCancellationParameters(int mode, int value) {
        KikaNcBuffer.setNoiseSuppressionParameters(mode, value);
    }

    public int getNoiseSuppressionParameters(int mode) {
        return KikaNcBuffer.getNoiseSuppressionParameters(mode);
    }

    public void onData(byte[] data, int length) {
        if (mSourceDataCallback != null) {
            byte[] leftResult = new byte[length / 2];
            byte[] rightResult = new byte[length / 2];
            for (int i = 0; i < leftResult.length; i += 2) {
                leftResult[i] = data[i * 2];
                leftResult[i + 1] = data[i * 2 + 1];
                rightResult[i] = data[i * 2 + 2];
                rightResult[i + 1] = data[i * 2 + 3];
            }
            mDbUtil.onData(leftResult, leftResult.length);
            mSourceDataCallback.onSource(leftResult, rightResult);
        }
        mKikaBuffer.onData(data, length);
    }

    public boolean mIsOpened() {
        return mIsOpened.get();
    }

    public int checkVolumeState() {
        if (!mIsOpened()) {
            Logger.w("Fail operation because the Usb audio not initialized : checkVolumeState");
            return ERROR_VOLUME_NOT_INITIALIZED;
        }
        Logger.d("[" + Thread.currentThread().getName() + "] checkVolumeState mUsbAudio checkVolumeState = " + mUsbAudio.checkVolumeState());
        return mUsbAudio.checkVolumeState();
    }

    public int volumeUp() {
        if (!mIsOpened()) {
            Logger.w("Fail operation because the Usb audio not initialized : volumeUp");
            return ERROR_VOLUME_NOT_INITIALIZED;
        }
        Logger.d("[" + Thread.currentThread().getName() + "] volumeUp mUsbAudio checkVolumeState = " + mUsbAudio.checkVolumeState());
        return mUsbAudio.volumeUp();
    }

    public int volumeDown() {
        if (!mIsOpened()) {
            Logger.w("Fail operation because the Usb audio not initialized : volumeDown");
            return ERROR_VOLUME_NOT_INITIALIZED;
        }
        Logger.d("[" + Thread.currentThread().getName() + "] volumeDown mUsbAudio checkVolumeState = " + mUsbAudio.checkVolumeState());
        return mUsbAudio.volumeDown();
    }

    public int checkFwVersion() {
        if (!mIsOpened()) {
            Logger.w("Fail operation because the Usb audio not initialized : checkFwVersion");
            return ERROR_VERSION;
        }
        byte[] result = mUsbAudio.checkFwVersion();

        return result[1] & 0xFF |
                (result[0] & 0xFF) << 8;
    }

    public int checkDriverVersion() {
        if (!mIsOpened()) {
            Logger.w("Fail operation because the Usb audio not initialized : checkDriverVersion");
            return ERROR_VERSION;
        }
        byte[] result = mUsbAudio.checkDriverVersion();

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

    public int getNcVersion() {
        return KikaNcBuffer.getVersion();
    }
}
