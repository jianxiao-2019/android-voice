package com.kikatech.go.music.google.serivce;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.kikatech.go.R;
import com.kikatech.go.music.model.YouTubeVideo;
import com.kikatech.go.ui.KikaMultiDexApplication;
import com.kikatech.go.util.BackgroundThread;
import com.kikatech.go.util.LogUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author SkeeterWang Created on 2018/1/15.
 */

public class YouTubeAPI {
    private static final String TAG = "YoutubeAPI";

    private static final String DEVELOPER_KEY = "AIzaSyBTCFdcfSCeWPYWONMWOd9Z3bnUKuLLApI";

    private static final String SEARCH_TYPE_VIDEO = "video";
    private static final long MAX_RESULT_SIZE = 12L;
    private static final long MAX_RELATED_SIZE = 25L;

    private static YouTubeAPI sIns;
    private static YouTube mYouTube;

    public static synchronized YouTubeAPI getIns() {
        if (sIns == null) {
            sIns = new YouTubeAPI();
        }
        return sIns;
    }

    private YouTubeAPI() {
        mYouTube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(com.google.api.client.http.HttpRequest request) throws IOException {
            }
        }).setApplicationName(KikaMultiDexApplication.getAppContext().getString(R.string.app_name)).build();
    }

    public void searchVideo(@NonNull final String videoKeyword, final IYoutubeApiCallback callback) {
        searchVideo(videoKeyword, null, callback);
    }

    public void searchVideo(@NonNull final String videoKeyword, @Nullable final String videoCategory, final IYoutubeApiCallback callback) {
        BackgroundThread.post(new Runnable() {
            @Override
            public void run() {
                ArrayList<YouTubeVideo> resultVideos = new ArrayList<>();
                try {
                    // Define the API request for retrieving search results.
                    YouTube.Search.List search = mYouTube.search().list("id,snippet");

                    // Set your developer key from the Google Cloud Console for
                    // non-authenticated requests. See:
                    // https://cloud.google.com/console
                    search.setKey(DEVELOPER_KEY);
                    search.setQ(videoKeyword);

                    // Restrict the search results to only include videos. See:
                    // https://developers.google.com/youtube/v3/docs/search/list#type
                    search.setType(SEARCH_TYPE_VIDEO);

                    // The category 'Music' is ID: 10. See: https://gist.github.com/dgp/1b24bf2961521bd75d6c
                    // https://developers.google.com/youtube/v3/docs/search/list#videoCategoryId
                    if (!TextUtils.isEmpty(videoCategory)) {
                        search.setVideoCategoryId(videoCategory);
                    }

                    // To increase efficiency, only retrieve the fields that the
                    // application uses.
                    //search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
                    search.setMaxResults(MAX_RESULT_SIZE);

                    SearchListResponse searchResponse = search.execute();

                    List<SearchResult> results = searchResponse != null ? searchResponse.getItems() : null;

                    if (results != null && !results.isEmpty()) {
                        for (SearchResult searchResult : results) {
                            YouTubeVideo youTubeVideo = new YouTubeVideo(searchResult);
                            resultVideos.add(youTubeVideo);
                        }
                    }
                } catch (IOException e) {
                    if (LogUtil.DEBUG) {
                        LogUtil.printStackTrace(TAG, e.getMessage(), e);
                    }
                }
                if (callback != null) {
                    callback.onLoaded(resultVideos);
                }
            }
        });
    }

    public void searchRelatedVideos(@NonNull final YouTubeVideo resultVideo, final IYoutubeApiCallback callback) {
        BackgroundThread.getHandler().post(new Runnable() {
            @Override
            public void run() {
                ArrayList<YouTubeVideo> relatedVideos = new ArrayList<>();
                try {
                    // Define the API request for retrieving search results.
                    YouTube.Search.List search = mYouTube.search().list("id,snippet");

                    // Set your developer key from the Google Cloud Console for
                    // non-authenticated requests. See:
                    // https://cloud.google.com/console
                    search.setKey(DEVELOPER_KEY);
                    search.setRelatedToVideoId(resultVideo.getVideoId());

                    // Restrict the search results to only include videos. See:
                    // https://developers.google.com/youtube/v3/docs/search/list#type
                    search.setType(SEARCH_TYPE_VIDEO);

                    // To increase efficiency, only retrieve the fields that the
                    // application uses.
                    //search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
                    search.setMaxResults(MAX_RELATED_SIZE);

                    SearchListResponse searchResponse = search.execute();

                    List<SearchResult> results = searchResponse != null ? searchResponse.getItems() : null;

                    if (results != null && !results.isEmpty()) {
                        relatedVideos.add(resultVideo);
                        for (SearchResult searchResult : results) {
                            YouTubeVideo youTubeVideo = new YouTubeVideo(searchResult);
                            relatedVideos.add(youTubeVideo);
                        }
                    }
                } catch (IOException e) {
                    if (LogUtil.DEBUG) {
                        LogUtil.printStackTrace(TAG, e.getMessage(), e);
                    }
                }

                if (callback != null) {
                    callback.onLoaded(relatedVideos);
                }
            }
        });
    }

    public interface IYoutubeApiCallback {
        void onLoaded(ArrayList<YouTubeVideo> result);
    }
}