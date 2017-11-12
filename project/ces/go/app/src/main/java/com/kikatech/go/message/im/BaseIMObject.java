package com.kikatech.go.message.im;

import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.text.TextUtils;

import com.kikatech.go.message.Chatable;
import com.kikatech.go.message.Message;
import com.kikatech.go.notification.ParcelableRemoteInput;
import com.kikatech.go.util.AppInfo;
import com.kikatech.go.util.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author SkeeterWang Created on 2017/8/10.
 */
@RequiresApi( api = Build.VERSION_CODES.KITKAT )
public abstract class BaseIMObject extends Chatable
{
	private static final String TAG = "BaseIMObject";

	protected static final String EXTRAS_LINE_CHAT_ID = "line.chat.id";
	protected static final String EXTRAS_LINE_STICKER_URL = "line.sticker.url";
	protected static final String EXTRAS_TITLE = "android.title";
	protected static final String EXTRAS_TEXT = "android.speak";
	protected static final String EXTRAS_SUBTEXT = "android.subText";
	protected static final String EXTRAS_TEXT_LINES = "android.textLines";
	protected static final String EXTRAS_MESSAGES = "android.messages";
	protected static final String EXTRAS_SUMMARY_TEXT = "android.summaryText";
	protected static final String EXTRAS_CONVERSATION_TITLE = "android.conversationTitle";
	protected static final String EXTRAS_SELF_DISPLAY_NAME = "android.selfDisplayName";
	protected static final String EXTRAS_LARGE_ICON = "android.largeIcon";
	protected static final String EXTRAS_EXTENSIONS_WEARABLE = "android.wearable.EXTENSIONS";
	protected static final String EXTRAS_EXTENSIONS_CAR = "android.car.EXTENSIONS";



	public enum ResultAction
	{
		INVALID_CONTENT,
		UPDATE_REFERENCE,
		GET_REFERENCE_AND_SHOW,
		SHOW_BUBBLE,
		DIALOG_CONTENT_INVISIBLE; // for line
		public static ResultAction toResultAction( String name )
		{
			try
			{
				return valueOf( name );
			}
			catch( Exception ignore ) {}
			return null;
		}
	}



	protected abstract void processNotificationContent();
	public abstract AppInfo getAppInfo();



	// TODO: Models

	// Notification infos
	protected String tag;
	protected Bundle extras;
	private PendingIntent pendingIntent;	// use to open App
	private PendingIntent actionIntent;		// use to send msg
	private ArrayList<ParcelableRemoteInput> parcelableRemoteInputs = new ArrayList<>();

	// Content data: from parsing result
	protected String msgImgUrl;
	protected ResultAction resultAction;

	// Extra data
	private long chatIndex;
	private boolean isInvited;
	private String combinedGroupName;	// currently for line using only
	private Bitmap combinedAvatar;		// currently for line using only
	private Bitmap largeIcon;
	private String avatar_file_path;


	BaseIMObject() {}

	/**
	 * To extract WearNotification with RemoteInputs that we can use to respond later on.
	 * ------------------------------------------------------------------------------------------------------------------
	 * Should work for communicators such:
	 * "com.whatsapp", "com.facebook.orca", "com.google.android.talk", "jp.naver.line.android", "org.telegram.messenger"
	 * ------------------------------------------------------------------------------------------------------------------
	 * @param statusBarNotification notification caught by NotificationListenerService
	 */
	public BaseIMObject( StatusBarNotification statusBarNotification )
	{
		try
		{
			processNotificationInfos( statusBarNotification );
		}
		catch( Exception e ){ if( LogUtil.DEBUG ) LogUtil.printStackTrace( TAG, e.getMessage(), e ); }
	}

