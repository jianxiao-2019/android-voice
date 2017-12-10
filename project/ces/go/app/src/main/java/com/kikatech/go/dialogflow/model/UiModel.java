package com.kikatech.go.dialogflow.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author SkeeterWang Created on 2017/11/28.
 */

abstract class UiModel implements Parcelable {
    abstract void print(String MAIN_TAG);

    UiModel() {
    }

    int iconRes;

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    public int getIconRes() {
        return iconRes;
    }

    UiModel(Parcel in) {
        iconRes = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(iconRes);
    }
}
