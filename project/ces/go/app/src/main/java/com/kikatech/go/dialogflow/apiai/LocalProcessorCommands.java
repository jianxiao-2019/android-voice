package com.kikatech.go.dialogflow.apiai;

/**
 * Created by brad_chang on 2018/1/2.
 */

class LocalProcessorCommands {
    static final String[] CANCEL = new String[]{
            "skip",
            "dismiss",
            "abort",
            "stop",
            "stop it",
            "cancel",
            "cancel it",
            "cancel that",
            "go to main page",
            "back to main page",
    };

    static final String FIXED_CANCEL_SYNONYM = "Cancel";
    static final String[] CANCEL_SYNONYM = new String[]{
            "pixel",
            "pencil",
    };
}