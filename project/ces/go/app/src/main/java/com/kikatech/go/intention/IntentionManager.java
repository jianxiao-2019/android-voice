package com.kikatech.go.intention;

import com.kikatech.go.intention.handler.IntentionHandler;
import com.kikatech.go.util.LogUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jasonli Created on 2017/10/27.
 */

public class IntentionManager {

    private static final String TAG = "IntentionManager";

    private static IntentionManager sIntentionManager;

    private Map<String, IntentionHandler> mIntentionMap = new HashMap<>();
    private List<Object> mSubscribers = new ArrayList<>();

    public static IntentionManager getInstance() {
        if (sIntentionManager == null) {
            sIntentionManager = new IntentionManager();
        }
        return sIntentionManager;
    }

    private IntentionManager() {

    }

    public void register(Object object) {
        mSubscribers.add(object);
    }

    public void unregister(Object object) {
        mSubscribers.remove(object);
    }

    public void resetIntention() {
        mIntentionMap.clear();
    }

    public void processIntention(Intention intention) {
        if (intention == null) {
            LogUtil.logw(TAG, "Null intention");
            return;
        }

        String topicName = intention.getTopic();
        IntentionHandler handler = getHandler(topicName);

        handler.onHandle(intention);



        for (Object subscriber : mSubscribers) {
            Method[] methods = subscriber.getClass().getMethods();
            for (Method method : methods) {
                if ("onHandleCallback".equals(method.getName())) {
                    try {
                        method.invoke(subscriber, handler, intention);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    public void processSpeech(String speech) {
        //IntentionHandler handler = getHandler(topicName);
        //ApiAiHelper.getInstance(context).queryIntention(speech);
    }

    private IntentionHandler getHandler(String topicName) {
        IntentionHandler handler = mIntentionMap.get(topicName);
        if (handler == null) {
            handler = IntentionHandler.createHandler(topicName);
            mIntentionMap.put(topicName, handler);
        }
        return handler;
    }
}
