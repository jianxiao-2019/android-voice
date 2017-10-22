package com.kikatech.go.access;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.kikatech.voice.util.log.Logger;


/**
 * Created by ryanlin on 27/09/2017.
 */

public class DetectionService extends AccessibilityService {

    public static AccessibilityNodeInfo sSendInfo;

    private EventHandler mEventHandler;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        Logger.d("-------------------------------");
        Logger.d("onAccessibilityEvent event.package name = " + event.getPackageName());
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            Logger.i("onAccessibilityEvent TYPE_WINDOW_STATE_CHANGED");
            if (FbEventHandler.PKG_NAME.equals(event.getPackageName())) {
                Logger.d("onAccessibilityEvent enter FB");
                mEventHandler = new FbEventHandler();
            } else if (WeChatEventHandler.PKG_NAME.equals(event.getPackageName())) {
                Logger.d("onAccessibilityEvent enter WeChat");
                mEventHandler = new WeChatEventHandler();
            } else if (AndroidMessagingHandler.PKG_NAME.equals(event.getPackageName())) {
                Logger.d("onAccessibilityEvent enter Android Messaging");
                mEventHandler = new AndroidMessagingHandler();
            } else if (InstagramEventHandler.PKG_NAME.equals(event.getPackageName())) {
                Logger.d("onAccessibilityEvent enter Instagram");
                mEventHandler = new InstagramEventHandler();
            } else if (WhatsAppEventHandler.PKG_NAME.equals(event.getPackageName())) {
                Logger.d("onAccessibilityEvent enter WhatsApp");
                mEventHandler = new WhatsAppEventHandler();
            } else if (!this.getPackageName().equals(event.getPackageName())) {
                mEventHandler = null;
                SendInfoManager.getInstance().setSEndInfo(null);
            }
        }

        if (mEventHandler != null) {
            mEventHandler.onEvent(event);
            SendInfoManager.getInstance().setSEndInfo(mEventHandler.getSendNodeInfo());
            Logger.d("onAccessibilityEvent SendInfo = " + SendInfoManager.getInstance().getSendInfo());
        }
        Logger.d("-------------------------------");
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SendInfoManager.getInstance().setSEndInfo(null);
    }
}
