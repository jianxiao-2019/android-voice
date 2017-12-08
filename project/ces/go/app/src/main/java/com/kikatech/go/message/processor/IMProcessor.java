package com.kikatech.go.message.processor;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.text.TextUtils;

import com.kikatech.go.accessibility.im.IMScene;
import com.kikatech.go.accessibility.scene.Scene;
import com.kikatech.go.util.AppConstants;
import com.kikatech.go.util.IntentUtil;
import com.kikatech.go.util.LogUtil;

/**
 * @author jasonli Created on 2017/10/25.
 */

public abstract class IMProcessor extends AccessibilityProcessor {

    private static final String TAG = "IMProcessor";
    private static final int TIMEOUT = 15000;

    protected String mTarget;
    protected String mMessage;

    public IMProcessor(Context context) {
        super(context);
        updateStage(ProcessingStage.IMProcessStage.STAGE_INITIAL);
    }

    abstract public String getPackage();

    public interface IIMProcessorFlow {
        void onStart();

        void onStop();
    }

    private IIMProcessorFlow mIIMProcessorFlow;
    public IMProcessor registerCallback(IIMProcessorFlow callback) {
        mIIMProcessorFlow = callback;
        return this;
    }

    @Override
    public void start() {
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

        if(mIIMProcessorFlow != null) {
            mIIMProcessorFlow.onStart();
        }
    }

    @Override
    public void stop() {
        setRunning(false);

        String stage = getStage();
        if(LogUtil.DEBUG) LogUtil.logw(TAG, "Send message failed with stage: " + stage);
        switch (stage) {
            case ProcessingStage.IMProcessStage.STAGE_INITIAL:
                if(LogUtil.DEBUG) showToast("Not install app: " + getPackage());
                break;
            case ProcessingStage.IMProcessStage.STAGE_OPEN_SHARE_INTENT:
                if(LogUtil.DEBUG) showToast("Cannot find search button");
                break;
            case ProcessingStage.IMProcessStage.STAGE_CLICK_SEARCH_BUTTON:
                if(LogUtil.DEBUG) showToast("Cannot enter search name");
                break;
            case ProcessingStage.IMProcessStage.STAGE_ENTER_USER_NAME:
                if(LogUtil.DEBUG) showToast("Cannot find user: " + mTarget);
                break;
            case ProcessingStage.IMProcessStage.STAGE_DONE:
                if(LogUtil.DEBUG) showToast("Message sent.");
                break;
            default:
                if(LogUtil.DEBUG) showToast("Send message failed.");
                break;
        }

        if(mIIMProcessorFlow != null) {
            mIIMProcessorFlow.onStop();
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
    public boolean onSceneShown(Scene sceneShown) {
        if (!isRunning()) {
            LogUtil.logwtf(TAG, "onSceneShown called while it has been stopped.");
            return true;
        }
        String stage = getStage();
        IMScene scene = (IMScene) sceneShown;
        String target = mTarget;
        try {
            switch (stage) {
                case ProcessingStage.IMProcessStage.STAGE_OPEN_SHARE_INTENT:
                    if (scene.clickSearchUserButton()) {
                        updateStage(ProcessingStage.IMProcessStage.STAGE_CLICK_SEARCH_BUTTON);
                    }
                    return true;
                case ProcessingStage.IMProcessStage.STAGE_CLICK_SEARCH_BUTTON:
                    if (scene.enterSearchUserName(target)) {
                        updateStage(ProcessingStage.IMProcessStage.STAGE_ENTER_USER_NAME);
                    }
                    return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        checkStage();
        return false;
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
        if(processor != null) {
            processor.setTarget(target);
            processor.setMessage(message);
        }
        return processor;
    }
}