package com.kikatech.go.access;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by ryanlin on 28/09/2017.
 */

public class WeChatEventHandler extends EventHandler {

    public static final String PKG_NAME = "com.tencent.mm";
    public static final String[] KEY_WORD_LIST = {"傳送", "发送", "Send"};

    @Override
    protected AccessibilityNodeInfo getSendNodeInfo(AccessibilityNodeInfo info) {
        if (info == null) {
            return null;
        }

        Log.d("Ryan", "WeChatEventHandler" +
                " info.className = " + info.getClassName() +
                " info.text = " + info.getText() +
                " childCount = " + info.getChildCount());
        for (String key : KEY_WORD_LIST) {
            if (key.equals(info.getText())) {
                return info;
            }
        }

        if (info.getChildCount() > 0
                && "android.widget.LinearLayout".equals(info.getClassName())) {
            for (int i = 0; i < info.getChildCount(); i++) {
                AccessibilityNodeInfo child = info.getChild(i);
                if (child == null) continue;
                Log.i("Ryan", "WeChatEventHandler" +
                        " child.className = " + child.getClassName() +
                        " child.text = " + child.getText() +
                        " childCount = " + child.getChildCount());
                if (child.getChildCount() > 0
                        && "android.widget.LinearLayout".equals(info.getClassName())) {
                    for (int j = 0; j < child.getChildCount(); j++) {
                        AccessibilityNodeInfo grandChild = child.getChild(j);
                        if (grandChild == null) continue;
                        Log.w("Ryan", "WeChatEventHandler" +
                                " grandChild.className = " + grandChild.getClassName() +
                                " grandChild.text = " + grandChild.getText() +
                                " childCount = " + grandChild.getChildCount());
                        for (String key : KEY_WORD_LIST) {
                            if (key.equals(grandChild.getText())) {
                                return grandChild;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    protected String getPackageName() {
        return PKG_NAME;
    }
}
