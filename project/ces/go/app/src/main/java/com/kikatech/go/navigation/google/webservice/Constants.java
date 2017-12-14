package com.kikatech.go.navigation.google.webservice;

import android.net.Uri;
import android.text.TextUtils;

import java.util.Locale;

/**
 * @author SkeeterWang Created on 2017/11/2.
 */
public class Constants {
    private static final String DEVELOPER_API_KEY = "AIzaSyBTCFdcfSCeWPYWONMWOd9Z3bnUKuLLApI";

    private static final String HTTPS = "https://";
    private static final String DOMAIN = "maps.googleapis.com";
    private static final String PATH_MAPS = "/maps";
    private static final String PATH_API = "/api";
    private static final String PATH_PLACE = "/place";
    private static final String PATH_TEXT_SEARCH = "/textsearch";
    private static final String FORMAT_JSON = "/json";
    private static final String PARAMETER_IS = "?";
    private static final String PARAMETER_AND = "&";

    private static final String PARAMETER_QUERY_IS = "query=";
    private static final String PARAMETER_LOCATION_IS = "location=";
    private static final String PARAMETER_LANGUAGE_IS = "language=";
    private static final String PARAMETER_KEY_IS = "key=";

    public static String getPlaceApiTextSearchUrl(String keyword, double latitude, double longitude) {
        return HTTPS + DOMAIN + PATH_MAPS + PATH_API + PATH_PLACE + PATH_TEXT_SEARCH + FORMAT_JSON
                + PARAMETER_IS
                + PARAMETER_QUERY_IS + Uri.encode(keyword)
                + PARAMETER_AND
                + PARAMETER_LOCATION_IS + latitude + "," + longitude
                + PARAMETER_AND
                + PARAMETER_LANGUAGE_IS + getLanguageCode()
                + PARAMETER_AND
                + PARAMETER_KEY_IS + DEVELOPER_API_KEY;
    }

    public static String getPlaceApiTextSearchUrl(String keyword) {
        return HTTPS + DOMAIN + PATH_MAPS + PATH_API + PATH_PLACE + PATH_TEXT_SEARCH + FORMAT_JSON
                + PARAMETER_IS
                + PARAMETER_QUERY_IS + Uri.encode(keyword)
                + PARAMETER_AND
                + PARAMETER_LANGUAGE_IS + getLanguageCode()
                + PARAMETER_AND
                + PARAMETER_KEY_IS + DEVELOPER_API_KEY;
    }

    private static String getLanguageCode() {
        Locale locale = Locale.US; //Locale.getDefault();

        StringBuilder result = new StringBuilder(locale.getLanguage());

        String region = locale.getCountry();

        if (!TextUtils.isEmpty(region)) {
            result.append('_').append(region);
        }
        return result.toString();
    }
}