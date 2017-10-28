package com.kikatech.go.message.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.kikatech.go.message.Message;
import com.kikatech.go.util.ContentResolverHelper;
import com.kikatech.go.util.LogUtil;

/**
 * @author chuen Created on 2017/9/27.
 */

@SuppressWarnings("deprecation")
public class SmsBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsBroadcastReceiver";

    public static final String SMS_BUNDLE = "pdus";

    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();

        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            if (sms != null) {
                String address = "";
                String smsBody = "";
                long timestamp = -1;

                for (Object smsData : sms) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) smsData);

                    smsBody = smsMessage.getDisplayMessageBody();
                    address = smsMessage.getOriginatingAddress();
                    timestamp = smsMessage.getTimestampMillis();
                }

                processSMSMessage(context, address, smsBody, timestamp);
            }
        }
    }

    private void processSMSMessage(Context context, String phone, String body, long timestamp) {
        Bundle contactData = ContentResolverHelper.getContactData(context, phone);
        if (contactData == null) return;

        String contactName = contactData.getString(ContentResolverHelper.KEY_CONTACT_DISPLAY_NAME);
        String contactPhoto = contactData.getString(ContentResolverHelper.KEY_CONTACT_PHOTO_URI);
        SmsObject smsObject = new SmsObject();
        smsObject.setId(phone);
        smsObject.setAddress(phone);
        smsObject.setUserName(contactName);
        smsObject.setMsgContent(body);
        smsObject.setPhotoUri(contactPhoto);

        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "Receive SMS from: " + phone);
            LogUtil.log(TAG, "Receive SMS content " + body);
        }

        Message message = Message.createMessage(timestamp, contactName, contactName, body);
        smsObject.addToLatestMessages(message);

        SmsManager.getInstance().onMessageReceived(context, smsObject);
    }
}
