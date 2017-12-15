package com.kikatech.voice.core.dialogflow.scene;

/**
 * Created by tianli on 17-11-13.
 */

public interface ISceneManager {
    void exitScene(SceneBase scene);

    void exitCurrentScene();

    void notifyUncaught();

    void setQueryWords(boolean queryAnyWords);
}
