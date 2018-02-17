package com.kikatech.voice.core.dialogflow.scene;

import com.kikatech.voice.core.dialogflow.intent.Intent;

/**
 * Created by tianli on 17-11-13.
 */

public interface ISceneManager {
    void exitScene(SceneBase scene);

    void exitCurrentScene();

    void redirectIntent(Intent intent);

    void notifyUncaught();

    void setQueryWords(boolean queryAnyWords);
}
