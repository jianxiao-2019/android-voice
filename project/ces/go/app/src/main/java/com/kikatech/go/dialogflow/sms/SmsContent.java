package com.kikatech.go.dialogflow.sms;

import android.content.Context;
import android.text.TextUtils;

import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.util.contact.ContactManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class SmsContent {

    private IntentContent mIntentContent;

    private String contactMatchedName;
    private final List<String> phoneNumbers;

    private boolean isContactMatched;

    public static class IntentContent {
        String smsBody = "";
        String firstName = "";
        String lastName = "";
        String chosenNumber = "";

        @Override
        public String toString() {
            return "smsBody:" + getDisplayString(smsBody) +
                    ", firstName:" + getDisplayString(firstName) +
                    ", lastName:" + getDisplayString(lastName) +
                    ", chosenNumber:" + getDisplayString(chosenNumber);
        }
    }

    SmsContent(IntentContent ic) {
        phoneNumbers = new ArrayList<>();
        update(ic);
    }

    private static String getDisplayString(String filed) {
        return TextUtils.isEmpty(filed) ? "<empty>" : filed;
    }

    void update(IntentContent ic) {
        mIntentContent = ic;
    }

    public String getContact() {
        return (mIntentContent.firstName + " " + mIntentContent.lastName).trim();
    }

    public String getSmsBody() {
        return mIntentContent.smsBody;
    }

    public String getMatchedName() {
        return contactMatchedName;
    }

    public boolean isContactAvailable() {
        return !TextUtils.isEmpty(mIntentContent.firstName) || !TextUtils.isEmpty(mIntentContent.lastName);
    }

    /**
     * 檢查是否有 SMS 內容
     * @return 是否有 SMS 內容
     */
    public boolean isSmsBodyAvailable() {
        return !TextUtils.isEmpty(mIntentContent.smsBody);
    }

    public boolean isSimilarContact() {
        return !TextUtils.isEmpty(contactMatchedName) && !contactMatchedName.equals(getContact());
    }

    public int getNumberCount() {
        return phoneNumbers.size();
    }

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public boolean isOkToSend() {
        return isContactAvailable() && isSmsBodyAvailable() && !isSimilarContact();
    }

    public boolean tryParseContact(Context ctx, String smsBody) {
        mIntentContent.firstName = smsBody;
        return isContactMatched(ctx);
    }

    public boolean isContactMatched(Context ctx) {
        ContactManager.PhoneBookContact pbc = ContactManager.getIns().findName(ctx, getContact());
        if (pbc != null) {
            contactMatchedName = pbc.displayName;
            phoneNumbers.clear();
            phoneNumbers.addAll(pbc.phoneNumbers);
            isContactMatched = true;

            if (LogUtil.DEBUG) {
                StringBuilder numb = new StringBuilder();
                for (String n : phoneNumbers) {
                    numb.append(n).append(", ");
                }
                LogUtil.log("SmsContent", "Find " + contactMatchedName + ", numbers:" + numb);
            }
        } else {
            LogUtil.log("SmsContent", "findName fail");
            isContactMatched = false;
        }

        if (LogUtil.DEBUG)
            LogUtil.log("SmsContent", "isContactMatched:" + isContactMatched + ", isSimilarContact:" + isSimilarContact());

        return isContactMatched;
    }

    @Override
    public String toString() {
        return "contact:" + getDisplayString(getContact()) + ", smsBody:" + getDisplayString(mIntentContent.smsBody) +
                ", matched:" + getDisplayString(getMatchedName()) +
                ", phoneNumber count:" + phoneNumbers.size() +
                "\nisSimilarContact:" + isSimilarContact() +
                ", isContactMatched:" + isContactMatched;
    }
}