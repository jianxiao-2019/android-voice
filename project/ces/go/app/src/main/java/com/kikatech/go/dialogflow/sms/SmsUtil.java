package com.kikatech.go.dialogflow.sms;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.message.sms.SmsManager;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.PermissionUtil;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class SmsUtil {

    private static final String KEY_NAME = "given-name";
    private static final String KEY_LAST_NAME = "last-name";

    private static final String KEY_ANY = "any";

    private static final String KEY_NUMBER = "number";

    public static SmsContent.IntentContent parseContactName(@NonNull Bundle parm) {
        SmsContent.IntentContent ic = new SmsContent.IntentContent();
        ic.smsBody = parseTagAny(parm);
        ic.lastName = parm.getString(KEY_LAST_NAME, "").replace("\"", "");;
        String names = parm.getString(KEY_NAME, "");
        try {
            if (LogUtil.DEBUG) LogUtil.log("SmsUtil", "names:" + names);
            JSONArray jsonNames = new JSONArray(names);
            for (int i = 0; i < jsonNames.length(); ++i) {
                ic.firstName += jsonNames.getString(i) + " ";
            }
            ic.firstName = ic.firstName.trim();
        } catch (JSONException e) {
            ic.firstName = names.replace("\"", "");
        }
        String chosenNum = parm.getString(KEY_NUMBER, "");
        try {
            if (LogUtil.DEBUG) LogUtil.log("SmsUtil", "chosenNum:" + chosenNum);
            JSONArray jsonNums = new JSONArray(chosenNum);
            if (jsonNums.length() > 0) {
                ic.chosenOption += jsonNums.getString(0);
            }
            ic.chosenOption = ic.chosenOption.trim();
        } catch (JSONException e) {
            ic.chosenOption = names.replace("\"", "");
        }

        if (LogUtil.DEBUG) {
            for (String k : parm.keySet()) {
                LogUtil.log("SmsUtil", "" + k + ":" + parm.getString(k));
                //return parm.getString(k);
            }
            LogUtil.log("SmsUtil", "" + ic.toString());
        }

        return ic;
    }

    public static boolean sendSms(Context ctx, String phoneNum, String msgContent) {
        if (!PermissionUtil.hasPermissionsSMS(ctx)) {
            if (LogUtil.DEBUG) LogUtil.log("SmsUtil", "Get SMS permission first!");
            return false;
        }

        if (TextUtils.isEmpty(phoneNum) || TextUtils.isEmpty(msgContent)) {
            if (LogUtil.DEBUG) LogUtil.log("SmsUtil", "Empty target or message!");
            return false;
        }

        boolean sent = SmsManager.getInstance().sendMessage(ctx, phoneNum, "", msgContent);
        if (LogUtil.DEBUG)
            LogUtil.log("SmsUtil", "phoneNum:" + phoneNum + ", msgContent:" + msgContent + ", send status:" + sent);
        return sent;
    }

    public static String parseTagAny(Bundle extra) {
        return extra.getString(KEY_ANY, "").replace("\"", "");
    }
}