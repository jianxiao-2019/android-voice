package com.kikatech.go.music;

/**
 * @author SkeeterWang Created on 2018/1/22.
 */

public class YouTubeUtil {
    private static final String TAG = "YouTubeUtil";

    public static final String DEVELOPER_KEY = "AIzaSyBTCFdcfSCeWPYWONMWOd9Z3bnUKuLLApI";

    private static final String RECOMMEND_PLAYLIST_TW = "PLFgquLnL59amN9tYr7o2a60yFUfzQO3sU";
    private static final String RECOMMEND_PLAYLIST_UA = "PLFgquLnL59alCl_2TQvOiD5Vgm1hCaGSI";
    public static final String RECOMMEND_PLAYLIST = RECOMMEND_PLAYLIST_UA;// DeviceUtil.isTaiwan() ? RECOMMEND_PLAYLIST_TW : RECOMMEND_PLAYLIST_UA;

    public static final String SEARCH_TYPE_VIDEO = "video";

    public static final long MAX_RESULT_SIZE = 12L;
    public static final long MAX_RELATED_SIZE = 25L;
    public static final long MAX_RECOMMEND_SIZE = 50L;
}
