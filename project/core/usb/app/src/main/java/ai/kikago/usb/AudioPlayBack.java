package ai.kikago.usb;

import com.kikatech.usb.datasource.impl.KikaGoDeviceDataSource;
import com.kikatech.usb.util.LogUtil;

;

public class AudioPlayBack {
    private static final String TAG = "AudioPlayBack";

    public static final int RAW_DATA_AVAILABLE_LENGTH = 600;

    // TODO : change to week reference?
    private static KikaGoDeviceDataSource sKikaGoDeviceDataSource;
    private static OnAudioPlayBackWriteListener mListener;

    // For check the hardware issue : audio source is mono or stereo.
    public interface OnAudioPlayBackWriteListener {
        void onWrite(int len);
    }

    public static void write(byte[] decodedAudio, int len) {
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, "ai play back write len = " + len + ", sKikaGoDeviceDataSource = " + sKikaGoDeviceDataSource);
        }
        if (mListener != null) {
            mListener.onWrite(len);
        }

        if (len == 0) {
            return;
        }
        if (sKikaGoDeviceDataSource != null) {
            sKikaGoDeviceDataSource.onData(decodedAudio, len);
        }
    }

    public static void setup(KikaGoDeviceDataSource kikaAudioDriver) {
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, "setup sKikaGoDeviceDataSource = " + sKikaGoDeviceDataSource);
        }
        sKikaGoDeviceDataSource = kikaAudioDriver;
    }

    public static void stop() {
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, "stop sKikaGoDeviceDataSource = " + sKikaGoDeviceDataSource);
        }
        sKikaGoDeviceDataSource = null;
    }

    public static void setListener(OnAudioPlayBackWriteListener listener) {
        mListener = listener;
    }
}
