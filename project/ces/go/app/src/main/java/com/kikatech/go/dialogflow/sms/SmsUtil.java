package com.kikatech.go.dialogflow.sms;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.im.IMUtil;
import com.kikatech.go.message.sms.SmsManager;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.PermissionUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class SmsUtil {
    private static final String TAG = "SmsUtil";

    private static final String KEY_NAME = "name";

    private static final String KEY_ANY = "any";

    private static final String KEY_NUMBER = "number";
    private static final String KEY_BODY = "messageBody";

    public static final int TAG_ANY_STAND_FOR_MSG_BODY = 0;
    public static final int TAG_ANY_STAND_FOR_NAME = 1;
    public static final int TAG_ANY_STAND_FOR_USER_INPUT = 2;

    public static SmsContent.IntentContent parseSmsContent(@NonNull Bundle parm) {
        return parseSmsContent(parm, TAG_ANY_STAND_FOR_MSG_BODY);
    }

    public static SmsContent.IntentContent parseSmsContent(@NonNull Bundle parm, int tagAnyStandFor) {
        if (LogUtil.DEBUG)
            LogUtil.log(TAG, "Bundle:" + parm + ", tagAnyStandFor:" + tagAnyStandFor);

        SmsContent.IntentContent ic = new SmsContent.IntentContent();

        String sendTarget = parm.getString(KEY_NAME, "").replace("\"", "");
        ic.sendTarget = !TextUtils.isEmpty(sendTarget) ? new String[]{sendTarget} : null;
        ic.smsBody = parm.getString(KEY_BODY, "").replace("\"", "");


        String userInputs = Intent.parseUserInput(parm);
        //List<String tagAny = parseAny(parm);
        if (!TextUtils.isEmpty(userInputs)) {
            if (tagAnyStandFor == TAG_ANY_STAND_FOR_MSG_BODY) {
                ic.smsBody = userInputs;
            } else if (tagAnyStandFor == TAG_ANY_STAND_FOR_NAME) {
                ic.sendTarget = new String[]{userInputs};
            }
        }

        if (ic.sendTarget == null || ic.sendTarget.length == 0) {
            String jsonString = Intent.parseSwitchSceneInfo(parm);
            if (!TextUtils.isEmpty(jsonString)) {
                if (LogUtil.DEBUG) LogUtil.log(TAG, "Find switch scene info:" + jsonString);
                try {
                    JSONObject json = new JSONObject(jsonString);
                    String target = json.getString(IMUtil.KEY_SWITCH_SCENE_NAME);
                    ic.sendTarget = !TextUtils.isEmpty(target) ? new String[]{target} : null;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        String chosenNum = parm.getString(KEY_NUMBER, "");
        if (!TextUtils.isEmpty(chosenNum)) {
            try {
                if (LogUtil.DEBUG) LogUtil.log(TAG, "chosenNum:" + chosenNum);
                JSONArray jsonNums = new JSONArray(chosenNum);
                if (jsonNums.length() > 0) {
                    ic.chosenOption += jsonNums.getString(0);
                }
                ic.chosenOption = ic.chosenOption.trim();
            } catch (JSONException e) {
                if (LogUtil.DEBUG) LogUtil.log(TAG, "Err:" + e);
            }
        }

        if (LogUtil.DEBUG) {
            for (String k : parm.keySet()) {
                LogUtil.log(TAG, "key : " + k + ", " + parm.get(k));
            }
            LogUtil.log(TAG, "Result : " + ic.toString());
        }

        return ic;
    }


    public static void sendSms(@NonNull Context ctx, String phoneNum, String msgContent) {
        if (!PermissionUtil.hasPermissionsSMS(ctx)) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "Get SMS permission first!");
            return;
        }

        if (TextUtils.isEmpty(phoneNum) || TextUtils.isEmpty(msgContent)) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "Empty target or message!");
            return;
        }

        boolean sent = SmsManager.getInstance().sendMessage(ctx, phoneNum, "", msgContent);
        if (LogUtil.DEBUG)
            LogUtil.log(TAG, "phoneNum:" + phoneNum + ", msgContent:" + msgContent + ", send status:" + sent);
    }

    public static String parseTagMessageBody(@NonNull Bundle parm) {
        String c = parm.getString(KEY_BODY, "").replace("\"", "");
        if (TextUtils.isEmpty(c)) {
            return Intent.parseUserInput(parm);
        }
        return c;
    }

    public static String parseTagAny(@NonNull Bundle parm) {
        String c = parm.getString(KEY_ANY, "").replace("\"", "");
        if (TextUtils.isEmpty(c)) {
            return Intent.parseUserInput(parm);
        }
        return c;
    }
}