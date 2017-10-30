package com.kikatech.go.contact;

import android.content.Context;
import android.util.Log;


import com.kikatech.go.util.ContentResolverHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jasonli Created on 2017/10/23.
 */

public class ContactManager {

    private static final String TAG = "ContactManager";

    private static ContactManager sContactManager;

    private List<Contact> mContactList = new ArrayList<>();

    public static ContactManager getInstance(Context context) {
        if(sContactManager == null) {
            sContactManager = new ContactManager(context);
        }
        return sContactManager;
    }

    public ContactManager(Context context) {
        mContactList.clear();
        List<Contact> contacts = ContentResolverHelper.retrieveContacts(context);
        mContactList.addAll(contacts);
    }

    public void printAll() {
        for(Contact contact : mContactList) {
            Log.w(TAG ,  "Contact = " + contact.getDisplayName() + "  " + contact.getPhoneNum());
        }
    }
}
