package com.kikatech.go.accessibility.scene;

import android.view.accessibility.AccessibilityEvent;

/**
 * Created by tianli on 17-10-21.
 */

public abstract class SceneRecognition {

    public abstract Scene recognize(AccessibilityEvent event);

}
