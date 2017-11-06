package com.kikatech.go.telephony;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.telephony.TelephonyManager;

import com.kikatech.go.ui.TelephonyActivity;
import com.kikatech.go.util.LogUtil;

/**
 * @author SkeeterWang Created on 2017/10/16.
 */
public class MyPhoneStateReceiver extends BroadcastReceiver
{
	private static final String TAG = "PhoneReceiver";

	@Override
	public void onReceive( final Context context, Intent intent )
	{
		if( LogUtil.DEBUG ) LogUtil.log( TAG, "onReceive" );

		final TelephonyManager tm = ( TelephonyManager ) context.getSystemService( Service.TELEPHONY_SERVICE );

		switch( tm.getCallState() )
		{
			case TelephonyManager.CALL_STATE_OFFHOOK:// 電話打進來接通狀態；電話打出時首先監聽到的狀態。
				if( LogUtil.DEBUG ) LogUtil.log( TAG, "[onCallStateChanged] CALL_STATE_OFFHOOK" );
				break;
			case TelephonyManager.CALL_STATE_RINGING:// 電話打進來狀態
				if( LogUtil.DEBUG ) LogUtil.log( TAG, "[onCallStateChanged] CALL_STATE_RINGING" );
				new Handler().postDelayed( new Runnable() {
					@Override
					public void run()
					{
						Intent controlIntent = new Intent( context, TelephonyActivity.class );
						controlIntent.setAction( TelephonyActivity.ACTION_CONTROL_PHONE_CALL );
						controlIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
						context.startActivity( controlIntent );
					}
				}, 1500 );
				break;
			case TelephonyManager.CALL_STATE_IDLE:// 不管是電話打出去還是電話打進來都會監聽到的狀態。
				if( LogUtil.DEBUG ) LogUtil.log( TAG, "[onCallStateChanged] CALL_STATE_IDLE" );
				new Handler().postDelayed( new Runnable() {
					@Override
					public void run()
					{
						Intent controlIntent = new Intent( context, TelephonyActivity.class );
						controlIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
						context.startActivity( controlIntent );
					}
				}, 1500 );
				break;
		}
	}
}