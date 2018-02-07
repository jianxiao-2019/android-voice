package com.kikatech.go.dialogflow.im.send.stage;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.kikatech.go.R;
import com.kikatech.go.accessibility.AccessibilityManager;
import com.kikatech.go.accessibility.AccessibilityUtils;
import com.kikatech.go.accessibility.im.MessageEventDispatcher;
import com.kikatech.go.accessibility.processor.AccessibilityProcessor;
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.im.send.IMContent;
import com.kikatech.go.dialogflow.navigation.NaviSceneUtil;
import com.kikatech.go.accessibility.processor.IMProcessor;
import com.kikatech.go.services.DialogFlowForegroundService;
import com.kikatech.go.util.IntentUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;

/**
 * Created by brad_chang on 2017/11/24.
 */

public class StageSendIMConfirm extends BaseSendIMStage {
    StageSendIMConfirm(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    public void action() {
        final Context ctx = mSceneBase.getContext();
        final IMContent imc = getIMContent();

        if (!AccessibilityUtils.isSettingsOn(ctx)) {
            if (LogUtil.DEBUG) {
                final String err = "Error : cannot get Accessibility permission";
                LogUtil.log(TAG, err);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ctx, err, Toast.LENGTH_LONG).show();
                    }
                });
            }
        } else {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "Send IM !!!!" + getIMContent().toString());
            }

            final Handler uiHandler = new Handler(Looper.getMainLooper());
            uiHandler.post(new Runnable() {
                @Override
                public void run() {

                    final MessageEventDispatcher messageEventDispatcher = new MessageEventDispatcher();
                    AccessibilityManager.getInstance().registerDispatcher(messageEventDispatcher);

                    final boolean isAppForeground = DialogFlowForegroundService.isAppForeground();
                    IMProcessor processor = IMProcessor.createIMProcessor(ctx, imc.getIMAppPackageName(), imc.getSendTarget(), imc.getMessageBody(true));
                    processor.registerCallback(new AccessibilityProcessor.IProcessorFlow() {
                        @Override
                        public void onStart() {
                            if (LogUtil.DEBUG) {
                                LogUtil.log(TAG, "Start ...");
                            }
                        }

                        @Override
                        public void onStageChanged(String stage) {

                        }

                        @Override
                        public void onStop(int result) {
                            if (LogUtil.DEBUG) {
                                LogUtil.log(TAG, "End ...");
                            }
                            boolean msgSentSuccess = result == AccessibilityProcessor.IProcessorFlow.RESULT_SUCCESS;

                            AccessibilityManager.getInstance().unregisterDispatcher(messageEventDispatcher);
                            boolean succeed, openKikaGo;
                            if (NaviSceneUtil.isNavigating() && !isAppForeground) {
                                succeed = true;
                                openKikaGo = false;
                            } else {
                                succeed = IntentUtil.openKikaGo(ctx);
                                openKikaGo = succeed;
                            }
                            if (LogUtil.DEBUG) {
                                LogUtil.logd(TAG, String.format("isNavigating: %1$s, isAppForeground: %2$s, succeed: %3$s", NaviSceneUtil.isNavigating(), isAppForeground, succeed));
                            }
                            if (succeed) {
                                Bundle args = new Bundle();
                                args.putString(SceneUtil.EXTRA_EVENT, SceneUtil.EVENT_DISPLAY_MSG_SENT);
                                args.putInt(SceneUtil.EXTRA_ALERT, msgSentSuccess ? R.raw.alert_succeed : R.raw.alert_error);
                                args.putBoolean(SceneUtil.EXTRA_SEND_SUCCESS, msgSentSuccess);
                                args.putBoolean(SceneUtil.EXTRA_OPEN_KIKA_GO, openKikaGo);
                                send(args);
                                uiHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        exitScene();
                                    }
                                }, SceneUtil.MSG_SENT_PAGE_DELAY);
                            }
                        }
                    });
                    if (processor != null) {
                        processor.start();
                    }
                }
            });
        }
    }
}