	private void processNotificationInfos( StatusBarNotification statusBarNotification )
	{
		if( LogUtil.DEBUG ) LogUtil.logd( TAG, "[processNotificationInfos] Start ---------- ---------- ---------- ----------" );

		Notification notification = statusBarNotification.getNotification();

		this.tag = statusBarNotification.getTag(); // TODO: find how to pass Tag with sending PendingIntent, might fix Hangout problem

		this.extras = new Bundle();
		this.extras.putAll( notification.extras );

		this.pendingIntent = notification.contentIntent;

		processWearableExtender( notification );

		if( LogUtil.DEBUG ) printNotificationInfos();

		processNotificationContent();

		if( LogUtil.DEBUG ) printContentData();

		removeParcelFileDescriptor();

		if( LogUtil.DEBUG ) LogUtil.logd( TAG, "[processNotificationInfos] Ended ---------- ---------- ---------- ----------" );
	}

	/** actionIntent, remoteInputs **/
	private void processWearableExtender( Notification notification )
	{
		NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender( notification );

		List< NotificationCompat.Action > actions = wearableExtender.getActions();
		for( NotificationCompat.Action action : actions )
		{
			if( action.getRemoteInputs() != null )
			{
				for( RemoteInput remoteInput : Arrays.asList( action.getRemoteInputs() ) )
					parcelableRemoteInputs.add( new ParcelableRemoteInput( remoteInput ) );
				actionIntent = action.actionIntent;
				if( LogUtil.DEBUG )
					LogUtil.log( TAG, "[actionIntent] " + actionIntent.toString() );
				break;
			}
		}

		// if there is no available wearable extender object, parsing car extender instead
		if( actions.size() == 0 )
		{
			NotificationCompat.CarExtender carExtender = new NotificationCompat.CarExtender( notification );
			if( carExtender.getUnreadConversation() != null )
			{
				RemoteInput remoteInput = carExtender.getUnreadConversation().getRemoteInput();
				parcelableRemoteInputs.add( new ParcelableRemoteInput( remoteInput ) );
				actionIntent = carExtender.getUnreadConversation().getReplyPendingIntent();
				largeIcon = carExtender.getLargeIcon();
				if( LogUtil.DEBUG ) LogUtil.log( TAG, "[actionIntent] " + actionIntent.toString() );
			}
		}
	}

	/**
	 * Possible java.lang.RuntimeException: Not allowed to write file descriptors here
	 * #issue: http://stackoverflow.com/questions/20929107/android-broadcasting-parcelable-data
	 **/
	private void removeParcelFileDescriptor()
	{
		if( extras.containsKey( "android.car.EXTENSIONS" ) )
			extras.remove( "android.car.EXTENSIONS" );
		if( extras.containsKey( "android.wearable.EXTENSIONS" ) )
			extras.remove( "android.wearable.EXTENSIONS" );
	}


	// TODO: Notification infos

	public void setTag( String tag )
	{
		this.tag = tag;
	}

	public String getTag()
	{
		return tag;
	}

	public String getAppName()
	{
		AppInfo appInfo = getAppInfo();
		return appInfo != null ? appInfo.getAppName() : null;
	}

	public void setExtras( Bundle extras )
	{
		this.extras = extras;
	}

	public Bundle getExtras()
	{
		return extras;
	}

	public void setPendingIntent( PendingIntent pendingIntent )
	{
		this.pendingIntent = pendingIntent;
	}

	public PendingIntent getPendingIntent()
	{
		return pendingIntent;
	}

	public void setActionIntent( PendingIntent actionIntent )
	{
		this.actionIntent = actionIntent;
	}

	public PendingIntent getActionIntent()
	{
		return actionIntent;
	}

	public void setRemoteInputs( ArrayList< ParcelableRemoteInput > remoteInputs )
	{
		if( remoteInputs == null ) return;
		if( parcelableRemoteInputs != null && parcelableRemoteInputs.isEmpty() )
			parcelableRemoteInputs = remoteInputs;
	}

	public ArrayList< ParcelableRemoteInput > getRemoteInputs()
	{
		return parcelableRemoteInputs;
	}

	// Content data

	public Bitmap getPhoto()
	{
		try
		{
			if( largeIcon != null ) return largeIcon;
			if( extras != null ) return ( Bitmap ) extras.get( EXTRAS_LARGE_ICON );
		}
		catch( Exception e ) { e.printStackTrace(); }
		return null;
	}

	public void setPhoto( Bitmap photo )
	{
		if( largeIcon == null && extras.get( EXTRAS_LARGE_ICON ) == null )
			largeIcon = photo;
	}

