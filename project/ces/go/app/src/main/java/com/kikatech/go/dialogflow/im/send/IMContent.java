package com.kikatech.go.dialogflow.im.send;

import android.content.Context;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.EmojiMessage;
import com.kikatech.go.dialogflow.im.IMUtil;
import com.kikatech.go.util.AppConstants;
import com.kikatech.go.util.AppInfo;
import com.kikatech.go.util.FileUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.util.contact.ContactManager;

/**
 * Created by brad_chang on 2017/11/24.
 */

public class IMContent extends EmojiMessage {
    private static final String TAG = "IMContent";

    private String parsedIMApp = "";
    private String targetName[];
    private String targetPhotoUri;
    private boolean explicitTarget = false;
    private String imAppPkgName = "";

    @Override
    public String toString() {
        return "parsedIMApp:" + parsedIMApp + ", imAppPkgName:" + imAppPkgName +
                ", targetName:" + targetNamesToString() +
                ", sendName : " + getSendTarget() +
                ", explicitTarget:" + explicitTarget +
                ", msgBody:" + messageBody + "\n, emoji:" + emojiUnicode + ", snedEmoji:" + mSendWithEmoji;
    }

    private String targetNamesToString() {
        StringBuilder display = null;
        if (targetName != null && targetName.length != 0) {
            display = new StringBuilder("[");
            for (String name : targetName) {
                display.append(name).append(",");
            }
            display.deleteCharAt(display.length() - 1);
            display.append("]");
        }
        return display != null ? display.toString() : "<empty>";
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
                    if (LogUtil.DEBUG) {
                        LogUtil.log(TAG, String.format("Unsupported IM app: %s", parsedIMAppName));
                    }
                    break;
                default:
                    if (LogUtil.DEBUG) {
                        LogUtil.log(TAG, String.format("Cannot recognize IM app: %s", parsedIMAppName));
                    }
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

    public AppInfo getAppInfo() {
        String appPkgName = getIMAppPackageName();
        return !TextUtils.isEmpty(appPkgName) ? AppInfo.toAppInfo(appPkgName) : null;
    }

    public String getSendTargetAvatar() {
        if (imAppPkgName.equals(AppConstants.PACKAGE_WHATSAPP)) {
            return targetPhotoUri;
        } else {
            String name = getSendTarget();
            AppInfo appInfo = getAppInfo();
            String appName = appInfo != null ? appInfo.getAppName() : null;
            return !TextUtils.isEmpty(name) && !TextUtils.isEmpty(appName) ? FileUtil.getImAvatarFilePath(appName, name) : null;
        }
    }

    public boolean isExplicitTarget(Context ctx) {
        if (imAppPkgName.equals(AppConstants.PACKAGE_WHATSAPP)) {
            ContactManager.MatchedContact mc = ContactManager.getIns().findContact(ctx, targetName);
            if (mc != null) {
                switch (mc.matchedType) {
                    case ContactManager.MatchedContact.MatchedType.FULL_MATCHED:
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, String.format("Find WhatsApp, fully matched contact: %s", mc.displayName));
                        }
                        explicitTarget = true;
                        targetName = new String[]{mc.displayName};
                        break;
                    case ContactManager.MatchedContact.MatchedType.FUZZY_MATCHED:
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, String.format("Find WhatsApp, fuzzy matched contact: %s", mc.displayName));
                        }
                        targetName = new String[]{mc.displayName};
                        break;
                }
                targetPhotoUri = mc.photoUri;
            }
        }
        return explicitTarget;
    }

    public void userConfirmSendTarget() {
        explicitTarget = true;
    }

    void update(IMContent ic) {
        parsedIMApp = checkNUpdate(parsedIMApp, ic.parsedIMApp);
        imAppPkgName = checkNUpdate(imAppPkgName, ic.imAppPkgName);
        targetName = checkNUpdate(targetName, ic.targetName);
        messageBody = checkNUpdate(messageBody, ic.messageBody);
    }

    public void updateEmoji(String unicode, String desc) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("updateEmoji: %1$s, %2$s", unicode, desc));
        }
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
        return (nv == null || nv.length == 0) ? ov : nv;
    }

    public void updateSendTarget(String[] target) {
        targetName = target;
    }
}