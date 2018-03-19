package com.kikatech.go.util;

/**
 * @author jasonli Created on 2017/10/19.
 */

public enum AppInfo {

    MESSENGER(AppConstants.PACKAGE_MESSENGER, AppConstants.NAME_MESSENGER, AppConstants.ICON_MESSENGER, AppConstants.SMALL_ICON_MESSENGER),
    LINE(AppConstants.PACKAGE_LINE, AppConstants.NAME_LINE, AppConstants.ICON_LINE, AppConstants.SMALL_ICON_LINE),
    WHATSAPP(AppConstants.PACKAGE_WHATSAPP, AppConstants.NAME_WHATSAPP, AppConstants.ICON_WHATSAPP, AppConstants.SMALL_ICON_WHATSAPP),
    SKYPE(AppConstants.PACKAGE_SKYPE, AppConstants.NAME_SKYPE, AppConstants.ICON_SKYPE, AppConstants.SMALL_ICON_SKYPE),
    KIK(AppConstants.PACKAGE_KIK, AppConstants.NAME_KIK, AppConstants.ICON_KIK, AppConstants.SMALL_ICON_KIK),
    KAKAOTALK(AppConstants.PACKAGE_KAKAOTALK, AppConstants.NAME_KAKAOTALK, AppConstants.ICON_KAKAOTALK, AppConstants.SMALL_ICON_KAKAOTALK),
    VIBER(AppConstants.PACKAGE_VIBER, AppConstants.NAME_VIBER, AppConstants.ICON_VIBER, AppConstants.SMALL_ICON_VIBER),
    SLACK(AppConstants.PACKAGE_SLACK, AppConstants.NAME_SLACK, AppConstants.ICON_SLACK, AppConstants.SMALL_ICON_SLACK),
    TELEGRAM(AppConstants.PACKAGE_TELEGRAM, AppConstants.NAME_TELEGRAM, AppConstants.ICON_TELEGRAM, AppConstants.SMALL_ICON_TELEGRAM),
    HANGOUTS(AppConstants.PACKAGE_HANGOUTS, AppConstants.NAME_HANGOUTS, AppConstants.ICON_HANGOUTS, AppConstants.SMALL_ICON_HANGOUTS),
    ALLO(AppConstants.PACKAGE_ALLO, AppConstants.NAME_ALLO, AppConstants.ICON_ALLO, AppConstants.SMALL_ICON_ALLO),
    BETWEEN(AppConstants.PACKAGE_BETWEEN, AppConstants.NAME_BETWEEN, AppConstants.ICON_BETWEEN, AppConstants.SMALL_ICON_BETWEEN),
    PLUS(AppConstants.PACKAGE_PLUS, AppConstants.NAME_PLUS, AppConstants.ICON_PLUS, AppConstants.SMALL_ICON_PLUS),
    WECHAT(AppConstants.PACKAGE_WECHAT, AppConstants.NAME_WECHAT, AppConstants.ICON_WECHAT, AppConstants.SMALL_ICON_WECHAT),
    SMS(AppConstants.PACKAGE_SMS, AppConstants.NAME_SMS, AppConstants.ICON_SMS, AppConstants.SMALL_ICON_SMS),

    PHONE(AppConstants.PACKAGE_PHONE, AppConstants.NAME_PHONE, AppConstants.ICON_PHONE, AppConstants.SMALL_ICON_PHONE);

    private final String packageName;
    private final String appName;
    private final int appIcon;
    private final int appIconSmall;

    AppInfo(String packageName, String appName, int appIcon, int appIconSmall) {
        this.packageName = packageName;
        this.appName = appName;
        this.appIcon = appIcon;
        this.appIconSmall = appIconSmall;
    }

    public final String getPackageName() {
        return packageName;
    }

    public String getAppName() {
        return appName;
    }

    public int getAppIcon() {
        return appIcon;
    }

    public int getAppIconSmall() {
        return appIconSmall;
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
            case AppConstants.PACKAGE_PHONE:
                return PHONE;
        }
        return null;
    }
}