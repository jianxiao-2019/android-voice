package com.kikatech.voice.core.dialogflow.intent;

import android.os.Bundle;

/**
 * Created by tianli on 17-11-2.
 */

public class Intent {

    private String mAction;
    private Bundle mExtra = new Bundle();

    public String getAction() {
        return mAction;
    }

    public void setAction(String mAction) {
        this.mAction = mAction;
    }

    public Bundle getExtra(){
        return mExtra;
    }
}
