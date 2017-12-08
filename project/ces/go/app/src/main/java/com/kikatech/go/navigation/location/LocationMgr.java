package com.kikatech.go.navigation.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.util.BackgroundThread;
import com.kikatech.go.util.TimeUtil;

/**
 * @author SkeeterWang Created on 2017/10/27.
 */
public class LocationMgr {
    private static final String TAG = "LocationMgr";

    private static final long FETCH_LOCATION_TIMEOUT = 15 * TimeUtil.MILLIS_IN_SECOND;
    private static final float LOCATION_PRECISION_DISTANCE = 2 * 1000;
    private static final int TWO_MINUTES = 2 * TimeUtil.MILLIS_IN_MINUTE;
    private static final String PROVIDER_TARGET[] = {LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER};

    private static boolean sGetGpsLocation = false;
    private static boolean sGetNetworkLocation = false;

    public static void init(final Context context) {
        fetchLocation(context, null);
    }

    @SuppressLint("MissingPermission")
    public static void fetchLocation(final Context context, final ILocationCallback callback) {
        if (!isLocationServiceEnabled(context)) {
            if (callback != null) {
                callback.onLocationNotSupportError(true);
            }
            return;
        }

        final FusedLocationProviderWrapper fusedWrapper = new FusedLocationProviderWrapper();
        Location lastLocation = fusedWrapper.getLastKnownLocation(context);
        if (lastLocation != null) {
            if (lastLocation.hasAccuracy() && lastLocation.getAccuracy() <= LOCATION_PRECISION_DISTANCE) {
                if (callback != null) {
                    callback.onGetLocation("fused", lastLocation.getLatitude(), lastLocation.getLongitude());
                }
                fusedWrapper.release();
                return;
            }
        }

        if (!hasGpsLocationFeature(context)) {
            if (callback != null) {
                callback.onLocationNotSupportError(false);
            }
            return;
        }

        sGetGpsLocation = sGetNetworkLocation = false;

        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        final LocationListener locationListeners[] = {null, null};

        for (int i = 0; i < PROVIDER_TARGET.length; ++i) {
            final String provider = PROVIDER_TARGET[i];
            boolean enabled = locationManager.isProviderEnabled(provider);

            LocationListener locationListener = enabled ? new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Location fusedLocation = fusedWrapper.getLastKnownLocation(context);
                    Location loc = isBetterLocation(fusedLocation, location) ? fusedLocation : location;

                    if (callback != null) {
                        callback.onGetLocation(provider, loc.getLatitude(), loc.getLongitude());
                    }

                    if (provider.equals(LocationManager.GPS_PROVIDER)) {
                        sGetGpsLocation = true;
                    } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                        sGetNetworkLocation = true;
                    }

                    for (LocationListener listener : locationListeners) {
                        if (listener != null) {
                            locationManager.removeUpdates(listener);
                        }
                    }
                    fusedWrapper.release();
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            } : null;


            if (locationListener != null) {
                locationListeners[i] = locationListener;
                locationManager.requestLocationUpdates(provider, 5000, LOCATION_PRECISION_DISTANCE, locationListener);
            }
        }

        BackgroundThread.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (locationManager != null) {
                    for (LocationListener locationListener : locationListeners) {
                        if (locationListener != null) {
                            locationManager.removeUpdates(locationListener);
                        }
                    }
                }
                fusedWrapper.release();

                if (!sGetGpsLocation && !sGetNetworkLocation) {
                    if (callback != null) {
                        callback.onFetchTimeOut();
                    }
                }
            }
        }, FETCH_LOCATION_TIMEOUT);
    }

    private static boolean hasGpsLocationFeature(Context context) {
        boolean hasGps = false;
        if (context != null) {
            try {
                PackageManager manager = context.getPackageManager();
                if (manager != null) {
                    hasGps = manager.hasSystemFeature(PackageManager.FEATURE_LOCATION) &&
                            manager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
                }
            } catch (Exception ignored) {
            }
        }
        return hasGps;
    }

    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    static public boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (location == null) {
            // if the new location is NULL
            return false;
        }
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            // If the new location is more than two minutes older, it must be worse
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    static private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    private static boolean isLocationServiceEnabled(Context context) {
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        for (String provider : PROVIDER_TARGET) {
            if (locationManager.isProviderEnabled(provider)) {
                return true;
            }
        }
        return false;
    }

    public interface ILocationCallback {
        void onGetLocation(String provider, double latitude, double longitude);

        void onFetchTimeOut();

        void onLocationNotSupportError(boolean isLocationNotEnabled);
    }
}
