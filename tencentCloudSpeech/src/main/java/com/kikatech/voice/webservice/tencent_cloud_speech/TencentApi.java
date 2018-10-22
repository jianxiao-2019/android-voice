package com.kikatech.voice.webservice.tencent_cloud_speech;

import android.content.Context;
import android.text.TextUtils;

import com.kikatech.voice.core.webservice.impl.BaseWebSocket;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voice.service.conf.VoiceConfiguration;
import com.tencent.ai.sdk.control.SpeechManager;
import com.tencent.ai.sdk.tr.ITrListener;
import com.tencent.ai.sdk.tr.TrSession;
import com.tencent.ai.sdk.tts.ITtsInitListener;
import com.tencent.ai.sdk.utils.ISSErrors;

import org.json.JSONObject;
import com.kikatech.voice.util.log.Logger;



public class TencentApi extends BaseWebSocket {
    private static final String TAG = "TencentApi";

    //TrSession.ISS_TR_PARAM_VOICE_TYPE_RSP_ALL
    private String VOICE_TYPE_RSP = TrSession.ISS_TR_PARAM_VOICE_TYPE_RSP_VOICE;


    /** SDK语音&语义识别的Session */
    private TrSession mTrSession;

    /** 录音线程 */
    private PcmRecorder mPcmRecorder;
    private Context mContext;
    private long mCid = 0;
    private long mEndCid = 0;

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
            mTrSession.setParam(TrSession.ISS_TR_PARAM_VOICE_TYPE, VOICE_TYPE_RSP);
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

        String message = null;
        int id = mTrSession.start(TrSession.ISS_TR_MODE_CLOUD_REC,false);
        if (id != ISSErrors.ISS_SUCCESS) {
            message = "Tr SessionStart error,id = " + id;
            Logger.d(TAG,message);
        }
    }

    private ITrListener mTrListener = new ITrListener() {
        @Override
        public void onTrInited(boolean state, int errId) {
            if (state) {
                Logger.d(TAG,"TrSession init成功");
            } else {
                Logger.d(TAG,"TrSession init失败, errId : " + errId);
            }
        }

        @Override
        public void onTrVoiceMsgProc(long uMsg, long wParam, String lParam, Object extraData) {
            if (uMsg == TrSession.ISS_TR_MSG_SpeechStart) {
                //"检测到说话开始";
            } else if (uMsg == TrSession.ISS_TR_MSG_SpeechEnd) {
                //"检测到说话结束";
            } else if (uMsg == TrSession.ISS_TR_MSG_VoiceResult) {
                Message msg;
                if (mEndCid == 0) {
                    mEndCid = System.currentTimeMillis();
                }
                msg = new TextMessage(1, new String[]{lParam}, "tencent", mCid, mEndCid);
                mListener.onMessage(msg);
                mCid = 0;
                mEndCid = 0;
            } else if(uMsg == TrSession.ISS_TR_MSG_ProcessResult) {
                Message msg;
                if (mCid == 0) {
                    mCid = System.currentTimeMillis();
                }
                msg = new IntermediateMessage(1, lParam, "tencent", mCid);
                mListener.onMessage(msg);
            }
        }


        @Override
        public void onTrSemanticMsgProc(long uMsg, long wParam, int cmd, String lParam, Object extraMsg) {
            mCid = 0;
            mEndCid = 0;
            startRecognize();
        }

        @Override
        public void onTrVoiceErrMsgProc(long uMsg, long errCode, String lParam, Object extraData) {
            mCid = 0;
            startRecognize();
        }

        @Override
        public void onTrSemanticErrMsgProc(long uMsg, long errCode, int cmd, String lParam, Object extraMsg) {
            mCid = 0;
            startRecognize();
        }
    };

}