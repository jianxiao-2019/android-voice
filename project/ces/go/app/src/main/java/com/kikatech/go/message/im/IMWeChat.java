package com.kikatech.go.message.im;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;

import com.kikatech.go.util.AppInfo;


/**
 * @author SkeeterWang Created on 2017/8/10.
 */
@RequiresApi( api = Build.VERSION_CODES.KITKAT )
public class IMWeChat extends BaseIMObject
{
	private static final String TAG = "IMWeChat";



	IMWeChat() {}

	public IMWeChat(StatusBarNotification statusBarNotification )
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

		groupName = null;
		userName = title != null ? title.contains( "(" ) ? title.substring( 0, title.indexOf( "(" ) ).trim() : title
								 : null;
		id = userName;
		if( text != null && title != null )
		{
			String[] msgs = text.split( "\\n" );
			msgContent = msgs[ msgs.length - 1 ];

			if( text.contains( title ) )			// User
			{
				/**
				 * title: {userName}
				 * speak: [8條]{userName}: {msg}
                 * speak(en): [8]{userName}: {msg}
				 */
				userName = title;
                msgContent = text.replaceFirst("\\[(\\d+\\S?)\\]" + userName + ": ", "");
			}
			else if( msgContent.contains( ": " ) )	// Group
			{
                /**
                 * title: {groupName}
                 * speak: [6條]{userName}: {msg}
                 * speak(en): [6]{userName}: {msg}
                 */
                groupName = title;

                String firstPart = text.substring(0, text.indexOf( ": " ) ).trim();
                userName = firstPart.replaceFirst("\\[(\\d+\\S?)\\]", "");
                msgContent = msgContent.substring( msgContent.indexOf( ": " ) + 1, msgContent.length() ).trim();;
			}
		}
		else
			msgContent = null;
	}

	@Override
	public AppInfo getAppInfo()
	{
		return AppInfo.WECHAT;
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

	private IMWeChat(Parcel in )
	{
		super( in );
	}

	public static final Parcelable.Creator<IMWeChat> CREATOR = new Parcelable.Creator<IMWeChat>()
	{
		@Override
		public IMWeChat createFromParcel(Parcel in )
		{
			return new IMWeChat( in );
		}

		@Override
		public IMWeChat[] newArray(int size )
		{
			return new IMWeChat[ size ];
		}
	};
}