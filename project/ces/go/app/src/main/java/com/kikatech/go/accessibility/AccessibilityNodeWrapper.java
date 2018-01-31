package com.kikatech.go.accessibility;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;

import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.StringUtil;
import com.kikatech.voice.util.request.MD5;

import java.util.List;

/**
 * Created by jason on 2018/1/30.
 */

public class AccessibilityNodeWrapper {

    private static final String TAG = AccessibilityNodeWrapper.class.getName();

    private AccessibilityNodeInfo mNodeInfo;

    public AccessibilityNodeWrapper(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            throw new IllegalArgumentException("Cannot construct AccessibilityNodeWrapper");
        }
        mNodeInfo = nodeInfo;
    }

    public String getPackageName() {
        if (!TextUtils.isEmpty(mNodeInfo.getPackageName())) {
            return mNodeInfo.getPackageName().toString();
        }
        return null;
    }

    public String getClassName() {
        if (!TextUtils.isEmpty(mNodeInfo.getClassName())) {
            return mNodeInfo.getClassName().toString();
        }
        return null;
    }

    public String getResourceId() {
        return mNodeInfo.getViewIdResourceName();
    }

    public String getText() {
        if (!TextUtils.isEmpty(mNodeInfo.getText())) {
            return mNodeInfo.getText().toString();
        }
        return null;
    }

    public String getContentDescription() {
        if (!TextUtils.isEmpty(mNodeInfo.getContentDescription())) {
            return mNodeInfo.getContentDescription().toString();
        }
        return null;
    }

    public boolean click() {
        AccessibilityNodeInfo nodeInfo = mNodeInfo;
        boolean clicked = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        if (!clicked && LogUtil.DEBUG) {
            //LogUtil.logwtf(TAG, "Failed to click view");
            AccessibilityUtils.printNode(nodeInfo);
        }
        return clicked;
    }

    public boolean fillUpText(String text) {
        AccessibilityNodeInfo nodeInfo = mNodeInfo;

        boolean filled = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);

            filled = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
            if (!filled && LogUtil.DEBUG) {
                LogUtil.logwtf(TAG, "Failed to filled view with text: " + text);
                AccessibilityUtils.printNode(nodeInfo);
            }
        }
        return filled;
    }

    public AccessibilityNodeWrapper getFirstChild() {
        if (mNodeInfo.getChildCount() > 0) {
            AccessibilityNodeInfo child = mNodeInfo.getChild(0);
            if (child != null) {
                return new AccessibilityNodeWrapper(child);
            }
        }
        return null;
    }

    public AccessibilityNodeWrapper findNodeById(String id) {
        List<AccessibilityNodeInfo> results = mNodeInfo.findAccessibilityNodeInfosByViewId(id);
        if (results != null && results.size() > 0) {
            return new AccessibilityNodeWrapper(results.get(0));
        }
        return null;
    }

    public AccessibilityNodeWrapper findNodeByClass(String clz) {
        List<AccessibilityNodeInfo> results = AccessibilityUtils.getAllChildNodeInfo(mNodeInfo);
        for (AccessibilityNodeInfo childNode : results) {
            if (childNode != null && StringUtil.equals(childNode.getClassName(), clz)) {
                return new AccessibilityNodeWrapper(childNode);
            }
        }
        return null;
    }

    public AccessibilityNodeWrapper findNodeByText(String text) {
        List<AccessibilityNodeInfo> results = mNodeInfo.findAccessibilityNodeInfosByText(text);
        if (results != null && results.size() > 0) {
            return new AccessibilityNodeWrapper(results.get(0));
        }
        return null;
    }

    public AccessibilityNodeWrapper findNodeByTextAndId(String id, String clz) {
        List<AccessibilityNodeInfo> results = mNodeInfo.findAccessibilityNodeInfosByViewId(id);
        if (results != null && results.size() > 0) {
            for (AccessibilityNodeInfo nodeInfo : results) {
                if (StringUtil.equals(nodeInfo.getClassName(), clz)) {
                    return new AccessibilityNodeWrapper(nodeInfo);
                }
            }
        }
        return null;
    }

    public AccessibilityNodeWrapper findNodeByTextAndClass(String text, String clz) {
        List<AccessibilityNodeInfo> results = mNodeInfo.findAccessibilityNodeInfosByText(text);
        if (results != null && results.size() > 0) {
            for (AccessibilityNodeInfo nodeInfo : results) {
                if (StringUtil.equals(nodeInfo.getClassName(), clz)) {
                    return new AccessibilityNodeWrapper(nodeInfo);
                }
            }
        }
        return null;
    }

    public AccessibilityNodeWrapper getParent() {
        AccessibilityNodeInfo parent = mNodeInfo.getParent();
        if (parent != null) {
            return new AccessibilityNodeWrapper(parent);
        }
        return null;
    }

    public void recycle() {
        try {
            mNodeInfo.recycle();
        } catch (Throwable ignore) {}
    }

    public void print() {
        print(false);
    }

    public void print(boolean withHierarchy) {
        LogUtil.log(TAG, toString());
        if (withHierarchy) {
            AccessibilityUtils.printViewHierarchy(mNodeInfo, 0);
        }
    }

    public String getHash() {
        return MD5.getMD5(toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AccessibilityNodeWrapper)) {
            return false;
        }

        AccessibilityNodeWrapper nodeWrapper = (AccessibilityNodeWrapper) obj;
        return TextUtils.equals(getHash(), nodeWrapper.getHash());
    }

    @Override
    public String toString() {
        return "[" + getClassName() + "] "
                + getPackageName() + " "
                + getResourceId() + " "
                + getText() + " "
                + getContentDescription() + " ";
    }
}
