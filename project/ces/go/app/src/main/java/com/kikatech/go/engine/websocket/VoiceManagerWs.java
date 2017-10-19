package com.kikatech.go.engine.websocket;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.SuggestionSpan;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.kikatech.go.KikaVoiceService;
import com.kikatech.go.R;
import com.kikatech.go.access.SendInfoManager;
import com.kikatech.go.engine.interfaces.IVoiceManager;
import com.kikatech.go.engine.interfaces.IVoiceView;
import com.kikatech.go.engine.recorder.VoiceDetectorListener;
import com.kikatech.go.engine.recorder.VoiceRecorder;
import com.kikatech.go.util.request.RequestManager;
import com.kikatech.go.util.log.Logger;
import com.kikatech.go.util.TimerUtil;
import com.kikatech.go.engine.tts.TtsManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ryanlin on 06/10/2017.
 */
public class VoiceManagerWs implements IVoiceManager, VoiceDetectorListener,
        DataSender.OnDataReceiveListener, TtsManager.TtsListener {

    private static final int TTS_MARKS_RECEIVE_TIMEOUT = 2000;

    private static final String SNAPCHAT_PKG_NAME = "com.snapchat.android";

    private static final String PREF_KEY_INIT_HINT = "pref_key_init_hint";
    private static final String PREF_KEY_UNSENT_HINT = "pref_key_unsent_hint";

    private IVoiceView mVoiceView;

    private final VoiceRecorder mVoiceRecorder;
    private final DataSender mDataSender;

    private float mPreVad;
    private boolean mIsPassVad = true;
    private boolean mIsSendingMsgWhenTtsOnComplete = false;
    private final AtomicBoolean mShouldPassDataToServer;

    private int mHintTextResId;

    private static VoiceManagerWs sVoiceManagerWs;
    public static VoiceManagerWs getInstance() {
        if (sVoiceManagerWs == null) {
            sVoiceManagerWs = new VoiceManagerWs();
        }
        return sVoiceManagerWs;
    }

    private VoiceManagerWs() {
        mVoiceRecorder = new VoiceRecorder(this);
        mDataSender = new DataSender(this);
        TtsManager.getInstance().setTtsListener(this);
        mShouldPassDataToServer = new AtomicBoolean(true);
    }

    // Callback of IVoiceManager
    @Override
    public void setVoiceView(IVoiceView voiceView) {
        mVoiceView = voiceView;
    }

    @UiThread
    @Override
    public void startListening() {
        Logger.i("VoiceManagerWs startListening");

        int result = mVoiceRecorder.startRecording();
        Logger.d("VoiceManagerWs startListening result = " + result);
        if (result == VoiceRecorder.STATUS_SUCCESS && mVoiceView != null) {
            mVoiceRecorder.setDebugFilePath(getDebugFilePath(mVoiceView.getContext()));
            mVoiceView.onStartListening();
            mDataSender.connect("zh_TW",
                    RequestManager.getSign(mVoiceView.getContext()),
                    RequestManager.generateUserAgent(mVoiceView.getContext()));
        }

        UiHandler.sendEmptyMessageDelayed(MSG_INIT_VAD_TIMEOUT, 3000);
    }

    @UiThread
    @Override
    public void stopListening() {
        Logger.i("VoiceManagerWs stopListening");
        int result = mVoiceRecorder.stopRecording();
        if (result == VoiceRecorder.STATUS_SUCCESS && mVoiceView != null) {
            mVoiceView.onStopListening();
            mDataSender.disconnect();
        }
    }

    @Override
    public void sendCommand(String command, String payload) {
        mDataSender.sendCommand(command, payload);
    }

    // Callback of VoiceDetectorListener
    @Override
    public void onRecorded(byte[] data) {
        Logger.v("VoiceManager onRecorded mShouldPassDataToServer = " + mShouldPassDataToServer.get());
        if (mShouldPassDataToServer.get()) {
            mDataSender.sendData(data);
        }
    }

    @Override
    public void onSpeechProbabilityChanged(float speechProbability) {
        Logger.d("VoiceManager onSpeechProbabilityChanged prob = " + speechProbability);
        if (speechProbability > 0.6) {
            UiHandler.removeMessages(MSG_TTS_END_TIMEOUT);
            UiHandler.removeMessages(MSG_INIT_VAD_TIMEOUT);
            if (mVoiceView != null) {
                Message msg = new Message();
                msg.what = MSG_UPDATE_HINT_STR;
                msg.arg1 = -1;
                UiHandler.sendMessage(msg);
            }
        }
        if (mPreVad > 0.6 && speechProbability == 0) {
            TimerUtil.startTag();
            TimerUtil.sUserVoiceEndTime = SystemClock.elapsedRealtime();
            Logger.v("VoiceManagerWs user end of speech.");
        }
        mPreVad = speechProbability;
        if (speechProbability > 0.6 && !mIsPassVad) {
            TtsManager.getInstance().bargeIn();
            sendInterruptEvent();
            mIsPassVad = true;
        }
    }

    private void sendInterruptEvent() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                while (true) {      // Wait for the TTS-MARKS.
                    String bargeInTtsMark = TtsManager.getInstance().getBargeInTtsMark();
                    if (bargeInTtsMark == null) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Logger.i("VoiceManager sendInterruptEvent bargeInTtsMark = " + bargeInTtsMark);
                        sendCommand("INTERRUPT", bargeInTtsMark);
                        break;
                    }

                    long duration = System.currentTimeMillis() - startTime;
                    Logger.i("VoiceManager sendInterruptEvent duration = " + duration);
                    if (duration > TTS_MARKS_RECEIVE_TIMEOUT) {
                        Logger.w("Tts marks timeout : send empty interrupt command.");
                        sendCommand("INTERRUPT", "");
                        break;
                    }
                }
            }
        }).start();
    }

    // Callback of OnDataReceiveListener
    @Override
    public void onResult(KikaVoiceMessage result) {
        Message msg = new Message();
        msg.what = MSG_VOICE_RESULT;
        msg.obj = result;

        UiHandler.sendMessage(msg);
    }

    @Override
    public void onWebSocketClosed() {
        if (mVoiceView == null) {
            return;
        }

        int result = mVoiceRecorder.stopRecording();
        if (result == VoiceRecorder.STATUS_SUCCESS) {
            UiHandler.sendEmptyMessage(MSG_HANDLE_CLOSE);
        }
    }

    @Override
    public void onWebSocketError() {
        if (mVoiceView == null) {
            return;
        }

        int result = mVoiceRecorder.stopRecording();
        if (result == VoiceRecorder.STATUS_SUCCESS) {
            UiHandler.sendEmptyMessage(MSG_HANDLE_CLOSE);
        }
    }

    @UiThread
    private void handleResult(KikaVoiceMessage result) {
        if (mVoiceView == null) {
            return;
        }
        Context context = mVoiceView.getContext();

        Logger.i("VoiceManagerWs handleResult seqId = " + result.seqId + " type = " + result.resultType + " payload = " + result.payload);
        // TODO : UI Thread?
        if (!TextUtils.isEmpty(result.sessionId) && mVoiceView instanceof KikaVoiceService) {
            ((KikaVoiceService) mVoiceView).setServerSessionId(result.sessionId);
        }
        if (result.seqId > 0) {
            mVoiceView.onUpdateRecognizedResult(result.payload, IVoiceView.RESULT_INTERMEDIATE);
        } else if (result.resultType == KikaVoiceMessage.ResultType.SPEECH) {
            mVoiceView.onUpdateRecognizedResult(result.payload, IVoiceView.RESULT_FINAL);

            UiHandler.sendEmptyMessageDelayed(MSG_INIT_VAD_TIMEOUT, 3000);
        } else if (result.resultType == KikaVoiceMessage.ResultType.REPEAT) {
            mVoiceView.onUpdateRecognizedResult("", IVoiceView.RESULT_FINAL);
            CharSequence content = mVoiceView.getTextOnEditor();
            if (!TextUtils.isEmpty(content)) {
                mDataSender.sendCommand("REPEAT", content.toString());
                mIsSendingMsgWhenTtsOnComplete = false;
                mHintTextResId = R.string.repeating;
            } else {
                mVoiceView.updateHintStr(R.string.content_empty);
                playLocalTts(R.raw.content_empty);
            }
            mShouldPassDataToServer.set(false);
        } else if (result.resultType == KikaVoiceMessage.ResultType.PRESEND) {
            mVoiceView.onUpdateRecognizedResult("", IVoiceView.RESULT_FINAL);
            CharSequence content = mVoiceView.getTextOnEditor();

            if (SendInfoManager.getInstance().getSendInfo() == null
                    && !SNAPCHAT_PKG_NAME.equals(mVoiceView.getCurrentEditorPackageName())) {
                mVoiceView.updateHintStr(R.string.not_supported_app);
                playLocalTts(R.raw.not_supported_app);
            } else if (!TextUtils.isEmpty(content)) {
                mDataSender.sendCommand("SEND", content.toString());
                mIsSendingMsgWhenTtsOnComplete = true;
                mHintTextResId = R.string.sending;
            } else {
                mVoiceView.updateHintStr(R.string.content_empty);
                playLocalTts(R.raw.content_empty);
            }
            mShouldPassDataToServer.set(false);
        } else if (result.resultType == KikaVoiceMessage.ResultType.SEND) {
            mVoiceView.onUpdateRecognizedResult("", IVoiceView.RESULT_FINAL);
            sendMessage();
        } else if (result.resultType == KikaVoiceMessage.ResultType.ALTER) {
            SpannableStringBuilder ssb = new SpannableStringBuilder(result.payload);
            Logger.d("onResult Alter start = " + result.alterStart + " end = " + result.alterEnd + " ssb = " + ssb);
            if (result.alterStart >= 0 && result.alterEnd > 0 && context != null) {
                SuggestionSpan span = new SuggestionSpan(context, new String[]{""}, SuggestionSpan.FLAG_AUTO_CORRECTION);
                ssb.setSpan(span, result.alterStart, result.alterEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            mVoiceView.onUpdateRecognizedResult(ssb, IVoiceView.RESULT_REPLACE_ALL);
            mVoiceView.updateHintStr(R.string.content_updated);
            mHintTextResId = -1;
            mShouldPassDataToServer.set(false);
        } else if (result.resultType == KikaVoiceMessage.ResultType.NOTALTERED) {
            mVoiceView.onUpdateRecognizedResult("", IVoiceView.RESULT_FINAL);

            // TODO : change content.
            mVoiceView.updateHintStr(R.string.not_altered);
            playLocalTts(R.raw.not_altered);
            mShouldPassDataToServer.set(false);
        } else if (result.resultType == KikaVoiceMessage.ResultType.CANCEL) {
            mVoiceView.onUpdateRecognizedResult("", IVoiceView.RESULT_FINAL);

            CharSequence content = mVoiceView.getTextOnEditor();
            if (!TextUtils.isEmpty(content)) {
                mVoiceView.onUpdateRecognizedResult("", IVoiceView.RESULT_REPLACE_ALL);
                playLocalTts(R.raw.has_been_erased);
                mVoiceView.updateHintStr(R.string.has_been_erased);
            } else {
                mVoiceView.updateHintStr(R.string.content_empty);
                playLocalTts(R.raw.content_empty);
            }
            mShouldPassDataToServer.set(false);
        } else if (result.resultType == KikaVoiceMessage.ResultType.TTSURL) {
            TtsManager.getInstance().startTts(result.url, true, this);
            if (mHintTextResId != -1) {
                mVoiceView.updateHintStr(mHintTextResId);
            }
            mShouldPassDataToServer.set(false);
        } else if (result.resultType == KikaVoiceMessage.ResultType.TTSMARKS) {
            TtsManager.getInstance().setTtsMasks(result.payload);
        }
    }

    @UiThread
    private void sendMessage() {
        AccessibilityNodeInfo info = SendInfoManager.getInstance().getSendInfo();
        Logger.d("SendMessage SendInfo = " + info);
        if (mVoiceView == null) {
            Logger.w("sendMessage, but not active.");
            return;
        }
        if (SNAPCHAT_PKG_NAME.equals(mVoiceView.getCurrentEditorPackageName())) {
            mVoiceView.sendKeyEvent(KeyEvent.KEYCODE_ENTER);
            mVoiceView.updateHintStr(R.string.has_been_send);
            playLocalTts(R.raw.has_been_send);
        } else if (info != null) {
            info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            mVoiceView.updateHintStr(R.string.has_been_send);
            playLocalTts(R.raw.has_been_send);
        } else {
            mVoiceView.updateHintStr(R.string.not_supported_app);
            playLocalTts(R.raw.not_supported_app);
        }
    }

    private static final int MSG_VOICE_RESULT = 0;
    private static final int MSG_TTS_END_TIMEOUT = 1;
    private static final int MSG_INIT_VAD_TIMEOUT = 2;
    private static final int MSG_UPDATE_HINT_STR = 3;
    private static final int MSG_HANDLE_CLOSE = 4;
    private Handler UiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_VOICE_RESULT:
                    handleResult((KikaVoiceMessage) msg.obj);
                    break;
                case MSG_TTS_END_TIMEOUT:
                    if (!mIsPassVad) {
                        mIsPassVad = true;

                        VoiceManagerWs.this.sendMessage();
                    }
                    break;
                case MSG_INIT_VAD_TIMEOUT:
                    Context context = mVoiceView.getContext();
                    if (context == null || mVoiceView == null) {
                        Logger.w("MSG_INIT_VAD_TIMEOUT but not active");
                        return;
                    }
                    CharSequence content = mVoiceView.getTextOnEditor();

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    if (TextUtils.isEmpty(content)) {
                        mVoiceView.updateHintStr(R.string.init_hint);
                        if (!preferences.getBoolean(PREF_KEY_INIT_HINT, false)) {
                            playLocalTts(R.raw.init_hint);
                            preferences.edit().putBoolean(PREF_KEY_INIT_HINT, true).apply();
                        }
                    } else {
                        mVoiceView.updateHintStr(R.string.unsent_hint);
                        if (!preferences.getBoolean(PREF_KEY_UNSENT_HINT, false)) {
                            playLocalTts(R.raw.unsent_hint);
                            preferences.edit().putBoolean(PREF_KEY_UNSENT_HINT, true).apply();
                        }
                    }
                    break;
                case MSG_UPDATE_HINT_STR:
                    handleUpdateHintStr(msg.arg1);
                    break;
                case MSG_HANDLE_CLOSE:
                    if (mVoiceView != null) {
                        mVoiceView.onStopListening();
                    }
                    break;
            }
        }
    };

    @UiThread
    private void handleUpdateHintStr(int strResId) {
        mVoiceView.updateHintStr(strResId);
    }

    @UiThread
    private void playLocalTts(int resId) {
        if (mVoiceView == null) {
            return;
        }
        Context context = mVoiceView.getContext();
        if (context != null) {
            MediaPlayer mp = MediaPlayer.create(context, resId);
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mShouldPassDataToServer.set(true);
                }
            });
            mp.start();
        }
    }

    // Callback of TtsListener
    @Override
    public void OnTtsStart() {
        mIsPassVad = false;
        mShouldPassDataToServer.set(true);
    }

    @Override
    public void OnTtsStartError() {
        mIsPassVad = false;
        mShouldPassDataToServer.set(true);
    }

    @Override
    public void OnTtsFinish() {
        if (mIsSendingMsgWhenTtsOnComplete) {
            UiHandler.sendEmptyMessageDelayed(MSG_TTS_END_TIMEOUT, 3000);
        }
        mShouldPassDataToServer.set(true);
    }

    @UiThread
    private String getDebugFilePath(Context context) {
        if (context == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", new Locale("en"));
        Date resultDate = new Date(System.currentTimeMillis());
        String timeStr = sdf.format(resultDate);

        if (mVoiceView instanceof KikaVoiceService) {
            ((KikaVoiceService) mVoiceView).setClientRecordId(timeStr);
        }

        return getCacheDir(context).toString() + "/kika_voice_" + timeStr;
    }

    @UiThread
    private File getCacheDir(@NonNull Context context) {
        try {
            File[] files = ContextCompat.getExternalCacheDirs(context);
            if (files != null && files.length > 0) {
                File file = files[0];
                if (file != null) {
                    createFolderIfNecessary(file);
                    return file;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return context.getCacheDir();
    }

    @UiThread
    private boolean createFolderIfNecessary(File folder) {
        if (folder != null) {
            if (!folder.exists() || !folder.isDirectory()) {
                return folder.mkdirs();
            }
            return true;
        }
        return false;
    }
}
