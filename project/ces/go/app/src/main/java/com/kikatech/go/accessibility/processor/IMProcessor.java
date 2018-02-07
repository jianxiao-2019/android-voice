package com.kikatech.go.accessibility.processor;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.text.TextUtils;

import com.kikatech.go.accessibility.scene.Scene;
import com.kikatech.go.util.AppConstants;
import com.kikatech.go.util.BackgroundThread;
import com.kikatech.go.util.IntentUtil;
import com.kikatech.go.util.LogUtil;

/**
 * @author jasonli Created on 2017/10/25.
 */

public abstract class IMProcessor extends AccessibilityProcessor {

    private static final String TAG = "IMProcessor";
    private static final int TIMEOUT = 15000;

    private static final int STAGE_TIMEOUT = 4000;

    protected String mTarget;
    protected String mMessage;

    protected Runnable mActionRunnable = null;

    public IMProcessor(Context context) {
        super(context);
        updateStage(ProcessingStage.IMProcessStage.STAGE_INITIAL);
    }

    abstract public String getPackage();
    abstract void initActionRunnable();

    @Override
    protected void updateStage(String stage) {
        super.updateStage(stage);

        // there is time limitation on each stage
        BackgroundThread.getHandler().removeCallbacks(timeOutTask);
        BackgroundThread.postDelayed(timeOutTask, STAGE_TIMEOUT);
    }

    private Runnable timeOutTask = new Runnable() {
        @Override
        public void run() {
            onStageTimeout();
        }
    };

    @Override
    public void onStageTimeout() {
        if (isRunning()) {
            LogUtil.logw(TAG, "onStageTimeout, stage:" + mStage);
            stop();
        }
    }

    @Override
    public void start() {
        super.start();
        initActionRunnable();
        setRunning(true);
        new CountDownTimer(TIMEOUT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                if(isRunning()) {
                    // TODO timeout for this command!
                    stop();
                }
            }
        }.start();
        if (openShareIntent()) {
            updateStage(ProcessingStage.IMProcessStage.STAGE_OPEN_SHARE_INTENT);
        }
    }

    @Override
    public void stop() {
        super.stop();
        BackgroundThread.getHandler().removeCallbacks(mActionRunnable);

        setRunning(false);

        String stage = getCurrentStage();
        switch (stage) {
            case ProcessingStage.IMProcessStage.STAGE_INITIAL:
                if(LogUtil.DEBUG) LogUtil.logw(TAG,"Not install app: " + getPackage());
                break;
            case ProcessingStage.IMProcessStage.STAGE_OPEN_SHARE_INTENT:
                if(LogUtil.DEBUG) LogUtil.logw(TAG,"Cannot find search button");
                break;
            case ProcessingStage.IMProcessStage.STAGE_CLICK_SEARCH_BUTTON:
                if(LogUtil.DEBUG) LogUtil.logw(TAG,"Cannot enter search name");
                break;
            case ProcessingStage.IMProcessStage.STAGE_ENTER_USER_NAME:
                if(LogUtil.DEBUG) LogUtil.logw(TAG,"Cannot find user: " + mTarget);
                break;
            case ProcessingStage.IMProcessStage.STAGE_DONE:
                if(LogUtil.DEBUG) LogUtil.log(TAG,"Message sent.");
                break;
            default:
                if(LogUtil.DEBUG) LogUtil.logw(TAG,"Send message failed.");
                break;
        }

        if (mIProcessorFlow != null) {
            if (ProcessingStage.IMProcessStage.STAGE_DONE.equals(stage)) {
                mIProcessorFlow.onStop(IProcessorFlow.RESULT_SUCCESS);
            } else {
                mIProcessorFlow.onStop(IProcessorFlow.RESULT_FAILED);
            }
        }
    }

    /**
     * Open the send message page for the specific IM by using shared intent
     */
    private boolean openShareIntent() {
        String packageName = getPackage();
        if(TextUtils.isEmpty(mMessage)) {
            LogUtil.logwtf(TAG, "Empty sending message to " + packageName);
            return false;
        }
        LogUtil.logd(TAG, "Open intent = " + packageName);
        Intent sharingIntent = getShareIntent(packageName, mMessage);
        return IntentUtil.sendPendingIntent(mContext, sharingIntent);
    }

    protected Intent getShareIntent(String packageName, String message) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setPackage(packageName);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, message);
        return intent;
    }

    protected void checkStage() {
        if(ProcessingStage.IMProcessStage.STAGE_DONE.equals(mStage)) {
            stop();
        }
    }

    public void setTarget(String target) {
        mTarget = target;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    @Override
    public void onSceneShown(Scene sceneShown) {
        if (!isRunning()) {
            LogUtil.logwtf(TAG, "onSceneShown called while it has been stopped.");
            return;
        }

        if (mScene == null) {
            mScene = sceneShown;
        }
        mScene.updateNodes(sceneShown);

        BackgroundThread.post(mActionRunnable);
    }





    public static IMProcessor createIMProcessor(Context context, String packageName, String target, String message) {
        IMProcessor processor = null;
        switch (packageName) {
            case AppConstants.PACKAGE_MESSENGER:
                processor = new MessengerProcessor(context);
                break;
            case AppConstants.PACKAGE_WHATSAPP:
                processor = new WhatsAppProcessor(context);
                break;
            case AppConstants.PACKAGE_WECHAT:
                processor = new WeChatProcessor(context);
                break;
            default:
                break;
        }
        if (processor != null) {
            processor.setTarget(target);
            processor.setMessage(message);
        }
        return processor;
    }
}
