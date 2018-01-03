package com.kikatech.go.accessibility.im.apps;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.kikatech.go.accessibility.AccessibilityUtils;
import com.kikatech.go.accessibility.im.IMScene;

/**
 * Created by tianli on 17-10-22.
 */

public class MessengerScene extends IMScene {

    /*
     * Since Messenger v147.0.0.25.86, we could not retrieve ID from nodes
     */
    private static final String VIEWID_BUTTON_SEARCH = "com.facebook.orca:id/action_share_search";
    private static final String VIEWID_BUTTON_SEND = "com.facebook.orca:id/single_tap_send_button";
    private static final String VIEWID_EDITTEXT_SEARCH = "com.facebook.orca:id/search_src_text";

    private static final String VIEWCD_BUTTON_SEARCH = "Search";

    public MessengerScene(AccessibilityEvent event, AccessibilityNodeInfo rootNodeInfo) {
        super(event, rootNodeInfo);
    }

    /**
     * Click the send message button on the right of the user item
     */
    public boolean clickSendMessage(String userName) {
        AccessibilityNodeInfo userItemNode = findUserItem(userName);
        AccessibilityNodeInfo sendBtn = findNodeByClass(userItemNode, AccessibilityUtils.AccessibilityConstants.CLASSNAME_BUTTON);
        userItemNode.recycle();
        if (sendBtn != null) {
            clickView(sendBtn);
            waitForView(1500);
            return true;
        }
        return false;
    }

    public AccessibilityNodeInfo findUserItem(String userName) {
        waitForView(1500);
        AccessibilityNodeInfo nodeInfo = mRootNodeInfo;
        AccessibilityNodeInfo targetNameTextNode = findNodeByTextAndClass(nodeInfo, userName, AccessibilityUtils.AccessibilityConstants.CLASSNAME_VIEW);
        if (targetNameTextNode != null) {
            return targetNameTextNode.getParent();
        }
        return null;
    }

    @Override
    public boolean clickSearchUserButton() {
        AccessibilityNodeInfo searchBtn = findNodeByContentDescription(mRootNodeInfo, VIEWCD_BUTTON_SEARCH);
        mRootNodeInfo.recycle();
        if(searchBtn == null) {
            return false;
        }
        clickView(searchBtn);
        return true;
    }

    @Override
    public boolean enterSearchUserName(String userName) {
        AccessibilityNodeInfo searchEditText = findNodeByClass(mRootNodeInfo, AccessibilityUtils.AccessibilityConstants.CLASSNAME_EDIT_TEXT);
        mRootNodeInfo.recycle();
        if (searchEditText == null) {
            return false;
        }
        waitForView(1000);
        fillUpEditText(searchEditText, userName);
        return true;
    }

    @Override
    protected String getSearchButtonId() {
        return VIEWID_BUTTON_SEARCH;
    }

    @Override
    protected String getSearchEditTextId() {
        return VIEWID_EDITTEXT_SEARCH;
    }

}
