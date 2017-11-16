package com.kikatech.go.dialogflow.sms;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.util.LogUtil;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class SendSmsUtil {

    private static final String KEY_NAME = "given-name";
    private static final String KEY_LASTNAME = "last-name";

    private static final String KEY_ANY = "any";

    public static SmsContent parseContactName(@NonNull Bundle parm) {
        SmsContent sc = new SmsContent();
        for (String k : parm.keySet()) {
            LogUtil.log("SendSmsUtil", "" + k + ":" + parm.getString(k));
            //return parm.getString(k);
        }
        LogUtil.log("SendSmsUtil", "" + sc.toString());
        return sc;
    }
}
