package com.kikatech.go.accessibility.processor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.kikatech.go.accessibility.AccessibilityManager;
import com.kikatech.go.accessibility.im.apps.WeChatScene;
import com.kikatech.go.util.AppConstants;

/**
 * @author jasonli Created on 2017/10/26.
 */

public class WeChatProcessor extends IMProcessor {

    public WeChatProcessor(Context context) {
        super(context);
    }

    @Override
    public void start() {
        AccessibilityManager.getInstance().register(WeChatScene.class, this);
        super.start();
    }

    @Override
    public void stop() {
        AccessibilityManager.getInstance().unregister(WeChatScene.class, this);
        super.stop();
    }

    @Override
    void initActionRunnable() {
        mActionRunnable = new Runnable() {
            @Override
            public void run() {
                String stage = getCurrentStage();
                final WeChatScene scene = (WeChatScene) mScene;
                String target = mTarget;
                switch (stage) {
                    case ProcessingStage.IMProcessStage.STAGE_OPEN_SHARE_INTENT:
                        if (scene.enterSearchUserName(mTarget)) {
                            updateStage(ProcessingStage.IMProcessStage.STAGE_ENTER_USER_NAME);
                        }
                        break;
                    case ProcessingStage.IMProcessStage.STAGE_ENTER_USER_NAME:
                        if (scene.findUserItem(target) != null) {
                            if (scene.selectUserItem(target)) {
                                updateStage(ProcessingStage.IMProcessStage.STAGE_PICK_USER);
                            }
                        }
                        break;
                    case ProcessingStage.IMProcessStage.STAGE_PICK_USER:
                        if (scene.clickShareButton()) {
                            updateStage(ProcessingStage.IMProcessStage.STAGE_DONE);
                        }
                        break;
                }
                checkStage();
            }
        };
    }

    @Override
    protected Intent getShareIntent(String packageName, String message) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        ComponentName componentName = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI");
        intent.setComponent(componentName);
        intent.setType("speak/plain");
        intent.putExtra(Intent.EXTRA_TEXT, message);
        return intent;
    }

    @Override
    public String getPackage() {
        return AppConstants.PACKAGE_WECHAT;
    }

}