package com.kikatech.go.navigation;

import android.content.Context;

import com.kikatech.go.navigation.location.LocationMgr;
import com.kikatech.go.navigation.provider.BaseNavigationProvider;
import com.kikatech.go.navigation.provider.BaseNavigationProvider.NavigationAvoid;
import com.kikatech.go.navigation.provider.BaseNavigationProvider.NavigationMode;
import com.kikatech.go.navigation.provider.GoogleIntentProvider;
import com.kikatech.go.services.DialogFlowForegroundService;
import com.kikatech.go.util.LogUtil;

/**
 * @author SkeeterWang Created on 2017/10/30.
 */
public class NavigationManager {
    private static final String TAG = "NavigationManager";

    private static NavigationManager sIns;
    private static BaseNavigationProvider mNavigationProvider;

    public static synchronized NavigationManager getIns() {
        if (sIns == null) {
            sIns = new NavigationManager();
        }
        return sIns;
    }

    private NavigationManager() {
        mNavigationProvider = new GoogleIntentProvider();
    }


    public void showMap(Context context, double latitude, double longitude) {
        if (LogUtil.DEBUG)
            LogUtil.log(TAG, "showMap, latitude: " + latitude + ", longitude: " + longitude);

        if (mNavigationProvider != null) {
            mNavigationProvider.showMap(context, latitude, longitude, mNavigationProvider.getDefaultZoomSize());
        }
    }

    public void showMap(Context context, double latitude, double longitude, int zoom) {
        if (mNavigationProvider != null) {
            mNavigationProvider.showMap(context, latitude, longitude, zoom);
        }
    }

    public void showMapWithCurrentLocation(final Context context) {
        if (mNavigationProvider != null) {
            showMapWithCurrentLocation(context, mNavigationProvider.getDefaultZoomSize());
        }
    }

    public void showMapWithCurrentLocation(final Context context, final int zoom) {
        if (mNavigationProvider != null) {
            LocationMgr.fetchLocation(context, new LocationMgr.ILocationCallback() {
                @Override
                public void onGetLocation(String provider, double latitude, double longitude) {
                    if (LogUtil.DEBUG)
                        LogUtil.log(TAG, "onGetLocation, latitude: " + latitude + ", longitude: " + longitude);
                    showMap(context, latitude, longitude, zoom);
                }

                @Override
                public void onFetchTimeOut() {
                }

                @Override
                public void onLocationNotSupportError(boolean isLocationNotEnabled) {
                }
            });
        }
    }


    public void search(Context context, double latitude, double longitude, String keyword) {
        if (mNavigationProvider != null) {
            mNavigationProvider.search(context, latitude, longitude, keyword);
        }
    }

    public void searchNearBy(Context context, String keyword) {
        if (mNavigationProvider != null) {
            mNavigationProvider.searchNearBy(context, keyword);
        }
    }


    public void startNavigation(Context context, String target, NavigationMode mode, NavigationAvoid... avoids) {
        if (mNavigationProvider != null) {
            mNavigationProvider.startNavigation(context, target, mode, avoids);
            DialogFlowForegroundService.processNavigationStarted();
        }
    }

    public void startNavigation(Context context, double latitude, double longitude, NavigationMode mode, NavigationAvoid... avoids) {
        if (mNavigationProvider != null) {
            mNavigationProvider.startNavigation(context, latitude, longitude, mode, avoids);
            DialogFlowForegroundService.processNavigationStarted();
        }
    }


    public void stopNavigation(final Context context, final INavigationCallback callback) {
        if (mNavigationProvider != null) {
            LocationMgr.fetchLocation(context, new LocationMgr.ILocationCallback() {
                @Override
                public void onGetLocation(String provider, double latitude, double longitude) {
                    if (LogUtil.DEBUG) {
                        LogUtil.log(TAG, "onGetLocation, latitude: " + latitude + ", longitude: " + longitude);
                    }
                    mNavigationProvider.stopNavigation(context, latitude, longitude);
                    DialogFlowForegroundService.processNavigationStopped();
                    if (callback != null) {
                        callback.onStop();
                    }
                }

                @Override
                public void onFetchTimeOut() {
                    if (LogUtil.DEBUG) {
                        LogUtil.logw(TAG, "onFetchTimeOut");
                    }
                    mNavigationProvider.stopNavigation(context, 0, 0);
                    DialogFlowForegroundService.processNavigationStopped();
                    if (callback != null) {
                        callback.onStop();
                    }
                }

                @Override
                public void onLocationNotSupportError(boolean isLocationNotEnabled) {
                    if (LogUtil.DEBUG) {
                        LogUtil.logw(TAG, String.format("onLocationNotSupportError, isLocationNotEnabled: %s", isLocationNotEnabled));
                    }
                    mNavigationProvider.stopNavigation(context, 0, 0);
                    DialogFlowForegroundService.processNavigationStopped();
                    if (callback != null) {
                        callback.onStop();
                    }
                }
            });
        }
    }


    public void showStreetView(Context context, double latitude, double longitude) {
        if (mNavigationProvider != null) {
            mNavigationProvider.showStreetView(context, latitude, longitude);
        }
    }

    public void showStreetViewWithCurrentLocation(final Context context) {
        if (mNavigationProvider != null) {
            LocationMgr.fetchLocation(context, new LocationMgr.ILocationCallback() {
                @Override
                public void onGetLocation(String provider, double latitude, double longitude) {
                    showStreetView(context, latitude, longitude);
                }

                @Override
                public void onFetchTimeOut() {
                }

                @Override
                public void onLocationNotSupportError(boolean isLocationNotEnabled) {
                }
            });
        }
    }


    public interface INavigationCallback {
        void onStop();
    }
}
