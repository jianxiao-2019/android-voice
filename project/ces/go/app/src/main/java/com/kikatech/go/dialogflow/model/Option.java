package com.kikatech.go.dialogflow.model;

import android.os.Parcel;

/**
 * @author SkeeterWang Created on 2017/11/16.
 */

public class Option extends UiModel {
    private String displayText;
    private String actionText;

    public Option(String displayText) {
        this(displayText, displayText);
    }

    public Option(String displayText, String actionText) {
        this.displayText = displayText;
        this.actionText = actionText;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getActionText() {
        return actionText;
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
        dest.writeString(displayText);
    }

    protected Option(Parcel in) {
        displayText = in.readString();
    }

    public static final Creator<Option> CREATOR = new Creator<Option>() {
        @Override
        public Option createFromParcel(Parcel in) {
            return new Option(in);
        }

        @Override
        public Option[] newArray(int size) {
            return new Option[size];
        }
    };

}