package com.kikatech.go.message.im;

import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.kikatech.go.ui.KikaMultiDexApplication;
import com.kikatech.go.util.AppInfo;
import com.kikatech.go.util.DeviceUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.PackageManagerUtil;


/**
 * @author SkeeterWang Created on 2017/8/10.
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class IMWhatsApp extends BaseIMObject {
    private static final String TAG = "IMWhatsApp";

    IMWhatsApp() {
    }

    public IMWhatsApp(StatusBarNotification statusBarNotification) {
        super(statusBarNotification);
    }


    @Override
    protected void processNotificationContent() {
        int versionNum = getWhatsAppVersionNumber();

        if (LogUtil.DEBUG) LogUtil.logd(TAG, "[CheckVersion] whatsAppVersionNum: " + versionNum);

        Object objectTitle = extras.get(EXTRAS_TITLE);
        String title = objectTitle != null ? objectTitle.toString() : null;
        Object objectText = extras.get(EXTRAS_TEXT);
        String text = objectText != null ? objectText.toString() : null;
        Object objectTextLines = extras.get(EXTRAS_TEXT_LINES);
        CharSequence[] textLines = objectTextLines != null ? (CharSequence[]) objectTextLines : null;
        Parcelable[] pArray = extras.getParcelableArray(EXTRAS_MESSAGES);
        Object objectMessage = pArray != null && pArray.length > 0 ? pArray[pArray.length - 1] : null;
        Bundle message = objectMessage != null && objectMessage instanceof Bundle ? (Bundle) objectMessage : null;

        if (DeviceUtil.overN()) {
            if (textLines != null && textLines.length > 0) // match case 3
            {
                String latestMsg = textLines[textLines.length - 1].toString();
                if (!TextUtils.isEmpty(latestMsg)) {
                    if (!TextUtils.isEmpty(title)) {
                        if (title.equals(AppInfo.WHATSAPP.getAppName())) // parsing from latest msg
                        {
                            if (latestMsg.contains("@")) // Group
                            {
                                groupName = latestMsg.substring(latestMsg.lastIndexOf("@") + 1, latestMsg.indexOf(":")).trim();
                                userName = latestMsg.substring(0, latestMsg.lastIndexOf("@")).trim();
                                id = groupName;
                            } else // User
                            {
                                userName = latestMsg.substring(0, latestMsg.indexOf(":")).trim();
                                id = userName;
                            }
                        } else // title can be group name or user name
                        {
                            userName = title;
                            id = userName;
                        }
                    }
                    msgContent = (latestMsg.substring(latestMsg.indexOf(":") + 1, latestMsg.length())).trim();
                    resultAction = ResultAction.GET_REFERENCE_AND_SHOW;
                }
            } else if (message != null) // match case 2
            {
                /** syntax
                 * key: sender, value:
                 * key: text, value: Gh
                 * key: time, value: 1497614123000
                 **/
                Object msgSender = message.get("sender");
                String msgSenderName = msgSender != null && msgSender instanceof String ? (String) msgSender : null;
                Object msgText = message.get("text");
                String msgTextContent = msgText != null && msgText instanceof String ? (String) msgText : null;

                // possible Unicode Character 'ZERO WIDTH SPACE' (U+200B) (ascii code 8203)
                // can check by below
                // if( LogUtil.DEBUG ) LogUtil.logwtf( TAG, "char: " + ( ( int ) ( msgSenderName.charAt( 0 ) ) ) );
                String cleanMsgSenderName = !TextUtils.isEmpty(msgSenderName) ? msgSenderName.replaceAll("[\\p{Cf}]", "") : null;

                if (!TextUtils.isEmpty(cleanMsgSenderName)) // Group
                {
                    if (!TextUtils.isEmpty(title)) {
                        groupName = title.contains("(") ? title.substring(0, title.indexOf("(")).trim() : title;
                        groupName = groupName.replace(msgSenderName, "");
                        groupName = groupName.replace("@", "");
                        groupName = groupName.replace(":", "");
                        groupName = groupName.replace("：", "");
                        groupName = groupName.trim();
                        userName = msgSenderName;
                        id = groupName;
                    }
                } else // User
                {
                    groupName = null;
                    userName = !TextUtils.isEmpty(title) ? title.contains("(") ? title.substring(0, title.indexOf("(")).trim() : title : null;
                    if (userName != null) {
                        userName = userName.replace(":", "");
                        userName = userName.replace("：", "");
                    }
                    id = userName;
                }
                msgContent = msgTextContent;
                resultAction = ResultAction.SHOW_BUBBLE;
            } else // match case 1
            {
                if (TextUtils.isEmpty(tag)) {
                    groupName = null;
                    userName = title;
                    id = userName;
                    msgContent = text;
                    resultAction = ResultAction.SHOW_BUBBLE;
                }
            }
        } else if (DeviceUtil.overLollipop()) {
            if (textLines != null && textLines.length > 0) // match case 3
            {
                String latestMsg = textLines[textLines.length - 1].toString();
                if (!TextUtils.isEmpty(latestMsg)) {
                    if (!TextUtils.isEmpty(title)) {
                        if (title.equals(AppInfo.WHATSAPP.getAppName())) {
                            if (latestMsg.contains("@")) // Group
                            {
                                groupName = latestMsg.substring(latestMsg.lastIndexOf("@") + 1, latestMsg.indexOf(":")).trim();
                                userName = latestMsg.substring(0, latestMsg.lastIndexOf("@")).trim();
                                id = groupName;
                            } else // User
                            {
                                userName = latestMsg.substring(0, latestMsg.indexOf(":"));
                                id = userName;
                            }
                        } else {
                            if (title.contains("@")) // Group
                            {
                                groupName = title.substring(0, title.indexOf("@")).trim();
                                userName = latestMsg.substring(latestMsg.indexOf("@"), latestMsg.indexOf(":")).trim();
                                id = groupName;
                            } else if (latestMsg.contains(": ")) // Group
                            {
                                groupName = title;
                                userName = latestMsg.substring(0, latestMsg.lastIndexOf(": ")).trim();
                                id = groupName;
                            } else {
                                userName = title;
                                id = userName;
                            }
                        }
                        msgContent = latestMsg.substring(latestMsg.indexOf(":") + 1, latestMsg.length());
                        resultAction = ResultAction.GET_REFERENCE_AND_SHOW;
                    }
                }
            } else {
                if (!TextUtils.isEmpty(tag)) // match case 2
                {
                    if (!TextUtils.isEmpty(title)) {
                        if (title.contains("@")) // Group
                        {
                            groupName = title.substring(title.indexOf("@") + 1, title.length()).trim();
                            userName = title.substring(0, title.lastIndexOf("@")).trim();
                            id = groupName;
                        } else {
                            userName = title;
                            id = userName;
                        }
                        msgContent = "";
                        resultAction = ResultAction.UPDATE_REFERENCE;
                    }
                } else // match case 1
                {
                    if (text != null && text.contains(": ")) // Group
                    {
                        groupName = title;
                        userName = text.substring(0, text.lastIndexOf(": ")).trim();
                        msgContent = text.substring(text.lastIndexOf(": ") + 2, text.length());
                        id = groupName;
                    } else {
                        groupName = null;
                        userName = title;
                        id = userName;
                        msgContent = text;
                    }
                    resultAction = ResultAction.SHOW_BUBBLE;
                }
            }
        }
