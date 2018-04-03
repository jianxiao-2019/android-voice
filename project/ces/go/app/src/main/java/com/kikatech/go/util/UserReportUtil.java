package com.kikatech.go.util;

import android.net.Uri;
import android.os.Bundle;

import com.kikatech.go.BuildConfig;
import com.kikatech.go.util.HttpClient.HttpClientExecutor;
import com.kikatech.go.util.HttpClient.HttpClientTask;

/**
 * @author SkeeterWang Created on 2018/3/30.
 */

public class UserReportUtil {
    private static final String TAG = "UserReportUtil";

    private static final String PROTOCOL_SCHEME_HTTPS = "https";
    private static final String GOOGLE_DOC_DOMAIN = "docs.google.com";
    private static final String GOOGLE_DOC_URL_FORMAT = "/forms/d/e/%s/formResponse";
    private static final String GOOGLE_DOC_ID = "1FAIpQLSdIvvIV3Q6W4KKiQ6CESaVzvcE-L-e9fHav7_nFACeTW4gEAA";
    private static final String REPORT_DOC_URL = String.format(GOOGLE_DOC_URL_FORMAT, GOOGLE_DOC_ID);
    private static final String ENTRY_KEY_VERSION_CODE = "entry.1326542407";
    private static final String ENTRY_KEY_TITLE = "entry.1362502237";
    private static final String ENTRY_KEY_DESCRIPTION = "entry.518604434";
    private static final String ENTRY_KEY_LOG_FILE_URL = "entry.1304221790";
    private static final String ENTRY_KEY_SUBMIT = "submit";
    private static final String ENTRY_VALUE_SUBMIT = "Submit";

    public static void report(String title, String description, String logFileUrl) {
        try {
            Uri reportUri = new Uri.Builder()
                    .scheme(PROTOCOL_SCHEME_HTTPS)
                    .authority(GOOGLE_DOC_DOMAIN)
                    .path(REPORT_DOC_URL)
                    .appendQueryParameter(ENTRY_KEY_VERSION_CODE, String.valueOf(BuildConfig.VERSION_CODE))
                    .appendQueryParameter(ENTRY_KEY_TITLE, title)
                    .appendQueryParameter(ENTRY_KEY_DESCRIPTION, description)
                    .appendQueryParameter(ENTRY_KEY_LOG_FILE_URL, logFileUrl)
                    .appendQueryParameter(ENTRY_KEY_SUBMIT, ENTRY_VALUE_SUBMIT)
                    .build();
            final String reportApi = reportUri.toString();

            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "Report URL: " + reportApi);
            }

            HttpClientExecutor.getIns().asyncPOST(reportApi, null, null);
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
    }
}
