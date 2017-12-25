package com.kikatech.go.dialogflow.sms;

import android.content.Context;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.EmojiMessage;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.util.contact.ContactManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class SmsContent extends EmojiMessage {

    private IntentContent mIntentContent;

    private String mChosenNumber;

    private ContactManager.MatchedContact mMatchedContact;

    public static class IntentContent {
        String smsBody = "";
        String sendTarget[];
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

        private String[] checkNUpdate(String[] ov, String[] nv) {
            return nv == null || nv.length == 0 ? ov : nv;
        }

        void update(IntentContent ic) {
            if (LogUtil.DEBUG)
                LogUtil.log("SmsContent", "update target:" + ic);
            smsBody = checkNUpdate(smsBody, ic.smsBody);
            sendTarget = checkNUpdate(sendTarget, ic.sendTarget);
            chosenOption = checkNUpdate(chosenOption, ic.chosenOption);
        }

        public boolean isNameEmpty() {
            return sendTarget == null || sendTarget.length == 0;
        }
    }

    public SmsContent(IntentContent ic) {
        update(ic);
    }

    public void update(IntentContent ic) {
        if (mIntentContent == null) {
            setIntentContent(ic);
        } else {
            mIntentContent.update(ic);
        }
    }

    public void updateNames(String[] names) {
        mIntentContent.sendTarget = names;
    }

    public void setIntentContent(IntentContent ic) {
        mIntentContent = ic;
    }

    public String[] getContact() {
        return mIntentContent.sendTarget;
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

    public int getChosenOption() {
        try {
            return Integer.parseInt(mIntentContent.chosenOption);
        } catch (Exception e) {
            if (LogUtil.DEBUG)
                LogUtil.log("SmsContent", "Err, cannot parse chosenOption :" + mIntentContent.chosenOption);
        }
        return -1;
    }

    @Override
    public String getMessageBody() {
        return getMessageBody(false);
    }

    @Override
    public String getMessageBody(boolean checkEmoji) {
        if (checkEmoji && mSendWithEmoji) {
            return mIntentContent.smsBody + emojiUnicode;
        } else {
            return mIntentContent.smsBody;
        }
    }

    public String getMatchedName() {
        return mMatchedContact != null ? mMatchedContact.displayName : null;
    }

    public String getMatchedAvatar() {
        return mMatchedContact != null ? mMatchedContact.photoUri : null;
    }

    public boolean isSimilarContact() {
        return mMatchedContact != null && mMatchedContact.matchedType != ContactManager.MatchedContact.MatchedType.FULL_MATCHED;
    }

    public int getNumberCount() {
        return getPhoneNumbers().size();
    }

    public List<ContactManager.NumberType> getPhoneNumbers() {
        return mMatchedContact != null && mMatchedContact.phoneNumbers != null ? mMatchedContact.phoneNumbers : new ArrayList<ContactManager.NumberType>();
    }

    public String getChosenPhoneNumber() {
        return mChosenNumber;
    }

    public void setChosenNumber(String number) {
        if (LogUtil.DEBUG) LogUtil.log("SmsContent", "setChosenNumber:" + number);
        mChosenNumber = number;
    }

    public ContactManager.MatchedContact isContactMatched(Context ctx) {
        mMatchedContact = ContactManager.getIns().findContact(ctx, getContact());
        return mMatchedContact;
    }


    // Debug Infos

    private static String getDisplayString(String field) {
        return TextUtils.isEmpty(field) ? "<empty>" : field;
    }

    private static String getDisplayString(String[] fields) {
        StringBuilder stringBuilder = null;
        if (fields != null && fields.length != 0) {
            stringBuilder = new StringBuilder("[");
            for (String field : fields) {
                stringBuilder.append(field).append(",");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            stringBuilder.append("]");
        }
        return stringBuilder == null ? "<empty>" : stringBuilder.toString();
    }

    @Override
    public String toString() {
        return "contact:" + getDisplayString(getContact()) + ", smsBody:" + getDisplayString(mIntentContent.smsBody) +
                ", matched:" + getDisplayString(getMatchedName()) +
                "\nChosen Number:" + mChosenNumber +
                ", phoneNumber count:" + getNumberCount() +
                "\nisSimilarContact:" + isSimilarContact() +
                "\n, emoji:" + emojiUnicode + ", snedEmoji:" + mSendWithEmoji;
    }
}