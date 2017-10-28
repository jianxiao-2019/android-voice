package com.kikatech.go.message.im;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.support.v4.app.RemoteInput;
import android.text.TextUtils;

import com.kikatech.go.message.Message;
import com.kikatech.go.notification.ParcelableRemoteInput;
import com.kikatech.go.util.AppConstants;
import com.kikatech.go.util.AppInfo;
import com.kikatech.go.util.DeviceUtil;
import com.kikatech.go.util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author jasonli Created on 2017/10/19.
 */

public class IMManager {

    private static final String TAG = "IMManager";

    public static final String ACTION_IM_MESSAGE_UPDATED = "com.kika.immessage.updated";
    public static final String DATA_IM_OBJECT = "IMObject";

    private static IMManager sIMManager;

    public static IMManager getInstance() {
        if(sIMManager == null) {
            sIMManager = new IMManager();
        }
        return sIMManager;
    }

    private List<BaseIMObject> mReferenceList;

    public IMManager() {
        mReferenceList = new ArrayList<>();
    }

    @RequiresApi( api = Build.VERSION_CODES.KITKAT )
    public List<BaseIMObject> getAllIMObjects() {
        List<BaseIMObject> imObjects = new ArrayList<>();
        for(BaseIMObject imObject : mReferenceList) {
            if(imObject.isValidContent() && imObject.getActionIntent() != null) {
                imObjects.add(imObject);
            }
        }
        return imObjects;
    }

    @RequiresApi( api = Build.VERSION_CODES.KITKAT )
    public List<BaseIMObject> getAllSortedIMObjects() {
        List<BaseIMObject> imObjects = new ArrayList<>();
        for(BaseIMObject imObject : mReferenceList) {
            if(imObject.isValidContent() && imObject.getActionIntent() != null) {
                imObjects.add(imObject);
            }
        }
        Collections.sort(imObjects, new Comparator<BaseIMObject>() {
            @Override
            public int compare(BaseIMObject o1, BaseIMObject o2) {
                long t1 = o1.getLatestTimestamp();
                long t2 = o2.getLatestTimestamp();
                if(t1 > t2) return 1;
                if(t2 > t1) return -1;
                return 0;
            }
        });
        return imObjects;
    }

    @RequiresApi( api = Build.VERSION_CODES.KITKAT )
    public void printAllIMObjects() {
        if(!LogUtil.DEBUG) return;
        List<BaseIMObject> imObjects = getAllIMObjects();
        for(BaseIMObject imObject : imObjects) {
            LogUtil.logv(TAG, "------------(START)------------ " + imObject.getAppName() + "   " + imObject.getUserName());
            List<Message> messages = imObject.getLatestMessages();
            for(Message message : messages) {
                LogUtil.logv(TAG, "" + message.getContent());
            }
            LogUtil.logv(TAG, "------------(END)------------ ");
        }
    }

    @RequiresApi( api = Build.VERSION_CODES.KITKAT )
    public synchronized void updateReference( BaseIMObject target ) {
        updateReference(target, true);
    }

    @RequiresApi( api = Build.VERSION_CODES.KITKAT )
    public synchronized void updateReference( BaseIMObject target, boolean updateContent )
    {
        if( LogUtil.DEBUG ) LogUtil.log( TAG, "[updateReference]" );

        if( target == null ) return;

        try
        {
            BaseIMObject reference = getReferenceIfExist( target );

            boolean isMessageDuplicated = false;
            String newMessageContent = target.getMsgContent();

            if( reference != null )
            {
                if( LogUtil.DEBUG ) LogUtil.logv( TAG, "[updateReference] reference not null, retrieveReferenceContent: " + updateContent );
                mReferenceList.remove( reference );
                if( updateContent )
                {
                    target.setLatestMessages( reference.getLatestMessages() );
                    target.setCombinedGroupName( reference.getCombinedGroupName() );
                    target.setCombinedAvatar( reference.getCombinedAvatar() );
                }
                target.setIsInvited( reference.getIsInvited() );
                if( target.getActionIntent() == null ) target.setActionIntent( reference.getActionIntent() );
                target.setRemoteInputs( reference.getRemoteInputs() );

                // checking if the message is duplicated
                Message lastMessage = target.getLatestMessage();
                if( lastMessage != null )
                    isMessageDuplicated = newMessageContent.equals( lastMessage.getContent() );
            }

            if( !isMessageDuplicated )
            {
                Message message = Message.createMessage(System.currentTimeMillis(), target.getUserName(),target.getUserName(), target.getMsgContent());
                target.addToLatestMessages(message);
            }

            if( LogUtil.DEBUG )
            {
                LogUtil.logd( TAG, "[updateReference] Messages saved, " + target.getUserName() );
                for( Message msg: target.getLatestMessages() )
                    LogUtil.logd(TAG, "[IM Message] " + target.getAppInfo().getAppName()
                            + msg.getUserName() + " -> " + msg.getContent());
            }

            mReferenceList.add( target );
        }
        catch( Exception e ) { if( LogUtil.DEBUG ) LogUtil.printStackTrace( TAG, e.getMessage(), e ); }
    }

    @RequiresApi( api = Build.VERSION_CODES.KITKAT )
    public synchronized BaseIMObject getReferenceIfExist( BaseIMObject target )
    {
        return getReferenceIfExist( target.getAppInfo(), target.getId() );
    }

