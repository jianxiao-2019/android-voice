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
        Log.d("Ryan", "BaiduApi connect begin");
        mBaiduInputStream = BaiduInputStreamFactory.getBaiduInputStream();
        asr = EventManagerFactory.create(mContext, "asr");
        asr.registerListener(mEventListener);
        Log.d("Ryan", "BaiduApi connect end");
    }

    @Override
    public void release() {
        Log.d("Ryan", "BaiduApi release begin");
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        asr.unregisterListener(mEventListener);
        asr = null;
        Log.d("Ryan", "BaiduApi release end");
    }

    @Override
    public void onStart() {
        mBaiduInputStream.start();
        Log.d("Ryan", "BaiduApi onStart begin 1 ");
        if (asr == null) {
            Log.e("Ryan", "onStart but asr == null");
        }
        Log.d("Ryan", "BaiduApi onStart mBaiduInputStream.hasData() = " + mBaiduInputStream.hasData());
        if (mBaiduInputStream.hasData()) {
            startInternal();
        } else {
            mShouldCallStart.set(true);
        }
        Log.d("Ryan", "BaiduApi onStart end");
    }

    private void startInternal() {
        Log.d("Ryan", "BaiduApi startInternal begin");
        String json = "{\"accept-audio-data\":true,\"" +
                "vad.endpoint-timeout\":0," +
                "\"outfile\":\"/storage/emulated/0/Android/data/com.baidu.speech.recognizerdemo/files/baiduASR/outfile.pcm\"," +
                "\"pid\":15362," +
                "\"infile\":\"#com.kikago.speech.baidu.BaiduInputStreamFactory.getBaiduInputStream()\"," +
                "\"accept-audio-volume\":false}";

        asr.send(SpeechConstant.ASR_START, json, null, 0, 0);
        Log.d("Ryan", "BaiduApi startInternal end");
    }

    @Override
    public void onStop() {
        if (asr == null) {
            Log.e("Ryan", "onStop but asr == null");
        }
        Log.d("Ryan", "BaiduApi onStop begin");
        asr.send(SpeechConstant.ASR_STOP, "{}", null, 0, 0);
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        Log.d("Ryan", "BaiduApi onStop end");
    }

    @Override
    public void sendCommand(String command, String payload) {
    }

    @Override
    public void sendData(byte[] data) {
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
            Log.v("Ryan", "onEvent name = " + name + " json = " + params);
            if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
                RecogResult recogResult = RecogResult.parseJson(params);
                Log.i("Ryan", "error = " + recogResult.getError() + " result = " + recogResult.getResultsRecognition()[0]);

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
