package com.kikatech.go.message;

import android.content.Context;

import com.kikatech.go.accessibility.AccessibilityManager;
import com.kikatech.go.accessibility.im.apps.MessengerScene;
import com.kikatech.go.accessibility.scene.Scene;
import com.kikatech.go.utils.AppConstants;

/**
 * @author jasonli Created on 2017/10/24.
 */

public class MessengerProcessor extends IMProcessor {

    private static final String TAG = MessengerProcessor.class.getSimpleName();

    public MessengerProcessor(Context context) {
        super(context);
    }

    @Override
    public void start() {
        AccessibilityManager.getInstance().register(MessengerScene.class, this);
        super.start();
    }

    @Override
    public void stop() {
        AccessibilityManager.getInstance().unregister(MessengerScene.class, this);
        super.stop();
    }

    @Override
    public String getPackage() {
        return AppConstants.PACKAGE_MESSENGER;
    }

    @Override
    public boolean onSceneShown(Scene sceneShown) {
        if (super.onSceneShown(sceneShown)) {
            return true;
        }
        String stage = getStage();
        MessengerScene scene = (MessengerScene) sceneShown;
        String target = mTarget;
        switch (stage) {
            case ProcessingStage.IMProcessStage.STAGE_ENTER_USER_NAME:
                if (scene.findUserItem(target) != null) {
                    if (scene.clickSendMessage(target)) {
                        updateStage(ProcessingStage.IMProcessStage.STAGE_DONE);
                    }
                }
                return true;
        }
        checkStage();
        return false;
    }
}
