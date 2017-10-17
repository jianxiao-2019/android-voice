package com.kikatech.vkb.access;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by ryanlin on 28/09/2017.
 */

public abstract class EventHandler {

    protected AccessibilityNodeInfo mSendNodeInfo = null;

    public AccessibilityNodeInfo getSendNodeInfo() {
        return mSendNodeInfo;
    }

    public void onEvent(AccessibilityEvent event) {
//        Log.i("Ryan", "onEvent mSendNodeInfo = " + mSendNodeInfo);
//        Log.v("Ryan", "onEvent event.getType = 0x" + Integer.toHexString(event.getEventType()));
        if (mSendNodeInfo != null || event == null
                || !getPackageName().equals(event.getPackageName())) {
            return;
        }
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                && event.getEventType() != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            return;
        }

//        Log.i("Ryan", "onEvent 2");
        AccessibilityNodeInfo info = event.getSource();

        int level = 1;
        while(info != null) {
//            Log.v("Ryan", "----------level = " + (level++) + "-----------");
            AccessibilityNodeInfo foundedInfo = findSendNodeInfo(info);
            if (foundedInfo == null) {
                info = info.getParent();
            } else {
                mSendNodeInfo = foundedInfo;
                info = null;
            }
        }
    }

    private AccessibilityNodeInfo findSendNodeInfo(AccessibilityNodeInfo parent) {
        if (parent == null || parent.getChildCount() == 0) {
            return null;
        }

        parent.refresh();
        for (int i = 0; i < parent.getChildCount(); i++) {
            AccessibilityNodeInfo info = parent.getChild(i);
            if (info == null) continue;

//            Log.v("Ryan", "info = " + info.getContentDescription() + " class = " + info.getClassName());
            AccessibilityNodeInfo target = getSendNodeInfo(info);
            if (target != null) {
                Log.i("Ryan", "!!!!!!Found send node!!!!!!");
                return target;
            }
        }
        return null;
    }

    protected abstract AccessibilityNodeInfo getSendNodeInfo(AccessibilityNodeInfo info);
    protected abstract String getPackageName();
}
