package com.kikatech.go.navigation;

import android.content.Context;

import com.kikatech.go.navigation.location.LocationMgr;
import com.kikatech.go.navigation.provider.BaseNavigationProvider;
import com.kikatech.go.navigation.provider.BaseNavigationProvider.NavigationAvoid;
import com.kikatech.go.navigation.provider.BaseNavigationProvider.NavigationMode;
import com.kikatech.go.navigation.provider.GoogleIntentProvider;
import com.kikatech.go.util.LogUtil;

/**
 * @author SkeeterWang Created on 2017/10/30.
 */
public class NavigationManager
{
	private static final String TAG = "NavigationManager";

	private static NavigationManager sIns;
	private static BaseNavigationProvider mNavigationService;

	public static synchronized NavigationManager getIns()
	{
		if( sIns == null ) {
			sIns = new NavigationManager();
		}
		return sIns;
	}

	private NavigationManager()
	{
		mNavigationService = new GoogleIntentProvider();
	}



	public void showMap( Context context, double latitude, double longitude )
	{
		if( LogUtil.DEBUG ) LogUtil.log( TAG, "showMap, latitude: " + latitude + ", longitude: " + longitude );

		if( mNavigationService != null ) {
			mNavigationService.showMap( context, latitude, longitude, mNavigationService.getDefaultZoomSize() );
		}
	}

	public void showMap( Context context, double latitude, double longitude, int zoom )
	{
		if( mNavigationService != null ) {
			mNavigationService.showMap( context, latitude, longitude, zoom );
		}
	}

	public void showMapWithCurrentLocation( final Context context )
	{
		if( mNavigationService != null ) {
			showMapWithCurrentLocation( context, mNavigationService.getDefaultZoomSize() );
		}
	}

	public void showMapWithCurrentLocation( final Context context, final int zoom )
	{
		if( mNavigationService != null ) {
			LocationMgr.fetchLocation( context, new LocationMgr.ILocationCallback()
			{
				@Override
				public void onGetLocation( String provider, double latitude, double longitude )
				{
					if( LogUtil.DEBUG ) LogUtil.log( TAG, "onGetLocation, latitude: " + latitude + ", longitude: " + longitude );
					showMap( context, latitude, longitude, zoom );
				}

				@Override
				public void onFetchTimeOut() {}

				@Override
				public void onLocationNotSupportError( boolean isLocationNotEnabled ) {}
			} );
		}
	}



	public void search( Context context, double latitude, double longitude, String keyword )
	{
		if( mNavigationService != null ) {
			mNavigationService.search( context, latitude, longitude, keyword );
		}
	}

	public void searchNearBy( Context context, String keyword )
	{
		if( mNavigationService != null ) {
			mNavigationService.searchNearBy( context, keyword );
		}
	}



	public void startNavigation( Context context, String target, NavigationMode mode, NavigationAvoid... avoids )
	{
		if( mNavigationService != null ) {
			mNavigationService.startNavigation( context, target, mode, avoids );
			NavigationService.processStart( context );
		}
	}

	public void startNavigation( Context context, double latitude, double longitude, NavigationMode mode, NavigationAvoid... avoids )
	{
		if( mNavigationService != null ) {
			mNavigationService.startNavigation( context, latitude, longitude, mode, avoids );
			NavigationService.processStart( context );
		}
	}



	public void showStreetView( Context context, double latitude, double longitude )
	{
		if( mNavigationService != null ) {
			mNavigationService.showStreetView( context, latitude, longitude );
		}
	}

	public void showStreetViewWithCurrentLocation( final Context context )
	{
		if( mNavigationService != null ) {
			LocationMgr.fetchLocation( context, new LocationMgr.ILocationCallback()
			{
				@Override
				public void onGetLocation( String provider, double latitude, double longitude )
				{
					showStreetView( context, latitude, longitude );
				}

				@Override
				public void onFetchTimeOut() {}

				@Override
				public void onLocationNotSupportError( boolean isLocationNotEnabled ) {}
			} );
		}
	}
}
