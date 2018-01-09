package com.kikatech.go.accessibility.im.apps;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.kikatech.go.accessibility.AccessibilityUtils;
import com.kikatech.go.accessibility.im.IMScene;
import com.kikatech.go.util.LogUtil;

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

    private static final String[] VIEWCD_BUTTON_SEARCHS = new String[] {
            "Search", "搜尋", "搜索"
    };
    private static final String[] VIEW_TEXT_NONFRIEND_HEADS = new String[] {
            "Discover", "More People", "探索", "更多用戶", "发现", "更多用户"
    };

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

    public boolean isTargetFriend() {
        AccessibilityNodeInfo listViewNode = findNodeByClass(mRootNodeInfo, AccessibilityUtils.AccessibilityConstants.CLASSNAME_LIST_VIEW);
        if (listViewNode != null) {
            AccessibilityNodeInfo firstNode = listViewNode.getChild(0);
            for (String headerText : VIEW_TEXT_NONFRIEND_HEADS) {
                AccessibilityNodeInfo nonFriendHeaderNode = findNodeByText(firstNode, headerText);
                if (nonFriendHeaderNode != null) {
                    if (LogUtil.DEBUG) LogUtil.log(TAG, "Target found is not a friend");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean clickSearchUserButton() {
        waitForView(1000);
        AccessibilityNodeInfo searchBtn;
        for (String searchCD : VIEWCD_BUTTON_SEARCHS) {
            searchBtn = findNodeByContentDescription(mRootNodeInfo, searchCD);
            if (searchBtn != null) {
                clickView(searchBtn);
                return true;
            }
        }
        if (LogUtil.DEBUG) LogUtil.logw(TAG, "Cannot find/click search button");
        return false;
    }

    @Override
    public boolean enterSearchUserName(String userName) {
        AccessibilityNodeInfo searchEditText = findNodeByClass(mRootNodeInfo, AccessibilityUtils.AccessibilityConstants.CLASSNAME_EDIT_TEXT);
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
