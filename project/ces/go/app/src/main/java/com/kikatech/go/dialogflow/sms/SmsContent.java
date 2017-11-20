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
    private String mChosenNumber;

    private boolean isContactMatched;

    public static class IntentContent {
        String smsBody = "";
        String firstName = "";
        String lastName = "";
        String chosenOption = "";

        @Override
        public String toString() {
            return "smsBody:" + getDisplayString(smsBody) +
                    ", firstName:" + getDisplayString(firstName) +
                    ", lastName:" + getDisplayString(lastName) +
                    ", chosenOption:" + getDisplayString(chosenOption);
        }

        private String checkNUpdate(String ov, String nv) {
            return TextUtils.isEmpty(nv) ? ov : nv;
        }

        void update(IntentContent ic) {
            smsBody = checkNUpdate(smsBody, ic.smsBody);
            firstName = checkNUpdate(firstName, ic.firstName);
            lastName = checkNUpdate(lastName, ic.lastName);
            chosenOption = checkNUpdate(chosenOption, ic.chosenOption);
        }
    }

    public SmsContent(IntentContent ic) {
        phoneNumbers = new ArrayList<>();
        update(ic);
    }

    private static String getDisplayString(String filed) {
        return TextUtils.isEmpty(filed) ? "<empty>" : filed;
    }

    public void update(IntentContent ic) {
        if(mIntentContent == null) {
            mIntentContent = ic;
        } else {
            mIntentContent.update(ic);
        }
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

    public String getChosenPhoneNumber() {
        return mChosenNumber;
    }

    public void setChosenNumber(String number) {
        if(LogUtil.DEBUG) LogUtil.log("SmsContent", "setChosenNumber:" + number);
        mChosenNumber = number;
    }

    public int getChosenOption() {
        try {
            return Integer.parseInt(mIntentContent.chosenOption);
        } catch (Exception e) {
            if(LogUtil.DEBUG) LogUtil.log("SmsContent", "Err, cannot parse chosenOption :" + mIntentContent.chosenOption);
        }
        return -1;
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
            for(String s:pbc.phoneNumbers) {
                phoneNumbers.add(s.replace(" ", ""));
            }
            isContactMatched = true;

            if (LogUtil.DEBUG) {
                StringBuilder numb = new StringBuilder();
                for (String n : phoneNumbers) {
                    numb.append(n).append(", ");
                }
                LogUtil.log("SmsContent", "Find " + contactMatchedName + ", numbers:" + numb);
            }
        } else {
            if(LogUtil.DEBUG) LogUtil.log("SmsContent", "findName fail");
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
                "\nChosen Number:" + mChosenNumber + ", phoneNumber count:" + phoneNumbers.size() +
                "\nisSimilarContact:" + isSimilarContact() +
                ", isContactMatched:" + isContactMatched;
    }
}