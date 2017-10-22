package com.kikatech.go.accessibility.scene;

import android.view.accessibility.AccessibilityEvent;

/**
 * Created by tianli on 17-10-20.
 */

public class Scene {

    protected AccessibilityEvent mEvent;

    public Scene(AccessibilityEvent event){
        mEvent = event;
    }
}
