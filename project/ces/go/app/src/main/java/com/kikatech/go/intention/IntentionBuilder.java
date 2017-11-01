package com.kikatech.go.intention;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jasonli Created on 2017/10/27.
 */

public class IntentionBuilder {

    private Intention mIntention;
    private Map<String, String> mParams = new HashMap<>();

    public IntentionBuilder(String name) {
        mIntention = new Intention();
        mIntention.setAction(name);
    }

    public IntentionBuilder setPreContexts(String[] contexts) {
        mIntention.setPostContext(contexts);
        return this;
    }

    public IntentionBuilder setPostContexts(String[] contexts) {
        mIntention.setPostContext(contexts);
        return this;
    }

    public IntentionBuilder addParam(String key, String value) {
        mParams.put(key, value);
        return this;
    }

    public Intention build() {
        mIntention.setParams(mParams);
        return mIntention;
    }

}
