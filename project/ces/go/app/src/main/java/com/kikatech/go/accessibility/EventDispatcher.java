package com.kikatech.go.accessibility;

import android.view.accessibility.AccessibilityEvent;

/**
 * Created by tianli on 17-10-20.
 */

public abstract class EventDispatcher {

    private EventDispatcher mChild;

    public EventDispatcher(){
    }

    public EventDispatcher(EventDispatcher dispatcher){
        mChild = dispatcher;
    }

    final public boolean dispatchAccessibilityEvent(AccessibilityEvent event){
        if(mChild != null && mChild.dispatchAccessibilityEvent(event)){
            return true;
        }
        if(onAccessibilityEvent(event)){
            return true;
        }
        return false;
    }

    protected boolean onAccessibilityEvent(AccessibilityEvent event){
        return false;
    }
}
