package com.kikatech.voice.webservice.tencent_cloud_speech;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.kikatech.voice.core.webservice.impl.BaseWebSocket;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voice.service.conf.VoiceConfiguration;
import com.tencent.ai.sdk.control.SpeechManager;
import com.tencent.ai.sdk.tr.ITrListener;
import com.tencent.ai.sdk.tr.TrSession;
import com.tencent.ai.sdk.tts.ITtsInitListener;
import com.tencent.ai.sdk.tts.TtsSession;
import com.tencent.ai.sdk.utils.ISSErrors;

import org.json.JSONObject;



public class TencentApi extends BaseWebSocket {
    private static final String TAG = "TencentApi";


    /** SDK语音&语义识别的Session */
    private TrSession mTrSession;

    /** SDK TtsSession, 当语音识别类型为 SPEECH_RECOGNIZE_TYPE_ALL 的时候有效 */
    private TtsSession mTTSSession;

    /** 录音线程 */
    private PcmRecorder mPcmRecorder;
    private Context mContext;
    private long mCid = 0;

    public TencentApi (Context context) {
        mContext = context;
    }

    private String getAppInfo() {
        String result = "";
        try {
            final JSONObject info = new JSONObject();
            info.put("appkey", "f76e21c0b0d611e88612251cd296a3ad");
            info.put("token", "c244e88caee0417e824b4436ccdf942d");
            /**
             * 如果产品是车机，填入CAR
             * 如果产品是电视，填入TV
             * 如果产品是音箱，填入SPEAKER
             * 如果产品是手机，填入PHONE
             */
            info.put("deviceName", "固定，填入CAR或者TV或者SPEAKER或者PHONE");
            info.put("productName", "产品名称，不要有特殊字符和空格");
            info.put("vendor","厂商英文名,不要有特殊字符和空格");

            final JSONObject json = new JSONObject();
            json.put("info", info);

            result = json.toString();
        } catch (Exception e) {
            // do nothing
        }
        Log.d("SpeechApplication", "info = " + result);
        return result;
    }

    @Override
    public void connect(VoiceConfiguration voiceConfiguration) {

        int ret = SpeechManager.getInstance().startUp(mContext, getAppInfo());
        if (ret != ISSErrors.ISS_SUCCESS) {
            System.exit(0);
        }
        // 初始化TrSession
        if (null == mTrSession) {
            mTrSession = TrSession.getInstance(mContext, mTrListener, 0, "", "");
            mTrSession.setParam(TrSession.ISS_TR_PARAM_VOICE_TYPE, TrSession.ISS_TR_PARAM_VOICE_TYPE_RSP_ALL);
        }

        // 初始化TTSSession
        if (null == mTTSSession) {
            mTTSSession = new TtsSession(mContext, mTTSInitListener, "");
        }
    }

    @Override
    public void release() {
    }

    @Override
    synchronized public void onStart() {
        startRecognize();
    }

    @Override
    synchronized public void onStop() {
        stopRecord();
    }

    @Override
    public void sendCommand(String command, String payload) {}

    @Override
    synchronized public void sendData(byte[] data) {
        mTrSession.appendAudioData(data, data.length);
    }

    @Override
    public boolean isConnected() {
        return mTrSession != null;
    }

    private void startRecognize() {
        // 停止上次录音
        mTrSession.stop();
        stopRecord();
        if (null != mTTSSession) {
            mTTSSession.stopSpeak();
        }

        String message = null;
        int id = mTrSession.start(TrSession.ISS_TR_MODE_CLOUD_REC,false);
        if (id != ISSErrors.ISS_SUCCESS) {
            message = "Tr SessionStart error,id = " + id;
            Log.e(TAG, message);
            Log.d(TAG,message);
        }
//        else {
//            // 开始录音
//            mPcmRecorder = new PcmRecorder(mContext);
//            mPcmRecorder.start();
//
//            Log.d(TAG,"\n开始语音识别流程：");
//            Log.d(TAG,"开始录音");
//        }
    }
    
