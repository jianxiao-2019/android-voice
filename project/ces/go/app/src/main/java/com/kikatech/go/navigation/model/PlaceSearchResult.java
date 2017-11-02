package com.kikatech.go.navigation.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kikatech.go.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SkeeterWang Created on 2017/11/1.
 */
public class PlaceSearchResult
{
	private static final String TAG = "PlaceSearchResult";

	@Expose
	@SerializedName( "results" )
	private List< Place > results = new ArrayList<>();

	public Place get( int index )
	{
		if( results != null && index < results.size() ) {
			return results.get( index );
		}
		return null;
	}

	public void print()
	{
		if( LogUtil.DEBUG ) {
			if( results != null && !results.isEmpty() ) {
				for( Place place : results ) {
					place.print();
					LogUtil.logd( TAG, "--------------------------------------------------" );
				}
			}
			else {
				LogUtil.logv( TAG, "Empty result" );
			}
		}
	}

	public String getResultText()
	{
		String result = "";
		if( results != null && !results.isEmpty() ) {
			for( Place place : results ) {
				result = result + place.getResultText() + "\n--------------------------------------------------\n";
			}
		}
		else {
			result = "Empty result";
		}
		return result;
	}
}
