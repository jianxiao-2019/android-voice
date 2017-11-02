package com.kikatech.go.navigation.provider;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.kikatech.go.util.LogUtil;

import java.util.Locale;

/**
 * @author SkeeterWang Created on 2017/10/30.
 */
public class GoogleIntentProvider extends BaseNavigationProvider
{
	private static final String TAG = "GoogleIntentProvider";

	private static final String PACKAGE_GOOGLE_MAP = "com.google.android.apps.maps";

	private static final int DEFAULT_ZOOM_SIZE = 10;



	@Override
	public int getDefaultZoomSize()
	{
		return DEFAULT_ZOOM_SIZE;
	}

	@Override
	public void showMap( Context context, double latitude, double longitude, int zoom )
	{
		Intent mapIntent = getShowMapIntent( latitude, longitude, zoom );
		sendGoogleMapIntent( context, mapIntent );
	}

	@Override
	public void search( Context context, double latitude, double longitude, String keyword )
	{
		Intent mapIntent = getSearchIntent( latitude, longitude, keyword );
		sendGoogleMapIntent( context, mapIntent );
	}

	@Override
	public void searchNearBy( Context context, String keyword )
	{
		Intent mapIntent = getSearchNearByIntent( keyword );
		sendGoogleMapIntent( context, mapIntent );
	}

	@Override
	public void startNavigation( Context context, String target, NavigationMode mode, NavigationAvoid... avoids )
	{
		Intent mapIntent = getNavigationIntent( target, mode, avoids );
		sendGoogleMapIntent( context, mapIntent );
	}

	@Override
	public void startNavigation( Context context, double latitude, double longitude, NavigationMode mode, NavigationAvoid... avoids )
	{
		Intent mapIntent = getNavigationIntent( latitude, longitude, mode, avoids );
		sendGoogleMapIntent( context, mapIntent );
	}

	@Override
	public void showStreetView( Context context, double latitude, double longitude )
	{
		Intent mapIntent = getStreetViewIntent( latitude, longitude );
		sendGoogleMapIntent( context, mapIntent );
	}


	private void sendGoogleMapIntent( Context context, Intent mapIntent )
	{
		sendGoogleMapIntent( context, mapIntent, true );
	}

	private void sendGoogleMapIntent( Context context, Intent mapIntent, boolean restartGoogleMap )
	{
		try
		{
			if( restartGoogleMap ) {
				mapIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
			}
			PendingIntent pendingIntent = PendingIntent.getActivity( context, 0, mapIntent, PendingIntent.FLAG_UPDATE_CURRENT );
			pendingIntent.send();
		}
		catch( Exception e )
		{
			if( LogUtil.DEBUG ) LogUtil.printStackTrace( TAG, e.getMessage(), e );
			try
			{
				context.startActivity( mapIntent );
			}
			catch( Exception ignore ) {}
		}
	}



	// google map intents

	private Intent getShowMapIntent( double latitude, double longitude, int zoom )
	{
		String GEO = String.format( Locale.ENGLISH, "geo:%f,%f?z=%d", latitude, longitude, zoom );
		Uri gmmIntentUri = Uri.parse( GEO );
		Intent mapIntent = new Intent( Intent.ACTION_VIEW, gmmIntentUri );
		mapIntent.setPackage( PACKAGE_GOOGLE_MAP );
		return mapIntent;
	}


	private Intent getSearchNearByIntent( String keyword )
	{
		return getSearchIntent( 0, 0, keyword );
	}

	private Intent getSearchIntent( double latitude, double longitude, String keyword )
	{
		String SEARCH = String.format( Locale.ENGLISH, "geo:%f,%f?q=%s", latitude, longitude, keyword );
		Uri gmmIntentUri = Uri.parse( SEARCH );
		Intent mapIntent = new Intent( Intent.ACTION_VIEW, gmmIntentUri );
		mapIntent.setPackage( PACKAGE_GOOGLE_MAP );
		return mapIntent;
	}


	private Intent getNavigationIntent( double latitude, double longitude, NavigationMode mode, NavigationAvoid... avoids )
	{
		// google.navigation:q=latitude,longitude
		String keyword = String.format( Locale.ENGLISH, "%f,%f", latitude, longitude );
		return getNavigationIntent( keyword, mode, avoids );
	}

	private Intent getNavigationIntent( String keyword, NavigationMode mode, NavigationAvoid... avoids )
	{
		String modeString = mode != null ? mode.getModeString() : null;
		String avoidsString = "";
		if( avoids != null ) {
			for( NavigationAvoid navigationAvoid : avoids ) {
				avoidsString += navigationAvoid.getAvoidString();
			}
		}
		else {
			avoidsString = null;
		}
		return getNavigationIntent( keyword, modeString, avoidsString );
	}

	private Intent getNavigationIntent( String keyword, String mode, String avoids )
	{
		// google.navigation:q=a+street+address
		String INTENT = "google.navigation:";
		String DESTINATION = String.format( Locale.ENGLISH, "q=%s", keyword );
		String MODE = !TextUtils.isEmpty( mode ) ? String.format( Locale.ENGLISH, "&mode=%s", mode ) : "";
		String AVOID = !TextUtils.isEmpty( avoids ) ? String.format( Locale.ENGLISH, "&avoid=%s", avoids ) : "";
		Uri gmmIntentUri = Uri.parse( INTENT + DESTINATION + MODE + AVOID );
		Intent mapIntent = new Intent( Intent.ACTION_VIEW, gmmIntentUri );
		mapIntent.setPackage( PACKAGE_GOOGLE_MAP );
		return mapIntent;
	}


	private Intent getStreetViewIntent( double latitude, double longitude )
	{
		/*
		// Uses a PanoID to show an image from Maroubra beach in Sydney, Australia
		Uri gmmIntentUri = Uri.parse( "google.streetview:panoid=Iaa2JyfIggYAAAQfCZU9KQ" );
		Intent mapIntent = new Intent( Intent.ACTION_VIEW, gmmIntentUri );
		mapIntent.setPackage( PACKAGE_GOOGLE_MAP );
		startActivity( mapIntent );

		// Opens Street View between two Pyramids in Giza. The values passed to the
		// cbp parameter will angle the camera slightly up, and towards the east.
		Uri gmmIntentUri = Uri.parse( "google.streetview:cbll=29.9774614,31.1329645&cbp=0,30,0,0,-15" );
		Intent mapIntent = new Intent( Intent.ACTION_VIEW, gmmIntentUri );
		mapIntent.setPackage( PACKAGE_GOOGLE_MAP );
		startActivity( mapIntent );
		*/
		String GEO = String.format( Locale.ENGLISH, "google.streetview:cbll=%f,%f", latitude, longitude );
		Uri gmmIntentUri = Uri.parse( GEO );
		Intent mapIntent = new Intent( Intent.ACTION_VIEW, gmmIntentUri );
		mapIntent.setPackage( PACKAGE_GOOGLE_MAP );
		return mapIntent;
	}
}
