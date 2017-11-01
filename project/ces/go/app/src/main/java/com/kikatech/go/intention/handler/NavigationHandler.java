package com.kikatech.go.intention.handler;

import android.text.TextUtils;

import com.kikatech.go.intention.ActionNames;
import com.kikatech.go.intention.Intention;

import java.util.Map;

/**
 * @author jasonli Created on 2017/10/27.
 */

public class NavigationHandler extends IntentionHandler {

    private static final String PARAM_DEST = "destination";
    private static final String PARAM_DEST_ANY = "destinationAny";

    @Override
    public void onHandle(Intention intention) {
        super.onHandle(intention);

        String dest = getDestination();
        switch (intention.getAction()) {
            case ActionNames.NAVI_START:
                if (TextUtils.isEmpty(dest)) {
                    mResponse = "Tell me the address";
                } else {
                    mResponse = "** Start navigate to : " + dest;
                }
                break;
            case ActionNames.NAVI_DEST:
                if (TextUtils.isEmpty(dest)) {
                    mResponse = "Tell me the address, again";
                } else {
                    mResponse = "** Start navigate to : " + dest;
                }
                break;
            case ActionNames.NAVI_STOP:
                mResponse = "Navigation stop.";
                break;
        }
    }

    public String getDestination() {
        Map<String, String> params = mIntention.getParams();
        for (String key : params.keySet()) {
            if (PARAM_DEST.equals(key)) {
                return params.get(key);
            }
            if (PARAM_DEST_ANY.equals(key)) {
                return params.get(key);
            }
        }
        return null;
    }

}
