package com.kikatech.voice.util.contact;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.kikatech.voice.util.fuzzy.FuzzySearchManager;
import com.kikatech.voice.util.log.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author SkeeterWang Created on 2017/11/9.
 */
public class ContactManager {

    public static final String TAG = "ContactManager";

    private static ContactManager sIns;

    public static synchronized ContactManager getIns() {
        if (sIns == null) {
            sIns = new ContactManager();
        }
        return sIns;
    }

    public PhoneBookContact findName(Context ctx, String name) {

        if (TextUtils.isEmpty(name)) {
            return null;
        }

        HashMap<String, PhoneBookContact> phoneBook = getPhoneBook(ctx);
        String[] contactNames = phoneBook != null ? phoneBook.keySet().toArray(new String[phoneBook.size()]) : null;
        String number = findNumber(name);

        if (contactNames == null || contactNames.length == 0) {
            return TextUtils.isEmpty(number) ? null : new PhoneBookContact(number);
        }

        FuzzySearchManager.FuzzyResult fuzzySearchResult = FuzzySearchManager.getIns().search(name, contactNames);

        int confidence = -1;
        String foundName = "";

        if (fuzzySearchResult != null) {
            foundName = fuzzySearchResult.getText();
            confidence = fuzzySearchResult.getConfidence();
        }
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "foundName: " + foundName + ", confidence: " + confidence);
        }

        if (TextUtils.isEmpty(foundName)) {
            return TextUtils.isEmpty(number) ? null : new PhoneBookContact(number);
        } else {
            if (confidence <= FuzzySearchManager.getIns().getLowConfidenceCriteria()) {
                if (LogUtil.DEBUG) {
                    LogUtil.logd(TAG, "low confidence, LOW_CONFIDENCE_CRITERIA:" + FuzzySearchManager.getIns().getLowConfidenceCriteria());
                }
                return TextUtils.isEmpty(number) ? null : new PhoneBookContact(number);
            } else {
                return phoneBook.get(foundName);
            }
        }
    }

    private String findNumber(String target) {
        if (TextUtils.isEmpty(target)) {
            return null;
        }
        String number = "";
        for (int i = 0; i < target.length(); ++i) {
            char c = target.charAt(i);
            if (c >= '0' && c <= '9') {
                number += c;
            }
        }
        return number;
    }

    private HashMap<String, PhoneBookContact> getPhoneBook(Context ctx) {
        Cursor phones = ctx.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        if (phones == null || phones.getCount() == 0) {
            return null;
        }

        //phones.moveToFirst();
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "phones count: " + phones.getCount());
        }

        HashMap<String, PhoneBookContact> phoneBook = new HashMap<>();

        final int nameIdx = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        final int pnIdx = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        final int idIdx = phones.getColumnIndex(ContactsContract.Contacts._ID);

        while (phones.moveToNext()) {
            String name = phones.getString(nameIdx);
            String phoneNumber = phones.getString(pnIdx);

            long id = Long.valueOf(phones.getString(idIdx));

            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(phoneNumber)) {
                if (phoneBook.containsKey(name)) {
                    phoneBook.get(name).addNumber(phoneNumber);
                } else {
                    phoneBook.put(name, new PhoneBookContact(id, name, phoneNumber));
                }
            }

            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "name: " + name + ", number: " + phoneNumber);
            }
        }
        phones.close();

        return phoneBook.size() > 0 ? phoneBook : null;
    }

    public static class PhoneBookContact {

        public long id;
        public String displayName;
        public List<String> phoneNumbers = new ArrayList<>();

        PhoneBookContact(String phoneNumber) {
            this(-1, null, phoneNumber);
        }

        PhoneBookContact(long id, String displayName, String phoneNumber) {
            this.id = id;
            this.displayName = displayName;
            if (!TextUtils.isEmpty(phoneNumber)) {
                this.phoneNumbers.add(phoneNumber);
            }
        }

        public PhoneBookContact clone(int idxNumber) {
            if (idxNumber < phoneNumbers.size()) {
                return new PhoneBookContact(id, displayName, phoneNumbers.get(idxNumber));
            }
            return null;
        }

        private void addNumber(String number) {
            if (!TextUtils.isEmpty(number)) {
                this.phoneNumbers.add(number);
            }
        }

        private void print() {
            if (LogUtil.DEBUG) {
                LogUtil.logd(TAG, "name: " + displayName);
                for (String number : phoneNumbers) {
                    LogUtil.logd(TAG, "number: " + number);
                }
                LogUtil.logd(TAG, "------------------------------");
            }
        }
    }
}
