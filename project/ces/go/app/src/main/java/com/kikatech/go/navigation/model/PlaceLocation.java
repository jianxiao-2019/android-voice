package com.kikatech.go.navigation.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kikatech.go.navigation.google.webservice.JsonKeys;
import com.kikatech.go.util.LogUtil;


/**
 * @author SkeeterWang Created on 2017/11/1.
 */
class PlaceLocation
{
	private static final String TAG = "PlaceLocation";

	@Expose
	@SerializedName( JsonKeys.KEY_LAT )
	private double lat;

	@Expose
	@SerializedName( JsonKeys.KEY_LNG )
	private double lng;

	double getLatitude()
	{
		return lat;
	}

	double getLongitude()
	{
		return lng;
	}

	void print()
	{
		if( LogUtil.DEBUG ) {
			LogUtil.logv( TAG, "lat: " + lat + ", lng: " + lng );
		}
	}

	String getResultText()
	{
		return "lat: " + lat + ", lng: " + lng;
	}
}
