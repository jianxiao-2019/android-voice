package com.kikatech.voice.service;

import android.content.Context;
import android.os.Handler;

import com.kikatech.voice.core.debug.DebugUtil;
import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.core.hotword.WakeUpDetector;
import com.kikatech.voice.core.recorder.VoiceRecorder;
import com.kikatech.voice.core.webservice.WebSocket;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.core.debug.ReportUtil;
import com.kikatech.voice.util.VoicePathConnector;
import com.kikatech.voice.util.log.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by tianli on 17-10-28.
 * Update by ryanlin on 25/12/2017.
 */

public class VoiceService implements WakeUpDetector.OnHotWordDetectListener {

    public static final int ERR_REASON_NOT_CREATED = 1;
    public static final int ERR_CONNECTION_ERROR = 2;

    private static final String SERVER_COMMAND_SETTINGS = "SETTINGS";

    private static final int MSG_VAD_BOS = 1;
    private static final int MSG_VAD_EOS = 2;

    private static final float VAD_PROB_CRITERIA = 0.6f;

    private VoiceConfiguration mConf;
    private WebSocket mWebService;

    private final IDataPath mDataPath;
    private final WakeUpDetector mWakeUpDetector;
    private final VoiceRecorder mVoiceRecorder;

    private VoiceRecognitionListener mVoiceRecognitionListener;
    private VoiceStateChangedListener mVoiceStateChangedListener;
    private VoiceActiveStateListener mVoiceActiveStateListener;

    private Handler mMainThreadHandler;
    private TimerHandler mTimerHandler;

    private boolean mIsAsrPaused = false;

    @Override
    public void onDetected() {
        if (mVoiceActiveStateListener != null) {
            mVoiceActiveStateListener.onWakeUp();
        }
        startVadBosTimer();
    }

    public interface VoiceRecognitionListener {
        void onRecognitionResult(Message message);
    }

    public interface VoiceStateChangedListener {
        void onCreated();

        void onStartListening();

        void onStopListening();

        void onDestroyed();

        void onError(int reason);

        void onVadBos();

        void onVadEos();

        void onConnectionClosed();

        void onSpeechProbabilityChanged(float prob);
    }

    public interface VoiceActiveStateListener {
        void onWakeUp();

        void onSleep();
    }

    private VoiceService(Context context, VoiceConfiguration conf) {
        mConf = conf;

        IDataPath finalPath = new VoiceService.VoiceDataSender(null);
        mWakeUpDetector = mConf.isSupportWakeUpMode() ? WakeUpDetector.getDetector(context, this) : null;
        mDataPath = VoicePathConnector.genDataPath(mConf, mWakeUpDetector, finalPath);
        mVoiceRecorder = new VoiceRecorder(VoicePathConnector.genVoiceSource(mConf), mDataPath);
    }

    public static VoiceService getService(Context context, VoiceConfiguration conf) {
        return new VoiceService(context, conf);
    }

    public void create() {
        if (mWebService != null) {
            mWebService.release();
        }

        mMainThreadHandler = new Handler();
        mTimerHandler = new TimerHandler();

        mWebService = WebSocket.openConnection(mWebSocketListener);
        mWebService.connect(mConf.getConnectionConfiguration());

        mVoiceRecorder.open();
        EventBus.getDefault().register(this);

        DebugUtil.updateCacheDir(mConf);
    }

    public void start() {
        Logger.d("VoiceService start mWebService.isConnecting() = " + mWebService.isConnecting());
        ReportUtil.getInstance().startTimeStamp("start record");
        if (!mWebService.isConnecting()) {
            mVoiceStateChangedListener.onError(ERR_REASON_NOT_CREATED);
            return;
        }

        DebugUtil.updateDebugInfo(mConf);

        if (mWakeUpDetector != null) {
            mWakeUpDetector.setDebugFilePath(DebugUtil.getDebugFilePath());
            mWakeUpDetector.reset();
        }

        mDataPath.start();
        mVoiceRecorder.start();

        if (mVoiceStateChangedListener != null) {
            mVoiceStateChangedListener.onStartListening();
        }

        if (mWakeUpDetector == null || mWakeUpDetector.isAwake()) {
            startVadBosTimer();
        }
    }

    private void cleanVadBosTimer() {
        if (mTimerHandler != null) {
            mTimerHandler.removeMessages(MSG_VAD_BOS);
        }
    }

