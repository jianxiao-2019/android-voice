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

    private boolean mIsInversePhase = false;

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

            // fw version 1221 means one of the channels has inverse-phase issue
            mIsInversePhase = Integer.toHexString(checkFwVersion()).equals("1221");
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
            separateChannelsToLeftAndRight(data, leftResult, rightResult);
            correctInversePhase(data, leftResult, rightResult);
            mDbUtil.onData(leftResult, leftResult.length);
            mSourceDataCallback.onSource(leftResult, rightResult);
        }
        mKikaBuffer.onData(data, length);
    }

    private void correctInversePhase(byte[] data, byte[] leftResult, byte[] rightResult) {
        if (mIsInversePhase) {
            short[] shorts = ByteToShort(leftResult);
            for (int i = 0; i < shorts.length; i += 1) {
                shorts[i] = (short) (shorts[i] * -1); //inverse the phase
            }
            ShortToByteWithBytes(leftResult, shorts);
            combineLeftAndRightChannels(data, leftResult, rightResult);
        }
    }

    private void separateChannelsToLeftAndRight(byte[] data, byte[] leftResult, byte[] rightResult) {
        for (int i = 0; i < leftResult.length; i += 2) {
            leftResult[i] = data[i * 2];
            leftResult[i + 1] = data[i * 2 + 1];
            rightResult[i] = data[i * 2 + 2];
            rightResult[i + 1] = data[i * 2 + 3];
        }
    }

    private void combineLeftAndRightChannels(byte[] data, byte[] leftResult, byte[] rightResult) {
        for (int i = 0; i < leftResult.length; i += 2) {
            data[i * 2] = leftResult[i];
            data[i * 2 + 1] = leftResult[i + 1];
        }
        for (int i = 0; i < rightResult.length; i += 2) {
            data[i * 2 + 2] = rightResult[i];
            data[i * 2 + 3] = rightResult[i + 1];
        }
    }

    private short[] ByteToShort(byte[] bytes) {
        int len = bytes.length / 2;
        short[] shorts = new short[len];
        for (int i = 0; i < len; ++i) {
            shorts[i] = (short) ((bytes[i * 2 + 1] << 8) | (bytes[i * 2] & 0xff));
        }
        return shorts;
    }

    private void ShortToByteWithBytes(byte[] bytes, short[] shorts) {
        for (int i = 0; i < shorts.length; i++) {
            bytes[2 * i] = (byte) (shorts[i] & 0xff);
            bytes[2 * i + 1] = (byte) ((shorts[i] >> 8) & 0xff);
        }
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
