package com.kikatech.go.dialogflow.model;

import android.os.Parcel;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.SceneUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SkeeterWang Created on 2017/11/16.
 */

public class OptionList extends UiModel {
    public static final byte REQUEST_TYPE_ORDINAL = 0x01;
    public static final byte REQUEST_TYPE_TEXT = 0x02;
    public static final byte REQUEST_TYPE_ORDINAL_TO_TEXT = 0x03;

    private byte requestType;
    private String title;
    private List<Option> options = new ArrayList<>();
    private boolean isDefaultList;

    public OptionList(byte requestType) {
        this.requestType = requestType;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
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

    public boolean isDefaultList() {
        return isDefaultList;
    }


    public String getTextToSpeak(String title) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(title)) {
            stringBuilder.append(title).append("\n");
        }
        if (options != null && !options.isEmpty()) {
            int LIST_SIZE = options.size();
            Option option;
            for (int i = 0; i < LIST_SIZE; i++) {
                option = options.get(i);
                if (option != null) {
                    boolean isLast = (i == LIST_SIZE - 1);
                    stringBuilder.append(option.getDisplayText());
                    if (!isLast) {
                        stringBuilder.append(" or ");
                    }
                }
            }
        }
        return stringBuilder.toString();
    }


    public static OptionList getDefaultOptionList() {
        OptionList optionList = new OptionList(OptionList.REQUEST_TYPE_TEXT);
        optionList.setTitle("You can say");
        optionList.setIconRes(SceneUtil.ICON_TRIGGER);
        optionList.isDefaultList = true;
        optionList.add(new Option("Navigate", "Navigate"));
        optionList.add(new Option("Message", "Message"));
        optionList.add(new Option("Make a call", "Make a call"));
        optionList.add(new Option("Play music", "Music"));
        return optionList;
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
        dest.writeByte(requestType);
        dest.writeString(title);
        dest.writeList(options);
    }

    protected OptionList(Parcel in) {
        super(in);
        requestType = in.readByte();
        title = in.readString();
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
