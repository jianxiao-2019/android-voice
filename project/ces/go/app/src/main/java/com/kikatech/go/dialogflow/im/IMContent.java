package com.kikatech.go.dialogflow.im;

import android.content.Context;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.ContactUtil;
import com.kikatech.go.util.AppConstants;
import com.kikatech.go.util.LogUtil;

/**
 * Created by brad_chang on 2017/11/24.
 */

public class IMContent {

    private String parsedIMApp = "";
    private String targetName = "";
    private boolean explicitTarget = false;
    private String msgBody = "";
    private String imAppPkgName = "";

//    private String contactMatchedName = "";
//    private boolean isContactMatched = false;

    @Override
    public String toString() {
        return "parsedIMApp:" + parsedIMApp + ", imAppPkgName:" + imAppPkgName +
                ", targetName:" + targetName +
                ", explicitTarget:" + explicitTarget +
                ", msgBody:" + msgBody;
    }

    IMContent(String parsedIMApp, String targetName, String msgBody) {
        this.parsedIMApp = parsedIMApp;
        this.imAppPkgName = analyzeIMApp(parsedIMApp);
        this.targetName = targetName;
        this.msgBody = msgBody;
    }

    private String analyzeIMApp(String parsedIMAppName) {
        if (!TextUtils.isEmpty(parsedIMAppName)) {
            switch (parsedIMAppName) {
                case IMUtil.DF_ENTIY_IM_APP_WHATS_APP:
                    return AppConstants.PACKAGE_WHATSAPP;
                case IMUtil.DF_ENTIY_IM_APP_FB_MESSENGER:
                    return AppConstants.PACKAGE_MESSENGER;
                case IMUtil.DF_ENTIY_IM_APP_LINE:
                case IMUtil.DF_ENTIY_IM_APP_WECHAT:
                    if (LogUtil.DEBUG)
                        LogUtil.log("IMContent", "Unsupported IM app : " + parsedIMAppName);
                    break;
                default:
                    if (LogUtil.DEBUG)
                        LogUtil.log("IMContent", "Cannot recognize IM app : " + parsedIMAppName);
                    break;
            }
        }
        return "";
    }

    public String getIMAppPackageName() {
        return imAppPkgName;
    }

    public String getSendTarget() {
        return targetName;
    }

    public boolean isExplicitTarget(Context ctx) {
        if(imAppPkgName.equals(AppConstants.PACKAGE_WHATSAPP)) {
            ContactUtil.MatchedContact mc = ContactUtil.matchContact(ctx, targetName);
            explicitTarget = mc.isContactMatched;

            if (LogUtil.DEBUG)
                LogUtil.log("IMContent", "Find WhatsApp, Match Contact:" + mc.contactMatchedName + ", matched:" + mc.isContactMatched);
        }
        return explicitTarget;
    }

    public String getMessageBody() {
        return msgBody;
    }

    public void userConfirmSendTarget() {
        explicitTarget = true;
    }

    public void update(IMContent ic) {
        parsedIMApp = checkNUpdate(parsedIMApp, ic.parsedIMApp);
        imAppPkgName = checkNUpdate(imAppPkgName, ic.imAppPkgName);
        targetName = checkNUpdate(targetName, ic.targetName);
        msgBody = checkNUpdate(msgBody, ic.msgBody);
    }

    private String checkNUpdate(String ov, String nv) {
        return TextUtils.isEmpty(nv) ? ov : nv;
    }

    public void updateSendTarget(String target) {
        targetName = target;
    }

    public void updateMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }

    public void setSendTarget(String sendTarget) {
        targetName = sendTarget;
    }
}