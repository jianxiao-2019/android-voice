package com.kikatech.go.dialogflow;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SkeeterWang Created on 2017/11/15.
 */

public abstract class BaseSceneStage extends SceneStage {

    public static final String EXTRA_OPTIONS_TITLE = "extra_options_title";
    public static final String EXTRA_OPTIONS_LIST = "extra_options_list";

    public BaseSceneStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    public String getOptionTextToSpeak(String text, OptionList optionList) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(text)) {
            stringBuilder.append(text).append("\n");
        }
        if (optionList != null && !optionList.isEmpty()) {
            Option option;
            for (int i = 0; i < optionList.size(); i++) {
                option = optionList.get(i);
                if (option != null) {
                    stringBuilder.append(String.valueOf(i+1)).append(" ").append(option.getDisplayText()).append("\n");
                }
            }
        }
        return stringBuilder.toString();
    }

    public static class OptionList implements Parcelable {
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
            in.readList( options, Option.class.getClassLoader() );
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

    public static class Option implements Parcelable {
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
}
