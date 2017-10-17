package com.kikatech.voice.util.request;

import android.graphics.Typeface;

/**
 * Created by huangqiang on 16/3/7.
 */
public class MiscUtil {
    private static final String TAG = "MiscUtil";

    private static final float EPSILON = 0.001f;

    public static boolean equalFloats(float val1, float val2) {
        return Math.abs(val1 - val2) < EPSILON;
    }

    public static Typeface getDefaultFlatTypeface(Typeface defaultValue) {
        try {
            return Typeface.create("sans-serif", Typeface.NORMAL);
        } catch (Exception e) {
//            if (LogUtils.verbose(TAG)) {
//                Log.v(TAG, "create default flat typeface failed!");
//            }
//            LogUtils.error(e);
        }
        return defaultValue;
    }

    public static boolean isValidHeaderString(String name) {
        for (int i = 0, length = name.length(); i < length; i++) {
            char c = name.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                return false;
            }
        }
        return true;
    }
}
