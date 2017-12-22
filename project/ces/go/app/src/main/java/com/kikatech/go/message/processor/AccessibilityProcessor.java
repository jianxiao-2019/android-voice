package com.kikatech.go.message.processor;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.kikatech.go.accessibility.scene.Scene;
import com.kikatech.go.ui.KikaAlphaUiActivity;
import com.kikatech.go.ui.KikaGoActivity;
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

    public AccessibilityProcessor(Context context) {
        mContext = context;
    }

    abstract public void start();
    abstract public void stop();
    abstract public boolean onSceneShown(Scene scene);

    protected void setRunning(boolean running) {
        mRunning = running;
    }

    public boolean isRunning() {
        return mRunning;
    }

    protected void updateStage(String stage) {
        LogUtil.logw(TAG, "Update Processing Stage: " + stage);
        mStage = stage;
    }

    protected String getStage() {
        return mStage;
    }

    protected void returnToApp() {
        IntentUtil.openKikaGo(mContext);
    }

    protected void showToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

}
