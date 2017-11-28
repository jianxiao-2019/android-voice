package com.kikatech.go.dialogflow.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author SkeeterWang Created on 2017/11/16.
 */

public class Option extends UiModel {
    private String displayText;
    private String nextSceneAction;

    public Option(String displayText, String nextSceneAction) {
        this.displayText = displayText;
        this.nextSceneAction = nextSceneAction;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getNextSceneAction() {
        return nextSceneAction;
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
        dest.writeString(nextSceneAction);
    }

    protected Option(Parcel in) {
        displayText = in.readString();
        nextSceneAction = in.readString();
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