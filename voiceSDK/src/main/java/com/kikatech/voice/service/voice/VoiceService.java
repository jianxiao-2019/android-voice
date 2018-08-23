package com.kikatech.voice.service.voice;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.kikatech.voice.core.debug.DebugUtil;
import com.kikatech.voice.core.debug.ReportUtil;
import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.core.hotword.WakeUpDetector;
import com.kikatech.voice.core.recorder.VoiceRecorder;
import com.kikatech.voice.core.webservice.IWebSocket;
import com.kikatech.voice.core.webservice.command.SocketCommand;
import com.kikatech.voice.core.webservice.impl.WebSocket;
import com.kikatech.voice.core.webservice.message.AlterMessage;
import com.kikatech.voice.core.webservice.message.BosMessage;
import com.kikatech.voice.core.webservice.message.EmojiRecommendMessage;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voice.service.conf.AsrConfiguration;
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

public class VoiceService implements WakeUpDetector.OnHotWordDetectListener,
        VoiceRecorder.IRecorderListener {

    public static final int ERR_REASON_NOT_CREATED = 1;
    public static final int ERR_CONNECTION_ERROR = 2;
    public static final int ERR_NO_SPEECH = 3;
    public static final int ERR_RECORD_OPEN_FAIL = 4;
    public static final int ERR_RECORD_DATA_FAIL = 5;

    private static final int MSG_VAD_BOS = 1;
    private static final int MSG_VAD_EOS = 2;

    private VoiceConfiguration mConf;
    private IWebSocket mWebService;

    private WakeUpDetector mWakeUpDetector;
    private VoiceRecorder mVoiceRecorder;

    private VoiceRecognitionListener mVoiceRecognitionListener;
    private VoiceWakeUpListener mVoiceWakeUpListener;
    private VoiceDataListener mVoiceDataListener;

    private Handler mMainThreadHandler;
    private TimerHandler mTimerHandler;

    private IntermediateMessage mLastIntermediateMessage;
    private BosMessage mLastBosMessage;
    private long mSkippedCid = -1;

    private boolean mIsStarting = false;

    private VoiceConfiguration.SpeechMode mCurrentSpeechMode = VoiceConfiguration.SpeechMode.CONVERSATION;

    @Override
    public void onRecorderData(byte[] data, int length) {
        if (mVoiceDataListener != null) {
            mVoiceDataListener.onData(data, length);
        }
    }

    @Override
    public void onRecorderError(final int errorCode) {
        if (mMainThreadHandler != null) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (errorCode == VoiceRecorder.ERR_OPEN_FAIL) {
                        handleError(ERR_RECORD_OPEN_FAIL);
                    } else if (errorCode == VoiceRecorder.ERR_RECORD_FAIL) {
                        handleError(ERR_RECORD_DATA_FAIL);
                    } else {
                        Logger.e("onRecorderError : Not supported error code : " + errorCode);
                    }
                }
            });
        } else {
            Logger.d("Don't invoke this method after destroyed.");
        }
    }

    public enum StopType {
        NORMAL,
        ERROR,
        COMPLETE,
        CANCEL,
    }

    @Override
    public void onDetected() {
        Logger.d("onDetected");
        startVadBosTimer();
        if (mMainThreadHandler != null) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mVoiceWakeUpListener != null) {
                        mVoiceWakeUpListener.onWakeUp();
                    }
                }
            });
        } else {
            Logger.d("Don't invoke this method after destroyed.");
        }
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
        mConf = conf;
    }

    public static VoiceService getService(Context context, VoiceConfiguration conf) {
        return new VoiceService(context, conf);
    }

    public void create() {
        Logger.d(Logger.TAG, "create", 1);
        if (mWebService != null) {
            mWebService.release();
        }

        Looper mainLooper = Looper.getMainLooper();
        mMainThreadHandler = new Handler(mainLooper);
        mTimerHandler = new TimerHandler(mainLooper);

        IWebSocket configWebSocket = mConf.getWebSocket();
        if (configWebSocket != null) {
            mWebService = configWebSocket;
        } else {
            mWebService = new WebSocket();
            mConf.setWebSocket(mWebService);
        }
        mWebService.setListener(mWebSocketListener);
        mWebService.connect(mConf);

        IDataPath finalPath = new VoiceService.VoiceDataSender(null);
        mWakeUpDetector = mConf.getWakeUpDetector();
        if (mWakeUpDetector != null) {
            mWakeUpDetector.setOnHotWordDetectListener(this);
        }
        IDataPath dataPath = VoicePathConnector.genDataPath(mConf, finalPath);
        mVoiceRecorder = new VoiceRecorder(VoicePathConnector.genVoiceSource(mConf), dataPath, this);

        if (mConf.getSpeechMode() == VoiceConfiguration.SpeechMode.AUDIO_UPLOAD) {
            mConf.getConnectionConfiguration().url = "ws://api-dev.kika.ai/v3/ns";
            mConf.getConnectionConfiguration().bundle.putString("sid", "0");
            mConf.getConnectionConfiguration().bundle.putString("type", "wakeup");
            mConf.getConnectionConfiguration().bundle.putString("format", "pcm");
        }

        mVoiceRecorder.open();
        EventBus.getDefault().register(this);

        DebugUtil.updateCacheDir(mConf);
    }

    public void setAsrAudioFilePath(String path, String fileName) {
        String preFix = DebugUtil.getFilePrefix(mConf);
        DebugUtil.setAsrAudioPath(String.format("%s/%s%s", path, preFix, fileName));
    }

    public void start() {
        start(mConf.getBosDuration());
    }

    public void start(int bosDuration) {
        Logger.d(Logger.TAG, "start", 1);
        if (mMainThreadHandler == null) {
            handleError(ERR_REASON_NOT_CREATED);
            Logger.e("Check the voice service was been created before started it.");
            return;
        }

        mIsStarting = true;

        ReportUtil.getInstance().startTimeStamp("start record");

        mCurrentSpeechMode = mConf.getSpeechMode();
        if (mCurrentSpeechMode == VoiceConfiguration.SpeechMode.ONE_SHOT) {
            sendCommand(SocketCommand.TOKEN, "1");
            sendCommand(SocketCommand.RESET, "");
        } else if (mCurrentSpeechMode == VoiceConfiguration.SpeechMode.CONVERSATION) {
            sendCommand(SocketCommand.TOKEN, "-1");
            sendCommand(SocketCommand.RESET, "");
        }

        if (mWakeUpDetector != null) {
            mWakeUpDetector.reset();
        }

        mVoiceRecorder.start();

        if ((mWakeUpDetector == null || mWakeUpDetector.isAwake())
                && mCurrentSpeechMode != VoiceConfiguration.SpeechMode.AUDIO_UPLOAD) {
            startVadBosTimer(bosDuration);
        }

        mLastIntermediateMessage = null;
    }

    public void stop(StopType stopType) {
        Logger.d(Logger.TAG, "stop, type = " + stopType, 1);
        mIsStarting = false;

        if (stopType == StopType.COMPLETE) {
            sendCommand(SocketCommand.COMPLETE, "");
            cleanVadEosTimer();
        } else if (stopType == StopType.CANCEL) {
            sendCommand(SocketCommand.STOP, "");
            cleanVadBosTimer();
            cleanVadEosTimer();
            if (mLastIntermediateMessage != null) {
                mSkippedCid = mLastIntermediateMessage.cid;
            } else if (mLastBosMessage != null) {
                mSkippedCid = mLastBosMessage.cid;
            }
        } else if (stopType == StopType.ERROR) {
            cleanVadBosTimer();
            cleanVadEosTimer();
        }

        mVoiceRecorder.stop();

        DebugUtil.convertCurrentPcmToWav();
        ReportUtil.getInstance().stopTimeStamp("stop record");
    }

    private void cleanVadBosTimer() {
        Logger.v("cleanVadBosTimer");
        if (mTimerHandler != null) {
            mTimerHandler.removeMessages(MSG_VAD_BOS);
        }
    }

    private void cleanVadEosTimer() {
        Logger.v("cleanVadEosTimer");
        if (mTimerHandler != null) {
            mTimerHandler.removeMessages(MSG_VAD_EOS);
        }
    }

    private void startVadBosTimer() {
        startVadBosTimer(mConf.getBosDuration());
    }

    private void startVadBosTimer(int bosDuration) {
        Logger.v("startVadBosTimer bosDuration = " + bosDuration);
        if (mTimerHandler != null && bosDuration > 0) {
            mTimerHandler.removeMessages(MSG_VAD_BOS);
            mTimerHandler.sendEmptyMessageDelayed(MSG_VAD_BOS, bosDuration);
        }
    }

    private void startVadEosTimer() {
        int eosDuration = mConf.getEosDuration();
        Logger.v("startVadEosTimer eosDuration = " + eosDuration);
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
        Logger.d("sleep");
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
        Logger.d("wake up");
        if (mWakeUpDetector != null && !mWakeUpDetector.isAwake()) {
            mWakeUpDetector.wakeUp();
            startVadBosTimer();
            if (mVoiceWakeUpListener != null) {
                mVoiceWakeUpListener.onWakeUp();
            }
        }
    }

    public void destroy() {
        Logger.d(Logger.TAG, "destroy", 1);
        stop(StopType.CANCEL);
        EventBus.getDefault().unregister(this);
        mVoiceRecorder.close();
        if (mWebService != null) {
            mWebService.release();
        }

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
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventMsg eventMsg) {
        if (eventMsg.type == EventMsg.Type.VD_VAD_CHANGED) {
            float prob = (float) eventMsg.obj;
            if (mVoiceDataListener != null) {
                mVoiceDataListener.onSpeechProbabilityChanged(prob);
            }
        }
    }

    public void sendCommand(String command, String payload) {
        if (mWebService != null) {
            mWebService.sendCommand(command, payload);
        } else {
            Logger.w("Don't send command after destroyed");
        }
    }

    public void sendAlignment(String[] alignment) {
        JSONArray mJSONArray = new JSONArray(Arrays.asList(alignment));
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("alignments", mJSONArray);
            sendCommand(SocketCommand.ALIGNMENT, jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void cancelAlignment() {
        sendAlignment(new String[]{""});
    }

    public void updateAsrSettings(AsrConfiguration conf) {
        sendCommand(SocketCommand.SETTINGS, conf.toJsonString());
        mConf.updateAsrConfiguration(conf);
    }

    private IWebSocket.OnWebSocketListener mWebSocketListener = new IWebSocket.OnWebSocketListener() {
        @Override
        public void onMessage(final Message message) {
            if (mMainThreadHandler != null) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mCurrentSpeechMode == VoiceConfiguration.SpeechMode.AUDIO_UPLOAD) {
                            return;
                        }

                        if (message instanceof BosMessage) {
                            Logger.d("[WebSocketListener] onMessage:" + message);
                            mLastBosMessage = (BosMessage) message;
                            return;
                        }

                        long messageCid = getMessageCid(message);
                        Logger.d("[WebSocketListener] onMessage:" + message + (mSkippedCid == messageCid ? "  <Skipped>" : ""));
                        if (mSkippedCid != messageCid) {
                            if (message instanceof TextMessage) {
                                mLastIntermediateMessage = null;
                                cleanVadBosTimer();
                                cleanVadEosTimer();
                                switch (mCurrentSpeechMode) {
                                    case ONE_SHOT:
                                        stop(StopType.CANCEL);
                                        break;
                                    case CONVERSATION:
                                        mWebService.onStop();
                                        mWebService.onStart();
                                        break;
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

                            if (mVoiceRecognitionListener != null) {
                                mVoiceRecognitionListener.onRecognitionResult(message);
                                ReportUtil.getInstance().logTimeStamp(message.toString());
                            }
                        }
                    }
                });
            } else {
                Logger.d("Don't invoke this method after destroyed.");
            }
            DebugUtil.logResultToFile(message);
        }

        @Override
        public void onError(int errorCode) {
            Logger.d(String.format("[WebSocketListener] onError: %s", errorCode));
            if (mMainThreadHandler == null) {
                Logger.w("Don't invoke this method after destroyed.");
                return;
            }
            switch (errorCode) {
                case IWebSocket.WebSocketError.WEB_SOCKET_CLOSED:
                    mMainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mIsStarting) {
                                handleError(ERR_CONNECTION_ERROR);
                            }
                        }
                    });
                    break;
                case IWebSocket.WebSocketError.DATA_ERROR:
                    mMainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mIsStarting) {
                                handleError(ERR_CONNECTION_ERROR);
                            }
                        }
                    });
                    break;
                case IWebSocket.WebSocketError.EMPTY_RESULT:
                    handleError(ERR_NO_SPEECH);
                    break;
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
        } else if (message instanceof AlterMessage) {
            AlterMessage alterMessage = (AlterMessage) message;
            cid = alterMessage.cid;
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
    }

    private class VoiceDataSender extends IDataPath {

        VoiceDataSender(IDataPath nextPath) {
            super(nextPath);
        }

        @Override
        public void start() {
            super.start();
            if (mWebService != null) {
                mWebService.onStart();
            }
        }

        @Override
        public void stop() {
            super.stop();
            if (mWebService != null) {
                mWebService.onStop();
            }
        }

        @Override
        public void onData(byte[] data, int length) {
            Logger.v("[VoiceDataSender] onData");
            if (mWebService == null) {
                Logger.w("invalid WebService");
                return;
            }
            if (ReportUtil.getInstance().isEverDetectedVad()
                    && !ReportUtil.getInstance().isEverSentDataToWeb()) {
                ReportUtil.getInstance().sentDataToWeb();
                ReportUtil.getInstance().logTimeStamp("first_send_data_to_web");
            }
            if (data.length != length) {
                data = Arrays.copyOf(data, length);
            }
            mWebService.sendData(data);
        }
    }

    private class TimerHandler extends Handler {
        private TimerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == MSG_VAD_BOS) {
                Logger.d("onBos");
                if (mWebService != null && !mWebService.isConnected()) {
                    handleError(ERR_CONNECTION_ERROR);
                } else {
                    handleError(ERR_NO_SPEECH);
                }
                mLastIntermediateMessage = null;
                return;
            } else if (msg.what == MSG_VAD_EOS) {
                Logger.d("onEos");
                stop(StopType.CANCEL);
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
        stop(errorCode == ERR_NO_SPEECH ? StopType.CANCEL : StopType.ERROR);
        if (mVoiceRecognitionListener != null) {
            mVoiceRecognitionListener.onError(errorCode);
        }
    }
}
