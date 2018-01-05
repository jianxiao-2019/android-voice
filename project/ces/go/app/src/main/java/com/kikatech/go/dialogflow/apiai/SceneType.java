package com.kikatech.go.dialogflow.apiai;

import com.kikatech.go.dialogflow.gotomain.SceneGotoMain;
import com.kikatech.go.dialogflow.im.reply.SceneReplyIM;
import com.kikatech.go.dialogflow.im.send.SceneSendIM;
import com.kikatech.go.dialogflow.music.SceneMusic;
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
    GOTO_MAIN_INTENT(SceneGotoMain.SCENE),
    STOP_INTENT(SceneStopIntent.SCENE),
    NAVIGATION(SceneNavigation.SCENE),
    SEND_SMS(SceneSendSms.SCENE),
    REPLY_SMS(SceneReplySms.SCENE),
    SEND_IM(SceneSendIM.SCENE),
    REPLY_IM(SceneReplyIM.SCENE),
    TELEPHONY_INCOMING(SceneIncoming.SCENE),
    TELEPHONY_OUTGOING(SceneOutgoing.SCENE),
    MUSIC(SceneMusic.SCENE);

    SceneType(String scene) {
        this.scene = scene;
    }

    private String scene;

    @Override
    public String toString() {
        return scene;
    }

    public static String getScene(String intentName) {
        intentName = intentName.toLowerCase();
        try {
            for (SceneType scene : values()) {
                if (intentName.startsWith(scene.scene.toLowerCase())) {
                    return scene.scene;
                }
            }
        } catch (Exception ignore) {
        }
        return null;
    }
}