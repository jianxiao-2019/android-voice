package com.kikatech.go.dialogflow.im.send.stage;

import android.app.PendingIntent;
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
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.im.IMContent;
import com.kikatech.go.message.processor.IMProcessor;
import com.kikatech.go.ui.KikaAlphaUiActivity;
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

    private boolean backToMainActivity(Context ctx) {
        android.content.Intent intent = new android.content.Intent(ctx, KikaAlphaUiActivity.class);
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
        return IntentUtil.sendPendingIntent(ctx, intent);
    }

    @Override
    public void doAction() {
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
            if (LogUtil.DEBUG) LogUtil.log(TAG, "Send IM !!!!" + getIMContent().toString());

            final Handler uiHandler = new Handler(Looper.getMainLooper());
            uiHandler.post(new Runnable() {
                @Override
                public void run() {

                    final MessageEventDispatcher messageEventDispatcher = new MessageEventDispatcher();
                    AccessibilityManager.getInstance().registerDispatcher(messageEventDispatcher);

                    IMProcessor processor = IMProcessor.createIMProcessor(
                            ctx, imc.getIMAppPackageName(), imc.getSendTarget(), imc.getMessageBody(true)).registerCallback(new IMProcessor.IIMProcessorFlow() {
                        @Override
                        public void onStart() {
                            if (LogUtil.DEBUG) LogUtil.log(TAG, "Start ...");
                        }

                        @Override
                        public void onStop() {
                            if (LogUtil.DEBUG) LogUtil.log(TAG, "End ...");
                            AccessibilityManager.getInstance().unregisterDispatcher(messageEventDispatcher);
                            boolean succeed = backToMainActivity(ctx);

                            if (succeed) {
                                Bundle args = new Bundle();
                                args.putString(SceneUtil.EXTRA_EVENT, SceneUtil.EVENT_DISPLAY_MSG_SENT);
                                args.putInt(SceneUtil.EXTRA_ALERT, R.raw.alert_succeed);
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