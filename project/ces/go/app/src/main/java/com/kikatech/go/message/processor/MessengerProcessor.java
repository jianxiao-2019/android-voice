package com.kikatech.go.message.processor;

import android.content.Context;

import com.kikatech.go.accessibility.AccessibilityManager;
import com.kikatech.go.accessibility.im.apps.MessengerScene;
import com.kikatech.go.util.AppConstants;

/**
 * @author jasonli Created on 2017/10/24.
 */

public class MessengerProcessor extends IMProcessor {

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
    void initActionRunnable() {
        mActionRunnable = new Runnable() {
            @Override
            public void run() {
                final String stage = getStage();
                final String target = mTarget;
                MessengerScene scene = (MessengerScene) mScene;
                try {
                    switch (stage) {
                        case ProcessingStage.IMProcessStage.STAGE_OPEN_SHARE_INTENT:
                            if (scene.clickSearchUserButton()) {
                                updateStage(ProcessingStage.IMProcessStage.STAGE_CLICK_SEARCH_BUTTON);
                            }
                            break;
                        case ProcessingStage.IMProcessStage.STAGE_CLICK_SEARCH_BUTTON:
                            if (scene.enterSearchUserName(target)) {
                                updateStage(ProcessingStage.IMProcessStage.STAGE_ENTER_USER_NAME);
                            }
                            break;
                        case ProcessingStage.IMProcessStage.STAGE_ENTER_USER_NAME:
                            if (scene.findUserItem(target) != null) {
                                if (scene.isTargetFriend()) {
                                    if (scene.clickSendMessage(target)) {
                                        updateStage(ProcessingStage.IMProcessStage.STAGE_DONE);
                                    }
                                }
                            }
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                checkStage();
            }
        };
    }

}
