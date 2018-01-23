package com.kikatech.go.view.youtube.model;

import android.content.res.AssetFileDescriptor;
import android.net.Uri;

import com.kikatech.go.util.LogUtil;

import java.util.Map;

/**
 * @author SkeeterWang Created on 2018/1/19.
 */

public class VideoInfo {
    private String TAG = "VideoInfo";

    private Map<String, String> mHeaders;
    private Uri mUri;
    private AssetFileDescriptor mAssetFileDescriptor;
    private int mSeekInSeconds = 0;
    private String mCaptionFilePath;

    private VideoInfo(Map<String, String> headers, Uri uri, AssetFileDescriptor afd, int seekInSeconds, String captionFilePath) {
        this.mHeaders = headers;
        this.mUri = uri;
        this.mAssetFileDescriptor = afd;
        this.mSeekInSeconds = seekInSeconds;
        this.mCaptionFilePath = captionFilePath;
    }


    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    public Uri getUri() {
        return mUri;
    }

    public AssetFileDescriptor getAssetFileDescriptor() {
        return mAssetFileDescriptor;
    }

    public int getSeekInSeconds() {
        return mSeekInSeconds;
    }

    public String getCaptionFilePath() {
        return mCaptionFilePath;
    }


    public void print() {
        print(TAG);
    }

    public void print(String TAG) {
        if (LogUtil.DEBUG) {
            if (mHeaders != null && !mHeaders.isEmpty()) {
                for (String key : mHeaders.keySet()) {
                    String value = mHeaders.get(key);
                    LogUtil.logd(TAG, String.format("[VideoInfo-headers] key: %1$s, value: %2$s", key, value));
                }
            } else {
                LogUtil.logd(TAG, "[VideoInfo-headers] empty");
            }
            LogUtil.logd(TAG, String.format("[VideoInfo] uri: %1$s", mUri != null ? mUri.toString() : null));
            LogUtil.logd(TAG, String.format("[VideoInfo] assetFileDescriptor: %1$s", mAssetFileDescriptor != null ? mAssetFileDescriptor.toString() : null));
            LogUtil.logd(TAG, String.format("[VideoInfo] mSeekInSeconds: %1$s", mSeekInSeconds));
            LogUtil.logd(TAG, String.format("[VideoInfo] mCaptionFilePath: %1$s", mCaptionFilePath));
        }
    }


    public static final class Builder {
        private Map<String, String> mHeaders;
        private Uri mUri;
        private AssetFileDescriptor mAssetFileDescriptor;
        private int mSeekInSeconds = 0;
        private String mCaptionFilePath;

        public Builder setHeaders(Map<String, String> headers) {
            this.mHeaders = headers;
            return this;
        }

        public Builder setPath(String path) {
            this.mUri = Uri.parse(path);
            return this;
        }

        public Builder setUri(Uri uri) {
            this.mUri = uri;
            return this;
        }

        public Builder setAssetFileDescriptor(AssetFileDescriptor afd) {
            this.mAssetFileDescriptor = afd;
            return this;
        }

        public Builder setSeekInSeconds(int seekInSeconds) {
            this.mSeekInSeconds = seekInSeconds;
            return this;
        }

        public Builder setCaptionFilePath(String captionFilePath) {
            this.mCaptionFilePath = captionFilePath;
            return this;
        }

        public VideoInfo build() {
            return new VideoInfo(mHeaders, mUri, mAssetFileDescriptor, mSeekInSeconds, mCaptionFilePath);
        }
    }
}