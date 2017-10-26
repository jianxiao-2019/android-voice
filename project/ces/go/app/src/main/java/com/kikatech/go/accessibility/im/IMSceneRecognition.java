package com.kikatech.go.accessibility.im;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.kikatech.go.accessibility.im.apps.MessengerScene;
import com.kikatech.go.accessibility.im.apps.WhatsAppScene;
import com.kikatech.go.accessibility.scene.Scene;
import com.kikatech.go.accessibility.scene.SceneRecognition;
import com.kikatech.go.util.AppConstants;
import com.kikatech.voice.util.log.Logger;

/**
 * Created by tianli on 17-10-22.
 */

public class IMSceneRecognition extends SceneRecognition{

    @Override
    public Scene recognize(AccessibilityEvent event, AccessibilityNodeInfo rootNodeInfo) {
        Logger.d("--------------onAccessibilityEvent ----------------------- " + event.getPackageName());
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            // TODO: 17-10-22 识别当前页面是发送列表页面
            if (AppConstants.PACKAGE_MESSENGER.equals(event.getPackageName())) {
                return new MessengerScene(event, rootNodeInfo);
            } else if (AppConstants.PACKAGE_WHATSAPP.equals(event.getPackageName())) {
                return new WhatsAppScene(event, rootNodeInfo);
            }
        }
        return null;
    }
}