	@Override
	public String getGroupName()
	{
		return groupName != null ? groupName
				: combinedGroupName != null ? combinedGroupName
				: null;
	}

	public String getMsgImgUrl()
	{
		return msgImgUrl;
	}

	public ResultAction getResultAction()
	{
		return resultAction;
	}

	// Extra data

	public void setChatIndex( long chatIndex )
	{
		this.chatIndex = chatIndex;
	}

	public long getChatIndex()
	{
		return chatIndex;
	}

	public void setIsInvited( boolean isInvited )
	{
		this.isInvited = isInvited;
	}

	public boolean getIsInvited()
	{
		return isInvited;
	}

	public void setCombinedGroupName( String combinedGroupName )
	{
		this.combinedGroupName = combinedGroupName;
	}

	public String getCombinedGroupName()
	{
		return combinedGroupName;
	}

	public void setCombinedAvatar( Bitmap combinedAvatar )
	{
		this.combinedAvatar = combinedAvatar;
	}

	public Bitmap getCombinedAvatar()
	{
		return combinedAvatar;
	}

	public void setAvatarFilePath( String avatar_file_path )
	{
		this.avatar_file_path = avatar_file_path;
	}

	public String getAvatarFilePath()
	{
		return avatar_file_path;
	}





	public boolean isGroup()
	{
		return !TextUtils.isEmpty( groupName ) || !TextUtils.isEmpty( combinedGroupName );
	}

	public boolean hasSavedCombinedData()
	{
		return combinedAvatar != null || !TextUtils.isEmpty( combinedGroupName );
	}





	// TODO: Judgement

	public boolean isValidContent()
	{
		return ! ( TextUtils.isEmpty( getGroupName() ) && TextUtils.isEmpty( userName ) && TextUtils.isEmpty( msgContent ) );
	}

	boolean isSelfAppMsg()
	{
		String APP_NAME = getAppName();
		return !TextUtils.isEmpty( APP_NAME ) &&
				( ( isGroup() && APP_NAME.equals( getGroupName() ) ) || APP_NAME.equals( userName ) );
	}





	// TODO: Logs

	public void printNotificationInfos()
	{
		try
		{
			LogUtil.log( TAG, "[NotificationInfo] tag: " + tag + ", app: " + getAppName() );

			if( extras != null && extras.keySet() != null )
			{
				for( String key : extras.keySet() )
				{
					switch( key )
					{
						case EXTRAS_MESSAGES:
							Parcelable[] pArray = extras.getParcelableArray( key );
							if( pArray == null ) return;
							for( Parcelable parcelable : pArray )
							{
								Bundle message = parcelable != null && parcelable instanceof Bundle ? ( Bundle ) parcelable : null;
								if( message != null )
									for( String messageKey : message.keySet() )
										safetyPrintKeyValue( "extras:message", message, messageKey );
							}
							break;
						//case IntentUtils.EXTRAS_EXTENSIONS_WEARABLE:
						//	break;
						case EXTRAS_TEXT_LINES:
							Object objectTextLines = extras.get( key );
							CharSequence[] textLines = objectTextLines != null ? ( CharSequence[] ) objectTextLines : null;
							if( textLines != null )
								for( CharSequence textLine : textLines )
									if( textLine != null ) LogUtil.logv( TAG, "[NotificationInfo][extras] key: " + key + ", value: " + textLine.toString() );
							break;
						default:
							safetyPrintKeyValue( "extras", extras, key );
							break;
					}
				}
			}
		}
		catch( Exception e ) { LogUtil.printStackTrace( TAG, e.getMessage(), e ); }
	}

