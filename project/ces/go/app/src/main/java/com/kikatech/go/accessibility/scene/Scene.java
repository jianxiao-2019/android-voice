package com.kikatech.go.accessibility.scene;

import android.os.Build;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.kikatech.go.accessibility.AccessibilityUtils;

import java.util.List;

/**
 * Created by tianli on 17-10-20.
 */

public class Scene {

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
        return null;
    }

    protected AccessibilityNodeInfo findNodeByText(AccessibilityNodeInfo parentNode, String text) {
        List<AccessibilityNodeInfo> results = parentNode.findAccessibilityNodeInfosByText(text);
        if (results.size() > 0) {
            return results.get(0);
        }
        return null;
    }

    protected List<AccessibilityNodeInfo> findNodesByText(AccessibilityNodeInfo parentNode, String text) {
        return parentNode.findAccessibilityNodeInfosByText(text);
    }

    protected AccessibilityNodeInfo findNodeByTextAndClass(AccessibilityNodeInfo parentNode, String text, String className) {
        List<AccessibilityNodeInfo> results = parentNode.findAccessibilityNodeInfosByText(text);
        for (AccessibilityNodeInfo nodeInfo : results) {
            if (className.equals(nodeInfo.getClassName())) {
                return nodeInfo;
            }
        }
        return null;
    }

    protected AccessibilityNodeInfo findNodeByTextAndId(AccessibilityNodeInfo parentNode, String text, String viewId) {
        List<AccessibilityNodeInfo> results = parentNode.findAccessibilityNodeInfosByViewId(viewId);
        for (AccessibilityNodeInfo nodeInfo : results) {
            if (text != null && text.equalsIgnoreCase(nodeInfo.getText().toString())) {
                return nodeInfo;
            }
        }
        return null;
    }

    protected void clickView(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }

        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        nodeInfo.recycle();
    }

    protected void longClickView(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }

        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
        nodeInfo.recycle();
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
//            ClipData data = ClipData.newPlainText("auto_fill_text", text);
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