    private void cleanVadEosTimer() {
        if (mTimerHandler != null) {
            mTimerHandler.removeMessages(MSG_VAD_EOS);
        }
    }

    private void startVadBosTimer() {
        startVadBosTimer(mConf.getBosDuration());
    }

    private void startVadBosTimer(int bosDuration) {
        if (mTimerHandler != null) {
            Logger.d("VoiceService startVadBosTimer");
            mTimerHandler.removeMessages(MSG_VAD_BOS);
            mTimerHandler.sendEmptyMessageDelayed(MSG_VAD_BOS, bosDuration);
        }
    }

    private void startVadEosTimer() {
        startVadEosTimer(mConf.getEosDuration());
    }

    private void startVadEosTimer(int eosDuration) {
        if (mTimerHandler != null && eosDuration > 0) {
            Logger.d("VoiceService startVadEosTimer");
            mTimerHandler.removeMessages(MSG_VAD_EOS);
            mTimerHandler.sendEmptyMessageDelayed(MSG_VAD_EOS, eosDuration);
        }
    }

    private boolean isBosTimerRunning() {
        return mTimerHandler != null && mTimerHandler.hasMessages(MSG_VAD_BOS);
    }

    private boolean isEosTimerRunning() {
        return mTimerHandler != null && mTimerHandler.hasMessages(MSG_VAD_EOS);
    }

    public synchronized void resumeAsr(int bosDuration) {
        mIsAsrPaused = false;
        startVadBosTimer(bosDuration);
    }

    public synchronized void resumeAsr(boolean startBosNow) {
        mIsAsrPaused = false;
        if (startBosNow) {
            startVadBosTimer();
        } else {
            Logger.w("VoiceService BOS is NOT starting now !!");
        }
    }

    public synchronized void pauseAsr() {
        mIsAsrPaused = true;
        cleanVadBosTimer();
        cleanVadEosTimer();
    }

    public void stop() {
        Logger.d("VoiceService stop");
        if (mWakeUpDetector != null) {
            mWakeUpDetector.close();
        }
        mVoiceRecorder.stop();
        mDataPath.stop();

        if (mVoiceStateChangedListener != null) {
            mVoiceStateChangedListener.onStopListening();
        }
        cleanVadBosTimer();
        cleanVadEosTimer();
        DebugUtil.convertCurrentPcmToWav();
        ReportUtil.getInstance().stopTimeStamp("stop record");
    }

    public void sleep() {
        if (mWakeUpDetector != null && mWakeUpDetector.isAwake()) {
            mWakeUpDetector.goSleep();
            if (mVoiceActiveStateListener != null) {
                mVoiceActiveStateListener.onSleep();
            }
        }
        cleanVadBosTimer();
        cleanVadEosTimer();
    }

    public void wakeUp() {
        if (mWakeUpDetector != null && !mWakeUpDetector.isAwake()) {
            mWakeUpDetector.wakeUp();
            if (mVoiceActiveStateListener != null) {
                mVoiceActiveStateListener.onWakeUp();
            }
        }
        startVadBosTimer();
    }

