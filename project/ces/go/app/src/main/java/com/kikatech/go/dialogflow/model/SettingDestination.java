package com.kikatech.go.dialogflow.model;

import android.support.annotation.IntDef;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kikatech.go.R;
import com.kikatech.go.dialogflow.model.json.key.SettingKeys;
import com.kikatech.go.util.LogUtil;

/**
 * @author SkeeterWang Created on 2017/12/20.
 */

public class SettingDestination {
    private static final String TAG = "SettingDestination";

    public static final int TYPE_CUSTOMIZED = 0;
    public static final int TYPE_DEFAULT_HOME = 1;
    public static final int TYPE_DEFAULT_WORK = 2;

    @IntDef({TYPE_CUSTOMIZED, TYPE_DEFAULT_HOME, TYPE_DEFAULT_WORK})
    public @interface Type {
    }

    @Expose
    @SerializedName(SettingKeys.KEY_DESTINATION_TYPE)
    @Type
    private int type;

    @Expose
    @SerializedName(SettingKeys.KEY_DESTINATION_NAME)
    private String name;

    @Expose
    @SerializedName(SettingKeys.KEY_DESTINATION_ADDRESS)
    private String address;


    public SettingDestination(@Type int type, String name) {
        this.type = type;
        this.name = name;
    }


    public int getType() {
        return type;
    }

    public void setType(@Type int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getTypeIconRes() {
        switch (type) {
            case TYPE_DEFAULT_HOME:
                return R.drawable.selector_settings_ic_home;
            case TYPE_DEFAULT_WORK:
                return R.drawable.selector_settings_ic_work;
            default:
            case TYPE_CUSTOMIZED:
                return R.drawable.selector_settings_ic_place;
        }
    }

    public boolean canRemove() {
        switch (type) {
            case TYPE_DEFAULT_HOME:
            case TYPE_DEFAULT_WORK:
                return false;
            default:
            case TYPE_CUSTOMIZED:
                return true;
        }
    }


    public void print() {
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, String.format("type: %s", type));
            LogUtil.logv(TAG, String.format("name: %s", name));
            LogUtil.logv(TAG, String.format("address: %s", address));
        }
    }
}
