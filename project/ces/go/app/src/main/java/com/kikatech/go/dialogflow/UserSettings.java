package com.kikatech.go.dialogflow;

import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.model.SettingDestination;
import com.kikatech.go.util.AppConstants;
import com.kikatech.go.util.Gson.GsonUtil;
import com.kikatech.go.util.preference.GlobalPref;
import com.kikatech.voice.service.conf.VoiceConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brad_chang on 2017/11/28.
 */

public class UserSettings {
    private static final String TAG = "UserSettings";

    public final static String DEFAULT_DBG_ASR_SERVER = VoiceConfiguration.HostUrl.KIKAGO_SQ;

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


    public static List<String> getDbgAsrServerList() {
        List<String> serverList = new ArrayList<>();
        serverList.add(VoiceConfiguration.HostUrl.HAO_DEV);
        serverList.add(VoiceConfiguration.HostUrl.API_DEV);
        serverList.add(VoiceConfiguration.HostUrl.KIKAGO_SQ);
        serverList.add(VoiceConfiguration.HostUrl.KIKAGO_PRODUCTION);
        return serverList;
    }

    public static void saveDbgAsrServer(String serverUrl) {
        GlobalPref.getIns().saveDbgAsrServer(serverUrl);
    }

    public static String getDbgAsrServer() {
        return GlobalPref.getIns().getDbgAsrServer();
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


    public static void saveSettingRecommendListPop(boolean isEnabled) {
        GlobalPref.getIns().saveSettingRecommendListPop(isEnabled);
    }

    public static boolean getSettingRecommendListPop() {
        return GlobalPref.getIns().getSettingRecommendListPop();
    }

    public static void saveSettingRecommendListHipHop(boolean isEnabled) {
        GlobalPref.getIns().saveSettingRecommendListHipHop(isEnabled);
    }

    public static boolean getSettingRecommendListHipHop() {
        return GlobalPref.getIns().getSettingRecommendListHipHop();
    }

    public static void saveSettingRecommendListRock(boolean isEnabled) {
        GlobalPref.getIns().saveSettingRecommendListRock(isEnabled);
    }

    public static boolean getSettingRecommendListRock() {
        return GlobalPref.getIns().getSettingRecommendListRock();
    }

    public static void saveSettingRecommendListEDM(boolean isEnabled) {
        GlobalPref.getIns().saveSettingRecommendListEDM(isEnabled);
    }

    public static boolean getSettingRecommendListEDM() {
        return GlobalPref.getIns().getSettingRecommendListEDM();
    }

    public static void saveSettingRecommendListLatin(boolean isEnabled) {
        GlobalPref.getIns().saveSettingRecommendListLatin(isEnabled);
    }

    public static boolean getSettingRecommendListLatin() {
        return GlobalPref.getIns().getSettingRecommendListLatin();
    }

    public static void saveSettingRecommendListCountry(boolean isEnabled) {
        GlobalPref.getIns().saveSettingRecommendListCountry(isEnabled);
    }

    public static boolean getSettingRecommendListCountry() {
        return GlobalPref.getIns().getSettingRecommendListCountry();
    }

    public static void saveSettingRecommendListJazz(boolean isEnabled) {
        GlobalPref.getIns().saveSettingRecommendListJazz(isEnabled);
    }

    public static boolean getSettingRecommendListJazz() {
        return GlobalPref.getIns().getSettingRecommendListJazz();
    }

    public static void saveSettingRecommendListIndie(boolean isEnabled) {
        GlobalPref.getIns().saveSettingRecommendListIndie(isEnabled);
    }

    public static boolean getSettingRecommendListIndie() {
        return GlobalPref.getIns().getSettingRecommendListIndie();
    }


    public static void saveSettingVolume(float volume) {
        GlobalPref.getIns().saveSettingVolume(volume);
    }

    public static float getSettingVolume() {
        return GlobalPref.getIns().getSettingVolume();
    }


    public static void saveSettingAsrLocale(@UserSettings.AsrLocale int locale) {
        GlobalPref.getIns().saveSettingAsrLocale(locale);
    }

    public static int getSettingAsrLocale() {
        return GlobalPref.getIns().getSettingAsrLocale();
    }
}
