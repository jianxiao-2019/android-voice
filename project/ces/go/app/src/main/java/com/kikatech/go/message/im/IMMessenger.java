package com.kikatech.go.message.im;

import android.os.Build;
import android.os.Parcel;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.kikatech.go.util.AppInfo;

/**
 * @author SkeeterWang Created on 2017/8/10.
 */
@RequiresApi( api = Build.VERSION_CODES.KITKAT )
public class IMMessenger extends BaseIMObject
{
	private static final String TAG = "IMMessenger";

	private static final String NOTIFICATION_TAG_GROUP = "GROUP";
	private static final String NOTIFICATION_TAG_USER = "ONE_TO_ONE";

	private static final String[] MESSENGER_GROUP_KEYS = new String[] {
			"傳送",
			"在",
	};



	IMMessenger() {}

	public IMMessenger(StatusBarNotification statusBarNotification )
	{
		super( statusBarNotification );
	}



	@Override
	protected void processNotificationContent()
	{
		if( TextUtils.isEmpty( tag ) || ( !tag.startsWith( NOTIFICATION_TAG_GROUP ) && !tag.startsWith( NOTIFICATION_TAG_USER ) ) ) return;

		Object objectTitle = extras.get( EXTRAS_TITLE );
		String title = objectTitle != null ? objectTitle.toString() : null;
		Object objectText = extras.get( EXTRAS_TEXT );
		String text = objectText != null ? objectText.toString() : null;
		Object objectTextLines = extras.get( EXTRAS_TEXT_LINES );
		CharSequence[] textLines = objectTextLines != null ? ( CharSequence[] ) objectTextLines : null;

		if( TextUtils.isEmpty( text ) && textLines != null && textLines.length > 0 )
			text = textLines[ textLines.length - 1 ].toString();
		/** tag
		 * GROUP:1408001802563621
		 * ONE_TO_ONE:100000353121678:100008195800328
		 */
		if( tag.startsWith( "GROUP" ) ) // Group
		{
			/** syntax
			 *
			 * title: {groupName}
			 *
			 * ========== Traditional Chinese ==========
			 * text: {userName} 傳送到 {groupName}：{msg}
			 * text: {userName} 傳送到你的群組：{msg}
			 * ---------------------------------------------
			 * text: {userName} 送出 1 張貼圖到 {groupName}
			 * text: {userName} 送出 1 張貼圖到你的群組。
			 * ---------------------------------------------
			 * text: {userName} 傳送了 1 張相片到 {groupName} 。
			 * text: {userName} 傳送了 1 張相片到你的群組。
			 * ---------------------------------------------
			 * text: {userName} 從 Disney Gif 傳送了 1 張 GIF 到 {groupName} 。
			 * text: {userName} 從 Tenor GIF Keyboard 傳送了 1 張 GIF 到你的群組。
			 * ---------------------------------------------
			 *
			 * ========== Simple Chinese ==========
			 * text: {userName} 在 {groupName} 説：{msg}
			 * text: {userName} 在小组说：123
			 * ---------------------------------------------
			 * text: {userName} 向 {groupName} 发送了贴图。
			 * text: {userName} 在群聊中发了贴图。
			 * ---------------------------------------------
			 * text: {userName} 向 {groupName} 发送了照片
			 * text: {userName} 向小组发送了照片
			 * ---------------------------------------------
			 * text: {userName} 用 Disney Gif 向 {groupName} 发送了 GIF
			 * text: {userName} 用 GIPHY 向小组发送了 GIF 动图。
			 * ---------------------------------------------
			 *
			 * ========== English ==========
			 * text: {userName} : {msg}
			 * ---------------------------------------------
			 * text: {userName}：{userFirstName} sent a sticker to Test.
			 * text: {userFirstName} sent a sticker to your group.
			 * ---------------------------------------------
			 * text: {userFirstName} sent a photo to Test.
			 * text: {userFirstName} sent a photo to your group.
			 * ---------------------------------------------
			 * text: {userFirstName} sent a GIF from Disney Gif to Test.
			 * text: {userFirstName} sent a GIF from Disney Gif to your group.
			 * ---------------------------------------------
			 */
			groupName = title;
			if( !TextUtils.isEmpty( text ) )
			{
				String[] groupKeys = MESSENGER_GROUP_KEYS;

				if( text.contains( "：" ) )
				{
					for( String key : groupKeys )
					{
						if( text.contains( key ) && text.indexOf( key ) < text.indexOf( "：" ) )
						{
							userName = ( text.substring( 0, text.indexOf( key ) ) ).trim();
							break;
						}
					}
					if( userName == null )
						userName = text.substring( 0, text.indexOf( "：" ) );
					msgContent = ( text.substring( text.indexOf( "：" ) + 1, text.length() ) ).trim();
				}
				else if( text.contains( ":" ) )
				{
					for( String key : groupKeys )
					{
						if( text.contains( key ) && text.indexOf( key ) < text.indexOf( ":" ) )
						{
							userName = ( text.substring( 0, text.indexOf( key ) ) ).trim();
							break;
						}
					}
					if( userName == null )
						userName = text.substring( 0, text.indexOf( ":" ) );
					msgContent = ( text.substring( text.indexOf( ":" ) + 1, text.length() ) ).trim();
				}
				else
				{
					userName = "";
					msgContent = text;
				}
				if( TextUtils.isEmpty( userName ) ) userName = groupName;
			}
		}
		else if( tag.startsWith( "ONE_TO_ONE" ) ) // User
		{
			/** syntax
			 * title: {userName}
			 * text: {userName} : {msg}
			 */
			userName = title;
			groupName = null;
			if( !TextUtils.isEmpty( text ) )
			{
				if( text.contains( userName ) && text.contains( "：" ) )
					msgContent = text.substring( text.indexOf( "：" ) + 1, text.length() );
				else if( text.contains( userName ) && text.contains( ":" ) )
					msgContent = text.substring( text.indexOf( ":" ) + 1, text.length() );
				else
					msgContent = text;
			}
		}
		id = tag;
	}

	@Override
	public AppInfo getAppInfo()
	{
		return AppInfo.MESSENGER;
	}

	@Override
	public boolean isValidContent()
	{
		return !isSelfAppMsg() && super.isValidContent();
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

	private IMMessenger(Parcel in )
	{
		super( in );
	}

	public static final Creator< IMMessenger > CREATOR = new Creator< IMMessenger >()
	{
		@Override
		public IMMessenger createFromParcel( Parcel in )
		{
			return new IMMessenger( in );
		}

		@Override
		public IMMessenger[] newArray( int size )
		{
			return new IMMessenger[ size ];
		}
	};
}
