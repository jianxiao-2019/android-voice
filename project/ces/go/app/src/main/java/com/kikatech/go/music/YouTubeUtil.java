package com.kikatech.go.music;

import com.kikatech.go.dialogflow.UserSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author SkeeterWang Created on 2018/1/22.
 */

public class YouTubeUtil {
    private static final String TAG = "YouTubeUtil";

    public static final String DEVELOPER_KEY = "AIzaSyBTCFdcfSCeWPYWONMWOd9Z3bnUKuLLApI";

    public enum RecommendPlayList {
        POP("Pop", "PLFgquLnL59alCl_2TQvOiD5Vgm1hCaGSI"),
        HIP_HOP("Hip Hop", "PLH6pfBXQXHEC2uDmDy5oi3tHW6X8kZ2Jo"),
        ROCK("Rock", "PLhd1HyMTk3f5PzRjJzmzH7kkxjfdVoPPj"),
        EDM("EDM", "PLFPg_IUxqnZNnACUGsfn50DySIOVSkiKI"),
        LATIN("Latin", "PLcfQmtiAG0X-fmM85dPlql5wfYbmFumzQ"),
        COUNTRY("Country", "PLvLX2y1VZ-tFJCfRG7hi_OjIAyCriNUT2"),
        JAZZ("Jazz", "PLMcThd22goGYit-NKu2O8b4YMtwSTK9b9"),
        INDIE("Indie", "PLSn1U7lJJ1UnczTmNYXW8ZEjJsCxTZull");

        private String name;
        private String id;

        RecommendPlayList(String name, String id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public void setEnable(boolean isEnabled) {
            switch (this) {
                case POP:
                    UserSettings.saveSettingRecommendListPop(isEnabled);
                    break;
                case HIP_HOP:
                    UserSettings.saveSettingRecommendListHipHop(isEnabled);
                    break;
                case ROCK:
                    UserSettings.saveSettingRecommendListRock(isEnabled);
                    break;
                case EDM:
                    UserSettings.saveSettingRecommendListEDM(isEnabled);
                    break;
                case LATIN:
                    UserSettings.saveSettingRecommendListLatin(isEnabled);
                    break;
                case COUNTRY:
                    UserSettings.saveSettingRecommendListCountry(isEnabled);
                    break;
                case JAZZ:
                    UserSettings.saveSettingRecommendListJazz(isEnabled);
                    break;
                case INDIE:
                    UserSettings.saveSettingRecommendListIndie(isEnabled);
                    break;
            }
        }

        public boolean isEnable() {
            switch (this) {
                case POP:
                    return UserSettings.getSettingRecommendListPop();
                case HIP_HOP:
                    return UserSettings.getSettingRecommendListHipHop();
                case ROCK:
                    return UserSettings.getSettingRecommendListRock();
                case EDM:
                    return UserSettings.getSettingRecommendListEDM();
                case LATIN:
                    return UserSettings.getSettingRecommendListLatin();
                case COUNTRY:
                    return UserSettings.getSettingRecommendListCountry();
                case JAZZ:
                    return UserSettings.getSettingRecommendListJazz();
                case INDIE:
                    return UserSettings.getSettingRecommendListIndie();
            }
            return false;
        }

        public static RecommendPlayList randomList() {
            return RecommendPlayList.values()[new Random().nextInt(RecommendPlayList.values().length)];
        }

        public static List<RecommendPlayList> getAllPlaylist() {
            List<RecommendPlayList> allList = new ArrayList<>();
            allList.addAll(Arrays.asList(RecommendPlayList.values()));
            return allList;
        }
    }

    public static final String SEARCH_TYPE_VIDEO = "video";

    public static final long MAX_RESULT_SIZE = 12L;
    public static final long MAX_RELATED_SIZE = 25L;
    public static final long MAX_RECOMMEND_SIZE = 50L;


    public static List<RecommendPlayList> getEnabledPlayList() {
        List<RecommendPlayList> enabledList = new ArrayList<>();
        for (RecommendPlayList playList : RecommendPlayList.values()) {
            if (playList.isEnable()) {
                enabledList.add(playList);
            }
        }
        if (enabledList.isEmpty()) {
            enabledList.add(RecommendPlayList.randomList());
        }
        return enabledList;
    }
}
