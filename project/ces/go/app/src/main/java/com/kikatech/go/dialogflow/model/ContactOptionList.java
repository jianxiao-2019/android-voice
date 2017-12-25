package com.kikatech.go.dialogflow.model;

import android.os.Parcel;

import com.kikatech.go.util.AppInfo;

/**
 * @author SkeeterWang Created on 2017/12/22.
 */

public class ContactOptionList extends OptionList {
    private static final String TAG = "ContactOptionList";

    private String avatar;
    private AppInfo appInfo;

    public ContactOptionList(byte requestType) {
        super(requestType);
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public AppInfo getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(AppInfo appInfo) {
        this.appInfo = appInfo;
    }


    /*---------- Parcelable impl ----------*/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(avatar);
    }

    private ContactOptionList(Parcel in) {
        super(in);
        avatar = in.readString();
    }

    public static final Creator<ContactOptionList> CREATOR = new Creator<ContactOptionList>() {
        @Override
        public ContactOptionList createFromParcel(Parcel in) {
            return new ContactOptionList(in);
        }

        @Override
        public ContactOptionList[] newArray(int size) {
            return new ContactOptionList[size];
        }
    };
}
