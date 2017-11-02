package com.kikatech.go.navigation.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kikatech.go.navigation.google.webservice.JsonKeys;
import com.kikatech.go.util.LogUtil;

/**
 * @author SkeeterWang Created on 2017/11/1.
 */
class PlaceGeometry
{
	@Expose
	@SerializedName( JsonKeys.KEY_LOCATION )
	private PlaceLocation location;

	Double getLatitude()
	{
		return location != null ? location.getLatitude() : null;
	}

	Double getLongitude()
	{
		return location != null ? location.getLongitude() : null;
	}

	void print()
	{
		if( LogUtil.DEBUG ) {
			if( location != null ) {
				location.print();
			}
		}
	}

	String getResultText()
	{
		String resultText = "";
		if( location != null ) {
			resultText = location.getResultText();
		}
		return resultText;
	}
}