package com.kikatech.go.dialogflow.sms;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class SendSmsUtil {

    private static final String KEY_NAME = "given-name";
    private static final String KEY_LAST_NAME = "last-name";

    private static final String KEY_ANY = "any";

    private static final String KEY_NUMBER = "number";

    public static SmsContent.IntentContent parseContactName(@NonNull Bundle parm) {
        SmsContent.IntentContent ic = new SmsContent.IntentContent();
        ic.smsBody = parm.getString(KEY_ANY, "").replace("\"", "");;
        ic.lastName = parm.getString(KEY_LAST_NAME, "");
        String names = parm.getString(KEY_NAME, "");
        try {
            if (LogUtil.DEBUG) LogUtil.log("SendSmsUtil", "names:" + names);
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
            if (LogUtil.DEBUG) LogUtil.log("SendSmsUtil", "chosenNum:" + chosenNum);
            JSONArray jsonNums = new JSONArray(chosenNum);
            if (jsonNums.length() > 0) {
                ic.chosenNumber += jsonNums.getString(0);
            }
            ic.chosenNumber = ic.chosenNumber.trim();
        } catch (JSONException e) {
            ic.chosenNumber = names.replace("\"", "");
        }

        if (LogUtil.DEBUG) {
            for (String k : parm.keySet()) {
                LogUtil.log("SendSmsUtil", "" + k + ":" + parm.getString(k));
                //return parm.getString(k);
            }
            LogUtil.log("SendSmsUtil", "" + ic.toString());
        }

        return ic;
    }
}