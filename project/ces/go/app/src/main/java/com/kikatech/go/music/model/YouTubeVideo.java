package com.kikatech.go.music.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemContentDetails;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchResultSnippet;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;

/**
 * @author wangskeeter Created on 2016/4/23.
 */
public class YouTubeVideo implements Parcelable {
    private String videoId;
    private String title;
    private String thumbnail;
    private String streamUrl;

    public YouTubeVideo(@NonNull SearchResult result) {
        ResourceId resId = result.getId();
        this.videoId = resId != null ? resId.getVideoId() : null;
        SearchResultSnippet srs = result.getSnippet();
        this.title = srs != null ? srs.getTitle() : null;
        ThumbnailDetails tds = srs != null ? srs.getThumbnails() : null;
        Thumbnail tb = tds != null ? tds.getDefault() : null;
        this.thumbnail = tb != null ? tb.getUrl() : null;
    }

    public YouTubeVideo(@NonNull PlaylistItem result) {
        PlaylistItemContentDetails picd = result.getContentDetails();
        this.videoId = picd != null ? picd.getVideoId() : null;
        PlaylistItemSnippet pis = result.getSnippet();
        this.title = pis != null ? pis.getTitle() : null;
        ThumbnailDetails tds = pis != null ? pis.getThumbnails() : null;
        Thumbnail tb = tds != null ? tds.getDefault() : null;
        this.thumbnail = tb != null ? tb.getUrl() : null;
    }


    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }


    /*---------- Parcelable impl ----------*/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(videoId);
        parcel.writeString(title);
        parcel.writeString(thumbnail);
        parcel.writeString(streamUrl);
    }

    private YouTubeVideo(Parcel in) {
        videoId = in.readString();
        title = in.readString();
        thumbnail = in.readString();
        streamUrl = in.readString();
    }

    public static final Parcelable.Creator<YouTubeVideo> CREATOR = new Parcelable.Creator<YouTubeVideo>() {
        @Override
        public YouTubeVideo createFromParcel(Parcel source) {
            return new YouTubeVideo(source);
        }

        @Override
        public YouTubeVideo[] newArray(int size) {
            return new YouTubeVideo[size];
        }
    };
}
