package com.kikatech.voice.core.dialogflow.scene;

/**
 * @author SkeeterWang Created on 2017/11/6.
 */
public enum SceneType {

    DEFAULT("Default"), //default scene, un-know intents will go through
    NAVIGATION("Navigation"),
    TELEPHONY_INCOMING("Telephony - Incoming"),
    TELEPHONY_OUTGOING("Telephony - Outgoing");

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
            for( SceneType scene : values() ){
                if (intentName.startsWith(scene.scene)){
                    return scene.scene;
                }
            }
        } catch (Exception ignore) {}
        return null;
    }
}