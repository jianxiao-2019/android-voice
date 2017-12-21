package com.kikatech.go.dialogflow;

import android.support.annotation.IntDef;

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
        int DEFAULT = SETTING_REPLY_MSG_READ;
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
        switch (pkgName) {
            case AppConstants.PACKAGE_WHATSAPP:
                return GlobalPref.getIns().getSettingReplyMsgWhatsApp();
            case AppConstants.PACKAGE_MESSENGER:
                return GlobalPref.getIns().getSettingReplyMsgMessenger();
            case AppConstants.PACKAGE_SMS:
                return GlobalPref.getIns().getSettingReplyMsgSms();
        }
        return ReplyMsgSetting.DEFAULT;
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
}
