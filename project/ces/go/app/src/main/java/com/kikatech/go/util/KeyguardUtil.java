package com.kikatech.go.util;

import android.app.KeyguardManager;
import android.content.Context;

/**
 * @author SkeeterWang Created on 2018/6/5.
 */

public class KeyguardUtil {
    public static boolean isScreenLocked(Context context) {
        KeyguardManager keyguardMgr = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardMgr != null && keyguardMgr.inKeyguardRestrictedInputMode();
    }
}
