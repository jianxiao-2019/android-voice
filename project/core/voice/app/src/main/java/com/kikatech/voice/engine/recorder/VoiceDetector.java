package com.kikatech.voice.engine.recorder;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.kikatech.androidspeex.Speex;
import com.kikatech.voice.util.log.Logger;
import com.kikatech.voice.VadUtil;
import com.kikatech.voice.engine.interfaces.IVoiceManager;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by ryanlin on 06/10/2017.
 */

public class VoiceDetector {

    private static final int MSG_ADD_DATA = 1;
    private static final int MSG_SEND_REMAIN_DATA = 2;

    private final VoiceDetectorListener mListener;

    private /*final*/ HandlerThread mHandlerThread;
    private DetectorHandler mDetectorHandler;
    private String mDebugFileName;

    public VoiceDetector(VoiceDetectorListener listener) {
//        mHandlerThread = new HandlerThread(this.getClass().getSimpleName());
        mListener = listener;
    }

    public void startDetecting() {
        Logger.d("VoiceDetector startDetecting");
        if (mHandlerThread != null) {
            mHandlerThread.quit();
            mHandlerThread.interrupt();
        }
        mHandlerThread = new HandlerThread(this.getClass().getSimpleName());
        mHandlerThread.start();
        mDetectorHandler = new DetectorHandler(mHandlerThread.getLooper());
    }

    public void stopDetecting() {
        Logger.d("VoiceDetector stopDetecting");
        mDetectorHandler.sendEmptyMessage(MSG_SEND_REMAIN_DATA);

        boolean isQuit = mHandlerThread.quitSafely();
        Logger.d("VoiceDetector stopDetecting isQuit = " + isQuit);
        mDetectorHandler = null;
    }

    public void addData(byte[] data) {
        if (mDetectorHandler == null) {
            Logger.e("Can't add data while VoiceDetector is stopped.");
            return;
        }

        byte[] tempData = new byte[data.length];
        System.arraycopy(data, 0, tempData, 0, data.length);

        Message message = new Message();
        message.what = MSG_ADD_DATA;
        message.obj = tempData;
        mDetectorHandler.sendMessage(message);
    }

    public void setDebugFileName(String fileName) {
        Logger.d("VoiceDetector setDebugFileName fileName = " + fileName);
        mDebugFileName = fileName;
    }

    private class DetectorHandler extends Handler {

        private final static int FRAME_LENGTH = 8000;
        private final short[] mBuf = new short[FRAME_LENGTH];
        private int mBufLen = 0;
        private float mPrevProb = -1;
        private Speex mSpeex;

        DetectorHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Logger.i("VoiceDetector handleMessage msg.what = " + msg.what);
            if (msg.what == MSG_ADD_DATA) {
                byte[] data = (byte[]) msg.obj;

                float[] sample = ByteToFloat(data, data.length / 2);
                float prob = VadUtil.speechProbability(sample, 0, sample.length,
                        VadUtil.sConf);
                if (mListener != null && mPrevProb != prob) {
                    mListener.onSpeechProbabilityChanged(prob);
                }
                mPrevProb = prob;

                handleVoiceData(data);
            } else if (msg.what == MSG_SEND_REMAIN_DATA) {
                while (mBufLen > 0) {
                    if (mListener != null) {
                        mListener.onRecorded(Speex_Encode_Func(mBuf, mBufLen));
                    }
                    mBufLen = 0;
                }
                // Thread.currentThread().interrupt();
                // Looper.myLooper().quit();
            } else {
                super.handleMessage(msg);
            }
        }

        private void handleVoiceData(byte[] data) {
            short[] vadData = ByteToShort(data, data.length / 2);
            int vadSize = vadData.length;
            if (IVoiceManager.DEBUG) {
                writeToFile(data, "");
            }
            if (vadSize + mBufLen < FRAME_LENGTH) {
                System.arraycopy(vadData, 0, mBuf, mBufLen, vadSize);
                mBufLen += vadSize;
            } else {
                int temp_len = vadSize + mBufLen - FRAME_LENGTH;
                if (temp_len > 0) {
                    System.arraycopy(vadData, 0, mBuf, mBufLen, FRAME_LENGTH - mBufLen);
                    short[] temp_data = new short[temp_len];
                    System.arraycopy(vadData, FRAME_LENGTH - mBufLen, temp_data, 0, temp_len);
                    if (mListener != null) {
                        mListener.onRecorded(Speex_Encode_Func(mBuf, FRAME_LENGTH));
                    }
                    System.arraycopy(temp_data, 0, mBuf, 0, temp_len);
                }
                mBufLen = temp_len;
            }
        }

        private synchronized byte[] Speex_Encode_Func(short[] buf, int len) {
            if (mSpeex == null) {
                mSpeex = new Speex();
                mSpeex.init();
            }
            int speex_bytes = 0, curr_bytes;
            int tot_bytes, offset = 0;
            int enc_frame_size = mSpeex.getFrameSize();
            byte[] data = new byte[FRAME_LENGTH];
            byte[] encoded = new byte[enc_frame_size];
            while (offset < len) {
                if (len - offset < enc_frame_size) {
                    curr_bytes = len - offset;
                } else {
                    curr_bytes = enc_frame_size;
                }
                short[] sample = new short[curr_bytes];
                System.arraycopy(buf, offset, sample, 0, curr_bytes);
                offset += curr_bytes;
                tot_bytes = mSpeex.encode(sample, 0, encoded, curr_bytes);
                data[speex_bytes++] = (byte) tot_bytes;
                System.arraycopy(encoded, 0, data, speex_bytes, tot_bytes);
                speex_bytes += tot_bytes;
            }

            byte[] tempData = new byte[speex_bytes];
            if (data.length < speex_bytes) {
                speex_bytes = data.length;
            }
            System.arraycopy(data, 0, tempData, 0, speex_bytes);
            if (IVoiceManager.DEBUG) {
                writeToFile(tempData, "_websocket");
            }

            return tempData;
        }

        private short[] ByteToShort(byte[] bytes, int len) {
            short[] shorts = new short[len];
            for (int i = 0; i < len; ++i) {
                shorts[i] = (short) ((bytes[i * 2 + 1] << 8) | (bytes[i * 2] & 0xff));
            }
            return shorts;
        }

        private float[] ByteToFloat(byte[] bytes, int len) {
            float[] floats = new float[len];
            for (int i = 0; i < len; ++i) {
                floats[i] = (float) ((bytes[i * 2 + 1] << 8) | (bytes[i * 2] & 0xff));
            }
            return floats;
        }

        private void writeToFile(byte[] data, String additional) {
            // Logger.d("VoiceDetector writeToFile mDebugFileName = " + mDebugFileName + " additional = " + additional);
            if (TextUtils.isEmpty(mDebugFileName)) {
                return;
            }

            // Create file
            File file = new File(mDebugFileName + additional);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }

            boolean append = true;
            OutputStream os = null;
            try {
                os = new BufferedOutputStream(new FileOutputStream(file, append));
                int len = data.length;
                os.write(data, 0, len);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeIO(os);
            }
        }

        private void closeIO(Closeable... closeables) {
            if (closeables == null) return;
            try {
                for (Closeable closeable : closeables) {
                    if (closeable != null) {
                        closeable.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
