package com.kikatech.go.util;

/**
 * @author jasonli Created on 2017/10/19.
 */

public enum AppInfo {

    MESSENGER(AppConstants.PACKAGE_MESSENGER, AppConstants.NAME_MESSENGER),
    LINE(AppConstants.PACKAGE_LINE, AppConstants.NAME_LINE),
    WHATSAPP(AppConstants.PACKAGE_WHATSAPP, AppConstants.NAME_WHATSAPP),
    SKYPE(AppConstants.PACKAGE_SKYPE, AppConstants.NAME_SKYPE),
    KIK(AppConstants.PACKAGE_KIK, AppConstants.NAME_KIK),
    KAKAOTALK(AppConstants.PACKAGE_KAKAOTALK, AppConstants.NAME_KAKAOTALK),
    VIBER(AppConstants.PACKAGE_VIBER, AppConstants.NAME_VIBER),
    SLACK(AppConstants.PACKAGE_SLACK, AppConstants.NAME_SLACK),
    TELEGRAM(AppConstants.PACKAGE_TELEGRAM, AppConstants.NAME_TELEGRAM),
    HANGOUTS(AppConstants.PACKAGE_HANGOUTS, AppConstants.NAME_HANGOUTS),
    ALLO(AppConstants.PACKAGE_ALLO, AppConstants.NAME_ALLO),
    BETWEEN(AppConstants.PACKAGE_BETWEEN, AppConstants.NAME_BETWEEN),
    PLUS(AppConstants.PACKAGE_PLUS, AppConstants.NAME_PLUS),
    WECHAT(AppConstants.PACKAGE_WECHAT, AppConstants.NAME_WECHAT),
    SMS(AppConstants.PACKAGE_SMS, AppConstants.NAME_SMS),
    ;

    private final String packageName;
    private final String appName;

    AppInfo(String packageName, String appName) {
        this.packageName = packageName;
        this.appName = appName;
    }

    public final String getPackageName() {
        return packageName;
    }

    public String getAppName() {
        return appName;
    }

    public static AppInfo toAppInfo(String packageName) {
        switch (packageName) {
            case AppConstants.PACKAGE_MESSENGER:
                return MESSENGER;
            case AppConstants.PACKAGE_LINE:
                return LINE;
            case AppConstants.PACKAGE_WHATSAPP:
                return WHATSAPP;
            case AppConstants.PACKAGE_SKYPE:
                return SKYPE;
            case AppConstants.PACKAGE_KIK:
                return KIK;
            case AppConstants.PACKAGE_KAKAOTALK:
                return KAKAOTALK;
            case AppConstants.PACKAGE_VIBER:
                return VIBER;
            case AppConstants.PACKAGE_SLACK:
                return SLACK;
            case AppConstants.PACKAGE_TELEGRAM:
                return TELEGRAM;
            case AppConstants.PACKAGE_HANGOUTS:
                return HANGOUTS;
            case AppConstants.PACKAGE_ALLO:
                return ALLO;
            case AppConstants.PACKAGE_BETWEEN:
                return BETWEEN;
            case AppConstants.PACKAGE_PLUS:
                return PLUS;
            case AppConstants.PACKAGE_WECHAT:
                return WECHAT;
            case AppConstants.PACKAGE_SMS:
                return SMS;
        }
        return null;
    }
}