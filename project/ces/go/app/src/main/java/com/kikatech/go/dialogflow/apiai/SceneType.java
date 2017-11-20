package com.kikatech.go.dialogflow.apiai;

import com.kikatech.go.dialogflow.navigation.SceneNavigation;
import com.kikatech.go.dialogflow.sms.reply.SceneReplySms;
import com.kikatech.go.dialogflow.sms.send.SceneSendSms;
import com.kikatech.go.dialogflow.stop.SceneStopIntent;
import com.kikatech.go.dialogflow.telephony.incoming.SceneIncoming;
import com.kikatech.go.dialogflow.telephony.outgoing.SceneOutgoing;

/**
 * @author SkeeterWang Created on 2017/11/6.
 */
public enum SceneType {

    DEFAULT("Default"), //default scene, un-know intents will go through
    STOP_INTENT(SceneStopIntent.SCENE),
    NAVIGATION(SceneNavigation.SCENE),
    SEND_SMS(SceneSendSms.SCENE),
    REPLY_SMS(SceneReplySms.SCENE),
    TELEPHONY_INCOMING(SceneIncoming.SCENE),
    TELEPHONY_OUTGOING(SceneOutgoing.SCENE);

    SceneType(String scene) {
        this.scene = scene;
    }

    private String scene;

    @Override
    public String toString() {
        return scene;
    }

    public static String getScene(String intentName) {
        try {
            for (SceneType scene : values()) {
                if (intentName.startsWith(scene.scene)) {
                    return scene.scene;
                }
            }
        } catch (Exception ignore) {
        }
        return null;
    }
}