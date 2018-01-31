package com.kikatech.go.accessibility.im.apps;

import android.view.accessibility.AccessibilityNodeInfo;

import com.kikatech.go.accessibility.AccessibilityNodeWrapper;
import com.kikatech.go.accessibility.AccessibilityUtils;
import com.kikatech.go.accessibility.im.IMScene;

/**
 * @author jasonli Created on 2017/10/26.
 */

public class WeChatScene extends IMScene {

    /**
     * Each build of WeChat would change id
     */
    // private static final String VIEWID_EDITTEXT_SEARCH = "com.tencent.mm:id/alt";
    // private static final String VIEWID_TEXT_TARGET_NAME = "com.tencent.mm:id/jy";
    // private static final String VIEWID_BUTTON_SHARE = "com.tencent.mm:id/aga";

    private static final String[] VIEW_TEXT_SHARE_BUTTONS = new String[] {
            "Share", "分享"
    };

    public WeChatScene(AccessibilityNodeInfo rootNodeInfo) {
        super(rootNodeInfo);
    }





    @Override
    public boolean enterSearchUserName(String userName) {
        AccessibilityNodeWrapper searchEditText = findNodeByClass(AccessibilityUtils.AccessibilityConstants.CLASSNAME_EDIT_TEXT);
        if (searchEditText == null) {
            return false;
        }
        waitForView(1000);
        searchEditText.fillUpText(userName);
        return true;
    }

    public AccessibilityNodeWrapper findUserItem(String userName) {
        waitForView(1000);
        return findNodeByTextAndClass(userName, AccessibilityUtils.AccessibilityConstants.CLASSNAME_TEXT_VIEW);
    }

    public boolean selectUserItem(String userName) {
        waitForView(500);
        AccessibilityNodeWrapper userItem = findUserItem(userName).getParent();
        if (userItem == null) {
            return false;
        }
        userItem.click();
        return true;
    }

    public boolean clickShareButton() {
        waitForView(1500);
        for (String shareText : VIEW_TEXT_SHARE_BUTTONS) {
            AccessibilityNodeWrapper shareButton = findNodeByTextAndClass(shareText, AccessibilityUtils.AccessibilityConstants.CLASSNAME_BUTTON);
            if (shareButton != null) {
                shareButton.click();
                return true;
            }
        }
        return false;
    }

}
