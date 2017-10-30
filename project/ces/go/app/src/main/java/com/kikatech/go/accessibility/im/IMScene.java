package com.kikatech.go.accessibility.im;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.kikatech.go.accessibility.scene.Scene;

/**
 * Created by tianli on 17-10-22.
 */

public class IMScene extends Scene {

    private static final String VIEWID_TO_BE_DEFINED = "TBD";

    public IMScene(AccessibilityEvent event, AccessibilityNodeInfo rootNodeInfo) {
        super(event, rootNodeInfo);
    }

    /**
     * Click button to search specific user name
     */
    public boolean clickSearchUserButton() {
        AccessibilityNodeInfo searchBtn = findNodeByViewId(mRootNodeInfo, getSearchButtonId());
        mRootNodeInfo.recycle();
        if(searchBtn == null) {
            return false;
        }
        clickView(searchBtn);
        return true;
    }

    /**
     * Fill up the user name to the search EditText
     */
    public boolean enterSearchUserName(String userName) {
        AccessibilityNodeInfo searchEditText = findNodeByViewId(mRootNodeInfo, getSearchEditTextId());
        mRootNodeInfo.recycle();
        if (searchEditText == null) {
            return false;
        }
        waitForView(1000);
        fillUpEditText(searchEditText, userName);
        return true;
    }

    protected String getSearchButtonId() {
        return VIEWID_TO_BE_DEFINED;
    }

    protected String getSearchEditTextId() {
        return VIEWID_TO_BE_DEFINED;
    }
}