package com.kikatech.go.dialogflow.sms;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.message.sms.SmsManager;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.PermissionUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class SmsUtil {

    private static final String KEY_NAME = "given-name";
    private static final String KEY_LAST_NAME = "last-name";

    private static final String KEY_ANY = "any";

    private static final String KEY_NUMBER = "number";

    public static final int TAG_ANY_STAND_FOR_MSG_BODY = 0;
    public static final int TAG_ANY_STAND_FOR_NAME = 1;
    public static final int TAG_ANY_STAND_FOR_USER_INPUT = 2;

    public static SmsContent.IntentContent parseContactName(@NonNull Bundle parm) {
        return parseContactName(parm, TAG_ANY_STAND_FOR_MSG_BODY);
    }

    private static List<String> parseAny(@NonNull Bundle parm) {
        List<String> ta = new ArrayList<>();
        String valTagAny = parm.getString(KEY_ANY, "");
        if (LogUtil.DEBUG) LogUtil.log("SmsUtil", "parseAny, valTagAny : " + valTagAny);
        // Check if it is jason format
        String sendTarget = "";
        try {
            JSONArray content = new JSONArray(valTagAny);
            int arrCount = content.length();
            for (int i = 0; i < arrCount; i++) {
                ta.add(content.getString(i));
            }
            if (LogUtil.DEBUG)
                LogUtil.log("SmsUtil", "tagAnySize:" + content.length() + ", valTagAny:" + valTagAny + ", sendTarget:" + sendTarget);
        } catch (JSONException ignored) {
            // Not a jason format
            if (!TextUtils.isEmpty(valTagAny)) {
                ta.add(valTagAny);
                if (LogUtil.DEBUG)
                    LogUtil.log("SmsUtil", "valTagAny:" + valTagAny);
            }
        }
        return ta;
    }

    public static SmsContent.IntentContent parseContactName(@NonNull Bundle parm, int tagAnyStandFor) {
        if (LogUtil.DEBUG)
            LogUtil.log("SmsUtil", "Bundle:" + parm + ", tagAnyStandFor:" + tagAnyStandFor);
        SmsContent.IntentContent ic = new SmsContent.IntentContent();
        ic.lastName = parm.getString(KEY_LAST_NAME, "").replace("\"", "");
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


        String userInputs = parm.getString(Intent.KEY_USER_INPUT, "");
        List<String> tagAny = parseAny(parm);

        if (tagAnyStandFor == TAG_ANY_STAND_FOR_MSG_BODY) {
            int valCount = tagAny.size();
            if (valCount > 1) {
                ic.smsBody = tagAny.get(0);
                if (LogUtil.DEBUG) LogUtil.log("SmsUtil", "Set sms body : " + ic.smsBody);

                String toSomebody = "to " + tagAny.get(1);
                if (userInputs.contains(toSomebody)) {
                    if (LogUtil.DEBUG) LogUtil.log("SmsUtil", "Find pattern:" + toSomebody);
                    ic.firstName = tagAny.get(1);
                }
            } else if (valCount > 0) {
                ic.smsBody = tagAny.get(0);
                if (LogUtil.DEBUG) LogUtil.log("SmsUtil", "Set sms body : " + ic.smsBody);
            }
        } else if (tagAnyStandFor == TAG_ANY_STAND_FOR_NAME) {
            // No, it's xxx
            if (tagAny.size() > 0) {
                ic.firstName = tagAny.get(1);
                if (LogUtil.DEBUG) LogUtil.log("SmsUtil", "Set firstName : " + ic.firstName);
            }
        }

        if (TextUtils.isEmpty(ic.smsBody) && tagAnyStandFor == TAG_ANY_STAND_FOR_USER_INPUT) {
            ic.smsBody = parm.getString(Intent.KEY_USER_INPUT, "");
            if (LogUtil.DEBUG)
                LogUtil.log("SmsUtil", "[TAG_ANY_STAND_FOR_USER_INPUT]Set smsBody : " + ic.smsBody);
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

    public static String parseTagAny(@NonNull Bundle parm) {
        String c = parm.getString(KEY_ANY, "").replace("\"", "");
        if (TextUtils.isEmpty(c)) {
            return parm.getString(Intent.KEY_USER_INPUT, "");
        }
        return c;
    }
}