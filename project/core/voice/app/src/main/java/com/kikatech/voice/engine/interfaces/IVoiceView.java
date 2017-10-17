package com.kikatech.voice.engine.interfaces;

import android.content.Context;

/**
 * Created by ryanlin on 06/10/2017.
 */

public interface IVoiceView {

    int RESULT_FINAL = 1;
    int RESULT_INTERMEDIATE = 2;
    int RESULT_REPLACE_ALL = 3;

    void onStartListening();
    void onStopListening();
    void onUpdateRecognizedResult(CharSequence result, int resultType);

    void updateHintStr(int hintStrResId);
    void sendKeyEvent(int keyCode);

    CharSequence getTextOnEditor();
    String getCurrentEditorPackageName();
    Context getContext();
}
