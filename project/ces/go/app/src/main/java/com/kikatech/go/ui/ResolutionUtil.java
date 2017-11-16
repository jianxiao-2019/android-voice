package com.kikatech.go.ui;

import android.content.Context;
import android.util.TypedValue;

/**
 * @author SkeeterWang Created on 2017/11/16.
 */

public class ResolutionUtil {

    public static int sp2px(Context context, float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public static int dp2px(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}
