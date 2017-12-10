package com.kikatech.go.dialogflow.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.kikatech.go.util.AppInfo;
import com.kikatech.go.util.LogUtil;

/**
 * @author SkeeterWang Created on 2017/11/28.
 */

public class UserInfo extends UiModel {
    private static final String TAG = "UserInfo";

    private String avatar;
    private String name;
    private AppInfo appInfo;

    public UserInfo(String avatar, String name, AppInfo appInfo) {
        this.avatar = avatar;
        this.name = name;
        this.appInfo = appInfo;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getName() {
        return name;
    }

    public AppInfo getAppInfo() {
        return appInfo;
    }


    @Override
    protected void print(String MAIN_TAG) {
        if (LogUtil.DEBUG) {
            LogUtil.logd(MAIN_TAG, String.format("[%1$s] avatar: %2$s", TAG, avatar));
            LogUtil.logd(MAIN_TAG, String.format("[%1$s] name: %2$s", TAG, name));
            LogUtil.logd(MAIN_TAG, String.format("[%1$s] appInfo: %2$s", TAG, appInfo.getAppName()));
        }
    }


    /*---------- Parcelable impl ----------*/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(avatar);
        dest.writeString(name);
        dest.writeSerializable(appInfo);
    }

    UserInfo(Parcel in) {
        super(in);
        avatar = in.readString();
        name = in.readString();
        appInfo = (AppInfo) in.readSerializable();
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };
}
