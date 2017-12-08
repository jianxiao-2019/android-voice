package com.kikatech.go.dialogflow.im.send;

import android.content.Context;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.EmojiMessage;
import com.kikatech.go.dialogflow.im.IMUtil;
import com.kikatech.go.util.AppConstants;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.util.contact.ContactManager;

/**
 * Created by brad_chang on 2017/11/24.
 */

public class IMContent extends EmojiMessage {

    private String parsedIMApp = "";
    private String targetName[];
    private boolean explicitTarget = false;
    private String imAppPkgName = "";

    @Override
    public String toString() {
        return "parsedIMApp:" + parsedIMApp + ", imAppPkgName:" + imAppPkgName +
                ", targetName:" + targetName +
                ", explicitTarget:" + explicitTarget +
                ", msgBody:" + messageBody + "\n, emoji:" + emojiUnicode + ", snedEmoji:" + mSendWithEmoji;
    }

    public IMContent(String parsedIMApp, String[] targetName, String msgBody) {
        this.parsedIMApp = parsedIMApp;
        this.imAppPkgName = analyzeIMApp(parsedIMApp);
        this.targetName = targetName;
        this.messageBody = msgBody;
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
        return targetName != null && targetName.length != 0 ? targetName[0] : null;
    }

    public boolean isExplicitTarget(Context ctx) {
        if (imAppPkgName.equals(AppConstants.PACKAGE_WHATSAPP)) {
            ContactManager.MatchedContact mc = ContactManager.getIns().findContact(ctx, targetName);
            explicitTarget = mc != null;

            if (LogUtil.DEBUG) {
                if (mc != null) {
                    LogUtil.log("IMContent", "Find WhatsApp, Match Contact:" + mc.displayName);
                }
            }
        }
        return explicitTarget;
    }

    public void userConfirmSendTarget() {
        explicitTarget = true;
    }

    public void update(IMContent ic) {
        parsedIMApp = checkNUpdate(parsedIMApp, ic.parsedIMApp);
        imAppPkgName = checkNUpdate(imAppPkgName, ic.imAppPkgName);
        targetName = checkNUpdate(targetName, ic.targetName);
        messageBody = checkNUpdate(messageBody, ic.messageBody);
    }

    public void updateEmoji(String unicode, String desc) {
        if (LogUtil.DEBUG)
            LogUtil.log("SmsContent", "updateEmoji:" + unicode + " , " + desc);
        emojiUnicode = unicode;
        emojiDesc = desc;
    }

    public String getEmojiUnicode() {
        return emojiUnicode;
    }

    public String getEmojiDesc() {
        return emojiDesc;
    }

    public boolean hasEmoji() {
        return !TextUtils.isEmpty(emojiUnicode) && !TextUtils.isEmpty(emojiDesc);
    }

    public void setSendWithEmoji(boolean b) {
        mSendWithEmoji = b;
    }

    private String checkNUpdate(String ov, String nv) {
        return TextUtils.isEmpty(nv) ? ov : nv;
    }

    private String[] checkNUpdate(String[] ov, String[] nv) {
        return nv == null || nv.length == 0 ? ov : nv;
    }
    public void updateSendTarget(String[] target) {
        targetName = target;
    }

    public void setSendTarget(String[] sendTarget) {
        targetName = sendTarget;
    }
}