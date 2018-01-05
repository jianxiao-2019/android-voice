package com.kikatech.go.accessibility.scene;

import android.os.Build;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.kikatech.go.accessibility.AccessibilityUtils;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tianli on 17-10-20.
 */

public class Scene {

    private static final String TAG = "Access.Scene";

    protected AccessibilityEvent mEvent;
    protected AccessibilityNodeInfo mRootNodeInfo;

    public Scene(AccessibilityEvent event, AccessibilityNodeInfo rootNodeInfo) {
        mEvent = event;
        mRootNodeInfo = rootNodeInfo;
    }

    protected AccessibilityNodeInfo findNodeByViewId(AccessibilityNodeInfo parentNode, String viewId) {
        List<AccessibilityNodeInfo> results = parentNode.findAccessibilityNodeInfosByViewId(viewId);
        if (results.size() > 0) {
            return results.get(0);
        }
        if (LogUtil.DEBUG) LogUtil.logwtf(TAG, "Could not findNodeByViewId: " + viewId);
        return null;
    }

    protected AccessibilityNodeInfo findNodeByText(AccessibilityNodeInfo parentNode, String text) {
        List<AccessibilityNodeInfo> results = findNodesByText(parentNode, text);
        if (results.size() > 0) {
            return results.get(0);
        }
        if (LogUtil.DEBUG) LogUtil.logwtf(TAG, "Could not findNodeByText: " + text);
        return null;
    }

    protected List<AccessibilityNodeInfo> findNodesByText(AccessibilityNodeInfo parentNode, String text) {
        List<AccessibilityNodeInfo> results = parentNode.findAccessibilityNodeInfosByText(text);
        if (results.size() == 0) {
            List<AccessibilityNodeInfo> allNodes = getAllNodes(parentNode);
            results = new ArrayList<>();
            for (AccessibilityNodeInfo nodeInfo : allNodes) {
                if (StringUtil.equalsIgnoreCase(nodeInfo.getText(), text) ||
                        StringUtil.equalsIgnoreCase(nodeInfo.getContentDescription(), text)) {
                    results.add(nodeInfo);
                }
            }
        }
        return results;
    }

    protected AccessibilityNodeInfo findNodeByTextAndClass(AccessibilityNodeInfo parentNode, String text, String className) {
        List<AccessibilityNodeInfo> results = parentNode.findAccessibilityNodeInfosByText(text);
        for (AccessibilityNodeInfo nodeInfo : results) {
            if (StringUtil.equals(nodeInfo.getClassName(), className)) {
                return nodeInfo;
            }
        }
        if (LogUtil.DEBUG) LogUtil.logwtf(TAG, "Could not findNodeByTextAndClass: " + text + " | " + className);
        return null;
    }

    protected AccessibilityNodeInfo findNodeByTextAndId(AccessibilityNodeInfo parentNode, String text, String viewId) {
        if (text == null) {
            return null;
        }
        List<AccessibilityNodeInfo> results = parentNode.findAccessibilityNodeInfosByViewId(viewId);
        for (AccessibilityNodeInfo nodeInfo : results) {
            if (text.equalsIgnoreCase(nodeInfo.getText().toString())) {
                return nodeInfo;
            }
        }
        if (LogUtil.DEBUG) LogUtil.logwtf(TAG, "Could not findNodeByTextAndId: text:" + text + " | id:" + viewId);
        return null;
    }

    protected AccessibilityNodeInfo findNodeByContentDescription(AccessibilityNodeInfo parentNode, String description) {
        List<AccessibilityNodeInfo> results = getAllNodes(parentNode);
        for(AccessibilityNodeInfo node : results) {
            CharSequence contentDescription = node.getContentDescription();
            if(contentDescription != null && StringUtil.equals(contentDescription.toString(), description)) {
                return node;
            }
        }
        if (LogUtil.DEBUG) LogUtil.logwtf(TAG, "Could not findNodeByContentDescription: " + description);
        return null;
    }

    protected AccessibilityNodeInfo findNodeByClass(AccessibilityNodeInfo parentNode, String className) {
        List<AccessibilityNodeInfo> results = getAllNodes(parentNode);
        for(AccessibilityNodeInfo node : results) {
            if(node != null && StringUtil.equals(node.getClassName(), className)) {
                return node;
            }
        }
        if (LogUtil.DEBUG) LogUtil.logwtf(TAG, "Could not findNodeByClass: " + className);
        return null;
    }

    protected List<AccessibilityNodeInfo> getAllNodes(AccessibilityNodeInfo parentNode) {
        List<AccessibilityNodeInfo> allNodes = new ArrayList<>();
        for(int i = 0; i < parentNode.getChildCount(); i++) {
            AccessibilityNodeInfo node = parentNode.getChild(i);
            if(node != null) {
                if(node.getChildCount() > 0) {
                    allNodes.addAll(getAllNodes(node));
                }
                allNodes.add(node);
            }
        }
        return allNodes;
    }

    protected void clickView(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            if (LogUtil.DEBUG) LogUtil.logwtf(TAG, "Cannot click null view");
            return;
        }

        boolean clicked = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        nodeInfo.recycle();
        if (!clicked && LogUtil.DEBUG) LogUtil.logwtf(TAG, "Failed to click view");
        return;
    }

    protected void longClickView(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            if (LogUtil.DEBUG) LogUtil.logwtf(TAG, "Cannot long click null view");
            return;
        }

        boolean longClicked = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
        nodeInfo.recycle();
        if (!longClicked && LogUtil.DEBUG) LogUtil.logwtf(TAG, "Failed to long click view");
        return;
    }

    protected void selectView(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            if (LogUtil.DEBUG) LogUtil.logwtf(TAG, "Cannot select null view");
            return;
        }

        boolean selected = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SELECT);
        nodeInfo.recycle();
        if (!selected && LogUtil.DEBUG) LogUtil.logwtf(TAG, "Failed to select view");
        return;
    }

    protected void fillUpEditText(AccessibilityNodeInfo nodeInfo, String text) {
        if (nodeInfo == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        } else {
            // TODO to be confirmed for API level < 21
//            ClipData data = ClipData.newPlainText("auto_fill_text", speak);
//            ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
//            clipboardManager.setPrimaryClip(data);
//            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS); // 获取焦点
//            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE); // 执行粘贴
        }

        nodeInfo.recycle();
    }

    protected void waitForView(long millisecond) {
        try {
            Thread.sleep(millisecond);
        } catch (InterruptedException ignore) {
        }
    }

    public void printView() {
        AccessibilityUtils.printViewHierarchy(mRootNodeInfo, 0);
    }
}
