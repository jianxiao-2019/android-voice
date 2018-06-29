package com.kikatech.go.util.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.reflect.TypeToken;
import com.kikatech.go.BuildConfig;
import com.kikatech.go.R;
import com.kikatech.go.dialogflow.UserSettings;
import com.kikatech.go.dialogflow.model.SettingDestination;
import com.kikatech.go.ui.KikaMultiDexApplication;
import com.kikatech.go.util.Gson.GsonUtil;

import java.util.ArrayList;
import java.util.List;

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


    public void saveDbgAsrServer(String serverUrl) {
        sEditor.putString(Key.KEY_DBG_ASR_SERVER, serverUrl);
        apply();
    }

    public String getDbgAsrServer() {
        return sPref.getString(Key.KEY_DBG_ASR_SERVER, UserSettings.DEFAULT_DBG_ASR_SERVER);
    }


    public void setIsFirstLaunch(boolean isFirstLaunch) {
        sEditor.putBoolean(Key.KEY_IS_FIRST_LAUNCH_APPLICATION, isFirstLaunch);
        apply();
    }

    public boolean getIsFirstLaunch() {
        return sPref.getBoolean(Key.KEY_IS_FIRST_LAUNCH_APPLICATION, true);
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


    public void saveSettingConfirmDestination(boolean toConfirm) {
        sEditor.putBoolean(Key.KEY_SETTING_CONFIRM_DESTINATION, toConfirm);
        apply();
    }

    public boolean getSettingConfirmDestination() {
        return sPref.getBoolean(Key.KEY_SETTING_CONFIRM_DESTINATION, false);
    }

    public void saveSettingConfirmCounter(boolean enable) {
        sEditor.putBoolean(Key.KEY_SETTING_CONFIRM_COUNTER, enable);
        apply();
    }

    public boolean getSettingConfirmCounter() {
        return sPref.getBoolean(Key.KEY_SETTING_CONFIRM_COUNTER, true);
    }


    public void saveSettingDestinationList(List<SettingDestination> list) {
        String json = GsonUtil.toJson(list);
        sEditor.putString(Key.KEY_SETTING_DESTINATION_LIST, json);
        apply();
    }

    public List<SettingDestination> getSettingDestinationList() {
        String json = sPref.getString(Key.KEY_SETTING_DESTINATION_LIST, UserSettings.getDefaultDestinationListJson());
        return GsonUtil.fromJsonList(json, new TypeToken<List<SettingDestination>>() {
        }.getType());
    }

    public void addNavigatedAddress(String address) {
        List<String> list = getNavigatedAddressList();
        if (!list.contains(address)) {
            list.add(address);
            String json = GsonUtil.toJson(list);
            sEditor.putString(Key.KEY_NAVIGATED_ADDR_LIST, json);
            apply();
        }
    }

    public List<String> getNavigatedAddressList() {
        String json = sPref.getString(Key.KEY_NAVIGATED_ADDR_LIST, "{}");
        List<String> ret = GsonUtil.fromJsonList(json, new TypeToken<List<String>>() {
        }.getType());
        if (ret == null) {
            ret = new ArrayList<>();
        }
        return ret;
    }


    public void saveSettingRecommendListPop(boolean isEnabled) {
        sEditor.putBoolean(Key.KEY_SETTING_RECOMMEND_LIST_POP, isEnabled);
        apply();
    }

    public boolean getSettingRecommendListPop() {
        return sPref.getBoolean(Key.KEY_SETTING_RECOMMEND_LIST_POP, true);
    }

    public void saveSettingRecommendListHipHop(boolean isEnabled) {
        sEditor.putBoolean(Key.KEY_SETTING_RECOMMEND_LIST_HIP_HOP, isEnabled);
        apply();
    }

    public boolean getSettingRecommendListHipHop() {
        return sPref.getBoolean(Key.KEY_SETTING_RECOMMEND_LIST_HIP_HOP, false);
    }

    public void saveSettingRecommendListRock(boolean isEnabled) {
        sEditor.putBoolean(Key.KEY_SETTING_RECOMMEND_LIST_ROCK, isEnabled);
        apply();
    }

    public boolean getSettingRecommendListRock() {
        return sPref.getBoolean(Key.KEY_SETTING_RECOMMEND_LIST_ROCK, false);
    }

    public void saveSettingRecommendListEDM(boolean isEnabled) {
        sEditor.putBoolean(Key.KEY_SETTING_RECOMMEND_LIST_EDM, isEnabled);
        apply();
    }

    public boolean getSettingRecommendListEDM() {
        return sPref.getBoolean(Key.KEY_SETTING_RECOMMEND_LIST_EDM, false);
    }

    public void saveSettingRecommendListLatin(boolean isEnabled) {
        sEditor.putBoolean(Key.KEY_SETTING_RECOMMEND_LIST_LATIN, isEnabled);
        apply();
    }

    public boolean getSettingRecommendListLatin() {
        return sPref.getBoolean(Key.KEY_SETTING_RECOMMEND_LIST_LATIN, false);
    }

    public void saveSettingRecommendListCountry(boolean isEnabled) {
        sEditor.putBoolean(Key.KEY_SETTING_RECOMMEND_LIST_COUNTRY, isEnabled);
        apply();
    }

    public boolean getSettingRecommendListCountry() {
        return sPref.getBoolean(Key.KEY_SETTING_RECOMMEND_LIST_COUNTRY, false);
    }

    public void saveSettingRecommendListJazz(boolean isEnabled) {
        sEditor.putBoolean(Key.KEY_SETTING_RECOMMEND_LIST_JAZZ, isEnabled);
        apply();
    }

    public boolean getSettingRecommendListJazz() {
        return sPref.getBoolean(Key.KEY_SETTING_RECOMMEND_LIST_JAZZ, false);
    }

    public void saveSettingRecommendListIndie(boolean isEnabled) {
        sEditor.putBoolean(Key.KEY_SETTING_RECOMMEND_LIST_INDIE, isEnabled);
        apply();
    }

    public boolean getSettingRecommendListIndie() {
        return sPref.getBoolean(Key.KEY_SETTING_RECOMMEND_LIST_INDIE, false);
    }

    public void saveSettingVolume(float volume) {
        sEditor.putFloat(Key.KEY_SETTING_VOLUME, volume);
        apply();
    }

    public float getSettingVolume() {
        return sPref.getFloat(Key.KEY_SETTING_VOLUME, 0.5f);
    }


    public boolean isFirstLaunch() {
        boolean isFirst = sPref.getBoolean(Key.KEY_FIRST_LAUNCH, true);
        if (isFirst) {
            sEditor.putBoolean(Key.KEY_FIRST_LAUNCH, false);
            apply();
        }
        return isFirst;
    }


    public void saveSettingAsrLocale(@UserSettings.AsrLocale int locale) {
        sEditor.putInt(Key.KEY_SETTING_ASR_LOCALE, locale);
        apply();
    }

    public int getSettingAsrLocale() {
        return sPref.getInt(Key.KEY_SETTING_ASR_LOCALE, UserSettings.AsrLocale.DEFAULT);
    }


    public void setCheckAudioFileTime(long time) {
        sEditor.putLong(Key.KEY_CHECK_AUDIO_FILE_TIME, time);
        apply();
    }

    public long getCheckAudioFileTime() {
        return sPref.getLong(Key.KEY_CHECK_AUDIO_FILE_TIME, 0);
    }


    public void setHasShowDialogUsbIllustration(boolean hasShow) {
        sEditor.putBoolean(Key.KEY_HAS_SHOW_DIALOG_USB_ILLUSTRATION, hasShow);
        apply();
    }

    public boolean getHasShowDialogUsbIllustration() {
        return sPref.getBoolean(Key.KEY_HAS_SHOW_DIALOG_USB_ILLUSTRATION, false);
    }

    public void setFloatingWakeUpTipShowingCount(int count) {
        sEditor.putInt(Key.KEY_FLOATING_WAKE_UP_TIP_SHOWS_COUNT, count);
        apply();
    }

    public int getFloatingWakeUpTipShowingCount() {
        return sPref.getInt(Key.KEY_FLOATING_WAKE_UP_TIP_SHOWS_COUNT, 0);
    }

    public void setCanShowDialogMoreCommands(boolean canShow) {
        sEditor.putBoolean(Key.KEY_CAN_SHOW_DIALOG_MORE_COMMANDS, canShow);
        apply();
    }

    public boolean getCanShowDialogMoreCommands() {
        return sPref.getBoolean(Key.KEY_CAN_SHOW_DIALOG_MORE_COMMANDS, false);
    }

    public void setHasShowDialogMoreCommands(boolean hasShow) {
        sEditor.putBoolean(Key.KEY_HAS_SHOW_DIALOG_MORE_COMMANDS, hasShow);
        apply();
    }

    public boolean getHasShowDialogMoreCommands() {
        return sPref.getBoolean(Key.KEY_HAS_SHOW_DIALOG_MORE_COMMANDS, false);
    }

    public void setHasDoneTutorial(boolean hasDone) {
        sEditor.putBoolean(Key.KEY_HAS_DONE_TUTORILA, hasDone);
        apply();
    }

    public boolean getHasDoneTutorial() {
        return sPref.getBoolean(Key.KEY_HAS_DONE_TUTORILA, false);
    }


    // ----- Remote Configurations -----

    public void saveRemoteConfigConfigVersion(long version) {
        sEditor.putLong(Key.KEY_REMOTE_CONFIG_CONFIG_VERSION, version);
        apply();
    }

    public long getRemoteConfigVersion() {
        return sPref.getLong(Key.KEY_REMOTE_CONFIG_CONFIG_VERSION, Long.valueOf(KikaMultiDexApplication.getAppContext().getString(R.string.remote_config_config_version)));
    }

    public void saveRemoteConfigAppVersionLatest(long version) {
        sEditor.putLong(Key.KEY_REMOTE_CONFIG_APP_VERSION_LATEST, version);
        apply();
    }

    public long getRemoteConfigAppVersionLatest() {
        return sPref.getLong(Key.KEY_REMOTE_CONFIG_APP_VERSION_LATEST, BuildConfig.VERSION_CODE);
    }

    public void saveRemoteConfigAppVersionMin(long version) {
        sEditor.putLong(Key.KEY_REMOTE_CONFIG_APP_VERSION_MIN, version);
        apply();
    }

    public long getRemoteConfigAppVersionMin() {
        return sPref.getLong(Key.KEY_REMOTE_CONFIG_APP_VERSION_MIN, 0);
    }
}