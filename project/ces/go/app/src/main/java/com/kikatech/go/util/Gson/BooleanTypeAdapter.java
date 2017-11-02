package com.kikatech.go.util.Gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.kikatech.go.util.LogUtil;

import java.lang.reflect.Type;

/**
 * @author SkeeterWang Created on 2017/5/26.
 */
class BooleanTypeAdapter implements JsonSerializer< Boolean >, JsonDeserializer< Boolean >
{
	private static final String TAG = "BooleanTypeAdapter";

	@Override
	public JsonElement serialize( Boolean arg0, Type arg1, JsonSerializationContext arg2 )
	{
		return new JsonPrimitive( arg0 ? 1 : 0 );
	}

	@Override
	public Boolean deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context )
	{
		try
		{
			return json.getAsInt() == 1;
		}
		catch( Exception e )
		{
			try
			{
				return json.getAsString().equals( "true" );
			}
			catch( Exception e2 )
			{
				if( LogUtil.DEBUG ) {
					LogUtil.printStackTrace( TAG, e.getMessage(), e );
					LogUtil.printStackTrace( TAG, e2.getMessage(), e2 );
				}
			}
		}
		return false;
	}
}