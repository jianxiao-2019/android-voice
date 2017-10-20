package com.kikatech.go.accessibility.message;

import android.view.accessibility.AccessibilityEvent;

import com.kikatech.go.accessibility.EventDispatcher;
import com.kikatech.go.util.log.Logger;

/**
 * Created by tianli on 17-10-20.
 */

public class MessageEventDispatcher extends EventDispatcher {

    @Override
    protected boolean onAccessibilityEvent(AccessibilityEvent event) {
        Logger.d("--------------onAccessibilityEvent -----------------------");
        boolean consumed = false;
        Logger.d("onAccessibilityEvent event.package name = " + event.getPackageName());
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            Logger.i("onAccessibilityEvent TYPE_WINDOW_STATE_CHANGED");
            if (MessageApps.PACKAGE_NAME_FACEBOOK.equals(event.getPackageName())) {
                Logger.d("onAccessibilityEvent enter Facebook");
                consumed = true;
            } else if (MessageApps.PACKAGE_NAME_WECHAT.equals(event.getPackageName())) {
                Logger.d("onAccessibilityEvent enter WeChat");
                consumed = true;
            } else if (MessageApps.PACKAGE_NAME_SMS.equals(event.getPackageName())) {
                Logger.d("onAccessibilityEvent enter SMS");
                consumed = true;
            } else if (MessageApps.PACKAGE_NAME_INSTAGRAM.equals(event.getPackageName())) {
                Logger.d("onAccessibilityEvent enter Instagram");
                consumed = true;
            } else if (MessageApps.PACKAGE_NAME_WHATSAPP.equals(event.getPackageName())) {
                Logger.d("onAccessibilityEvent enter WhatsApp");
                consumed = true;
            }
//            else if (!this.getPackageName().equals(event.getPackageName())) {
//                mEventHandler = null;
//                SendInfoManager.getInstance().setSEndInfo(null);
//            }
        }
        return consumed;
    }
}
