package com.kikatech.go.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

public class AccessibilityWatchDog extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final AccessibilityEventDispatcher dispatcher = AccessibilityManager.getInstance().mRoot;
        if(dispatcher != null){
            final AccessibilityEventDispatcher handler = dispatcher.dispatchAccessibilityEvent(event);
            if(handler != null){
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
