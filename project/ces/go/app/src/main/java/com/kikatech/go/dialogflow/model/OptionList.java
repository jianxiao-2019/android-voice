package com.kikatech.go.dialogflow.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SkeeterWang Created on 2017/11/16.
 */

public class OptionList implements Parcelable {
    public static final byte REQUEST_TYPE_ORDINAL = 0x01;
    public static final byte REQUEST_TYPE_TEXT = 0x02;

    private byte requestType;
    private List<Option> options = new ArrayList<>();

    public OptionList(byte requestType) {
        this.requestType = requestType;
    }

    public void add(Option option) {
        this.options.add(option);
    }

    public int size() {
        return options.size();
    }

    public int indexOf(Option option) {
        return options.indexOf(option);
    }

    public boolean isEmpty() {
        return options.isEmpty();
    }

    public Option get(int index) {
        return index >= 0 && index < options.size() ? options.get(index) : null;
    }

    public byte getRequestType() {
        return requestType;
    }

    public List<Option> getList() {
        return options;
    }


    public String getTextToSpeak(String text) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(text)) {
            stringBuilder.append(text).append("\n");
        }
        if (options != null && !options.isEmpty()) {
            Option option;
            for (int i = 0; i < options.size(); i++) {
                option = options.get(i);
                if (option != null) {
                    stringBuilder.append(String.valueOf(i + 1)).append(" ").append(option.getDisplayText()).append("\n");
                }
            }
        }
        return stringBuilder.toString();
    }


    public static String getDefaultOptionListTitle() {
        return "You can Say";
    }

    public static OptionList getDefaultOptionList() {
        OptionList optionList = new OptionList(OptionList.REQUEST_TYPE_TEXT);
        optionList.add(new Option("Navigate", null));
        optionList.add(new Option("Message", null));
        optionList.add(new Option("Make a call", null));
        return optionList;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(requestType);
        dest.writeList(options);
    }

    protected OptionList(Parcel in) {
        requestType = in.readByte();
        in.readList(options, Option.class.getClassLoader());
    }

    public static final Creator<OptionList> CREATOR = new Creator<OptionList>() {
        @Override
        public OptionList createFromParcel(Parcel in) {
            return new OptionList(in);
        }

        @Override
        public OptionList[] newArray(int size) {
            return new OptionList[size];
        }
    };
}
