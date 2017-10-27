package com.kikatech.go.notification;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.kikatech.go.util.AppConstants;
import com.kikatech.go.util.LogUtil;

import java.util.List;

/**
 * @author wangskeeter Created on 2017/1/9.
 */
@RequiresApi( api = Build.VERSION_CODES.KITKAT )
public class NotificationWatchDog extends NotificationListenerService
{
    private static final String TAG = "NotificationWatchDog";



    @Override
    public void onNotificationPosted( StatusBarNotification statusBarNotification )
    {
		if( statusBarNotification != null )
			process( statusBarNotification );
	}

	private synchronized void process( StatusBarNotification statusBarNotification )
	{
		try
		{
			String packageName = statusBarNotification.getPackageName();

			if( !TextUtils.isEmpty( packageName ) )
			{
				switch( packageName )
				{
					case AppConstants.PACKAGE_LINE:
					case AppConstants.PACKAGE_MESSENGER:
					case AppConstants.PACKAGE_BETWEEN:
					case AppConstants.PACKAGE_WHATSAPP:
					case AppConstants.PACKAGE_TELEGRAM:
					case AppConstants.PACKAGE_KAKAOTALK:
					case AppConstants.PACKAGE_HANGOUTS:
					case AppConstants.PACKAGE_SKYPE:
					case AppConstants.PACKAGE_KIK:
					case AppConstants.PACKAGE_SLACK:
					case AppConstants.PACKAGE_VIBER:
					case AppConstants.PACKAGE_ALLO:
					case AppConstants.PACKAGE_PLUS:
					case AppConstants.PACKAGE_WECHAT:
					    // TODO process IM notification
						break;
					default:
						return;
				}
			}
		}
		catch( Exception e ) { if( LogUtil.DEBUG ) LogUtil.printStackTrace( TAG, e.getMessage(), e ); }
	}





    @Override
    public void onNotificationRemoved( StatusBarNotification statusBarNotification )
    {
		// super.onNotificationRemoved( statusBarNotification );
        // TODO remove notification from stack in Activity
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        if( LogUtil.DEBUG ) LogUtil.log( TAG, "onCreate" );
        // ensureServiceRunning();
    }

    @Override
    public int onStartCommand( Intent intent, int flags, int startId )
    {
        if( LogUtil.DEBUG ) LogUtil.log( TAG, "onStartCommand" );
        return START_STICKY;
    }




    // TODO: not working yet
    private void ensureServiceRunning()
    {
        try
        {
            boolean serviceRunning = false;
            ComponentName collectorComponent = new ComponentName( this, NotificationWatchDog.class );
            ActivityManager manager = ( ActivityManager ) getSystemService( Context.ACTIVITY_SERVICE );
            List< ActivityManager.RunningServiceInfo > runningServices = manager.getRunningServices( Integer.MAX_VALUE );
            if( runningServices == null )
            {
                if( LogUtil.DEBUG )
                    LogUtil.logw( TAG, "runningServices is NULL" );
                return;
            }
            if( LogUtil.DEBUG )
                LogUtil.logw( TAG, "runningServices is not NULL" );
            for( ActivityManager.RunningServiceInfo service : runningServices )
            {
                if( service.service.equals( collectorComponent ) )
                {
                    if( LogUtil.DEBUG )
                        LogUtil.logd( TAG, "[service] service: " + service.service +
                                ", pid: " + service.pid +
                                ", currentPID: " + Process.myPid() +
                                ", clientPackage: " + service.clientPackage +
                                ", clientCount: " + service.clientCount +
                                ", clientLabel: " + ( ( service.clientLabel == 0 ) ? "0" : "(" + getResources().getString( service.clientLabel ) + ")" ) );
                    if( service.pid == Process.myPid() /*&& service.clientCount > 0 && !TextUtils.isEmpty(service.clientPackage)*/ )
                        serviceRunning = true;
                }
            }
            if( serviceRunning )
            {
                if( LogUtil.DEBUG )
                    LogUtil.logw( TAG, "service is running" );
                return;
            }
            if( LogUtil.DEBUG )
                LogUtil.logw( TAG, "service not running, reviving..." );
            toggleNotificationListenerService();
        }
        catch( Exception ignore ) {}
    }

    private void toggleNotificationListenerService()
    {
        if( LogUtil.DEBUG ) LogUtil.log( TAG, "toggleNotificationListenerService() called" );
        try
        {
            ComponentName thisComponent = new ComponentName( this, NotificationWatchDog.class );
            PackageManager packageManager = getPackageManager();
            packageManager.setComponentEnabledSetting( thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP );
            packageManager.setComponentEnabledSetting( thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP );
        }
        catch( Exception ignore ) {}
    }

    @Override
    public IBinder onBind( Intent intent )
    {
        if( LogUtil.DEBUG ) LogUtil.log( TAG, "onBind" );
        return super.onBind( intent );
    }

    @Override
    public void onListenerConnected()
    {
        super.onListenerConnected();
        if( LogUtil.DEBUG ) LogUtil.log( TAG, "onListenerConnected" );
    }

    @Override
    public void onListenerDisconnected()
    {
        super.onListenerDisconnected();
        if( LogUtil.DEBUG ) LogUtil.log( TAG, "onListenerDisconnected" );
    }
}