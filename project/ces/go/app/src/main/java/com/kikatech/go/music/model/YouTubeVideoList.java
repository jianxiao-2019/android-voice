package com.kikatech.go.music.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SkeeterWang Created on 2018/1/22.
 */

public class YouTubeVideoList implements Parcelable {
    private static final String TAG = "YouTubeVideoList";

    private static final int LIST_TYPE_KEYWORD_RESULT = 0;
    private static final int LIST_TYPE_RECOMMEND = 1;

    @IntDef({LIST_TYPE_KEYWORD_RESULT, LIST_TYPE_RECOMMEND})
    public @interface ListType {
        int DEFAULT = LIST_TYPE_KEYWORD_RESULT;
        int KEYWORD_RESULT = LIST_TYPE_KEYWORD_RESULT;
        int RECOMMEND = LIST_TYPE_RECOMMEND;
    }


    @ListType
    private int mListType;
    private ArrayList<YouTubeVideo> mList = new ArrayList<>();
    private int mPlayingIndex;


    public YouTubeVideoList() {
        this(ListType.DEFAULT);
    }

    public YouTubeVideoList(@ListType int listType) {
        this.mListType = listType;
    }


    public void add(YouTubeVideo video) {
        mList.add(video);
    }

    public void addAll(@NonNull YouTubeVideoList listToAdd) {
        addAll(listToAdd.getList());
    }

    public void addAll(@NonNull List<YouTubeVideo> listToAdd) {
        mList.addAll(listToAdd);
    }

    public boolean remove(YouTubeVideo video) {
        return mList.remove(video);
    }

    public List<YouTubeVideo> subList(int fromIndex, int toIndex) {
        return mList.subList(fromIndex, toIndex);
    }

    public void clear() {
        mList.clear();
    }

    public int size() {
        return mList.size();
    }

    public boolean isEmpty() {
        return mList.isEmpty();
    }


    public YouTubeVideo next() {
        int nextIndex = mPlayingIndex + 1;
        mPlayingIndex = nextIndex < mList.size() ? nextIndex : 0;
        return mList.get(mPlayingIndex);
    }

    public YouTubeVideo prev() {
        int nextIndex = mPlayingIndex - 1;
        mPlayingIndex = nextIndex >= 0 ? nextIndex : mList.size() - 1;
        return mList.get(mPlayingIndex);
    }


    public void setListType(@ListType int listType) {
        this.mListType = listType;
    }

    @ListType
    public int getListType() {
        return mListType;
    }

    public ArrayList<YouTubeVideo> getList() {
        return mList;
    }

    public YouTubeVideo get(int index) {
        return index >= 0 && index < mList.size() ? mList.get(index) : null;
    }

    public YouTubeVideo getCurrent() {
        return !isEmpty() ? mList.get(mPlayingIndex) : null;
    }


    /*---------- Parcelable impl ----------*/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(mListType);
        parcel.writeList(mList);
        parcel.writeInt(mPlayingIndex);
    }

    private YouTubeVideoList(Parcel in) {
        mListType = in.readInt();
        in.readList(mList, YouTubeVideo.class.getClassLoader());
        mPlayingIndex = in.readInt();
    }

    public static final Parcelable.Creator<YouTubeVideoList> CREATOR = new Parcelable.Creator<YouTubeVideoList>() {
        @Override
        public YouTubeVideoList createFromParcel(Parcel source) {
            return new YouTubeVideoList(source);
        }

        @Override
        public YouTubeVideoList[] newArray(int size) {
            return new YouTubeVideoList[size];
        }
    };

}
