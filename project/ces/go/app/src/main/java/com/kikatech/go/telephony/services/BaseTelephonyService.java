package com.kikatech.go.telephony.services;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;

import com.kikatech.go.util.LogUtil;

import java.lang.reflect.Method;

/**
 * @author SkeeterWang Created on 2017/10/26.
 */
public abstract class BaseTelephonyService
{
	private static final String TAG = "BaseTelephonyService";

	public abstract void answerPhoneCall( Context context );

	/** must check permissions first **/
	@SuppressWarnings( "MissingPermission" )
	public void makePhoneCall( Context context, @NonNull String number )
	{
		try
		{
			if( LogUtil.DEBUG ) LogUtil.log( TAG, "makePhoneCall, number: " + number );

			Uri numberUri = Uri.parse( "tel:" + number );
			Intent dial = new Intent( Intent.ACTION_CALL, numberUri );
			dial.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity( dial );
		}
		catch( Exception e )
		{
			if( LogUtil.DEBUG ) LogUtil.printStackTrace( TAG, e.getMessage(), e );
		}
	}

	@SuppressWarnings( "unchecked" )
	public void killPhoneCall( Context context )
	{
		try
		{
			if( LogUtil.DEBUG ) LogUtil.log( TAG, "killPhoneCall" );

			TelephonyManager telephonyManager = ( TelephonyManager ) context.getSystemService( Context.TELEPHONY_SERVICE );

			Class classTelephony = Class.forName( telephonyManager.getClass().getName() );
			Method methodGetITelephony = classTelephony.getDeclaredMethod( "getITelephony" );

			methodGetITelephony.setAccessible( true );

			Object telephonyInterface = methodGetITelephony.invoke( telephonyManager );

			Class telephonyInterfaceClass = Class.forName( telephonyInterface.getClass().getName() );
			Method methodEndCall = telephonyInterfaceClass.getDeclaredMethod( "endCall" );

			methodEndCall.invoke( telephonyInterface );
		}
		catch( Exception e )
		{
			if( LogUtil.DEBUG ) LogUtil.printStackTrace( TAG, e.getMessage(), e );
		}
	}
}
