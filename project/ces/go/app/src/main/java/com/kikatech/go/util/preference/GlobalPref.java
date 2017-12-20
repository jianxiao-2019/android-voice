package com.kikatech.go.util.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.kikatech.go.dialogflow.UserSettings;
import com.kikatech.go.ui.KikaMultiDexApplication;

/**
 * @author SkeeterWang Created on 2017/12/19.
 */

public class GlobalPref {
    private static final String TAG = "GlobalPref";

    private static final String PREF_NAME = "kika";

    private static SharedPreferences sPref;
    private static SharedPreferences.Editor sEditor;
    private static GlobalPref sIns;

    public static synchronized GlobalPref getIns() {
        if (sIns == null) {
            sIns = new GlobalPref();
        }
        return sIns;
    }

    private GlobalPref() {
        sPref = KikaMultiDexApplication.getAppContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sEditor = sPref.edit();
    }

    private void apply() {
        sEditor.apply();
        sEditor.commit();
    }


    public void saveSettingReplyMsgWhatsApp(@UserSettings.ReplyMsgSetting int setting) {
        sEditor.putInt(Key.KEY_SETTING_REPLY_MSG_WHATSAPP, setting);
        apply();
    }

    public int getSettingReplyMsgWhatsApp() {
        return sPref.getInt(Key.KEY_SETTING_REPLY_MSG_WHATSAPP, UserSettings.ReplyMsgSetting.DEFAULT);
    }

    public void saveSettingReplyMsgMessenger(@UserSettings.ReplyMsgSetting int setting) {
        sEditor.putInt(Key.KEY_SETTING_REPLY_MSG_MESSENGER, setting);
        apply();
    }

    public int getSettingReplyMsgMessenger() {
        return sPref.getInt(Key.KEY_SETTING_REPLY_MSG_MESSENGER, UserSettings.ReplyMsgSetting.DEFAULT);
    }

    public void saveSettingReplyMsgSms(@UserSettings.ReplyMsgSetting int setting) {
        sEditor.putInt(Key.KEY_SETTING_REPLY_MSG_SMS, setting);
        apply();
    }

    public int getSettingReplyMsgSms() {
        return sPref.getInt(Key.KEY_SETTING_REPLY_MSG_SMS, UserSettings.ReplyMsgSetting.DEFAULT);
    }
}