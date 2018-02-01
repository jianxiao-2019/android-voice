package com.kikatech.go.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import com.kikatech.go.util.LogUtil;

import java.util.List;

public class AccessibilityWatchDog extends AccessibilityService {

    private static final String TAG = "AccessibilityWatchDog";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Record current top activity
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null && event.getClassName() != null) {
                ComponentName componentName = new ComponentName(
                        event.getPackageName().toString(),
                        event.getClassName().toString()
                );

                ActivityInfo activityInfo = tryGetActivity(componentName);
                boolean isActivity = activityInfo != null;
                if (isActivity) {
                    AccessibilityManager.getInstance().recordActivity(componentName.getPackageName(), componentName.getClassName());
                }
            }
        }

        final AccessibilityEventDispatcher dispatcher = AccessibilityManager.getInstance().mRoot;
        if (dispatcher != null) {
            AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
            if (rootNodeInfo == null) {
                List<AccessibilityWindowInfo> windowInfos = getWindows();
                for (AccessibilityWindowInfo windowInfo : windowInfos) {
                    rootNodeInfo = windowInfo.getRoot();
                    if (rootNodeInfo != null) break;
                }
            }
            if (rootNodeInfo != null) {
                rootNodeInfo.refresh();
                final AccessibilityEventDispatcher handler = dispatcher.dispatchAccessibilityEvent(event, rootNodeInfo);
                if (handler != null) {
                    AccessibilityManager.getInstance().onScene(handler.mScene);
                }
            } else {
                if (LogUtil.DEBUG) LogUtil.logd(TAG, "Catch Accessibility event but got null root node");
            }
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
