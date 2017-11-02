package com.kikatech.go.navigation.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationServices;


/**
 * @author SkeeterWang Created on 2017/10/27.
 */
public class FusedLocationProviderWrapper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
	private static final String TAG = "FusedLocationProviderWrapper";

	private GoogleApiClient mApiClient;
	private static Location mLocation = null;

	@Override
	public void onConnected( Bundle bundle )
	{
		FusedLocationProviderApi fusedLocation = LocationServices.FusedLocationApi;

		if( fusedLocation != null && mApiClient != null )
		{
			//noinspection MissingPermission
			Location location = fusedLocation.getLastLocation( mApiClient );
			if( location != null )
			{
				mLocation = location;
			}
		}
		release();
	}

	@Override
	public void onConnectionSuspended( int cause )
	{
		release();
	}

	@Override
	public void onConnectionFailed( @NonNull ConnectionResult connectionResult )
	{
		release();
	}

	public Location getLastKnownLocation( Context context )
	{
		int resultCode = -1;
		try
		{
			resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable( context );
		}
		catch( Exception ignored ) {}

		if( ConnectionResult.SUCCESS == resultCode )
		{
			if( mApiClient == null )
			{
				try
				{
					mApiClient = new GoogleApiClient.Builder( context ).addApi( LocationServices.API )
																	   .addConnectionCallbacks( this )
																	   .addOnConnectionFailedListener( this ).build();
				}
				catch( Exception ignored ) {}
			}

			if( mApiClient != null ) {
				mApiClient.connect();
			}
		}
		return mLocation;
	}

	public void release()
	{
		if( mApiClient != null && mApiClient.isConnected() )
		{
			mApiClient.disconnect();
			mApiClient = null;
		}
	}
}