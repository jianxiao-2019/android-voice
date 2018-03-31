package com.kikatech.go.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author SkeeterWang Created on 2018/3/31.
 */

public class NetworkUtil {
    private static final String TAG = "NetworkUtil";

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conMgr == null) {
            return false;
        }
        NetworkInfo networkInfo = conMgr.getActiveNetworkInfo();
        /*
        NetworkInfo mobileNwInfo = connectivityManager.getNetworkInfo( ConnectivityManager.TYPE_MOBILE );
        NetworkInfo wifiNwInfo = connectivityManager.getNetworkInfo( ConnectivityManager.TYPE_WIFI );
        return ( ( mobileNwInfo != null && mobileNwInfo.isConnected() ) || ( wifiNwInfo != null && wifiNwInfo.isConnected() ) );
        */
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
}
