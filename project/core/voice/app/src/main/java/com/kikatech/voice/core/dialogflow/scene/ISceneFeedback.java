package com.kikatech.voice.core.dialogflow.scene;

import android.os.Bundle;

/**
 * Created by tianli on 17-11-11.
 */

public interface ISceneFeedback {
    void onText(String text, Bundle extras, IDialogFlowFeedback.IToSceneFeedback feedback);
}