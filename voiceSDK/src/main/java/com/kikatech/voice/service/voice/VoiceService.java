package com.kikatech.voice.service.voice;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import com.kikatech.voice.core.debug.DebugUtil;
import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.core.hotword.WakeUpDetector;
import com.kikatech.voice.core.recorder.VoiceRecorder;
import com.kikatech.voice.core.webservice.WebSocket;
import com.kikatech.voice.core.webservice.message.EditTextMessage;
import com.kikatech.voice.core.webservice.message.EmojiRecommendMessage;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.NBestMessage;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.core.debug.ReportUtil;
import com.kikatech.voice.service.conf.VoiceConfiguration;
import com.kikatech.voice.service.event.EventMsg;
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
    // TODO: refactor latter
    private SharedPreferences sPref;
    private SharedPreferences.Editor sEditor;

    public static final int ERR_REASON_NOT_CREATED = 1;
    public static final int ERR_CONNECTION_ERROR = 2;
    public static final int ERR_NO_SPEECH = 3;

    public static final String SERVER_COMMAND_NBEST = "NBEST";
    private static final String SERVER_COMMAND_SETTINGS = "SETTINGS";
    private static final String SERVER_COMMAND_TOKEN = "TOKEN";
    private static final String SERVER_COMMAND_STOP = "STOP";           // stop and drop current results
    private static final String SERVER_COMMAND_RESET = "RESET";          // stop, drop current results and start new conversation
    private static final String SERVER_COMMAND_COMPLETE = "COMPLETE";       // stop and wait final results
    private static final String SERVER_COMMAND_ALIGNMENT = "ALIGNMENT";

    private static final int MSG_VAD_BOS = 1;
    private static final int MSG_VAD_EOS = 2;

    private VoiceConfiguration mConf;
    private WebSocket mWebService;

    private final IDataPath mDataPath;
    private final WakeUpDetector mWakeUpDetector;
    private final VoiceRecorder mVoiceRecorder;

    private VoiceRecognitionListener mVoiceRecognitionListener;
    private VoiceWakeUpListener mVoiceWakeUpListener;
    private VoiceDataListener mVoiceDataListener;

    private Handler mMainThreadHandler;
    private TimerHandler mTimerHandler;

    private IntermediateMessage mLastIntermediateMessage;
    private long mSkippedCid = -1;

    private VoiceConfiguration.SpeechMode mCurrentSpeechMode = VoiceConfiguration.SpeechMode.CONVERSATION;

    public enum StopType {
        NORMAL,
        COMPLETE,
        CANCEL,
    }

    @Override
    public void onDetected() {
        if (mMainThreadHandler != null) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mVoiceWakeUpListener != null) {
                        mVoiceWakeUpListener.onWakeUp();
                    }
                }
            });
        }
        startVadBosTimer();
    }

    public interface VoiceRecognitionListener {

        void onRecognitionResult(Message message);

        void onError(int reason);
    }

    public interface VoiceWakeUpListener {

        void onWakeUp();

        void onSleep();
    }

    public interface VoiceDataListener {

        void onData(byte[] data, int readSize);

        void onSpeechProbabilityChanged(float prob);
    }

    private VoiceService(Context context, VoiceConfiguration conf) {
        sPref = context.getSharedPreferences("voiceSDK", Context.MODE_PRIVATE);
        sEditor = sPref.edit();

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

        registerMessage();
    }

    private void registerMessage() {
        Message.register(Message.MSG_TYPE_INTERMEDIATE, IntermediateMessage.class);
        Message.register(Message.MSG_TYPE_ASR, TextMessage.class);

        AsrConfiguration asrConfiguration = mConf.getConnectionConfiguration().getAsrConfiguration();
        if (asrConfiguration.getAlterEnabled()) {
            Message.register(Message.MSG_TYPE_ALTER, EditTextMessage.class);
        }
        if (asrConfiguration.getEmojiEnabled()) {
            Message.register(Message.MSG_TYPE_EMOJI, EmojiRecommendMessage.class);
        }
        if (mConf.getIsSupportNBest()) {
            Message.register(Message.MSG_TYPE_NBEST, NBestMessage.class);
        }
    }

    private void unregisterMessage() {
        Message.unregisterAll();
    }

    public void start() {
        start(mConf.getBosDuration());
    }

    public void start(int bosDuration) {
        Logger.i("1qaz start");
        ReportUtil.getInstance().startTimeStamp("start record");
        DebugUtil.updateDebugInfo(mConf);

        mCurrentSpeechMode = mConf.getSpeechMode();
        if (mCurrentSpeechMode == VoiceConfiguration.SpeechMode.ONE_SHOT) {
            sendCommand(SERVER_COMMAND_TOKEN, "1");
        } else {
            sendCommand(SERVER_COMMAND_TOKEN, "-1");
        }

        if (mWakeUpDetector != null) {
            mWakeUpDetector.setDebugFilePath(DebugUtil.getDebugFilePath());
            mWakeUpDetector.reset();
        }

        mDataPath.start();
        mVoiceRecorder.start();

        if (mWakeUpDetector == null || mWakeUpDetector.isAwake()) {
            startVadBosTimer(bosDuration);
        }
    }

    public void stop(StopType stopType) {
        Logger.i("1qaz stopType = " + stopType);
        if (stopType == StopType.COMPLETE) {
            sendCommand(SERVER_COMMAND_COMPLETE, "");
            cleanVadEosTimer();
        }
        if (stopType == StopType.CANCEL) {
            sendCommand(SERVER_COMMAND_STOP, "");
            cleanVadBosTimer();
            cleanVadEosTimer();
            if (mLastIntermediateMessage != null) {
                mSkippedCid = mLastIntermediateMessage.cid;
            }
        }

        mVoiceRecorder.stop();
        mDataPath.stop();

        DebugUtil.convertCurrentPcmToWav();
        ReportUtil.getInstance().stopTimeStamp("stop record");
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
        Logger.d("1qaz VoiceService startVadBosTimer bosDuration = " + bosDuration);
        if (mTimerHandler != null && bosDuration > 0) {
            mTimerHandler.removeMessages(MSG_VAD_BOS);
            mTimerHandler.sendEmptyMessageDelayed(MSG_VAD_BOS, bosDuration);
        }
    }

    private void startVadEosTimer() {
        int eosDuration = mConf.getEosDuration();
        Logger.d("1qaz VoiceService startVadEosTimer eosDuration = " + eosDuration);
        if (mTimerHandler != null && eosDuration > 0) {
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

    public void sleep() {
        if (mWakeUpDetector != null && mWakeUpDetector.isAwake()) {
            mWakeUpDetector.goSleep();
            if (mVoiceWakeUpListener != null) {
                mVoiceWakeUpListener.onSleep();
            }
        }
        cleanVadBosTimer();
        cleanVadEosTimer();
    }

    public void wakeUp() {
        if (mWakeUpDetector != null && !mWakeUpDetector.isAwake()) {
            mWakeUpDetector.wakeUp();
            if (mVoiceWakeUpListener != null) {
                mVoiceWakeUpListener.onWakeUp();
            }
        }
        startVadBosTimer();
    }

    public void destroy() {
        Logger.d("VoiceService mWebService.release()");

        stop(StopType.CANCEL);
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

        if (mWakeUpDetector != null) {
            mWakeUpDetector.close();
        }

        unregisterMessage();

        checkFiles();
    }

    private synchronized void checkFiles() {
        final String KEY_CHECK_FILE_TIME = "KEY_CHECK_FILE_TIME";
        long lastCheckedTime = sPref.getLong(KEY_CHECK_FILE_TIME, 0);
        long millisecond = (System.currentTimeMillis() - lastCheckedTime);
        boolean shouldCheckFile = lastCheckedTime == 0 || millisecond >= 24 * 60 * 60 * 1000;
        if (Logger.DEBUG) {
            Logger.d(String.format("Last check time is %s ms ago.", millisecond));
            Logger.d(String.format("Should Check And Delete Files? %s", shouldCheckFile));
        }
        if (shouldCheckFile) {
            DebugUtil.checkFiles(mConf);
            sEditor.putLong(KEY_CHECK_FILE_TIME, System.currentTimeMillis());
            sEditor.apply();
            sEditor.commit();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventMsg eventMsg) {
        if (eventMsg.type == EventMsg.Type.VD_VAD_CHANGED) {
            Logger.d("onMessageEvent VD_VAD_CHANGED prob = " + eventMsg.obj);
            float prob = (float) eventMsg.obj;
            if (mVoiceDataListener != null) {
                mVoiceDataListener.onSpeechProbabilityChanged(prob);
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
                mWebService.sendCommand(SERVER_COMMAND_ALIGNMENT, jsonObject.toString());
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
        public void onMessage(final Message message) {
            Logger.d("[WebSocketListener] 1qaz onMessage:" + message);
            if (mMainThreadHandler != null) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mSkippedCid != getMessageCid(message)) {
                            if (message instanceof TextMessage) {
                                mLastIntermediateMessage = null;
                                cleanVadBosTimer();
                                cleanVadEosTimer();
                                if (mCurrentSpeechMode == VoiceConfiguration.SpeechMode.ONE_SHOT) {
                                    stop(StopType.CANCEL);
                                }
                            } else {
                                if (isBosTimerRunning()) {
                                    cleanVadBosTimer();
                                    if (mCurrentSpeechMode == VoiceConfiguration.SpeechMode.ONE_SHOT) {
                                        startVadEosTimer();
                                    }
                                } else if (isEosTimerRunning()) {
                                    startVadEosTimer();
                                }
                            }
                            if (message instanceof IntermediateMessage) {
                                mLastIntermediateMessage = (IntermediateMessage) message;
                            }

                            Logger.d("1qaz mSkippedCid = " + mSkippedCid + " message.cid = " + getMessageCid(message));
                            if (mVoiceRecognitionListener != null) {
                                mVoiceRecognitionListener.onRecognitionResult(message);
                                ReportUtil.getInstance().logTimeStamp(message.toString());
                            }
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
                        handleError(ERR_CONNECTION_ERROR);
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
                        handleError(ERR_CONNECTION_ERROR);
                    }
                });
            }
        }
    };

    private long getMessageCid(Message message) {
        long cid = Long.MIN_VALUE;
        if (message instanceof IntermediateMessage) {
            IntermediateMessage intermediateMessage = (IntermediateMessage) message;
            cid = intermediateMessage.cid;
        } else if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            cid = textMessage.cid;
        } else if (message instanceof EditTextMessage) {
            EditTextMessage editTextMessage = (EditTextMessage) message;
            cid = editTextMessage.cid;
        } else if (message instanceof EmojiRecommendMessage) {
            EmojiRecommendMessage emoji = ((EmojiRecommendMessage) message);
            cid = emoji.cid;
        }
        return cid;
    }

    public void setVoiceRecognitionListener(VoiceRecognitionListener listener) {
        mVoiceRecognitionListener = listener;
    }

    public void setVoiceWakeUpListener(VoiceWakeUpListener listener) {
        mVoiceWakeUpListener = listener;
    }

    public void setVoiceDataListener(VoiceDataListener listener) {
        mVoiceDataListener = listener;
        mVoiceRecorder.setVoiceDataListener(listener);
    }

    private class VoiceDataSender extends IDataPath {

        public VoiceDataSender(IDataPath nextPath) {
            super(nextPath);
        }

        @Override
        public void onData(byte[] data) {
            Logger.i("VoiceDataSender onData");
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
                Logger.i("1qaz onBos");
                handleError(ERR_NO_SPEECH);
                return;
            } else if (msg.what == MSG_VAD_EOS) {
                Logger.i("1qaz onEos");
                stop(StopType.CANCEL);
                mSkippedCid = mLastIntermediateMessage.cid;
                TextMessage finalResult = new TextMessage(mLastIntermediateMessage);
                mLastIntermediateMessage = null;
                if (mVoiceRecognitionListener != null) {
                    mVoiceRecognitionListener.onRecognitionResult(finalResult);
                    ReportUtil.getInstance().logTimeStamp(finalResult.toString());
                }
                return;
            }
            super.handleMessage(msg);
        }
    }

    public void setWakeUpDetectorEnable(boolean enable) {
        if (mWakeUpDetector != null) {
            mWakeUpDetector.enableDetector(enable);
        }
    }

    public boolean isWakeUpDetectorEnabled() {
        return mWakeUpDetector != null && mWakeUpDetector.isEnabled();
    }

    private void handleError(int errorCode) {
        stop(StopType.CANCEL);
        if (mVoiceRecognitionListener != null) {
            mVoiceRecognitionListener.onError(errorCode);
        }
    }
}