    public void destroy() {
        Logger.d("VoiceService mWebService.release()");

        stop();
        EventBus.getDefault().unregister(this);
        mVoiceRecorder.close();
        if (mWebService == null) {
            return;
        }
        mWebService.release();
        mWebService = null;

        if (mMainThreadHandler != null) {
            mMainThreadHandler.removeCallbacksAndMessages(null);
            mMainThreadHandler = null;
        }
        if (mTimerHandler != null) {
            mTimerHandler.removeCallbacksAndMessages(null);
            mTimerHandler = null;
        }

        if (mVoiceStateChangedListener != null) {
            mVoiceStateChangedListener.onDestroyed();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventMsg eventMsg) {
        if (eventMsg.type == EventMsg.Type.VD_VAD_CHANGED) {
            Logger.d("onMessageEvent VD_VAD_CHANGED prob = " + eventMsg.obj);
            float prob = (float) eventMsg.obj;
            if (prob > VAD_PROB_CRITERIA) {
                if (isBosTimerRunning()) {
                    cleanVadBosTimer();
                    startVadEosTimer();
                } else if (isEosTimerRunning()) {
                    startVadEosTimer();
                }
            }
            if (mVoiceStateChangedListener != null) {
                mVoiceStateChangedListener.onSpeechProbabilityChanged(prob);
            }
        }
    }

    public void sendCommand(String command, String alter) {
        if (mWebService != null) {
            mWebService.sendCommand(command, alter);
        }
    }

    public void sendAlignment(String[] alignment) {
        JSONArray mJSONArray = new JSONArray(Arrays.asList(alignment));
        if (mWebService != null) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("alignments", mJSONArray);
                mWebService.sendCommand("ALIGNMENT", jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void cancelAlignment() {
        sendAlignment(new String[]{""});
    }

    public void updateAsrSettings(AsrConfiguration conf) {
        if (mWebService != null) {
            Logger.d("updateAsrSettings conf = " + conf.toJsonString());
            mWebService.sendCommand(SERVER_COMMAND_SETTINGS, conf.toJsonString());
            mConf.updateAsrConfiguration(conf);
        }
    }

    private WebSocket.OnWebSocketListener mWebSocketListener = new WebSocket.OnWebSocketListener() {
        @Override
        public void onOpen() {
            Logger.d("[WebSocketListener] onOpen mMainThreadHandler = " + mMainThreadHandler);
            if (mMainThreadHandler != null) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mVoiceStateChangedListener != null) {
                            mVoiceStateChangedListener.onCreated();
                        }
                    }
                });
            }
        }

        @Override
        public void onMessage(final Message message) {
            Logger.d("[WebSocketListener] onMessage:" + message);
            if (mMainThreadHandler != null) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mVoiceRecognitionListener != null && !mIsAsrPaused) {
                            mVoiceRecognitionListener.onRecognitionResult(message);
                            ReportUtil.getInstance().logTimeStamp(message.toString());
                        }
                    }
                });
            }
            DebugUtil.logResultToFile(message);
        }

        @Override
        public void onWebSocketClosed() {
            Logger.d("[WebSocketListener] onWebSocketClosed");
            if (mMainThreadHandler != null) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mVoiceStateChangedListener != null) {
                            mVoiceStateChangedListener.onConnectionClosed();
                        }
                    }
                });
            }
        }

        @Override
        public void onWebSocketError() {
            Logger.d("[WebSocketListener] onWebSocketError");
            if (mMainThreadHandler != null) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mVoiceStateChangedListener != null) {
                            mVoiceStateChangedListener.onError(ERR_CONNECTION_ERROR);
                        }
                    }
                });
            }
        }
    };

    public void setVoiceRecognitionListener(VoiceRecognitionListener listener) {
        mVoiceRecognitionListener = listener;
    }

    public void setVoiceStateChangedListener(VoiceStateChangedListener listener) {
        mVoiceStateChangedListener = listener;
    }

    public void setVoiceActiveStateListener(VoiceActiveStateListener listener) {
        mVoiceActiveStateListener = listener;
    }

    private class VoiceDataSender extends IDataPath {

        public VoiceDataSender(IDataPath nextPath) {
            super(nextPath);
        }

        @Override
        public void onData(byte[] data) {
            Logger.i("VoiceDataSender onData");
            if (mIsAsrPaused) {
                Logger.i("VoiceDataSender onData is pausing, drop data.");
                return;
            }
            if (mWebService != null) {
                if (ReportUtil.getInstance().isEverDetectedVad() == true && ReportUtil.getInstance().isEverSentDataToWeb() == false) {
                    ReportUtil.getInstance().sentDataToWeb();
                    ReportUtil.getInstance().logTimeStamp("first_send_data_to_web");
                }
                mWebService.sendData(data);
            }
        }
    }

    private class TimerHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == MSG_VAD_BOS) {
                if (mVoiceStateChangedListener != null) {
                    mVoiceStateChangedListener.onVadBos();
                }
                return;
            } else if (msg.what == MSG_VAD_EOS) {
                if (mVoiceStateChangedListener != null) {
                    mVoiceStateChangedListener.onVadEos();
                }
                return;
            }
            super.handleMessage(msg);
        }
    }

    void setWakeUpDetectorEnable(boolean enable) {
        if (mWakeUpDetector != null) {
            mWakeUpDetector.enableDetector(enable);
        }
    }

    boolean isWakeUpDetectorEnabled() {
        return mWakeUpDetector != null && mWakeUpDetector.isEnabled();
    }
}