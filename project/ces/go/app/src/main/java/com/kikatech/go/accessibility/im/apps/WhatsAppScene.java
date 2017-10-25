package com.kikatech.go.accessibility.im.apps;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.kikatech.go.accessibility.AccessibilityUtils;
import com.kikatech.go.accessibility.im.IMScene;

/**
 * Created by tianli on 17-10-22.
 */

public class WhatsAppScene extends IMScene {

    private static final String VIEWID_BUTTON_SEARCH = "com.whatsapp:id/menuitem_search";
    private static final String VIEWID_EDITTEXT_SEARCH = "com.whatsapp:id/search_src_text";
    private static final String VIEWID_CONTACT_NAME = "com.whatsapp:id/contactpicker_row_name";
    private static final String VIEWID_BUTTON_SEND = "com.whatsapp:id/send";
    private static final String VIEWID_LAYOUT_INPUT_CHATROOM = "com.whatsapp:id/input_layout_content";

    public WhatsAppScene(AccessibilityEvent event, AccessibilityNodeInfo rootNodeInfo) {
        super(event, rootNodeInfo);
    }

    public boolean selectUserItem(String userName) {
        waitForView(1500);
        AccessibilityNodeInfo userItem = findUserItem(userName);
        if (userItem == null) {
            return false;
        }
        clickView(userItem.getParent());
        return true;
    }

    public boolean clickSendButton() {
        waitForView(1500);
        AccessibilityNodeInfo buttonSend = findNodeByViewId(mRootNodeInfo, VIEWID_BUTTON_SEND);
        if (buttonSend == null) {
            return false;
        }
        clickView(buttonSend);
        return true;
    }

    public boolean isInChatroomPage() {
        return findNodeByViewId(mRootNodeInfo, VIEWID_LAYOUT_INPUT_CHATROOM) != null;
    }

    @Override
    public AccessibilityNodeInfo findUserItem(String userName) {
        AccessibilityNodeInfo nodeInfo = mRootNodeInfo;
        return findNodeByTextAndClass(nodeInfo, userName, AccessibilityUtils.AccessibilityConstants.CLASSNAME_TEXT_VIEW);
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
