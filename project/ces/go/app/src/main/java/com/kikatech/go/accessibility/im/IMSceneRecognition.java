package com.kikatech.go.accessibility.im;

import android.view.accessibility.AccessibilityEvent;

import com.kikatech.go.accessibility.im.apps.FacebookScene;
import com.kikatech.go.accessibility.im.apps.InstagramScene;
import com.kikatech.go.accessibility.im.apps.SMSScene;
import com.kikatech.go.accessibility.im.apps.WeChatScene;
import com.kikatech.go.accessibility.im.apps.WhatsAppScene;
import com.kikatech.go.accessibility.scene.Scene;
import com.kikatech.go.accessibility.scene.SceneRecognition;
import com.kikatech.voice.util.log.Logger;

/**
 * Created by tianli on 17-10-22.
 */

public class IMSceneRecognition extends SceneRecognition{

    @Override
    public Scene recognize(AccessibilityEvent event) {
        Logger.d("--------------onAccessibilityEvent ----------------------- " + event.getPackageName());
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // TODO: 17-10-22 识别当前页面是发送列表页面
            if (MessageApps.PACKAGE_NAME_FACEBOOK.equals(event.getPackageName())) {
                Logger.d("onAccessibilityEvent enter Facebook");
                return new FacebookScene(event);
            } else if (MessageApps.PACKAGE_NAME_WECHAT.equals(event.getPackageName())) {
                Logger.d("onAccessibilityEvent enter WeChat");
                return new WeChatScene(event);
            } else if (MessageApps.PACKAGE_NAME_SMS.equals(event.getPackageName())) {
                Logger.d("onAccessibilityEvent enter SMS");
                return new SMSScene(event);
            } else if (MessageApps.PACKAGE_NAME_INSTAGRAM.equals(event.getPackageName())) {
                Logger.d("onAccessibilityEvent enter Instagram");
                return new InstagramScene(event);
            } else if (MessageApps.PACKAGE_NAME_WHATSAPP.equals(event.getPackageName())) {
                Logger.d("onAccessibilityEvent enter WhatsApp");
                return new WhatsAppScene(event);
            }
        }
        return null;
    }
}
