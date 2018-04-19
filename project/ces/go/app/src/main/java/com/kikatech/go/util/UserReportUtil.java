package com.kikatech.go.util;

import android.net.Uri;

import com.kikatech.go.BuildConfig;
import com.kikatech.go.util.HttpClient.HttpClientExecutor;

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
    private static final String ENTRY_KEY_VERSION_CODE = "entry.200544489";
    private static final String ENTRY_KEY_TITLE = "entry.1560057994";
    private static final String ENTRY_KEY_DESCRIPTION = "entry.518604434";
    private static final String ENTRY_KEY_EMAIL = "entry.905210517";
    private static final String ENTRY_KEY_LOG_FILE_URL = "entry.1304221790";
    private static final String ENTRY_KEY_SUBMIT = "submit";
    private static final String ENTRY_VALUE_SUBMIT = "Submit";

    public static void report(String title, String description, String email, String logFileUrl) {
        try {
            Uri reportUri = new Uri.Builder()
                    .scheme(PROTOCOL_SCHEME_HTTPS)
                    .authority(GOOGLE_DOC_DOMAIN)
                    .path(REPORT_DOC_URL)
                    .appendQueryParameter(ENTRY_KEY_VERSION_CODE, String.valueOf(BuildConfig.VERSION_CODE))
                    .appendQueryParameter(ENTRY_KEY_TITLE, title)
                    .appendQueryParameter(ENTRY_KEY_DESCRIPTION, description)
                    .appendQueryParameter(ENTRY_KEY_EMAIL, email)
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

    public enum FAQReportReason {
        ERROR_HARDWARE_FAILURE("error_hardware_failure", "Hardware connected but cannot be detected."),
        ERROR_HARDWARE_DATA_INCORRECT("error_hardware_data_incorrect", "Hardware connected but data is incorrect."),
        ERROR_WAKE_UP_DETECTOR_FAILED("error_app_wake_up_detector_failed", "Wake up detector failed even without using KikaGo hardware.");

        public static final String KEY_TITLE = "key_title";
        public static final String KEY_DESCRIPTION = "key_description";

        public final String title;
        public final String description;

        FAQReportReason(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }
}
