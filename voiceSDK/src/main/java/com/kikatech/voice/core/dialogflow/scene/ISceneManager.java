package com.kikatech.voice.core.dialogflow.scene;

/**
 * Created by tianli on 17-11-13.
 */

public interface ISceneManager {
    void exitSceneAndSleep(SceneBase scene);

    void exitScene(SceneBase scene);

    void exitCurrentScene();

    void setQueryWords(boolean queryAnyWords);
}
