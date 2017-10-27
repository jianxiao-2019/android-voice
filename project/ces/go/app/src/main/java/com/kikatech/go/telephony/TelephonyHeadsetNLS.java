package com.kikatech.go.telephony;

import android.content.ComponentName;
import android.content.Context;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.KeyEvent;

import com.kikatech.go.notification.NotificationWatchDog;
import com.kikatech.go.util.LogUtil;

import java.util.List;


/**
 * @author SkeeterWang Created on 2017/10/26.
 */
@RequiresApi( api = Build.VERSION_CODES.LOLLIPOP )
public class TelephonyHeadsetNLS extends BaseTelephonyService
{
	private static final String TAG = "TelephonyHeadsetNLS";

	private static final String PACKAGE_TELECOM_SERVER = "com.android.server.telecom";

	private static MediaController mTelecomMediaController;

	@Override
	public void answerPhoneCall( Context context )
	{
		if( LogUtil.DEBUG ) LogUtil.log( TAG, "answerPhoneCall" );
		try
		{
			Context appCtx = context.getApplicationContext();
			MediaSessionManager mediaSessionManager = ( MediaSessionManager ) appCtx.getSystemService( Context.MEDIA_SESSION_SERVICE );

			List< MediaController > mediaControllerList = mediaSessionManager.getActiveSessions( new ComponentName( appCtx, NotificationWatchDog.class ) );

			for( MediaController mediaController : mediaControllerList )
			{
				String packageName = mediaController.getPackageName();
				if( LogUtil.DEBUG ) LogUtil.logv( TAG, "packageName: " + packageName );
				if( PACKAGE_TELECOM_SERVER.equals( packageName ) )
				{
					if( LogUtil.DEBUG ) LogUtil.logd( TAG, "telecom server media session detected." );
					mTelecomMediaController = mediaController;
					break;
				}
			}

			if( mTelecomMediaController != null ) {
				if( LogUtil.DEBUG ) LogUtil.logv( TAG, "Headset Hook sent to telecom server" );
				mTelecomMediaController.dispatchMediaButtonEvent( new KeyEvent( KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK ) );
				mTelecomMediaController.dispatchMediaButtonEvent( new KeyEvent( KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK ) );
			}
		}
		catch( Exception e )
		{
			if( LogUtil.DEBUG ) LogUtil.printStackTrace( TAG, e.getMessage(), e );
		}
	}
}
