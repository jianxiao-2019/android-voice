package com.kikatech.go.dialogflow.telephony;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.go.dialogflow.telephony.incoming.SceneActions;
import com.kikatech.go.dialogflow.telephony.incoming.SceneIncoming;
import com.kikatech.go.dialogflow.telephony.outgoing.SceneOutgoing;
import com.kikatech.voice.service.DialogFlowService;
import com.kikatech.voice.service.IDialogFlowService;

/**
 * Created by tianli on 17-11-12.
 */

public class TelephonySceneManager extends BaseSceneManager {

    private PhoneStateDispatcher mPhoneStateReceiver;

    private SceneIncoming mSceneIncoming;
    private SceneOutgoing mSceneOutgoing;

    public TelephonySceneManager(Context context, @NonNull IDialogFlowService service) {
        super(context, service);
        mPhoneStateReceiver = new PhoneStateDispatcher(mPhoneListener);
        mPhoneStateReceiver.register(mContext);
    }

    @Override
    protected void registerScenes() {
        mService.registerScene(mSceneIncoming = new SceneIncoming(
                mContext, mService.getTtsFeedback()));
        mService.registerScene(mSceneOutgoing = new SceneOutgoing(
                mContext, mService.getTtsFeedback()));
    }

    @Override
    protected void unregisterScenes() {
        mService.unregisterScene(mSceneIncoming);
        mService.unregisterScene(mSceneOutgoing);
    }

    @Override
    public void close() {
        unregisterScenes();
        mPhoneStateReceiver.unregister(mContext);
    }

    private PhoneStateDispatcher.ICallStateChangeListener mPhoneListener = new PhoneStateDispatcher.ICallStateChangeListener() {
        @Override
        public void onInComingCallRinging(String phoneNumber) {
            mService.resetContexts();
            String incoming = String.format(SceneActions.KIKA_PROCESS_INCOMING_CALL, phoneNumber);
            mService.talk(incoming);
        }

        @Override
        public void onInComingCallEnded() {
            mService.resetContexts();
        }
    };
}
