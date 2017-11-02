package com.kikatech.go.navigation.google.webservice;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.kikatech.go.navigation.ErrorReasons;
import com.kikatech.go.navigation.location.LocationMgr;
import com.kikatech.go.navigation.model.PlaceSearchResult;
import com.kikatech.go.util.Gson.GsonUtil;
import com.kikatech.go.util.HttpClient.HttpClientExecutor;
import com.kikatech.go.util.HttpClient.HttpClientTask;
import com.kikatech.go.util.HttpClient.HttpClientUtil;
import com.kikatech.go.util.LogUtil;


/**
 * @author SkeeterWang Created on 2017/11/1.
 */
public class GooglePlaceApi
{
	private static final String TAG = "GooglePlaceApi";

	private static GooglePlaceApi sIns;

	public static synchronized GooglePlaceApi getIns()
	{
		if( sIns == null ) {
			sIns = new GooglePlaceApi();
		}
		return sIns;
	}



	public void search( final Context context, final String keyword, final IOnSearchResultListener listener )
	{
		if( LogUtil.DEBUG ) LogUtil.log( TAG, "search, keyword: " + keyword );
		LocationMgr.fetchLocation( context, new LocationMgr.ILocationCallback()
		{
			@Override
			public void onGetLocation( String provider, double latitude, double longitude )
			{
				if( LogUtil.DEBUG ) LogUtil.log( TAG, "onGetLocation, latitude: " + latitude + ", longitude: " + longitude );
				performSearch( keyword, latitude, longitude, listener );
			}

			@Override
			public void onFetchTimeOut()
			{
				if( LogUtil.DEBUG ) LogUtil.logw( TAG, "onFetchTimeOut" );
				if( listener != null ) {
					listener.onError( ErrorReasons.ERR_FETCH_TIMEOUT );
				}
			}

			@Override
			public void onLocationNotSupportError( boolean isLocationNotEnabled )
			{
				if( LogUtil.DEBUG ) LogUtil.logw( TAG, "onLocationNotSupportError, isLocationNotEnabled: " + isLocationNotEnabled );
				if( listener != null ) {
					listener.onError( ErrorReasons.ERR_LOCATION_NOT_SUPPORT );
				}
			}
		} );
	}

	private void performSearch( final String keyword, double latitude, double longitude, final IOnSearchResultListener listener )
	{
		if( LogUtil.DEBUG ) LogUtil.log( TAG, "performSearch, keyword: " + keyword + ", latitude: " + latitude + ", longitude: " + longitude );
		String url = Constants.getPlaceApiTextSearchUrl( keyword, latitude, longitude );
		HttpClientExecutor.getIns().asyncGET( url, false, new HttpClientTask.HttpClientCallback() {
			@Override
			public void onResponse( Bundle result )
			{
				String resultString = result.getString( HttpClientUtil.KEY_RESULT );

				if( !TextUtils.isEmpty( resultString ) ) {
					PlaceSearchResult placeSearchResult = GsonUtil.fromJson( resultString, PlaceSearchResult.class );
					if( listener != null ) {
						listener.onResult( placeSearchResult );
					}
				}
				else {
					if( listener != null ) {
						listener.onError( ErrorReasons.ERR_EMPTY_RESULT );
					}
				}
			}

			@Override
			public void onError( String err )
			{
				if( listener != null ) {
					listener.onError( err );
				}
			}
		} );
	}



	public interface IOnSearchResultListener
	{
		void onResult( PlaceSearchResult result );
		void onError( String err );
	}
}
