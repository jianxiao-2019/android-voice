package com.kikatech.go.access;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by ryanlin on 02/10/2017.
 */

public class WhatsAppEventHandler extends EventHandler {

    public static final String PKG_NAME = "com.whatsapp";
    public static final String[] KEY_WORD_LIST = {"傳送", "发送", "Send"};

    @Override
    protected AccessibilityNodeInfo getSendNodeInfo(AccessibilityNodeInfo info) {
        if (info == null) {
            return null;
        }

        Log.d("Ryan", "WhatsAppEventHandler" +
                " info.className = " + info.getClassName() +
                " info.description = " + info.getContentDescription() +
                " info.text = " + info.getText() +
                " childCount = " + info.getChildCount());

        for (String key : KEY_WORD_LIST) {
            if (key.equals(info.getContentDescription())) {
                return info;
            }
        }

        return null;
    }

    @Override
    protected String getPackageName() {
        return PKG_NAME;
    }
}
