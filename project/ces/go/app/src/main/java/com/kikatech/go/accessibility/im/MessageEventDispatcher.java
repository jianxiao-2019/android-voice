package com.kikatech.go.accessibility.im;

import com.kikatech.go.accessibility.AccessibilityEventDispatcher;
import com.kikatech.go.accessibility.scene.SceneRecognition;

/**
 * Created by tianli on 17-10-20.
 */

public class MessageEventDispatcher extends AccessibilityEventDispatcher {

    @Override
    protected SceneRecognition onCreateRecognition() {
        return new IMSceneRecognition();
    }
}
