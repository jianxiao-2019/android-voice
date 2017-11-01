package com.kikatech.go.intention.handler;

import com.kikatech.go.intention.ActionNames;
import com.kikatech.go.intention.Intention;
import com.kikatech.go.util.LogUtil;

/**
 * @author jasonli Created on 2017/10/27.
 */

public abstract class IntentionHandler {

    private static final String TAG = "IntentionHandler";

    protected Intention mIntention;
    protected String mResponse;

    public void onHandle(Intention intention) {
        LogUtil.log(TAG, this.getClass().getSimpleName() + " onHandle " + intention.getAction());
        if (mIntention != null) {
            intention.updateMetadata(mIntention.getParams());
        }

        mIntention = intention;
    }

    public static IntentionHandler createHandler(String topicName) {
        IntentionHandler handler = null;
        switch (topicName) {
            case ActionNames.TOPIC_NAVI:
                handler = new NavigationHandler();
                break;
            case ActionNames.TOPIC_PHONECALL:
                handler = new PhoneCallHandler();
                break;
            case ActionNames.TOPIC_GLOBAL:
            default:
                handler = new GlobalHandler();
                break;
        }
        return handler;
    }

    public String getResponse() {
        return mResponse;
    }

}
