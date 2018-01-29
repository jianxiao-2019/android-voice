package com.kikatech.voicesdktester.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

/**
 * Created by ryanlin on 13/10/2017.
 */

public class PreferenceUtil {

    public static final String KEY_DEBUG = "pref_debug";

    public static final String KEY_INIT_HINT = "pref_key_init_hint";
    public static final String KEY_UNSENT_HINT = "pref_key_unsent_hint";

    public static final String KEY_LANGUAGE = "key_language";

    public static final String KEY_VOICE_ENGINE = "voice_engine";
    public static final String KEY_SERVER_LOCATION = "server_location";

    private static SharedPreferences getSharedPreference(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setBoolean(@NonNull Context context, @NonNull String key, boolean value) {
        getSharedPreference(context).edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(@NonNull Context context, @NonNull String key, boolean defValue) {
        return getSharedPreference(context).getBoolean(key, defValue);
    }

    public static void setInt(@NonNull Context context, @NonNull String key, int value) {
        getSharedPreference(context).edit().putInt(key, value).apply();
    }

    public static int getInt(@NonNull Context context, @NonNull String key, int defValue) {
        return getSharedPreference(context).getInt(key, defValue);
    }

    public static void setString(@NonNull Context context, @NonNull String key, String value) {
        getSharedPreference(context).edit().putString(key, value).apply();
    }

    public static String getString(@NonNull Context context, @NonNull String key, String defValue) {
        return getSharedPreference(context).getString(key, defValue);
    }
}
