package com.kikatech.go.util;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;

/**
 * @author SkeeterWang Created on 2018/6/5.
 */

public class KeyguardUtil {
    public static boolean isScreenLocked(Context context) {
        boolean inKeyguardRestrictedInputMode = __inKeyguardRestrictedInputMode(context);
        boolean isPowerManagerLocked = __isPowerManagerLocked(context);
        return inKeyguardRestrictedInputMode || isPowerManagerLocked;
    }

    private static boolean __inKeyguardRestrictedInputMode(Context context) {
        KeyguardManager keyguardMgr = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardMgr != null && keyguardMgr.inKeyguardRestrictedInputMode();
    }

    private static boolean __isPowerManagerLocked(Context context) {
        PowerManager powerMgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return powerMgr != null && !powerMgr.isInteractive();
    }
}
