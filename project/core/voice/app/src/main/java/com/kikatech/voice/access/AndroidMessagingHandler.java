package com.kikatech.voice.access;

import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by ryanlin on 29/09/2017.
 */

public class AndroidMessagingHandler extends EventHandler {

    public static final String PKG_NAME = "com.google.android.apps.messaging";
    public static final String[] KEY_WORD_LIST = {"傳送簡訊", "发送短信", "Send SMS"};

    @Override
    protected AccessibilityNodeInfo getSendNodeInfo(AccessibilityNodeInfo info) {
        if ("android.widget.LinearLayout".equals(info.getClassName())) {
            for (String key : KEY_WORD_LIST) {
                if (key.equals(info.getContentDescription())) {
                    return info;
                }
            }
        }
        return null;
    }

    @Override
    protected String getPackageName() {
        return PKG_NAME;
    }
}
