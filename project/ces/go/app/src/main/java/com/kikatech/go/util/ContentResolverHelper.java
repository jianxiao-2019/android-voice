package com.kikatech.go.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;

import com.kikatech.go.contact.Contact;
import com.kikatech.go.message.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chuen Created on 2017/9/27.
 */

public class ContentResolverHelper {
    private static final String TAG = "ContentResolverHelper";

    public static final String KEY_CONTACT_DISPLAY_NAME = "contact_display_name";
    public static final String KEY_CONTACT_PHOTO_URI = "contact_photo_uri";

    public static List<Message> getSMSMessages(Context context, String phone) {
        return getSMSMessages(context, phone, false);
    }

    public static List<Message> getSMSMessages(Context context, String phone, boolean onlyLast) {
        List<Message> messages = new ArrayList<>();

        if (!PermissionUtil.hasPermissionsSMS(context)) return messages;

        final int TYPE_SENT_BY_YOU = 2;
        ContentResolver resolver = context.getContentResolver();
        Uri queryUri = Uri.parse("content://sms");
        String[] queryColumn = new String[]{"_id", "date", "body", "type"};
        String where = "address = ?";
        String[] whereArgs = new String[]{phone};
        String constrains = "date DESC LIMIT " + (onlyLast ? "1" : "100");
        Cursor cursor = resolver.query(queryUri, queryColumn, where, whereArgs, constrains);
        if (cursor != null) {
            Bundle contactData = getContactData(context, phone);
            String contactName = phone;
            if (contactData != null) contactName = contactData.getString(KEY_CONTACT_DISPLAY_NAME);

            int idxId = cursor.getColumnIndex("_id");
            int idxType = cursor.getColumnIndex("type");
            int idxBody = cursor.getColumnIndex("body");
            int idxDate = cursor.getColumnIndex("date");

            while (cursor.moveToNext()) {
                try {
                    int id = cursor.getInt(idxId);
                    int type = cursor.getInt(idxType);
                    long date = cursor.getLong(idxDate);
                    String body = cursor.getString(idxBody);

                    String sender = type == TYPE_SENT_BY_YOU ? null : contactName;
                    Message talkMessage = Message.createMessage(date, sender, contactName, body);

                    // let the newest message to be appeared at the bottom
                    messages.add(0, talkMessage);
                } catch (Exception ignore) {
                }
            }
            cursor.close();
        }

        return messages;
    }

    public static Bundle getContactData(Context context, String phoneNumber) {
        if (!PermissionUtil.hasPermission(context, PermissionUtil.Permission.READ_CONTACTS))
            return null;
        Bundle contactData = new Bundle();
        contactData.putString(KEY_CONTACT_DISPLAY_NAME, phoneNumber);
        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            String[] columns = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_URI};
            Cursor cursor = context.getContentResolver().query(uri, columns, null, null, null);
            if (cursor == null) return null;

            if (cursor.moveToFirst()) {
                contactData.putString(KEY_CONTACT_DISPLAY_NAME, cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)));
                contactData.putString(KEY_CONTACT_PHOTO_URI, cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI)));
            }

            cursor.close();
        } catch (SecurityException e) {
            if (LogUtil.DEBUG) LogUtil.logw(TAG, "Get contact name error, " + e.getMessage());
            LogUtil.reportToFabric(e);
        }

        return contactData;
    }

    public static List<Contact> retrieveContacts(Context context) {
        List<Contact> contacts = new ArrayList<>();
        if (!PermissionUtil.hasPermission(context, PermissionUtil.Permission.READ_CONTACTS))
            return contacts;
        try {
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            String[] columns = new String[] {
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                    ContactsContract.RawContacts.ACCOUNT_TYPE,
                    ContactsContract.RawContacts.ACCOUNT_NAME,
            };
            Cursor cursor = context.getContentResolver().query(uri, columns, null, null, null);
            if (cursor == null) return contacts;

            if (cursor.getCount() > 0) {
                while(cursor.moveToNext()) {
                    Contact contact = new Contact();
                    contact.setDisplayName(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                    contact.setPhoneNum(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    contact.setPhotoUri(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)));
                    contacts.add(contact);
                }
            }

            cursor.close();
        } catch (SecurityException e) {
            if (LogUtil.DEBUG) LogUtil.logw(TAG, "Get contacts error, " + e.getMessage());
        }

        return contacts;
    }
}