	private void printRemoteInputInfos()
	{
		try
		{
			for( ParcelableRemoteInput parcelableRemoteInput : parcelableRemoteInputs )
			{
				RemoteInput remoteInput = parcelableRemoteInput.toRemoteInput();

				// Some more details of RemoteInput... no idea what for but maybe it will be useful at some point
				String resultKey = remoteInput.getResultKey();
				String label = remoteInput.getLabel().toString();
				Boolean canFreeForm = remoteInput.getAllowFreeFormInput();

				LogUtil.logv( TAG, "[NotificationInfo][RemoteInput] resultKey: " + resultKey + ", label: " + label + ", canFreeForm: " + canFreeForm );

				if( remoteInput.getExtras() != null )
				{
					Bundle remoteInputArgs = remoteInput.getExtras();
					if( remoteInputArgs.keySet() != null )
						for( String key : remoteInputArgs.keySet() )
							safetyPrintKeyValue( "RemoteInput", remoteInputArgs, key );
				}

				if( remoteInput.getChoices() != null && remoteInput.getChoices().length > 0 )
					for( CharSequence choice : remoteInput.getChoices() )
						LogUtil.logv( TAG, "[NotificationInfo][RemoteInput] choice: " + choice.toString() );
			}
		}
		catch( Exception e ) { LogUtil.printStackTrace( TAG, e.getMessage(), e ); }
	}

	private void safetyPrintKeyValue( String printTag, Bundle bundle, String key )
	{
		try
		{
			Object value = bundle.get( key );
			if( value != null )
				LogUtil.logv( TAG, "[NotificationInfo][" + printTag + "] key: " + key + ", value: " + value.toString() );
		}
		catch( Exception e ) { LogUtil.printStackTrace( TAG, e.getMessage(), e ); }
	}

	public void printContentData()
	{
		LogUtil.logv( TAG, "[ContentData] id: " + id );
		LogUtil.logv( TAG, "[ContentData] groupName: " + groupName );
		LogUtil.logv( TAG, "[ContentData] userName: " + userName );
		LogUtil.logv( TAG, "[ContentData] msgContent: " + msgContent );
	}





	// TODO: Parcelable interfaces

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel( Parcel dest, int flags )
	{
		try
		{
			// Notification infos
			dest.writeString( tag );
			dest.writeBundle( extras );
			dest.writeParcelable( pendingIntent, flags );
			dest.writeParcelable( actionIntent, flags );
			dest.writeTypedList( parcelableRemoteInputs );
			// Content data
			dest.writeString( id );
			dest.writeString( groupName );
			dest.writeString( userName );
			dest.writeString( msgContent );
			dest.writeString( msgImgUrl );
			dest.writeString( resultAction != null ? resultAction.name() : null );
			// Extra data
			dest.writeLong( chatIndex );
			dest.writeByte( ( byte ) ( isInvited ? 1 : 0 ) );
			dest.writeString( combinedGroupName );
			dest.writeParcelable( combinedAvatar, flags );
			dest.writeString( avatar_file_path );
			dest.writeTypedList( latestMessages );
			dest.writeParcelable( largeIcon, flags );
		}
		catch( Exception e ) { if( LogUtil.DEBUG ) LogUtil.printStackTrace( TAG, e.getMessage(), e ); }
	}

	protected BaseIMObject( Parcel in )
	{
		try
		{
			// Notification infos
			tag = in.readString();
			extras = in.readBundle( getClass().getClassLoader() );
			pendingIntent = in.readParcelable( PendingIntent.class.getClassLoader() );
			actionIntent = in.readParcelable( PendingIntent.class.getClassLoader() );
			parcelableRemoteInputs = in.createTypedArrayList( ParcelableRemoteInput.CREATOR );
			// Content data
			id = in.readString();
			groupName = in.readString();
			userName = in.readString();
			msgContent = in.readString();
			msgImgUrl = in.readString();
			resultAction = ResultAction.toResultAction( in.readString() );
			// Extra data
			chatIndex = in.readLong();
			isInvited = in.readByte() != 0;
			combinedGroupName = in.readString();
			combinedAvatar = in.readParcelable( Bitmap.class.getClassLoader() );
			avatar_file_path = in.readString();
			latestMessages = in.createTypedArrayList( Message.CREATOR );
			largeIcon = in.readParcelable( Bitmap.class.getClassLoader() );
		}
		catch( Exception e ) { if( LogUtil.DEBUG ) LogUtil.printStackTrace( TAG, e.getMessage(), e ); }
	}





	public interface IMsgProcessListener
	{
		void onSucceed();
		void onError(Exception e);
	}
}
