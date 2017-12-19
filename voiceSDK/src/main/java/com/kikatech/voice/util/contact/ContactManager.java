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
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "init ...");
        }
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
                        if (mPhoneBook.size() == 0) {
                            final long t = System.currentTimeMillis();
                            HashMap<String, PhoneBookContact> pb = getPhoneBook(ctx);
                            mPhoneBook.clear();
                            if(pb != null && pb.size() > 0) {
                                mPhoneBook.putAll(pb);
                            }

                            if (LogUtil.DEBUG) {
                                LogUtil.log(TAG, "onParseComplete, spend " + (System.currentTimeMillis() - t));
                            }
                        } else if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "No need to init phone book info, phoneBookCount:" + mPhoneBook.size());
                        }
                    }
                }
            }).start();
        } else if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "No need to init phone book info, phoneBookCount:" + mPhoneBook.size());
        }
    }

    private MatchedContact findFullMatchedName(String[] targetNames, HashMap<String, PhoneBookContact> phoneBook) {
        String[] contactNames = phoneBook.size() > 0 ? phoneBook.keySet().toArray(new String[phoneBook.size()]) : null;
        if (contactNames == null || contactNames.length == 0) {
            return null;
        }
        for (String targetName : targetNames) {
            for (String contactName : contactNames) {
                if (targetName.equals(contactName)) {
                    return new MatchedContact(MatchedContact.MatchedType.FULL_MATCHED, phoneBook.get(contactName));
                }
            }
        }
        return null;
    }

    private MatchedContact findFuzzyName(String[] targetNames, HashMap<String, PhoneBookContact> phoneBook) {
        String[] contactNames = phoneBook.size() > 0 ? phoneBook.keySet().toArray(new String[phoneBook.size()]) : null;
        if (contactNames == null || contactNames.length == 0) {
            return null;
        }
        int confidence;
        String foundName;
        FuzzySearchManager.FuzzyResult fuzzySearchResult;
        for (String targetName : targetNames) {
            fuzzySearchResult = FuzzySearchManager.getIns().search(targetName, contactNames);
            if (fuzzySearchResult != null) {
                foundName = fuzzySearchResult.getText();
                confidence = fuzzySearchResult.getConfidence();
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, String.format("foundName: %1$s, confidence: %2$s", foundName, confidence));
                }
                if (!TextUtils.isEmpty(foundName)) {
                    if (confidence > FuzzySearchManager.getIns().getLowConfidenceCriteria()) {
                        return new MatchedContact(MatchedContact.MatchedType.FUZZY_MATCHED, phoneBook.get(foundName));
                    } else {
                        if (LogUtil.DEBUG) {
                            LogUtil.logd(TAG, "low confidence, LOW_CONFIDENCE_CRITERIA:" + FuzzySearchManager.getIns().getLowConfidenceCriteria());
                        }
                    }
                }
            }
        }
        return null;
    }

    private MatchedContact findNumberName(String[] targetNames) {
        String number;
        for (String targetName : targetNames) {
            number = findNumber(targetName);
            if (!TextUtils.isEmpty(number)) {
                return new MatchedContact(MatchedContact.MatchedType.NUMBER_MATCHED, new PhoneBookContact(number, ""));
            }
        }
        return null;
    }

    public MatchedContact findContact(final Context ctx, final String targetName) {
        return findContact(ctx, new String[]{targetName});
    }

    public MatchedContact findContact(final Context ctx, final String[] targetNames) {
        if (LogUtil.DEBUG) {
            if (targetNames != null) {
                for (int i = 0; i < targetNames.length; i++) {
                    LogUtil.logd(TAG, String.format("target: %1$s. %2$s", i + 1, targetNames[i]));
                }
            }
        }
        if (targetNames == null || targetNames.length == 0) {
            return null;
        }
        init(ctx);
        synchronized (mPhoneBook) {
            MatchedContact result = findFullMatchedName(targetNames, mPhoneBook);
            if (result != null) {
                return result;
            }
            result = findFuzzyName(targetNames, mPhoneBook);
            if (result != null) {
                return result;
            }
            result = findNumberName(targetNames);
            return result;
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
        Cursor phones;
        try {
            phones = ctx.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        } catch (SecurityException se) {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "SecurityException:" + se);
            }
            return null;
        }

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
                    phoneLabel = getPhoneType(phonetype);
                }
            }
            phoneCur.close();
        }
        return phoneLabel;
    }

    private String getPhoneType(int phonetype) {
        switch (phonetype) {
            case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
                return "Home";
            case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
                return "Work";
            case ContactsContract.CommonDataKinds.Email.TYPE_OTHER:
                return "Other";
            case ContactsContract.CommonDataKinds.Email.TYPE_MOBILE:
                return "Mobile";
            default:
                return "Custom";
        }
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

        NumberType(String number, String type) {
            this.number = number;
            this.type = type;
        }

        public String getTypeOrNumber() {
            return TextUtils.isEmpty(type) ? number : type;
        }
    }

    public static class MatchedContact extends PhoneBookContact {
        public final class MatchedType {
            public static final byte NUMBER_MATCHED = 0x01;
            public static final byte FUZZY_MATCHED = 0x02;
            public static final byte FULL_MATCHED = 0x03;
        }

        public byte matchedType;

        MatchedContact(byte matchType, PhoneBookContact contact) {
            this.matchedType = matchType;
            this.id = contact.id;
            this.displayName = contact.displayName;
            this.phoneNumbers = contact.phoneNumbers;
        }

        @Override
        public String toString() {
            return displayName + ", num count:" + phoneNumbers.size() + ", matchedType:" + matchedType + ", id:" + id;
        }
    }

    public static class PhoneBookContact {

        public long id;
        public String displayName;
        public List<NumberType> phoneNumbers = new ArrayList<>();

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

        PhoneBookContact() {
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