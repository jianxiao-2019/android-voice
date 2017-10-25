package com.kikatech.go.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class AccessibilityWatchDog extends AccessibilityService {

    private static final String TAG = AccessibilityWatchDog.class.getSimpleName();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final AccessibilityEventDispatcher dispatcher = AccessibilityManager.getInstance().mRoot;
        if (dispatcher != null) {
            AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
            if (rootNodeInfo != null) {
                rootNodeInfo.refresh();
            }
            final AccessibilityEventDispatcher handler = dispatcher.dispatchAccessibilityEvent(event, rootNodeInfo);
            if (handler != null) {
                AccessibilityManager.getInstance().onScene(handler.mScene);
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
