package com.kikatech.go.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import com.kikatech.go.util.LogUtil;

import java.util.List;

public class AccessibilityWatchDog extends AccessibilityService {

    private static final String TAG = "AccessibilityWatchDog";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
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
}
