package com.kikatech.go.message;

import android.content.Context;

import com.kikatech.go.accessibility.AccessibilityManager;
import com.kikatech.go.accessibility.im.apps.WhatsAppScene;
import com.kikatech.go.accessibility.scene.Scene;
import com.kikatech.go.util.AppConstants;

/**
 * @author jasonli Created on 2017/10/25.
 */

public class WhatsAppProcessor extends IMProcessor {

    public WhatsAppProcessor(Context context) {
        super(context);
    }

    @Override
    public void start() {
        AccessibilityManager.getInstance().register(WhatsAppScene.class, this);
        super.start();
    }

    @Override
    public void stop() {
        AccessibilityManager.getInstance().unregister(WhatsAppScene.class, this);
        super.stop();
    }

    @Override
    public String getPackage() {
        return AppConstants.PACKAGE_WHATSAPP;
    }

    @Override
    public boolean onSceneShown(Scene sceneShown) {
        if (super.onSceneShown(sceneShown)) {
            return true;
        }
        String stage = getStage();
        WhatsAppScene scene = (WhatsAppScene) sceneShown;
        String target = mTarget;
        switch (stage) {
            case ProcessingStage.IMProcessStage.STAGE_ENTER_USER_NAME:
                if (scene.findUserItem(target) != null) {
                    if (scene.selectUserItem(target)) {
                        updateStage(ProcessingStage.IMProcessStage.STAGE_PICK_USER);
                    }
                }
                return true;
            case ProcessingStage.IMProcessStage.STAGE_PICK_USER:
                // click the send button in WhatsApp share page
                if (scene.clickSendButton()) {
                    updateStage(ProcessingStage.IMProcessStage.STAGE_CLICK_SEND_BUTTON);
                }
                return true;
            case ProcessingStage.IMProcessStage.STAGE_CLICK_SEND_BUTTON:
                // click the send button in WhatsApp chatroom
                if (!scene.isInChatroomPage()) {
                    // wait for chatroom launched
                    return true;
                }
                if (scene.clickSendButton()) {
                    updateStage(ProcessingStage.IMProcessStage.STAGE_DONE);
                }
                return true;
        }
        checkStage();
        return false;
    }
}
