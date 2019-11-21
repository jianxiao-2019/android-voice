package com.kikago.speech.baidu;

import android.content.Context;
import android.util.Log;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.kikatech.voice.core.webservice.impl.BaseWebSocket;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voice.service.conf.VoiceConfiguration;

import java.util.concurrent.atomic.AtomicBoolean;

public class BaiduApi extends BaseWebSocket {

    private static final String TAG = "BaiduApi";

    private Context mContext;
    /**
     * SDK 内部核心 EventManager 类
     */
    private EventManager asr;

    private ForBaiduInputStream mBaiduInputStream;

    private final AtomicBoolean mShouldCallStart = new AtomicBoolean(false);
    private long mCid, mEndCid;

    public BaiduApi(Context context) {
        mContext = context;
    }

    @Override
    public void connect(VoiceConfiguration voiceConfiguration) {
        Log.d(TAG, "BaiduApi connect begin");
        mBaiduInputStream = BaiduInputStreamFactory.getBaiduInputStream();
        asr = EventManagerFactory.create(mContext, "asr");
        asr.registerListener(mEventListener);



        mBaiduInputStream.start();

        if (asr == null) {
            Log.e(TAG, "onStart but asr == null");
        }


        if (mBaiduInputStream.hasData()) {
            Log.e(TAG, "--------------BaiduInputStream.hasData()有数据");
            startInternal();

            mShouldCallStart.set(false);
        } else {
            Log.e(TAG, "--------------BaiduInputStream.hasData()没有数据");
            mShouldCallStart.set(true);
            Log.e(TAG, "--------------"+mShouldCallStart.get());
        }


    }

    @Override
    public void release() {
        if (asr == null) {
            Log.e(TAG, "onStop but asr == null");
            return;
        }

        Log.d(TAG, "BaiduApi onStop-> release()");
        asr.send(SpeechConstant.ASR_STOP, "{}", null, 0, 0);
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        asr.unregisterListener(mEventListener);
        asr = null;

    }


    @Override
    public void onStart() {

    }

    private void startInternal() {
        Log.d(TAG, "BaiduApi startInternal---------------------------> ");

        if (asr == null) {
            Log.e(TAG, "asr == null");
            return;
        }

        String json = "{\"accept-audio-data\":true,\"" +
                "vad.endpoint-timeout\":0," +
                "\"pid\":15362," +
                "\"infile\":\"#com.kikago.speech.baidu.BaiduInputStreamFactory.getBaiduInputStream()\"," +
                "\"accept-audio-volume\":false}";

        Log.d(TAG, json);
        asr.send(SpeechConstant.ASR_START, json, null, 0, 0);

    }

    @Override
    public void onStop() {

    }

    @Override
    public void sendCommand(String command, String payload) {

    }

    @Override
    public void sendData(byte[] data) {
        //Log.d(TAG, "sendData data = " + data.length);


        mBaiduInputStream.writeByte(data, data.length);

        if (mShouldCallStart.compareAndSet(true, false)) {
            startInternal();
        }
    }

    @Override
    public boolean isConnected() {
        return asr != null;
    }

    private EventListener mEventListener = new EventListener() {

        @Override
        public void onEvent(String name, String params, byte[] data, int offset, int length) {

            //Log.v("Ryan", "onEvent name = " + name + " json = " + params);
            if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
                RecogResult recogResult = RecogResult.parseJson(params);
                Log.i(TAG, "error = " + recogResult.getError() + " result = " + recogResult.getResultsRecognition()[0]);

                Message msg;
                if (!recogResult.isFinalResult()) { // Partial Result
                    if (mCid == 0) {
                        mCid = System.currentTimeMillis();
                    }
                    msg = new IntermediateMessage(1, recogResult.getResultsRecognition()[0], "baidu", mCid);
                } else { // Final Result
                    // TODO: process n-best result from #getResultHolder
                    if (mEndCid == 0) {
                        mEndCid = System.currentTimeMillis();
                    }
                    msg = new TextMessage(1, recogResult.getResultsRecognition(), "baidu", mCid, mEndCid);

                    mCid = 0;
                    mEndCid = 0;
                }
                mListener.onMessage(msg);
            }
        }
    };
}
