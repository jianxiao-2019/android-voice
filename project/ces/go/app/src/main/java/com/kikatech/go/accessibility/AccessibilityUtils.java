package com.kikatech.go.accessibility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jasonli Created on 2017/10/17.
 */

public class AccessibilityUtils {

    final static String TAG = "AccessibilityUtils";

    // 此方法用来判断当前应用的辅助功能服务是否开启
    public static boolean isSettingsOn(Context context) {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.i(TAG, e.getMessage());
        }

        if (accessibilityEnabled == 1) {
            String services = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (services != null) {
                return services.toLowerCase().contains(context.getPackageName().toLowerCase());
            }
        }

        return false;
    }

    // 引导至辅助功能设置页面
    public static void openAccessibilitySettings(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
        }
    }

    public static List<String> collectedAllVisibleText(AccessibilityNodeInfo rootNodeInfo) {
        List<AccessibilityNodeInfo> allNodeInfo = getAllChildNodeInfo(rootNodeInfo);
        List<String> allTexts = new ArrayList<>();
        for (AccessibilityNodeInfo nodeInfo : allNodeInfo) {
            CharSequence text = nodeInfo.getText();
            CharSequence description = nodeInfo.getContentDescription();
            CharSequence className = nodeInfo.getClassName();
            // the following view type are not valuable for us to collect text data on screen
            if (AccessibilityConstants.CLASSNAME_EDIT_TEXT.equals(className) ||
                    AccessibilityConstants.CLASSNAME_IMAGE_VIEW.equals(className) ||
                    AccessibilityConstants.CLASSNAME_BUTTON.equals(className) ||
                    AccessibilityConstants.CLASSNAME_FRAMELAYOUT.equals(className) ||
                    AccessibilityConstants.CLASSNAME_LINEARLAYOUT.equals(className) ||
                    AccessibilityConstants.CLASSNAME_RELATIVELAYOUT.equals(className)) {
                continue;
            }
            if (!TextUtils.isEmpty(text)) {
                allTexts.add(text.toString());
            } else if (!TextUtils.isEmpty(description)) {
                allTexts.add(description.toString());
            }
        }
        return allTexts;
    }

    private static List<AccessibilityNodeInfo> getAllChildNodeInfo(AccessibilityNodeInfo rootNodeInfo) {
        ArrayList<AccessibilityNodeInfo> allNodeInfo = new ArrayList<>();

        int childCount = rootNodeInfo.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo childNodeInfo = rootNodeInfo.getChild(i);
            allNodeInfo.add(childNodeInfo);
            allNodeInfo.addAll(getAllChildNodeInfo(childNodeInfo));
        }
        return allNodeInfo;
    }

    @SuppressLint("NewApi")
    public static void printViewHierarchy(AccessibilityNodeInfo nodeInfo, final int level) {

        if (nodeInfo == null) {
            Log.v(TAG, "The node info is null.");
            return;
        }
        // to fix issue with viewIdResName = null on Android 6+
        nodeInfo.refresh();

        String spacerString = "";

        for (int i = 0; i < level; ++i) {
            spacerString += '-';
        }
        //Log the info you care about here... I choose classname and view resource name, because they are simple, but interesting.
        String viewIdResourceName = "(NO_VIEW_ID)";
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)) {
            viewIdResourceName = nodeInfo.getViewIdResourceName();
        }
        Log.d(TAG, spacerString + nodeInfo.getClassName() + " "
                + nodeInfo.getContentDescription() + " "
                + nodeInfo.getText() + " "
                + viewIdResourceName + " "
        );

        for (int i = 0; i < nodeInfo.getChildCount(); ++i) {
            printViewHierarchy(nodeInfo.getChild(i), level + 1);
        }
    }

    public interface AccessibilityConstants {
        public static final String CLASSNAME_EDIT_TEXT = "android.widget.EditText";
        public static final String CLASSNAME_TEXT_VIEW = "android.widget.TextView";
        public static final String CLASSNAME_IMAGE_VIEW = "android.widget.ImageView";
        public static final String CLASSNAME_BUTTON = "android.widget.Button";

        public static final String CLASSNAME_FRAMELAYOUT = "android.widget.FrameLayout";
        public static final String CLASSNAME_LINEARLAYOUT = "android.widget.LinearLayout";
        public static final String CLASSNAME_RELATIVELAYOUT = "android.widget.RelativeLayout";
        public static final String CLASSNAME_LIST_VIEW = "android.widget.ListView";

        public static final String CLASSNAME_VIEW = "android.view.View";
        public static final String CLASSNAME_VIEW_GROUP = "android.view.ViewGroup";
    }

}
