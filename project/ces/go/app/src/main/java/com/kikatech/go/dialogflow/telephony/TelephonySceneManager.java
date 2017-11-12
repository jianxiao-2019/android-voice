package com.kikatech.go.dialogflow.telephony;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.telephony.incoming.SceneIncoming;
import com.kikatech.go.dialogflow.telephony.outgoing.SceneOutgoing;
import com.kikatech.voice.service.DialogFlowService;

/**
 * Created by tianli on 17-11-12.
 */

public class TelephonySceneManager {

    private PhoneStateDispatcher mPhoneStateReceiver;

    private DialogFlowService mService;

    private Context mContext;
    private SceneIncoming mSceneIncoming;
    private SceneOutgoing mSceneOutgoing;

    public TelephonySceneManager(Context context, @NonNull DialogFlowService service) {
        mContext = context.getApplicationContext();
        mService = service;
        mPhoneStateReceiver = new PhoneStateDispatcher(mPhoneListener);
        mPhoneStateReceiver.register(mContext);
        registerScenes();
    }

    private void registerScenes() {
        mService.registerScene(SceneIncoming.SCENE, mSceneIncoming = new SceneIncoming(
                mService.getTtsFeedback()));
        mService.registerScene(SceneOutgoing.SCENE, mSceneOutgoing = new SceneOutgoing(
                mService.getTtsFeedback()));
    }

    private void unregisterScenes() {
        mService.unregisterScene(SceneIncoming.SCENE, mSceneIncoming);
        mService.unregisterScene(SceneOutgoing.SCENE, mSceneOutgoing);
    }

    public void close() {
        unregisterScenes();
        mPhoneStateReceiver.unregister(mContext);
    }

    private PhoneStateDispatcher.ICallStateChangeListener mPhoneListener = new PhoneStateDispatcher.ICallStateChangeListener() {
        @Override
        public void onInComingCallRinging(String phoneNumber) {
            mService.resetContexts();
            String incoming = String.format(SceneTelephonyIncoming.KIKA_PROCESS_INCOMING_CALL, phoneNumber);
            mService.talk(incoming);
        }

        @Override
        public void onInComingCallEnded() {
            mService.resetContexts();
        }
    };
}
