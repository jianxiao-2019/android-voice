package com.kikatech.go.message.sms;

import android.content.Context;
import android.content.Intent;

import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.PermissionUtil;

import java.util.ArrayList;

/**
 * @author jasonli Created on 2017/10/20.
 */

public class SmsManager {

    private static final String TAG = SmsManager.class.getSimpleName();

    public static final String ACTION_SMS_MESSAGE_UPDATED = "com.kika.smsmessage.updated";
    public static final String KEY_DATA_SMS_OBJECT = "smsobject";

    private static SmsManager sSmsManager;

    public static SmsManager getInstance() {
        if (sSmsManager == null) {
            sSmsManager = new SmsManager();
        }
        return sSmsManager;
    }

    private SmsManager() {

    }

    public boolean sendMessage(Context context, String phoneNum, String userName,String msg) {
        SmsObject smsObject = new SmsObject();
        smsObject.setUserName(userName);
        smsObject.setAddress(phoneNum);
        smsObject.setMsgContent(msg);
        smsObject.setId(phoneNum);
        return sendMessage(context, smsObject, msg);
    }

    public boolean sendMessage(Context context, SmsObject smsObject, String msg) {
        String sendTo = smsObject.getAddress();
        if (PermissionUtil.hasPermission(context, PermissionUtil.Permission.SEND_SMS)) {
            try {
                android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
                ArrayList<String> msgParts = smsManager.divideMessage(msg);
                smsManager.sendMultipartTextMessage(sendTo, null, msgParts, null, null);
                LogUtil.logw(TAG, "SMS Message sent: " + smsObject.getTitle() + " :" + smsObject.getMsgContent());
                return true;
            } catch (Exception e) {
                LogUtil.printStackTrace(TAG, "Send SMS message failed" ,e);
                return false;
            }
        } else {
            // should not be here. Because when user turn off permission, then the bubble would disappear
            Exception report = new RuntimeException("Send SMS message without SEND_SMS permission!");
            return false;
        }
    }

    public void onMessageReceived(Context context, SmsObject smsObject) {

        Intent intent = new Intent(ACTION_SMS_MESSAGE_UPDATED);
        intent.putExtra(KEY_DATA_SMS_OBJECT, smsObject);
        context.sendBroadcast(intent);

        LogUtil.logw(TAG, "SMS Message received: " + smsObject.getTitle() + " :" + smsObject.getMsgContent());
    }
}
