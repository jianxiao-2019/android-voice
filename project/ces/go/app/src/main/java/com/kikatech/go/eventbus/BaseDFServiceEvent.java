package com.kikatech.go.eventbus;

import android.os.Bundle;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author SkeeterWang Created on 2017/12/1.
 */

class BaseDFServiceEvent {

    private String action;
    private Bundle extras = new Bundle();

    BaseDFServiceEvent(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public Bundle getExtras() {
        return extras;
    }

    public void putExtra(String key, String value) {
        extras.putString(key, value);
    }

    public void putExtra(String key, boolean value) {
        extras.putBoolean(key, value);
    }

    public void putExtra(String key, Bundle value) {
        extras.putBundle(key, value);
    }

    public void putExtra(String key, byte value) {
        extras.putByte(key, value);
    }

    public void putExtra(String key, Serializable value) {
        extras.putSerializable(key, value);
    }

    public void putExtra(String key, ArrayList<? extends Parcelable> value) {
        extras.putParcelableArrayList(key, value);
    }
}
