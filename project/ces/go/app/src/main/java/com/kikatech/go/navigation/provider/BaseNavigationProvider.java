package com.kikatech.go.navigation.provider;

import android.content.Context;

/**
 * @author SkeeterWang Created on 2017/10/30.
 */
public abstract class BaseNavigationProvider
{
	private static final String TAG = "BaseNavigationProvider";


	public enum NavigationMode
	{
		DRIVE( "d" ), WALK( "w" ), BIKE( "b" );

		private String modeString;

		NavigationMode( String modeString )
		{
			this.modeString = modeString;
		}

		public String getModeString()
		{
			return modeString;
		}
	}

	public enum NavigationAvoid
	{
		TOLL( "t" ), HIGHWAY( "h" ), FERRY( "f" );

		String avoidString;

		NavigationAvoid( String avoidString )
		{
			this.avoidString = avoidString;
		}

		public String getAvoidString()
		{
			return avoidString;
		}
	}


	public abstract int getDefaultZoomSize();

	public abstract void showMap( Context context, double latitude, double longitude, int zoom );

	public abstract void search( Context context, double latitude, double longitude, String keyword );

	public abstract void searchNearBy( Context context, String keyword );

	public abstract void startNavigation( Context context, String target, NavigationMode mode, NavigationAvoid... avoids );

	public abstract void startNavigation( Context context, double latitude, double longitude, NavigationMode mode, NavigationAvoid... avoids );

	public abstract void showStreetView( Context context, double latitude, double longitude );
}
