package com.kikatech.go.dialogflow.sms;

import android.text.TextUtils;

import com.kikatech.go.util.LogUtil;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class SmsContent {

    private String contact;
    private String smsBody;
    private boolean isSimilarContact;
    private boolean hasOnlyOneNumber;
    private boolean isContactMatched;

    public String getContact() {
        return contact;
    }

    public String getSmsBody() {
        return smsBody;
    }

    public boolean isContactAvailable() {
        return !TextUtils.isEmpty(contact);
    }

    public boolean isSmsBodyAvailable() {
        return !TextUtils.isEmpty(smsBody);
    }

    public boolean isSimilarContact() {
        return isSimilarContact;
    }

    public boolean hasOnlyOneNumber() {
        return hasOnlyOneNumber;
    }

    public boolean isOkToSend() {
        return isContactAvailable() && isSmsBodyAvailable() && !isSimilarContact && hasOnlyOneNumber;
    }

    public boolean isContactMatched() {
        return isContactMatched;
    }

    @Override
    public String toString() {
        return "contact:" + contact + ", smsBody:" + smsBody +
                "\nisSimilarContact:" + isSimilarContact + ", hasOnlyOneNumber:" + hasOnlyOneNumber + ", isContactMatched:" + isContactMatched;
    }
}