    @RequiresApi( api = Build.VERSION_CODES.KITKAT )
    public synchronized BaseIMObject getReferenceIfExist( AppInfo appInfo, String id )
    {
        if( LogUtil.DEBUG ) LogUtil.log( TAG, "[getReferenceIfExist] app: " + appInfo.getAppName() + ", id: " + id );

        for( BaseIMObject referenceIM : mReferenceList )
        {
            AppInfo referenceAppInfo = referenceIM.getAppInfo();

            if( !referenceAppInfo.equals( appInfo ) ) continue;

            if( referenceIM.getId().equals( id ) )
            {
                if( LogUtil.DEBUG )
                {
                    LogUtil.logd( TAG, "[getReferenceIfExist] app: " + referenceAppInfo.getAppName() + ", getId: " + referenceIM.getId() );
                    LogUtil.logd( TAG, "\n ---------- ---------- ---------- ----------" );
                }
                return referenceIM;
            }
        }
        if( LogUtil.DEBUG )
        {
            LogUtil.logd( TAG, "[getReferenceIfExist] not exist." );
            LogUtil.logd( TAG, "\n ---------- ---------- ---------- ----------" );
        }
        return null;
    }

    public boolean sendMessage(Context context, BaseIMObject imObject, String msg) {
        if(!DeviceUtil.overKitKat()) return false;
        if(imObject == null) {
            LogUtil.logwtf( TAG, "Send message with null IMObject" );
            return false;
        }

        Intent localIntent = new Intent();
        localIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );

        List<ParcelableRemoteInput> parcelableRemoteInputs = imObject.getRemoteInputs();
        PendingIntent actionIntent = imObject.getActionIntent();
        Bundle extras = imObject.getExtras();

        try {
            if (parcelableRemoteInputs != null) {
                ArrayList<RemoteInput> remoteInputs = new ArrayList<>();
                for (ParcelableRemoteInput parcelableRemoteInput : parcelableRemoteInputs) {
                    RemoteInput remoteInput = parcelableRemoteInput.toRemoteInput();
                    remoteInputs.add(remoteInput);
                    extras.putCharSequence(remoteInput.getResultKey(), msg);
                }
                if (remoteInputs.size() == 0) {
                    if (LogUtil.DEBUG)
                        LogUtil.logwtf(TAG, "[sendMessage] The RemoteInputs is empty, causing send message failed!");
                    Exception exception = new RuntimeException("Empty RemoteInput causing send message failed!");
                    LogUtil.reportToFabric(exception);
                    return false;
                }

                // Others that I had tried, and failed
                // localBundle.putCharSequence( resultKey, "Random1 answer" );
                // localIntent.putExtra( resultKey, "Random2 answer" );
                // localIntent.putExtra( "resultKey", "Random3 Answer" );
                // localIntent.setClipData( ClipData.newIntent( "android.remoteinput.results", localIntent ) );
                RemoteInput.addResultsToIntent(remoteInputs.toArray(new RemoteInput[0]), localIntent, extras);

                if (LogUtil.DEBUG)
                    LogUtil.log(TAG, "actionIntent != null ? " + (actionIntent != null));

                actionIntent.send(context, 0, localIntent);

                Message message = Message.createMessage(null, msg);
                imObject.addToLatestMessages(message);
            }
        }
		catch( Exception e )
        {
            if (LogUtil.DEBUG) LogUtil.printStackTrace(TAG, e.getMessage(), e);
            LogUtil.reportToFabric(e);
            return false;
        }
        return true;
    }





    @RequiresApi( api = Build.VERSION_CODES.KITKAT )
    public void processNotification(Context context, StatusBarNotification statusBarNotification) {
        String packageName = statusBarNotification.getPackageName();

        if( !TextUtils.isEmpty( packageName ) )
        {
            BaseIMObject imObject;
            BaseIMObject reference;
            BaseIMObject.ResultAction resultAction;
            switch( packageName )
            {
                case AppConstants.PACKAGE_MESSENGER:
                    LogUtil.log( TAG, "onNotificationPosted, AppInfo: MESSENGER" );
                    imObject = new IMMessenger( statusBarNotification );
                    if( !imObject.isValidContent() ) return;
                    break;
                case AppConstants.PACKAGE_TELEGRAM:
                    LogUtil.log( TAG, "onNotificationPosted, AppInfo: PACKAGE_TELEGRAM" );
                    imObject = new IMTelegram( statusBarNotification );
                    if( !imObject.isValidContent() ) return;
                    break;
                case AppConstants.PACKAGE_WECHAT:
                    LogUtil.log( TAG, "onNotificationPosted, AppInfo: WECHAT" );
                    imObject = new IMWeChat( statusBarNotification );
                    if( imObject.getActionIntent() == null ) return;
                    break;
                default:
                    return;
            }
            if( !imObject.isValidContent() ) return;
            // TODO update reference in IMManager
            IMManager.getInstance().updateReference(imObject);

            // TODO notify UI there is a new message
            Intent intent = new Intent(ACTION_IM_MESSAGE_UPDATED);
            intent.putExtra(DATA_IM_OBJECT, imObject);
            context.sendBroadcast(intent);
        }
    }

}
