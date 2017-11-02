package com.kikatech.go.util.Gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * @author wangskeeter Created on 16/7/12.
 */
public class GsonUtil
{
	private static final String TAG = "GsonUtils";

	private static BooleanTypeAdapter booleanTypeAdapter = new BooleanTypeAdapter();

	private static GsonBuilder gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
															  .registerTypeAdapter( boolean.class, booleanTypeAdapter )
															  .registerTypeAdapter( Boolean.class, booleanTypeAdapter );
    private static Gson gson = gsonBuilder.create();



    public static String toJson( Object src )
    {
        return gson.toJson( src );
    }

    public static  < T > T fromJson( String json, Class< T > classOfT )
    {
		try
		{
			return gson.fromJson( json, classOfT );
		}
		catch( Exception ignore ) { return null; }
	}

	public static < T > ArrayList< T > fromJsonList( String jsonList, Type typeToken )
	{
		try
		{
			return gson.fromJson( jsonList, typeToken );
		}
		catch( Exception ignore ) { return null; }
	}
}
