package com.kikatech.voice.access;

import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by ryanlin on 11/10/2017.
 */

public class SendInfoManager {

    private AccessibilityNodeInfo mSendInfo;

    private static SendInfoManager sSendInfoManager;
    public static SendInfoManager getInstance() {
        if (sSendInfoManager == null) {
            sSendInfoManager = new SendInfoManager();
        }
        return sSendInfoManager;
    }

    private SendInfoManager() {

    }

    public void setSEndInfo(AccessibilityNodeInfo info) {
        mSendInfo = info;
    }

    public AccessibilityNodeInfo getSendInfo() {
        return mSendInfo;
    }
}
