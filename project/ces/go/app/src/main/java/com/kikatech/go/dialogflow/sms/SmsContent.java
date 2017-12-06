package com.kikatech.go.dialogflow.sms;

import android.content.Context;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.ContactUtil;
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
    private List<ContactManager.NumberType> phoneNumbers;
    private String mChosenNumber;

    private String emojiUnicode = "";
    private String emojiDesc = "";
    private boolean mSendWithEmoji = false;

    private boolean isContactMatched;

    public static class IntentContent {
        String smsBody = "";
        String sendTarget = "";
        String chosenOption = "";

        @Override
        public String toString() {
            return "smsBody:" + getDisplayString(smsBody) +
                    ", sendTarget:" + getDisplayString(sendTarget) +
                    ", chosenOption:" + getDisplayString(chosenOption);
        }

        private String checkNUpdate(String ov, String nv) {
            return TextUtils.isEmpty(nv) ? ov : nv;
        }

        void update(IntentContent ic) {
            if (LogUtil.DEBUG)
                LogUtil.log("SmsContent", "update target:" + ic);
            smsBody = checkNUpdate(smsBody, ic.smsBody);
            sendTarget = checkNUpdate(sendTarget, ic.sendTarget);
            chosenOption = checkNUpdate(chosenOption, ic.chosenOption);
        }

        public boolean isNameEmpty() {
            return TextUtils.isEmpty(sendTarget);
        }
    }

    public SmsContent(IntentContent ic) {
        phoneNumbers = new ArrayList<>();
        update(ic);
    }

    private static String getDisplayString(String filed) {
        return TextUtils.isEmpty(filed) ? "<empty>" : filed;
    }

    public void updateName(String name) {
        mIntentContent.sendTarget = name;
    }

    public void update(IntentContent ic) {
        if (mIntentContent == null) {
            setIntentContent(ic);
        } else {
            mIntentContent.update(ic);
        }
    }

    public void updateEmoji(String unicode, String desc) {
        if (LogUtil.DEBUG)
            LogUtil.log("SmsContent", "updateEmoji:" + unicode + " , " + desc);
        emojiUnicode = unicode;
        emojiDesc = desc;
    }

    public String getEmojiUnicode() {
        return emojiUnicode;
    }

    public String getEmojiDesc() {
        return emojiDesc;
    }

    public boolean hasEmoji() {
        return !TextUtils.isEmpty(emojiUnicode) && !TextUtils.isEmpty(emojiDesc);
    }

    public void setSendWithEmoji(boolean b) {
        mSendWithEmoji = b;
    }

    public void setIntentContent(IntentContent ic) {
        mIntentContent = ic;
    }

    public String getContact() {
        return mIntentContent.sendTarget.trim();
    }

    public String getSmsBody() {
        return getSmsBody(false);
    }

    public String getSmsBody(boolean checkEmoji) {
        if(checkEmoji && mSendWithEmoji) {
            return mIntentContent.smsBody + emojiUnicode;
        } else {
            return mIntentContent.smsBody;
        }
    }

    public String getMatchedName() {
        return contactMatchedName;
    }

    public boolean isContactAvailable() {
        return !mIntentContent.isNameEmpty();
    }

    /**
     * 檢查是否有 SMS 內容
     *
     * @return 是否有 SMS 內容
     */
    public boolean isSmsBodyAvailable() {
        return !TextUtils.isEmpty(mIntentContent.smsBody);
    }

    public boolean isSimilarContact() {
        return !TextUtils.isEmpty(contactMatchedName) && !contactMatchedName.equals(getContact());
    }

    public int getNumberCount() {
        return getPhoneNumbers().size();
    }

    public List<ContactManager.NumberType> getPhoneNumbers() {
        if (phoneNumbers == null) {
            return new ArrayList<>();
        }
        return phoneNumbers;
    }

    public String getChosenPhoneNumber() {
        return mChosenNumber;
    }

    public void setChosenNumber(String number) {
        if (LogUtil.DEBUG) LogUtil.log("SmsContent", "setChosenNumber:" + number);
        mChosenNumber = number;
    }

    public int getChosenOption() {
        try {
            return Integer.parseInt(mIntentContent.chosenOption);
        } catch (Exception e) {
            if (LogUtil.DEBUG)
                LogUtil.log("SmsContent", "Err, cannot parse chosenOption :" + mIntentContent.chosenOption);
        }
        return -1;
    }

    public boolean isContactMatched(Context ctx) {
        ContactUtil.MatchedContact mc = ContactUtil.matchContact(ctx, getContact());
        contactMatchedName = mc.contactMatchedName;
        phoneNumbers = mc.phoneNumbers;
        isContactMatched = mc.isContactMatched;
        return isContactMatched;
    }

    @Override
    public String toString() {
        return "contact:" + getDisplayString(getContact()) + ", smsBody:" + getDisplayString(mIntentContent.smsBody) +
                ", matched:" + getDisplayString(getMatchedName()) +
                "\nChosen Number:" + mChosenNumber +
                ", phoneNumber count:" + getNumberCount() +
                "\nisSimilarContact:" + isSimilarContact() +
                ", matchContact:" + isContactMatched +
                "\n, emoji:" + emojiUnicode + ", snedEmoji:" + mSendWithEmoji;
    }
}