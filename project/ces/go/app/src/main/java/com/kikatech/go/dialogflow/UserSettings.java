package com.kikatech.go.dialogflow;

import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.model.SettingDestination;
import com.kikatech.go.util.AppConstants;
import com.kikatech.go.util.Gson.GsonUtil;
import com.kikatech.go.util.preference.GlobalPref;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brad_chang on 2017/11/28.
 */

public class UserSettings {
    private static final String TAG = "UserSettings";

    public final static int SETTING_REPLY_MSG_IGNORE = 0;
    public final static int SETTING_REPLY_MSG_READ = 1;
    public final static int SETTING_REPLY_MSG_ASK_USER = 2;

    @IntDef({SETTING_REPLY_MSG_IGNORE, SETTING_REPLY_MSG_READ, SETTING_REPLY_MSG_ASK_USER})
    public @interface ReplyMsgSetting {
        int DEFAULT = SETTING_REPLY_MSG_ASK_USER;
    }

    private final static int SETTING_ASR_LOCALE_EN = 0;
    private final static int SETTING_ASR_LOCALE_ZH = 1;

    @IntDef({SETTING_ASR_LOCALE_EN, SETTING_ASR_LOCALE_ZH})
    public @interface AsrLocale {
        int DEFAULT = SETTING_ASR_LOCALE_EN;
        int EN = SETTING_ASR_LOCALE_EN;
        int ZH = SETTING_ASR_LOCALE_ZH;
    }

    public static void saveReplyMsgSetting(String pkgName, @ReplyMsgSetting int setting) {
        switch (pkgName) {
            case AppConstants.PACKAGE_WHATSAPP:
                GlobalPref.getIns().saveSettingReplyMsgWhatsApp(setting);
                break;
            case AppConstants.PACKAGE_MESSENGER:
                GlobalPref.getIns().saveSettingReplyMsgMessenger(setting);
                break;
            case AppConstants.PACKAGE_SMS:
                GlobalPref.getIns().saveSettingReplyMsgSms(setting);
                break;
        }
    }

    public static int getReplyMsgSetting(String pkgName) {
        if (!TextUtils.isEmpty(pkgName)) {
            switch (pkgName) {
                case AppConstants.PACKAGE_WHATSAPP:
                    return GlobalPref.getIns().getSettingReplyMsgWhatsApp();
                case AppConstants.PACKAGE_MESSENGER:
                    return GlobalPref.getIns().getSettingReplyMsgMessenger();
                case AppConstants.PACKAGE_SMS:
                    return GlobalPref.getIns().getSettingReplyMsgSms();
            }
        }
        return ReplyMsgSetting.DEFAULT;
    }

    public static void saveSettingConfirmDestination(boolean toConfirm) {
        GlobalPref.getIns().saveSettingConfirmDestination(toConfirm);
    }

    public static boolean getSettingConfirmDestination() {
        return GlobalPref.getIns().getSettingConfirmDestination();
    }

    public static void saveSettingConfirmCounter(boolean enable) {
        GlobalPref.getIns().saveSettingConfirmCounter(enable);
    }

    public static boolean getSettingConfirmCounter() {
        return GlobalPref.getIns().getSettingConfirmCounter();
    }


    public static String getDefaultDestinationListJson() {
        List<SettingDestination> list = new ArrayList<>();
        list.add(new SettingDestination(SettingDestination.TYPE_DEFAULT_HOME, "Home"));
        list.add(new SettingDestination(SettingDestination.TYPE_DEFAULT_WORK, "Work"));
        return GsonUtil.toJson(list);
    }

    public static void saveSettingDestinationList(List<SettingDestination> list) {
        GlobalPref.getIns().saveSettingDestinationList(list);
    }

    public static List<SettingDestination> getSettingDestinationList() {
        return GlobalPref.getIns().getSettingDestinationList();
    }


    public static void saveSettingAsrLocale(@UserSettings.AsrLocale int locale) {
        GlobalPref.getIns().saveSettingAsrLocale(locale);
    }

    public static int getSettingAsrLocale() {
        return GlobalPref.getIns().getSettingAsrLocale();
    }
}
