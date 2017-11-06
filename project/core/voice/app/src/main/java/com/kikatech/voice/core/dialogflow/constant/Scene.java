package com.kikatech.voice.core.dialogflow.constant;

/**
 * @author SkeeterWang Created on 2017/11/6.
 */
public enum Scene {

    DEFAULT("Default"), //default scene, un-know intents will go through
    NAVIGATION("Navigation");

    Scene(String scene) {
        this.scene = scene;
    }

    private String scene;

    @Override
    public String toString() {
        return scene;
    }

    public static String getScene(String intentName) {
        try {
            for( Scene scene : values() ){
                if (intentName.startsWith(scene.scene)){
                    return scene.scene;
                }
            }
        } catch (Exception ignore) {}
        return null;
    }
}