package com.kikatech.go.accessibility.im;

import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.kikatech.go.accessibility.scene.Scene;

/**
 * Created by tianli on 17-10-22.
 */

public class IMScene extends Scene{

    public IMScene(AccessibilityEvent event) {
        super(event);
    }

    public void searchUser(String user){
        AccessibilityNodeInfo node = mEvent.getSource();
        Bundle bundle = new Bundle();
        bundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, user);
        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle);
    }

    public void send(){
    }
}
