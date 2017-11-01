package com.kikatech.go.intention;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jasonli Created on 2017/10/27.
 */

public class Intention {

    private String[] mPreContext;
    private String[] mPostContext;

    private String mTopic = "";
    private String mAction;

    private Map<String, String> mParams = new HashMap<>();

    /**
     * Keep updating the metadata in the same conversational concept
     * @param data the metadata in old Intention record
     */
    public void updateMetadata(Map<String, String> data) {
        for (String key : data.keySet()) {
            String value = data.get(key);

            if (!mParams.containsKey(key)) {
                mParams.put(key, value);
            }
        }
    }

    /**
     * Get the concept topic of this intention
     */
    public String getTopic() {
        return mTopic;
    }

    public String getAction() {
        return mAction;
    }

    public Map<String, String> getParams() {
        return mParams;
    }

    /**
     * Make sure your action name has been set to the format 'topic.xxx'
     */
    public void setAction(String action) {
        mAction = action;
        try {
            int indexToFiltered = mAction.indexOf(".");
            if (indexToFiltered <= 0) {
                indexToFiltered = mAction.indexOf(" ");
            }
            if (indexToFiltered <= 0) {
                indexToFiltered = mAction.indexOf("_");
            }
            mTopic = mAction.substring(0, indexToFiltered);
        } catch (Exception ignore) {}
    }

    public void setPreContext(String[] contexts) {
        mPreContext = contexts;
    }

    public void setPostContext(String[] contexts) {
        mPostContext = contexts;
    }

    public void setParams(Map<String, String> params) {
        mParams = params;
    }

}
