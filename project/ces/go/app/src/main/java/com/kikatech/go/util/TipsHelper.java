package com.kikatech.go.util;

import android.content.Context;
import android.os.Bundle;

import com.kikatech.go.util.dialog.DialogUtil;
import com.kikatech.go.util.preference.GlobalPref;

/**
 * @author SkeeterWang Created on 2018/5/2.
 */

public class TipsHelper {
    private static final String TAG = "TipsHelper";

    public static synchronized void showDialogMoreCommands(Context context, final DialogUtil.IDialogListener listener) {
        DialogUtil.showMoreCommands(context, new DialogUtil.IDialogListener() {
            @Override
            public void onApply(Bundle args) {
                if (listener != null) {
                    listener.onApply(args);
                }
            }

            @Override
            public void onCancel() {
                if (listener != null) {
                    listener.onCancel();
                }
            }
        });
        setCanShowDialogMoreCommands(false);
        setHasShowDialogMoreCommands(true);
    }

    public static synchronized boolean shouldShowDialogMoreCommands() {
        boolean hasShow = getHasShowDialogMoreCommands();
        boolean canShow = getCanShowDialogMoreCommands();
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, String.format("hasShow: %s, canShow: %s", hasShow, canShow));
        }
        return !hasShow && canShow;
    }

    public static synchronized void setCanShowDialogMoreCommands(boolean canShow) {
        GlobalPref.getIns().setCanShowDialogMoreCommands(canShow);
    }

    private static synchronized boolean getCanShowDialogMoreCommands() {
        return GlobalPref.getIns().getCanShowDialogMoreCommands();
    }

    private static synchronized void setHasShowDialogMoreCommands(boolean hasShow) {
        GlobalPref.getIns().setHasShowDialogMoreCommands(hasShow);
    }

    private static synchronized boolean getHasShowDialogMoreCommands() {
        return GlobalPref.getIns().getHasShowDialogMoreCommands();
    }
}
