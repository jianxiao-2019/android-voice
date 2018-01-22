package com.kikatech.go.services.presenter;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.SparseArray;

import com.kikatech.go.music.google.serivce.YouTubeAPI;
import com.kikatech.go.music.model.YouTubeVideo;
import com.kikatech.go.music.model.YouTubeVideoList;
import com.kikatech.go.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

/**
 * @author SkeeterWang Created on 2018/1/17.
 */

public class YouTubeExtractorManager {
    private static final String TAG = "YouTubeExtractorManager";

    private static YouTubeExtractorManager sIns;

    private List<YouTubeExtractTask> mExtractTaskList = new ArrayList<>();
    //    private List<YouTubeVideo> mResultsList = new ArrayList<>(); // 5 main videos
//    private HashMap<String, List<YouTubeVideo>> mPlayLists = new HashMap<>(); // Complete play list with one main video and its related videos
    private YouTubeVideoList mPlayingList = new YouTubeVideoList(); // one of mRelatedVideosList

    public static synchronized YouTubeExtractorManager getIns() {
        if (sIns == null) {
            sIns = new YouTubeExtractorManager();
        }
        return sIns;
    }

    private YouTubeExtractorManager() {
    }


    public synchronized void loadPlayList(final Context context, final YouTubeVideoList list, final IExtractListener listener) {
        final YouTubeVideo mainVideo = list != null && !list.isEmpty() ? list.get(0) : null;
        if (mainVideo == null) {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "error: main video is null");
            }
            if (listener != null) {
                listener.onError();
            }
            return;
        }
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "mainVideo: " + mainVideo.getTitle());
        }
        switch (list.getListType()) {
            case YouTubeVideoList.ListType.KEYWORD_RESULT:
                loadCompleteList(context, mainVideo, listener);
                break;
            case YouTubeVideoList.ListType.RECOMMEND:
                loadRecommendList(context, list, mainVideo, listener);
                break;
        }
    }

    private synchronized void loadCompleteList(final Context context, @NonNull final YouTubeVideo mainVideo, final IExtractListener listener) {
        mPlayingList.clear();
        mPlayingList.setListType(YouTubeVideoList.ListType.KEYWORD_RESULT);
        YouTubeAPI.getIns().searchRelatedVideos(mainVideo, new YouTubeAPI.IYoutubeApiCallback() {
            @Override
            public void onLoaded(YouTubeVideoList result) {
                if (LogUtil.DEBUG) {
                    LogUtil.logv(TAG, "mainVideo: " + mainVideo.getTitle());
                }
                if (result != null && !result.isEmpty()) {
                    mPlayingList.addAll(result);
                    loadVideo(context, mainVideo, listener);
                } else {
                    if (listener != null) {
                        listener.onError();
                    }
                }
            }
        });
    }

    private synchronized void loadRecommendList(final Context context, @NonNull final YouTubeVideoList list, @NonNull final YouTubeVideo mainVideo, final IExtractListener listener) {
        mPlayingList.clear();
        mPlayingList.setListType(YouTubeVideoList.ListType.RECOMMEND);
        mPlayingList.addAll(list);
        loadVideo(context, mainVideo, listener);
    }


    public synchronized void next(Context context, final IExtractListener listener) {
        YouTubeVideo nextVideo = mPlayingList.next();
        if (!TextUtils.isEmpty(nextVideo.getStreamUrl())) {
            if (listener != null) {
                listener.onLoaded(nextVideo);
            }
        } else {
            loadVideo(context, nextVideo, listener);
        }
    }

    public synchronized void prev(Context context, final IExtractListener listener) {
        YouTubeVideo prevVideo = mPlayingList.prev();
        if (!TextUtils.isEmpty(prevVideo.getStreamUrl())) {
            if (listener != null) {
                listener.onLoaded(prevVideo);
            }
        } else {
            loadVideo(context, prevVideo, listener);
        }
    }


    private synchronized void loadVideo(Context context, final YouTubeVideo video, final IExtractListener listener) {
        if (TextUtils.isEmpty(video.getVideoId()) || video.getStreamUrl() != null) {
            if (listener != null) {
                listener.onError();
            }
            return;
        }
        YouTubeExtractTask task = new YouTubeExtractTask(context, new YouTubeExtractTask.IExtractTaskListener() {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta) {
                String videoUrl = getVideoUrl(ytFiles);
                if (TextUtils.isEmpty(videoUrl)) {
                    if (listener != null) {
                        listener.onError();
                    }
                    return;
                }
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, String.format("[VideoInfo] title: %1$s, id: %2$s, url: %3$s", videoMeta.getTitle(), videoMeta.getVideoId(), videoUrl));
                }
                video.setStreamUrl(videoUrl);
                if (listener != null) {
                    listener.onLoaded(video);
                }
            }
        });
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, video.getVideoId());
        mExtractTaskList.add(task);
    }

    private String getVideoUrl(SparseArray<YtFile> ytFiles) {
        if (ytFiles == null || ytFiles.size() <= 0) {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, "YtFile list is empty.");
            }
            return null;
        }
        for (int iTag : ACCEPT_ITAG_LIST) {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, String.format("fetching iTag: %s", iTag));
            }
            if (ytFiles.get(iTag) != null) {
                final String downloadUrl = ytFiles.get(iTag).getUrl();
                if (!TextUtils.isEmpty(downloadUrl)) {
                    return downloadUrl;
                }
            }
        }
        return null;
    }


    public synchronized void clearTasks() {
        for (YouTubeExtractTask task : mExtractTaskList) {
            if (!task.isCancelled()) {
                task.cancel(true);
            }
        }
        mExtractTaskList.clear();
    }


    /**
     * <P>META_MAP.put( 18, new Meta( 18, "mp4", 360, Meta.VCodec.H264, Meta.ACodec.AAC, 96, false ) );</P>
     * <P>META_MAP.put( 43, new Meta( 43, "webm", 360, Meta.VCodec.VP8, Meta.ACodec.VORBIS, 128, false ) );</P>
     * <P>META_MAP.put( 5, new Meta( 5, "flv", 240, Meta.VCodec.H263, Meta.ACodec.MP3, 64, false ) );</P>
     * <P>META_MAP.put( 36, new Meta( 36, "3gp", 240, Meta.VCodec.MPEG4, Meta.ACodec.AAC, 32, false ) );</P>
     * <P>META_MAP.put( 17, new Meta( 17, "3gp", 144, Meta.VCodec.MPEG4, Meta.ACodec.AAC, 24, false ) );</P>
     * <P>META_MAP.put( 22, new Meta( 22, "mp4", 720, Meta.VCodec.H264, Meta.ACodec.AAC, 192, false ) );</P>
     **/
    private static final int[] ACCEPT_ITAG_LIST = new int[]{94, 18, 43, 5, 36, 17, 22};


    private static final class YouTubeExtractTask extends YouTubeExtractor {
        private IExtractTaskListener mListener;

        private YouTubeExtractTask(Context con, IExtractTaskListener listener) {
            super(con);
            this.mListener = listener;
        }

        @Override
        protected void onExtractionComplete(SparseArray<YtFile> sparseArray, VideoMeta videoMeta) {
            if (mListener != null) {
                mListener.onExtractionComplete(sparseArray, videoMeta);
            }
        }

        private interface IExtractTaskListener {
            void onExtractionComplete(SparseArray<YtFile> sparseArray, VideoMeta videoMeta);
        }
    }


    public interface IExtractListener {
        void onLoaded(YouTubeVideo loadedVideo);

        void onError();
    }
}
