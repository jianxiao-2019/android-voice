package com.kikatech.go.dialogflow.telephony;

import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.navigation.SceneNavigation;
import com.kikatech.go.dialogflow.telephony.incoming.SceneIncoming;
import com.kikatech.go.dialogflow.telephony.outgoing.SceneOutgoing;
import com.kikatech.voice.service.DialogFlowService;

/**
 * Created by tianli on 17-11-12.
 */

public class TelephonySceneManager {

    private PhoneStateDispatcher mPhoneStateReceiver;

    private DialogFlowService mService;

    private SceneIncoming mSceneIncoming;
    private SceneOutgoing mSceneOutgoing;

    public TelephonySceneManager(@NonNull DialogFlowService service) {
        mService = service;
        mPhoneStateReceiver = new PhoneStateDispatcher(mPhoneListener);
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
    }

    private PhoneStateDispatcher.ICallStateChangeListener mPhoneListener = new PhoneStateDispatcher.ICallStateChangeListener() {
        @Override
        public void onInComingCallRinging(String phoneNumber) {
//            callbackPreStartTelephonyIncoming( phoneNumber );
        }

        @Override
        public void onInComingCallEnded() {
//            if (mCallback != null) {
//                mCallback.resetContextImpl();
//            }
        }
    };
}
