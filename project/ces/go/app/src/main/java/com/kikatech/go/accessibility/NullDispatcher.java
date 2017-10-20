package com.kikatech.go.accessibility;

import android.view.accessibility.AccessibilityEvent;

/**
 * Created by tianli on 17-10-20.
 */

public class NullDispatcher extends EventDispatcher {

    @Override
    protected boolean onAccessibilityEvent(AccessibilityEvent event) {
        return false;
    }

}
