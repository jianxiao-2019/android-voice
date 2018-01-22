package com.kikatech.go.dialogflow.music;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.util.LogUtil;

/**
 * @author SkeeterWang Created on 2018/1/22.
 */

public class MusicSceneUtil {
    private static final String TAG = "MusicSceneUtil";

    private static final String DF_PARAM_SONG_NAME = "song_name";

    public static String parseSongName(@NonNull Bundle params) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "params : " + params);
        }
        String songName = params.getString(DF_PARAM_SONG_NAME, null);
        if (!TextUtils.isEmpty(songName)) {
            try {
                songName = songName.substring(1, songName.length() - 1);
            } catch (Exception ignore) {
            }
        }
        return songName;
    }
}
