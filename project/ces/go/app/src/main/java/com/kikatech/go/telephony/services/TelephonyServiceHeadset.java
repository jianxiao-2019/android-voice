package com.kikatech.go.telephony.services;

import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.kikatech.go.util.LogUtil;

/**
 * @author SkeeterWang Created on 2017/10/26.
 */
public class TelephonyServiceHeadset extends BaseTelephonyService
{
	private static final String TAG = "TelephonyServiceHeadset";

	private static final String PERMISSION_CALL_PRIVILEGED = "android.permission.CALL_PRIVILEGED";

	private static final String EXTRA_KEY_STATE = "state";
	private static final String EXTRA_KEY_MICROPHONE = "microphone";
	private static final String EXTRA_KEY_NAME = "name";

	private static final String EXTRA_VALUE_NAME = "Headset";

	@Override
	public void answerPhoneCall( Context context )
	{
		if( LogUtil.DEBUG ) LogUtil.log( TAG, "answerPhoneCall" );
		try
		{
			Runtime.getRuntime().exec( "input keyevent " + Integer.toString( KeyEvent.KEYCODE_HEADSETHOOK ) );
		}
		catch( Exception e )
		{
			if( LogUtil.DEBUG ) LogUtil.printStackTrace( TAG, e.getMessage(), e );
			answerRingingCallWithIntent( context );
		}
	}

	private static void answerRingingCallWithIntent( Context context )
	{
		try
		{
			Intent localIntent1 = new Intent( Intent.ACTION_HEADSET_PLUG );
			localIntent1.addFlags( Intent.FLAG_ACTIVITY_NO_HISTORY );
			localIntent1.putExtra( EXTRA_KEY_STATE, 1 );
			localIntent1.putExtra( EXTRA_KEY_MICROPHONE, 1 );
			localIntent1.putExtra( EXTRA_KEY_NAME, EXTRA_VALUE_NAME );
			context.sendOrderedBroadcast( localIntent1, PERMISSION_CALL_PRIVILEGED );

			Intent localIntent2 = new Intent( Intent.ACTION_MEDIA_BUTTON );
			KeyEvent localKeyEvent1 = new KeyEvent( KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK );
			localIntent2.putExtra( Intent.EXTRA_KEY_EVENT, localKeyEvent1 );
			context.sendOrderedBroadcast( localIntent2, PERMISSION_CALL_PRIVILEGED );

			Intent localIntent3 = new Intent( Intent.ACTION_MEDIA_BUTTON );
			KeyEvent localKeyEvent2 = new KeyEvent( KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK );
			localIntent3.putExtra( Intent.EXTRA_KEY_EVENT, localKeyEvent2 );
			context.sendOrderedBroadcast( localIntent3, PERMISSION_CALL_PRIVILEGED );

			Intent localIntent4 = new Intent( Intent.ACTION_HEADSET_PLUG );
			localIntent4.addFlags( Intent.FLAG_ACTIVITY_NO_HISTORY );
			localIntent4.putExtra( EXTRA_KEY_STATE, 0 );
			localIntent4.putExtra( EXTRA_KEY_MICROPHONE, 1 );
			localIntent4.putExtra( EXTRA_KEY_NAME, EXTRA_VALUE_NAME );
			context.sendOrderedBroadcast( localIntent4, PERMISSION_CALL_PRIVILEGED );
		}
		catch( Exception e )
		{
			if( LogUtil.DEBUG ) LogUtil.printStackTrace( TAG, e.getMessage(), e );
		}
	}
}
