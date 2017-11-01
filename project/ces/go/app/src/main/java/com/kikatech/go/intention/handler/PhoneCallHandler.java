package com.kikatech.go.intention.handler;

import com.kikatech.go.intention.ActionNames;
import com.kikatech.go.intention.Intention;

import java.util.Map;

/**
 * @author jasonli Created on 2017/10/27.
 */

public class PhoneCallHandler extends IntentionHandler {

    private static final String PARAM_CONTACT = "contact";

    @Override
    public void onHandle(Intention intention) {
        super.onHandle(intention);

        String contact = getContact();
        switch (intention.getAction()) {
            case ActionNames.PHONECALL_START:
                if (contact != null) {
                    mResponse = "Call to: " + contact;
                } else {
                    mResponse = "No match contact";
                }
                break;
        }
    }

    private String getContact() {
        Map<String, String> params = mIntention.getParams();
        return params.get(PARAM_CONTACT);
    }
}