    /**
     * 停止录音
     */
    private void stopRecord() {
//        if(null != mPcmRecorder) {
//            if (mPcmRecorder.stopThread()) {
//                Log.d(TAG,"停止录音");
//            }
//        }
    }
//
//    @Override
//    public void onRecord(byte[] buffer, int bufferSize) {
//        if (null != mTrSession) {
//            mTrSession.appendAudioData(buffer, bufferSize);
//        }
//    }

    private ITrListener mTrListener = new ITrListener() {
        @Override
        public void onTrInited(boolean state, int errId) {
            String msg = "onTrInited - state : " + state + ", errId : " + errId;
            Log.i(TAG, "onTrInited - state : " + state + ", errId : " + errId);
            if (state) {
                Log.d(TAG,"TrSession init成功");
            } else {
                Log.d(TAG,"TrSession init失败, errId : " + errId);
            }
        }

        @Override
        public void onTrVoiceMsgProc(long uMsg, long wParam, String lParam, Object extraData) {
            String msg = null;
            Log.i(TAG, "onTrVoiceMsgProc - uMsg : " + uMsg + ", wParam : " + wParam + ", lParam : " + lParam);
            if (uMsg == TrSession.ISS_TR_MSG_SpeechStart) {
                msg = "检测到说话开始";
            } else if (uMsg == TrSession.ISS_TR_MSG_SpeechEnd) {
                msg = "检测到说话结束";
            } else if (uMsg == TrSession.ISS_TR_MSG_VoiceResult) {
                msg = "end" + lParam;

                Message msg2;
                mCid = System.currentTimeMillis();
                msg2 = new TextMessage(1, new String[]{lParam}, "tencent", mCid);
                mListener.onMessage(msg2);
                stopRecord();
            }

            if (!TextUtils.isEmpty(msg)) {
                Log.d(TAG,msg);
            }

            if(uMsg == 20013) {
                Message msg2;
                mCid = System.currentTimeMillis();

                Log.i(TAG, "2 onTrVoiceMsgProc - uMsg : " + uMsg + ", wParam : " + wParam + ", lParam : " + lParam);
                msg2 = new IntermediateMessage(1, lParam, "tencent", mCid);
                mListener.onMessage(msg2);
            }
        }


        @Override
        public void onTrSemanticMsgProc(long uMsg, long wParam, int cmd, String lParam, Object extraMsg) {
            Log.i(TAG, "onTrSemanticMsgProc - uMsg : " + uMsg + ", wParam : " + wParam + ", lParam : " + lParam + ", extraMsg : " + extraMsg);
            Log.d(TAG,"语音 -> 语义 结束，结果为 ：");
            Log.d(TAG,lParam);

            stopRecord();
            mCid = 0;
            //if (mSpeechRecognizeType == SPEECH_RECOGNIZE_TYPE_ALL) {
            //    parseSemanticToTTS(lParam);
            //}

        }

        @Override
        public void onTrVoiceErrMsgProc(long uMsg, long errCode, String lParam, Object extraData) {
            Log.i(TAG, "onTrVoiceErrMsgProc - uMsg : " + uMsg + ", errCode : " + errCode + ", lParam : " + lParam);
            Log.d(TAG,"语音 -> 文本 出现错误，errCode ：" + errCode + ", msg : " + lParam);

            stopRecord();
            mCid = 0;
        }

        @Override
        public void onTrSemanticErrMsgProc(long uMsg, long errCode, int cmd, String lParam, Object extraMsg) {
            Log.i(TAG, "onTrSemanticErrMsgProc - uMsg : " + uMsg + ", errCode : " + errCode + ", cmd : " + cmd
                    + ", lParam : " + lParam + ", extraMsg : " + extraMsg);
            Log.d(TAG,"语音 -> 语义 出现错误，errCode ：" + errCode + ", cmd : " + cmd +", msg : " + lParam);

            stopRecord();
            mCid = 0;
        }
    };

    private ITtsInitListener mTTSInitListener = new ITtsInitListener() {
        @Override
        public void onTtsInited(boolean state, int errId) {
            String msg = "";
            if (state) {
                msg = "TTS引擎初始化成功";
            } else {
                msg = "TTS引擎初始化失败，errId ：" + errId;
            }

            Log.d(TAG, msg);
        }
    };
}