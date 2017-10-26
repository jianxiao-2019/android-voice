package com.kikatech.go.accessibility.im.apps;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.kikatech.go.accessibility.im.IMScene;

/**
 * @author jasonli Created on 2017/10/26.
 */

public class WeChatScene extends IMScene {

    private static final String VIEWID_EDITTEXT_SEARCH = "com.tencent.mm:id/alt";
    private static final String VIEWID_TEXT_TARGET_NAME = "com.tencent.mm:id/jy";
    private static final String VIEWID_BUTTON_SHARE = "com.tencent.mm:id/aga";

    public WeChatScene(AccessibilityEvent event, AccessibilityNodeInfo rootNodeInfo) {
        super(event, rootNodeInfo);
    }

    public AccessibilityNodeInfo findUserItem(String userName) {
        waitForView(1000);
        return findNodeByTextAndId(mRootNodeInfo, userName, VIEWID_TEXT_TARGET_NAME);
    }

    public boolean selectUserItem(String userName) {
        waitForView(500);
        AccessibilityNodeInfo userItem = findUserItem(userName);
        if (userItem == null) {
            return false;
        }
        clickView(userItem.getParent());
        return true;
    }

    public boolean clickShareButton() {
        waitForView(1500);
        AccessibilityNodeInfo shareButton = findNodeByViewId(mRootNodeInfo, VIEWID_BUTTON_SHARE);
        if (shareButton == null) {
            return false;
        }
        clickView(shareButton);
        return true;
    }

    @Override
    protected String getSearchButtonId() {
        return VIEWID_EDITTEXT_SEARCH;
    }

    @Override
    protected String getSearchEditTextId() {
        return VIEWID_EDITTEXT_SEARCH;
    }
}