/*
        if( versionNum >= 217254 ) // 2.17.254
		{
			if( DeviceUtil.overN() )
			{
				if( textLines != null && textLines.length > 0 ) // match case 3
				{
					String latestMsg = textLines[ textLines.length - 1 ].toString();
					if( !TextUtils.isEmpty( latestMsg ) )
					{
						if( !TextUtils.isEmpty( title ) )
						{
							if( title.equals( AppInfo.WHATSAPP.getAppName() ) ) // parsing from latest msg
							{
								if( latestMsg.contains( "@" ) ) // Group
								{
									groupName = latestMsg.substring( latestMsg.lastIndexOf( "@" ) + 1, latestMsg.indexOf( ":" ) ).trim();
									userName = latestMsg.substring( 0, latestMsg.lastIndexOf( "@" ) ).trim();
									id = groupName;
								}
								else // User
								{
									userName = latestMsg.substring( 0, latestMsg.indexOf( ":" ) ).trim();
									id = userName;
								}
							}
							else // title can be group name or user name
							{
								userName = title;
								id = userName;
							}
						}
						msgContent = ( latestMsg.substring( latestMsg.indexOf( ":" ) + 1, latestMsg.length() ) ).trim();
						resultAction = ResultAction.GET_REFERENCE_AND_SHOW;
					}
				}
				else if( message != null ) // match case 2
				{
					// syntax
					// key: sender, value:
					// key: text, value: Gh
					// key: time, value: 1497614123000
					Object msgSender = message.get( "sender" );
					String msgSenderName = msgSender != null && msgSender instanceof String ? ( String ) msgSender : null;
					Object msgText = message.get( "text" );
					String msgTextContent = msgText != null && msgText instanceof String ? ( String ) msgText : null;

					// possible Unicode Character 'ZERO WIDTH SPACE' (U+200B) (ascii code 8203)
					// can check by below
					// if( LogUtil.DEBUG ) LogUtil.logwtf( TAG, "char: " + ( ( int ) ( msgSenderName.charAt( 0 ) ) ) );
					String cleanMsgSenderName = !TextUtils.isEmpty( msgSenderName ) ? msgSenderName.replaceAll( "[\\p{Cf}]", "" ) : null;

					if( !TextUtils.isEmpty( cleanMsgSenderName ) ) // Group
					{
						if( !TextUtils.isEmpty( title ) )
						{
							groupName = title.contains( "(" ) ? title.substring( 0, title.indexOf( "(" ) ).trim() : title;
							groupName = groupName.replace( msgSenderName, "" );
							groupName = groupName.replace( "@", "" );
							groupName = groupName.replace( ":", "" );
							groupName = groupName.replace( "：", "" );
							groupName = groupName.trim();
							userName = msgSenderName;
							id = groupName;
						}
					}
					else // User
					{
						groupName = null;
						userName = !TextUtils.isEmpty( title ) ? title.contains( "(" ) ? title.substring( 0, title.indexOf( "(" ) ).trim() : title : null;
						if( userName != null )
						{
							userName = userName.replace( ":", "" );
							userName = userName.replace( "：", "" );
						}
						id = userName;
					}
					msgContent = msgTextContent;
					resultAction = ResultAction.SHOW_BUBBLE;
				}
				else // match case 1
				{
					if( TextUtils.isEmpty( tag ) )
					{
						groupName = null;
						userName = title;
						id = userName;
						msgContent = text;
						resultAction = ResultAction.SHOW_BUBBLE;
					}
				}
			}
			else if( DeviceUtil.overLollipop() )
			{
				if( textLines != null && textLines.length > 0 ) // match case 3
				{
					String latestMsg = textLines[ textLines.length - 1 ].toString();
					if( !TextUtils.isEmpty( latestMsg ) )
					{
						if( !TextUtils.isEmpty( title ) )
						{
							if( title.equals( AppInfo.WHATSAPP.getAppName() ) )
							{
								if( latestMsg.contains( "@" ) ) // Group
								{
									groupName = latestMsg.substring( latestMsg.lastIndexOf( "@" ) + 1, latestMsg.indexOf( ":" ) ).trim();
									userName = latestMsg.substring( 0, latestMsg.lastIndexOf( "@" ) ).trim();
									id = groupName;
								}
								else // User
								{
									userName = latestMsg.substring( 0, latestMsg.indexOf( ":" ) );
									id = userName;
								}
							}
							else
							{
								if( title.contains( "@" ) ) // Group
								{
									groupName = title.substring( 0, title.indexOf( "@" ) ).trim();
									userName = latestMsg.substring( latestMsg.indexOf( "@" ), latestMsg.indexOf( ":" ) ).trim();
									id = groupName;
								}
								else
								{
									userName = title;
									id = userName;
								}
							}
							msgContent = latestMsg.substring( latestMsg.indexOf( ":" ) + 1, latestMsg.length() );
							resultAction = ResultAction.GET_REFERENCE_AND_SHOW;
						}
					}
				}
				else
				{
					if( !TextUtils.isEmpty( tag ) ) // match case 2
					{
						if( !TextUtils.isEmpty( title ) )
						{
							if( title.contains( "@" ) ) // Group
							{
								groupName = title.substring( title.indexOf( "@" ) + 1, title.length() ).trim();
								userName = title.substring( 0, title.lastIndexOf( "@" ) ).trim();
								id = groupName;
							}
							else
							{
								userName = title;
								id = userName;
							}
							msgContent = "";
							resultAction = ResultAction.UPDATE_REFERENCE;
						}
					}
					else // match case 1
					{
						groupName = null;
						userName = title;
						id = userName;
						msgContent = text;
						resultAction = ResultAction.SHOW_BUBBLE;
					}
				}
			}
			else // if( DeviceUtil.overKitKat() )
			{
				// TODO: support kitkat
			}
		}
		else if( versionNum >= 217107 ) // 2.17.107
		{
		}
		else // 2.17.60
		{
			if( DeviceUtil.overN() )
			{
				if( textLines != null && textLines.length > 0 ) // match case 3
				{
					String latestMsg = textLines[ textLines.length - 1 ].toString();
					if( !TextUtils.isEmpty( latestMsg ) )
					{
						if( !TextUtils.isEmpty( title ) )
						{
							if( title.equals( AppInfo.WHATSAPP.getAppName() ) ) // parsing from latest msg
							{
								if( latestMsg.contains( "@" ) ) // Group
								{
									groupName = latestMsg.substring( latestMsg.lastIndexOf( "@" ) + 1, latestMsg.indexOf( ":" ) ).trim();
									userName = latestMsg.substring( 0, latestMsg.lastIndexOf( "@" ) ).trim();
									id = groupName;
								}
								else // User
								{
									userName = latestMsg.substring( 0, latestMsg.indexOf( ":" ) ).trim();
									id = userName;
								}
							}
							else // title can be group name or user name
							{
								userName = title;
								id = userName;
							}
						}
						msgContent = ( latestMsg.substring( latestMsg.indexOf( ":" ) + 1, latestMsg.length() ) ).trim();
						resultAction = ResultAction.GET_REFERENCE_AND_SHOW;
					}
				}
				else if( message != null ) // match case 2
				{
					// syntax
					// key: sender, value:
					// key: text, value: Gh
					// key: time, value: 1497614123000
					Object msgSender = message.get( "sender" );
					String msgSenderName = msgSender != null && msgSender instanceof String ? ( String ) msgSender : null;
					Object msgText = message.get( "text" );
					String msgTextContent = msgText != null && msgText instanceof String ? ( String ) msgText : null;

					// possible Unicode Character 'ZERO WIDTH SPACE' (U+200B) (ascii code 8203)
					// can check by below
					// if( LogUtil.DEBUG ) LogUtil.logwtf( TAG, "char: " + ( ( int ) ( msgSenderName.charAt( 0 ) ) ) );
					String cleanMsgSenderName = !TextUtils.isEmpty( msgSenderName ) ? msgSenderName.replaceAll( "[\\p{Cf}]", "" ) : null;

					if( !TextUtils.isEmpty( cleanMsgSenderName ) ) // Group
					{
						if( !TextUtils.isEmpty( title ) )
						{
							groupName = title.contains( "(" ) ? title.substring( 0, title.indexOf( "(" ) ).trim() : title;
							groupName = groupName.replace( msgSenderName, "" );
							groupName = groupName.replace( "@", "" );
							groupName = groupName.replace( ":", "" );
							groupName = groupName.replace( "：", "" );
							groupName = groupName.trim();
							userName = msgSenderName;
							id = groupName;
						}
					}
					else // User
					{
						groupName = null;
						userName = !TextUtils.isEmpty( title ) ? title.contains( "(" ) ? title.substring( 0, title.indexOf( "(" ) ).trim() : title : null;
						if( userName != null )
						{
							userName = userName.replace( ":", "" );
							userName = userName.replace( "：", "" );
						}
						id = userName;
					}
					msgContent = msgTextContent;
					resultAction = ResultAction.SHOW_BUBBLE;
				}
				else // match case 1
				{
					if( TextUtils.isEmpty( tag ) )
					{
						groupName = null;
						userName = title;
						id = userName;
						msgContent = text;
						resultAction = ResultAction.SHOW_BUBBLE;
					}
				}
			}
			else // Parsing for Android 5.0 and 6.0, discard 4.4
			{
				if( textLines != null && textLines.length > 0 ) // match case 3
				{
					String latestMsg = textLines[ textLines.length - 1 ].toString();
					if( !TextUtils.isEmpty( latestMsg ) )
					{
						if( !TextUtils.isEmpty( title ) )
						{
							if( title.equals( AppInfo.WHATSAPP.getAppName() ) )
							{
								if( latestMsg.contains( "@" ) ) // Group
								{
									groupName = latestMsg.substring( latestMsg.lastIndexOf( "@" ) + 1, latestMsg.indexOf( ":" ) ).trim();
									userName = latestMsg.substring( 0, latestMsg.lastIndexOf( "@" ) ).trim();
									id = groupName;
								}
								else // User
								{
									userName = latestMsg.substring( 0, latestMsg.indexOf( ":" ) );
									id = userName;
								}
							}
							else
							{
								if( title.contains( "@" ) ) // Group
								{
									groupName = title.substring( 0, title.indexOf( "@" ) ).trim();
									userName = latestMsg.substring( latestMsg.indexOf( "@" ), latestMsg.indexOf( ":" ) ).trim();
									id = groupName;
								}
								else
								{
									userName = title;
									id = userName;
								}
							}
							msgContent = latestMsg.substring( latestMsg.indexOf( ":" ) + 1, latestMsg.length() );
							resultAction = ResultAction.GET_REFERENCE_AND_SHOW;
						}
					}
				}
				else
				{
					if( !TextUtils.isEmpty( tag ) ) // match case 2
					{
						if( !TextUtils.isEmpty( title ) )
						{
							if( title.contains( "@" ) ) // Group
							{
								groupName = title.substring( title.indexOf( "@" ) + 1, title.length() ).trim();
								userName = title.substring( 0, title.lastIndexOf( "@" ) ).trim();
								id = groupName;
							}
							else
							{
								userName = title;
								id = userName;
							}
							msgContent = "";
							resultAction = ResultAction.UPDATE_REFERENCE;
						}
					}
					else // match case 1
					{
						groupName = null;
						userName = title;
						id = userName;
						msgContent = text;
						resultAction = ResultAction.SHOW_BUBBLE;
					}
				}
			}
		}
*/

    }

    @Override
    public AppInfo getAppInfo() {
        return AppInfo.WHATSAPP;
    }


    private static int getWhatsAppVersionNumber() {
        String versionName = PackageManagerUtil.getAppVersionName(KikaMultiDexApplication.getAppContext(), AppInfo.WHATSAPP.getPackageName());
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "[getWhatsAppVersionNumber] versionName: " + versionName);
        }
        int versionNumber = 0;
        try {
            versionName = versionName.replaceAll("[^\\d]", "");
            versionNumber = Integer.parseInt(versionName);
        } catch (Exception ignore) {
        }
        return versionNumber;
    }

    // TODO: Parcelable interfaces

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    private IMWhatsApp(Parcel in) {
        super(in);
    }

    public static final Creator<IMWhatsApp> CREATOR = new Creator<IMWhatsApp>() {
        @Override
        public IMWhatsApp createFromParcel(Parcel in) {
            return new IMWhatsApp(in);
        }

        @Override
        public IMWhatsApp[] newArray(int size) {
            return new IMWhatsApp[size];
        }
    };
}