package com.kikatech.go.dialogflow.model;

import android.os.Parcel;

/**
 * @author SkeeterWang Created on 2017/12/10.
 */

public class TtsText extends UiModel {
    private String uiText;

    public TtsText(String text) {
        this(0/* Invalid resource id*/, text);
    }

    public TtsText(int iconRes, String text) {
        this.iconRes = iconRes;
        this.uiText = text;
    }

    public void setUiText(String text) {
        this.uiText = text;
    }

    public String getUiText() {
        return uiText;
    }


    @Override
    void print(String MAIN_TAG) {
    }


    /*---------- Parcelable impl ----------*/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uiText);
    }

    private TtsText(Parcel in) {
        super(in);
        uiText = in.readString();
    }

    public static final Creator<TtsText> CREATOR = new Creator<TtsText>() {
        @Override
        public TtsText createFromParcel(Parcel in) {
            return new TtsText(in);
        }

        @Override
        public TtsText[] newArray(int size) {
            return new TtsText[size];
        }
    };
}
