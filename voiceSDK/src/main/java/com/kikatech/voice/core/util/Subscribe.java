package com.kikatech.voice.core.util;

import android.text.TextUtils;

import com.kikatech.voice.core.dialogflow.DialogObserver;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tianli on 17-11-12.
 */

public class Subscribe<T> {

    private HashMap<String, List<T>> mSubscribers = new HashMap<>();

    public synchronized void register(String key, T observer) {
        if (!TextUtils.isEmpty(key) && observer != null) {
            if (mSubscribers.get(key) == null) {
                List<T> objectList = new ArrayList<>();
                objectList.add(observer);
                mSubscribers.put(key, objectList);
            } else {
                if (!mSubscribers.get(key).contains(observer)) {
                    mSubscribers.get(key).add(observer);
                }
            }
        }
    }

    public synchronized void unregister(String key, T object) {
        if (!TextUtils.isEmpty(key) && object != null) {
            List<T> list = mSubscribers.get(key);
            if (list != null) {
                list.remove(object);
                if (list.isEmpty()) {
                    mSubscribers.remove(key);
                }
            }
        }
    }

    public List<T> list(String key) {
        List<T> list = new ArrayList<>();
        synchronized (this) {
            if (mSubscribers.containsKey(key)) {
                list.addAll(mSubscribers.get(key));
            }
        }
        return list;
    }

    public boolean contains(String key) {
        return mSubscribers.containsKey(key);
    }
}
