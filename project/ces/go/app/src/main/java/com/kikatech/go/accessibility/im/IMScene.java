package com.kikatech.go.accessibility.im;

import android.view.accessibility.AccessibilityNodeInfo;

import com.kikatech.go.accessibility.AccessibilityNodeWrapper;
import com.kikatech.go.accessibility.scene.Scene;

/**
 * Created by tianli on 17-10-22.
 */

public class IMScene extends Scene {

    protected static final String TAG = IMScene.class.getName();

    private static final String VIEWID_TO_BE_DEFINED = "TBD";

    public IMScene(AccessibilityNodeInfo rootNodeInfo) {
        super(rootNodeInfo);
    }

    /**
     * Click button to search specific user name
     */
    public boolean clickSearchUserButton() {
        waitForView(1500);
        AccessibilityNodeWrapper searchBtn = findNodeByViewId(getSearchButtonId());
        if (searchBtn == null) {
            return false;
        }
        searchBtn.click();
        return true;
    }

    /**
     * Fill up the user name to the search EditText
     */
    public boolean enterSearchUserName(String userName) {
        AccessibilityNodeWrapper searchEditText = findNodeByViewId(getSearchEditTextId());
        if (searchEditText == null) {
            return false;
        }
        waitForView(1000);
        searchEditText.fillUpText(userName);
        return true;
    }

    protected String getSearchButtonId() {
        return VIEWID_TO_BE_DEFINED;
    }

    protected String getSearchEditTextId() {
        return VIEWID_TO_BE_DEFINED;
    }
}