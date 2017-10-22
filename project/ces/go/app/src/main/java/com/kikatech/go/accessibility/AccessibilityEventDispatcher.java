package com.kikatech.go.accessibility;

import android.view.accessibility.AccessibilityEvent;

import com.kikatech.go.accessibility.scene.Scene;
import com.kikatech.go.accessibility.scene.SceneRecognition;

/**
 * Created by tianli on 17-10-20.
 */

public abstract class AccessibilityEventDispatcher {


    final public static AccessibilityEventDispatcher NULL = new NullDispatcher();

    Scene mScene = null;

    AccessibilityEventDispatcher mChain = null;

    public AccessibilityEventDispatcher(){
    }

    final public AccessibilityEventDispatcher chain(AccessibilityEventDispatcher dispatcher){
        mChain = dispatcher;
        return this;
    }

    final public AccessibilityEventDispatcher dispatchAccessibilityEvent(AccessibilityEvent event){
        AccessibilityEventDispatcher dispatcher = null;
        if(mChain != null){
            dispatcher = mChain.dispatchAccessibilityEvent(event);
        }
        if(dispatcher == null){
            SceneRecognition r = onCreateRecognition();
            if(r != null){
                mScene = r.recognize(event);
                return this;
            }
        }
        if(dispatcher == null){
            dispatcher = NULL;
        }
        return dispatcher;
    }

    protected abstract SceneRecognition onCreateRecognition();

    static private class NullDispatcher extends AccessibilityEventDispatcher {

        @Override
        protected SceneRecognition onCreateRecognition() {
            return null;
        }
    }

}
