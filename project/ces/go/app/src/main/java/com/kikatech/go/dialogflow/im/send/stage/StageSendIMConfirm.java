package com.kikatech.go.dialogflow.im.send.stage;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.kikatech.go.accessibility.AccessibilityUtils;
import com.kikatech.go.dialogflow.im.IMContent;
import com.kikatech.go.message.processor.IMProcessor;
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

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    IMProcessor processor = IMProcessor.createIMProcessor(
                            ctx, imc.getIMAppPackageName(), imc.getSendTarget(), imc.getMessageBody());
                    if (processor != null) {
                        processor.start();
                    }
                }
            });
        }
    }
}