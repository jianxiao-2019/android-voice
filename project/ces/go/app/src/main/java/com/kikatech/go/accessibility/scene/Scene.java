package com.kikatech.go.accessibility.scene;

import android.view.accessibility.AccessibilityNodeInfo;

import com.kikatech.go.accessibility.AccessibilityNodeWrapper;
import com.kikatech.go.accessibility.AccessibilityUtils;
import com.kikatech.go.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tianli on 17-10-20.
 */

public class Scene {

    private static final String TAG = "Access.Scene";

    protected AccessibilityNodeInfo mRootNodeInfo;

    protected Map<String, AccessibilityNodeWrapper> mAllNodes = new HashMap<>();

    public Scene(AccessibilityNodeInfo rootNodeInfo) {
        mRootNodeInfo = rootNodeInfo;
        updateNodes(rootNodeInfo);
    }





    public AccessibilityNodeInfo getRootNodeInfo() {
        return mRootNodeInfo;
    }

    public synchronized void updateNodes(Scene scene) {
        updateNodes(scene.getRootNodeInfo());
    }

    private synchronized void updateNodes(AccessibilityNodeInfo rootNodeInfo) {
        mRootNodeInfo = rootNodeInfo;
        List<AccessibilityNodeInfo> allNodeInfo = AccessibilityUtils.getAllChildNodeInfo(rootNodeInfo);
        for (AccessibilityNodeInfo nodeInfo : allNodeInfo) {
            if (AccessibilityUtils.isNodeValid(nodeInfo)) {
                AccessibilityNodeWrapper nodeWrapper = new AccessibilityNodeWrapper(nodeInfo);
                AccessibilityNodeWrapper oldNodeWrapper = mAllNodes.get(nodeWrapper.getHash());
                // Do not replace recorded node strategy
                if (oldNodeWrapper != null) {
                    // in case RatingBar widget's child number varied each time
                    if (AccessibilityUtils.AccessibilityConstants.CLASSNAME_RATING_BAR.equals(oldNodeWrapper.getClassName())
                        && oldNodeWrapper.getChildCount() > nodeWrapper.getChildCount()) {
                        continue;
                    }
                }
                mAllNodes.put(nodeWrapper.getHash(), nodeWrapper);
            }
        }
    }

    public synchronized AccessibilityNodeWrapper findNodeByViewId(String viewId) {
        for (AccessibilityNodeWrapper nodeWrapper : mAllNodes.values()) {
            if (StringUtil.equals(viewId, nodeWrapper.getResourceId())) {
                return nodeWrapper;
            }
        }
        List<AccessibilityNodeInfo> results = mRootNodeInfo.findAccessibilityNodeInfosByViewId(viewId);
        if (results != null && results.size() > 0) {
            return new AccessibilityNodeWrapper(results.get(0));
        }
        return null;
    }

    public synchronized AccessibilityNodeWrapper findNodeByText(String text) {
        for (AccessibilityNodeWrapper nodeWrapper : mAllNodes.values()) {
            if (StringUtil.equals(text, nodeWrapper.getText()) ||
                StringUtil.equals(text, nodeWrapper.getContentDescription())) {
                return nodeWrapper;
            }
        }
        return null;
    }

    public synchronized AccessibilityNodeWrapper findNodeByText(String[] texts) {
        for (String text : texts) {
            AccessibilityNodeWrapper node = findNodeByText(text);
            if (node != null) {
                return node;
            }
        }
        return null;
    }

    public synchronized AccessibilityNodeWrapper findNodeByClass(String clz) {
        for (AccessibilityNodeWrapper nodeWrapper : mAllNodes.values()) {
            if (StringUtil.equals(clz, nodeWrapper.getClassName())) {
                return nodeWrapper;
            }
        }
        return null;
    }

    public synchronized AccessibilityNodeWrapper findNodeByContentDescription(String contentDescription) {
        for (AccessibilityNodeWrapper nodeWrapper : mAllNodes.values()) {
            if (StringUtil.equals(contentDescription, nodeWrapper.getContentDescription())) {
                return nodeWrapper;
            }
        }
        return null;
    }

    public synchronized AccessibilityNodeWrapper findNodeByTextAndId(String text, String viewId) {
        for (AccessibilityNodeWrapper nodeWrapper : mAllNodes.values()) {
            if (StringUtil.equals(text, nodeWrapper.getText()) &&
                StringUtil.equals(viewId, nodeWrapper.getResourceId())) {
                return nodeWrapper;
            }
        }
        return null;
    }

    public synchronized AccessibilityNodeWrapper findNodeByTextAndId(String[] texts, String viewId) {
        for (String text : texts) {
            AccessibilityNodeWrapper node = findNodeByTextAndId(text, viewId);
            if (node != null) {
                return node;
            }
        }
        return null;
    }

    public synchronized AccessibilityNodeWrapper findNodeByTextAndClass(String text, String clz) {
        for (AccessibilityNodeWrapper nodeWrapper : mAllNodes.values()) {
            if ((StringUtil.equals(text, nodeWrapper.getText()) ||
                 StringUtil.equals(text, nodeWrapper.getContentDescription())) &&
                 StringUtil.equals(clz, nodeWrapper.getClassName())) {
                return nodeWrapper;
            }
        }
        return null;
    }

    public synchronized AccessibilityNodeWrapper findNodeByTextAndClass(String[] texts, String viewId) {
        for (String text : texts) {
            AccessibilityNodeWrapper node = findNodeByTextAndClass(text, viewId);
            if (node != null) {
                return node;
            }
        }
        return null;
    }





    protected void waitForView(long millisecond) {
        try {
            Thread.sleep(millisecond);
        } catch (InterruptedException ignore) {}
    }

    public void recycle() {
        for (AccessibilityNodeWrapper nodeWrapper : mAllNodes.values()) {
            nodeWrapper.recycle();
        }
        mAllNodes.clear();
    }

    public void printView() {
        AccessibilityUtils.printViewHierarchy(mRootNodeInfo, 0);
    }
}
