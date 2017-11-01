package com.kikatech.go.intention.handler;

import com.kikatech.go.intention.Intention;

/**
 * @author jasonli Created on 2017/10/28.
 */

public class GlobalHandler extends IntentionHandler {

    @Override
    public void onHandle(Intention intention) {
        super.onHandle(intention);

        mResponse = "Unknown intention.";
    }
}
