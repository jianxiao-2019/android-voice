package com.kikatech.go.message.im;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;

import com.kikatech.go.util.AppInfo;
import com.kikatech.go.util.StringUtil;

/**
 * @author SkeeterWang Created on 2017/8/10.
 */
@RequiresApi( api = Build.VERSION_CODES.KITKAT )
public class IMTelegram extends BaseIMObject
{
	private static final String TAG = "IMTelegram";



	IMTelegram() {}

	public IMTelegram(StatusBarNotification statusBarNotification )
	{
		super( statusBarNotification );
	}



	@Override
	protected void processNotificationContent()
	{
		Object objectTitle = extras.get( EXTRAS_TITLE );
		String title = objectTitle != null ? objectTitle.toString() : null;
		Object objectText = extras.get( EXTRAS_TEXT );
		String text = objectText != null ? objectText.toString() : null;

		if( title != null && ( title.equals( "Telegram" ) || title.equals( "Telegram Notifications" ) ) )
		{
			/**
			 * title: Telegram
			 * speak: 14 new messages from 3 chats
			 *
			 * title: Telegram Notifications
			 * speak: New in version X.X:...
			 */
			return;
		}
		if( text != null )
		{
			// speak: 5 new messages
			if( StringUtil.matchRegularExpression( text, "^[0-9]+ new messages" ) ) return;
		}

		groupName = null;
		userName = title != null ? title.contains( "(" ) ? title.substring( 0, title.indexOf( "(" ) ).trim() : title
								 : null;
		id = userName;
		if( text != null )
		{
			String[] msgs = text.split( "\\n" );
			msgContent = msgs[ msgs.length - 1 ];

			if( msgContent.contains( ": " ) ) // Group
			{
				/**
				 * title: {groupName}
				 * speak: {userName}: {msg}
				 */
				groupName = title;
				userName = msgContent.substring( 0, msgContent.indexOf( ": " ) ).trim();
				msgContent = msgContent.substring( msgContent.indexOf( ": " ) + 1, msgContent.length() ).trim();;
			}
		}
		else
			msgContent = null;
	}

	@Override
	public AppInfo getAppInfo()
	{
		return AppInfo.TELEGRAM;
	}



	// TODO: Parcelable interfaces

	@Override
	public int describeContents()
	{
		return super.describeContents();
	}

	@Override
	public void writeToParcel( Parcel dest, int flags )
	{
		super.writeToParcel( dest, flags );
	}

	private IMTelegram(Parcel in )
	{
		super( in );
	}

	public static final Parcelable.Creator< IMTelegram > CREATOR = new Parcelable.Creator< IMTelegram >()
	{
		@Override
		public IMTelegram createFromParcel( Parcel in )
		{
			return new IMTelegram( in );
		}

		@Override
		public IMTelegram[] newArray( int size )
		{
			return new IMTelegram[ size ];
		}
	};
}