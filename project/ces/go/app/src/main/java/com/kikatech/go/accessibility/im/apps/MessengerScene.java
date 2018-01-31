package com.kikatech.go.accessibility.im.apps;

import android.view.accessibility.AccessibilityNodeInfo;

import com.kikatech.go.accessibility.AccessibilityNodeWrapper;
import com.kikatech.go.accessibility.AccessibilityUtils;
import com.kikatech.go.accessibility.scene.Scene;
import com.kikatech.go.util.LogUtil;

/**
 * Created by jason on 2018/1/30.
 */

public class MessengerScene extends Scene {

    private static final String TAG = MessengerScene.class.getName();

    /*
     * Since Messenger v147.0.0.25.86, we could not retrieve ID from nodes
     * private static final String VIEWID_BUTTON_SEARCH = "com.facebook.orca:id/action_share_search";
     * private static final String VIEWID_BUTTON_SEND = "com.facebook.orca:id/single_tap_send_button";
     * private static final String VIEWID_EDITTEXT_SEARCH = "com.facebook.orca:id/search_src_text";
     */

    private static final String[] VIEWCD_BUTTON_SEARCHS = new String[] {
            "Search", "搜尋", "搜索"
    };
    private static final String[] VIEW_TEXT_NONFRIEND_HEADS = new String[] {
            "Discover", "More People", "探索", "更多用戶", "发现", "更多用户"
    };

    public MessengerScene(AccessibilityNodeInfo rootNodeInfo) {
        super(rootNodeInfo);
    }





    /**
     * Click button to search specific user name
     */
    public synchronized boolean clickSearchUserButton() {
        waitForView(1000);
        AccessibilityNodeWrapper searchBtn;
        for (String searchCD : VIEWCD_BUTTON_SEARCHS) {
            searchBtn = findNodeByContentDescription(searchCD);
            if (searchBtn != null) {
                searchBtn.click();
                return true;
            }
        }
        if (LogUtil.DEBUG) LogUtil.logw(TAG, "Cannot find/click search button");
        return false;
    }

    public synchronized boolean enterSearchUserName(String userName) {
        AccessibilityNodeWrapper searchEditText = findNodeByClass(AccessibilityUtils.AccessibilityConstants.CLASSNAME_EDIT_TEXT);
        if (searchEditText == null) {
            return false;
        }
        waitForView(1000);
        searchEditText.fillUpText(userName);
        return true;
    }

    /**
     * Click the send message button on the right of the user item
     */
    public synchronized boolean clickSendMessage(String userName) {
        AccessibilityNodeWrapper userItemNode = findUserItem(userName);
        if (userItemNode != null) {
            AccessibilityNodeWrapper userItem = userItemNode.getParent();
            AccessibilityNodeWrapper sendBtn = userItem.findNodeByClass(AccessibilityUtils.AccessibilityConstants.CLASSNAME_BUTTON);
            userItemNode.recycle();
            if (sendBtn != null) {
                sendBtn.click();
                waitForView(1500);
                return true;
            }
        }
        return false;
    }

    public AccessibilityNodeWrapper findUserItem(String userName) {
        waitForView(1500);
        return findNodeByTextAndClass(userName, AccessibilityUtils.AccessibilityConstants.CLASSNAME_VIEW);
    }

    public boolean isTargetFriend() {
        AccessibilityNodeWrapper listViewNode = findNodeByClass(AccessibilityUtils.AccessibilityConstants.CLASSNAME_LIST_VIEW);
        if (listViewNode != null) {
            AccessibilityNodeWrapper firstNode = listViewNode.getFirstChild();
            for (String headerText : VIEW_TEXT_NONFRIEND_HEADS) {
                AccessibilityNodeWrapper nonFriendHeaderNode = firstNode.findNodeByText(headerText);
                if (nonFriendHeaderNode != null) {
                    if (LogUtil.DEBUG) LogUtil.log(TAG, "Target found is not a friend");
                    return false;
                }
            }
        }
        return true;
    }

}
