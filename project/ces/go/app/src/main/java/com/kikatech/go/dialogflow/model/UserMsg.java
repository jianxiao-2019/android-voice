package com.kikatech.go.dialogflow.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.kikatech.go.util.AppInfo;
import com.kikatech.go.util.LogUtil;

/**
 * @author SkeeterWang Created on 2017/11/28.
 */

public class UserMsg extends UserInfo {
    private static final String TAG = "UserMsg";

    private String msg;

    public UserMsg(String avatar, String name, AppInfo appInfo, String msg) {
        super(avatar, name, appInfo);
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }


    @Override
    protected void print(String MAIN_TAG) {
        if (LogUtil.DEBUG) {
            super.print(MAIN_TAG);
            LogUtil.logd(MAIN_TAG, String.format("[%1$s] msg: %2$s", TAG, msg));
        }
    }


    /*---------- Parcelable impl ----------*/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(msg);
    }

    protected UserMsg(Parcel in) {
        super(in);
        msg = in.readString();
    }

    public static final Creator<UserMsg> CREATOR = new Creator<UserMsg>() {
        @Override
        public UserMsg createFromParcel(Parcel in) {
            return new UserMsg(in);
        }

        @Override
        public UserMsg[] newArray(int size) {
            return new UserMsg[size];
        }
    };
}
