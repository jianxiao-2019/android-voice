package com.kikatech.go.accessibility.processor;

import android.content.Context;
import android.widget.Toast;

import com.kikatech.go.accessibility.scene.Scene;
import com.kikatech.go.services.DialogFlowForegroundService;
import com.kikatech.go.util.BackgroundThread;
import com.kikatech.go.util.IntentUtil;
import com.kikatech.go.util.LogUtil;

/**
 * @author jasonli Created on 2017/10/23.
 */
// 处理Accessibility执行事件的流程
public abstract class AccessibilityProcessor {

    private static final String TAG = "AccessibilityProcessor";

    protected Context mContext;
    protected boolean mRunning = false;
    protected String mStage;

    protected Scene mScene = null;

    protected IProcessorFlow mIProcessorFlow = null;

    private static final int STAGE_TIMEOUT = 4000;

    public AccessibilityProcessor(Context context) {
        mContext = context;
    }

    abstract public void onSceneShown(Scene scene);

    abstract void onStageTimeout();

    protected void start() {
        DialogFlowForegroundService.processAccessibilityStarted();
        if (mIProcessorFlow != null) {
            mIProcessorFlow.onStart();
        }
    }

    protected void stop() {
        DialogFlowForegroundService.processAccessibilityStopped();
    }

    protected synchronized void setRunning(boolean running) {
        mRunning = running;
    }

    public synchronized boolean isRunning() {
        return mRunning;
    }

    public void registerCallback(IProcessorFlow callback) {
        mIProcessorFlow = callback;
    }

    protected void updateStage(String stage) {
        LogUtil.logw(TAG, "Update Processing Stage: " + stage);
        mStage = stage;

        BackgroundThread.getHandler().removeCallbacks(timeOutTask);
        BackgroundThread.postDelayed(timeOutTask, STAGE_TIMEOUT);
    }

    private Runnable timeOutTask = new Runnable() {
        @Override
        public void run() {
            onStageTimeout();
        }
    };

    protected String getCurrentStage() {
        return mStage;
    }

    protected void returnToApp() {
        IntentUtil.openKikaGo(mContext);
    }

    protected void showToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }


    public interface IProcessorFlow {

        int RESULT_SUCCESS  = 1;
        int RESULT_FAILED   = 2;

        void onStart();
        void onStop(int result);
    }

}
