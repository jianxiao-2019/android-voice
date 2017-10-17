package com.kikatech.voice.access;

import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by ryanlin on 28/09/2017.
 */

public class FbEventHandler extends EventHandler {

    public static final String PKG_NAME = "com.facebook.orca";

    @Override
    protected AccessibilityNodeInfo getSendNodeInfo(AccessibilityNodeInfo info) {
        if (info != null && "Send".equals(info.getContentDescription())) {
            return info;
        }
        return null;
    }

    @Override
    protected String getPackageName() {
        return PKG_NAME;
    }
}
