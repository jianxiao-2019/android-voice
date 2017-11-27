package com.kikatech.voice.util.contact;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
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
    private final HashMap<String, PhoneBookContact> mPhoneBook = new HashMap<>();

    private int mPhoneTypeIdx = -1;
    private int mPhoneLabelIdx = -1;

    public static synchronized ContactManager getIns() {
        if (sIns == null) {
            sIns = new ContactManager();
        }
        return sIns;
    }

    public void init(final Context ctx) {
        int phoneBookCount;
        synchronized (mPhoneBook) {
            phoneBookCount = mPhoneBook.size();
        }

        if (phoneBookCount == 0) {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "init phone book info");
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (mPhoneBook) {

                        final long t = System.currentTimeMillis();

                        HashMap<String, PhoneBookContact> pb = getPhoneBook(ctx);
                        mPhoneBook.clear();
                        mPhoneBook.putAll(pb);

                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "onParseComplete, spend " + (System.currentTimeMillis() - t));
                        }
                    }
                }
            }).start();
        } else {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "no need to init phone book info");
            }
        }
    }

    private PhoneBookContact findName(String name, HashMap<String, PhoneBookContact> phoneBook) {
        String[] contactNames = phoneBook.size() > 0 ? phoneBook.keySet().toArray(new String[phoneBook.size()]) : null;
        String number = findNumber(name);

        if (contactNames == null || contactNames.length == 0) {
            return TextUtils.isEmpty(number) ? null : new PhoneBookContact(number, "");
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
            return TextUtils.isEmpty(number) ? null : new PhoneBookContact(number, "");
        } else {
            if (confidence < FuzzySearchManager.getIns().getLowConfidenceCriteria()) {
                if (LogUtil.DEBUG) {
                    LogUtil.logd(TAG, "low confidence, LOW_CONFIDENCE_CRITERIA:" + FuzzySearchManager.getIns().getLowConfidenceCriteria());
                }
                return TextUtils.isEmpty(number) ? null : new PhoneBookContact(number, "");
            } else {
                return phoneBook.get(foundName);
            }
        }
    }

    public PhoneBookContact findName(final Context ctx, final String name) {

        if (TextUtils.isEmpty(name)) {
            return null;
        }

        init(ctx);

        synchronized (mPhoneBook) {
            return findName(name, mPhoneBook);
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

    private synchronized HashMap<String, PhoneBookContact> getPhoneBook(Context ctx) {
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
                String numberType = queryPhoneType(ctx, phoneNumber);
                if (phoneBook.containsKey(name)) {
                    PhoneBookContact contact = phoneBook.get(name);
                    if (!contact.containNumber(phoneNumber)) {
                        contact.addNumber(phoneNumber, numberType);
                    }
                } else {
                    phoneBook.put(name, new PhoneBookContact(id, name, phoneNumber, numberType));
                }

                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, "number: " + name + ", number: " + phoneNumber + ", numberType:" + numberType);
                }
            }
        }
        phones.close();

        return phoneBook.size() > 0 ? phoneBook : null;
    }

    private String queryPhoneType(Context ctx, String phoneNumber) {
        Cursor phoneCur = ctx.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?", new String[]{phoneNumber}, null);

        String phoneLabel = "";
        if (phoneCur != null) {
            if (phoneCur.getCount() > 0) {
                checkCursorIdx(phoneCur);
                while (phoneCur.moveToNext()) {
                    int phonetype = phoneCur.getInt(mPhoneTypeIdx);
                    String customLabel = phoneCur.getString(mPhoneLabelIdx);
                    phoneLabel = (String) ContactsContract.CommonDataKinds.Email.getTypeLabel(ctx.getResources(), phonetype, customLabel);
                }
            }
            phoneCur.close();
        }
        return phoneLabel;
    }

    private void checkCursorIdx(Cursor phoneCur) {
        if (mPhoneTypeIdx == -1) {
            mPhoneTypeIdx = phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
        }
        if (mPhoneLabelIdx == -1) {
            mPhoneLabelIdx = phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL);
        }
    }

    public static class NumberType {
        public String number;
        public String type;

        public NumberType(String number, String type) {
            this.number = number;
            this.type = type;
        }

        public String getTypeOrNumber() {
            return TextUtils.isEmpty(type) ? number : type;
        }
    }

    public static class PhoneBookContact {

        public long id;
        public String displayName;
        public final List<NumberType> phoneNumbers = new ArrayList<>();

        PhoneBookContact(String phoneNumber, String type) {
            this(-1, null, phoneNumber, type);
        }

        PhoneBookContact(long id, String displayName, String phoneNumber, String type) {
            this.id = id;
            this.displayName = displayName;
            if (!TextUtils.isEmpty(phoneNumber)) {
                this.phoneNumbers.add(new NumberType(phoneNumber, type));
                //phoneNumbers.put(phoneNumber, type);
            }
        }

        public PhoneBookContact clone(int idxNumber) {
            if (idxNumber < phoneNumbers.size()) {
                NumberType nt = phoneNumbers.get(idxNumber);
                return new PhoneBookContact(id, displayName, nt.number, nt.type);
            }
            return null;
        }

        private void addNumber(String number, String type) {
            if (!TextUtils.isEmpty(number)) {
                this.phoneNumbers.add(new NumberType(number, type));
            }
        }

        private boolean containNumber(String number) {
            for (NumberType numberType : phoneNumbers) {
                if (PhoneNumberUtils.compare(number, numberType.number)) {
                    return true;
                }
            }
            return false;
        }

        private void print() {
            if (LogUtil.DEBUG) {
                LogUtil.logd(TAG, "number: " + displayName);
                for (NumberType nt : phoneNumbers) {
                    LogUtil.logd(TAG, "number: " + nt.number + ", type:" + nt.type);
                }
                LogUtil.logd(TAG, "------------------------------");
            }
        }
    }
}