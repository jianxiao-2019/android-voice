package com.kikatech.go.telephony;

import android.content.Context;
import android.media.AudioManager;

import com.kikatech.go.util.DeviceUtil;
import com.kikatech.go.util.LogUtil;

/**
 * @author SkeeterWang Created on 2017/10/26.
 */
public class TelephonyServiceManager
{
	private static final String TAG = "TelephonyManager";

	public static final String TEST_PHONE_NUMBER = "18516894507";

	private static TelephonyServiceManager sIns;
	private static BaseTelephonyService mTelephonyService;

	public static synchronized TelephonyServiceManager getIns()
	{
		if( sIns == null ) {
			sIns = new TelephonyServiceManager();
		}
		return sIns;
	}

	private TelephonyServiceManager()
	{
		if( DeviceUtil.overLollipop() ) {
			mTelephonyService = new TelephonyHeadsetNLS();
		}
		else {
			mTelephonyService = new TelephonyHeadset();
		}
	}



	public void makePhoneCall( Context context, String number )
	{
		if( LogUtil.DEBUG ) LogUtil.log( TAG, "makePhoneCall, number: " + number );

		if( mTelephonyService != null ) {
			mTelephonyService.makePhoneCall( context, number );
		}
	}

	public void answerPhoneCall( Context context )
	{
		if( LogUtil.DEBUG ) LogUtil.log( TAG, "answerPhoneCall" );

		if( mTelephonyService != null ) {
			mTelephonyService.answerPhoneCall( context );
		}
	}

	public void killPhoneCall( Context context )
	{
		if( LogUtil.DEBUG ) LogUtil.log( TAG, "killPhoneCall" );

		if( mTelephonyService != null ) {
			mTelephonyService.killPhoneCall( context );
		}
	}



	public void turnOnSpeaker( Context context )
	{
		if( LogUtil.DEBUG ) LogUtil.log( TAG, "turnOnSpeaker" );

		AudioManager audioManager = ( AudioManager ) context.getSystemService( Context.AUDIO_SERVICE );

		audioManager.setMode( AudioManager.MODE_IN_COMMUNICATION );

		if( !audioManager.isSpeakerphoneOn() ) {
			audioManager.setSpeakerphoneOn( true );
			/*
			audioManager.setStreamVolume( AudioManager.STREAM_VOICE_CALL,
										  audioManager.getStreamMaxVolume( AudioManager.STREAM_VOICE_CALL ),
										  AudioManager.STREAM_VOICE_CALL );
										  */
		}
	}

	public void turnOffSpeaker( Context context )
	{
		if( LogUtil.DEBUG ) LogUtil.log( TAG, "turnOffSpeaker" );

		AudioManager audioManager = ( AudioManager ) context.getSystemService( Context.AUDIO_SERVICE );

		audioManager.setMode( AudioManager.MODE_IN_COMMUNICATION );

		if( audioManager.isSpeakerphoneOn() ) {
			audioManager.setSpeakerphoneOn( false );
		}
	}

	public boolean isSpeakerOn( Context context )
	{
		AudioManager audioManager = ( AudioManager ) context.getSystemService( Context.AUDIO_SERVICE );
		return audioManager.isSpeakerphoneOn();
	}
}
