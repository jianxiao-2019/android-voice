package com.kikatech.go.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.kikatech.go.BuildConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SkeeterWang Created on 2017/4/18.
 */
public class PermissionUtil
{
	private static final String TAG = "PermissionUtils";

	public static final int PERMISSION_REQUEST = 3;

	private static final String[] SPECIAL_PERMISSIONS = new String[] {
			Manifest.permission.SYSTEM_ALERT_WINDOW,
			Manifest.permission.WRITE_SETTINGS };

	private static final Permission[] PHONE_PERMISSIONS = new Permission[] {
			Permission.CALL_PHONE,
			Permission.READ_PHONE_STATE
	};

	public enum Permission
	{
		// STORAGE
		READ_EXTERNAL_STORAGE( Manifest.permission.READ_EXTERNAL_STORAGE ),
		WRITE_EXTERNAL_STORAGE( Manifest.permission.WRITE_EXTERNAL_STORAGE ),
		// PHONE
		CALL_PHONE( Manifest.permission.CALL_PHONE ),
		READ_PHONE_STATE( Manifest.permission.READ_PHONE_STATE ),
		PROCESS_OUTGOING_CALLS( Manifest.permission.PROCESS_OUTGOING_CALLS );

		private String permission;

		Permission( String permission )
		{
			this.permission = permission;
		}
	}





	public static void checkPermission( final Activity activity, Permission... permissions )
	{
		List< String > permissionsToRequest = new ArrayList<>();
		for( Permission permission : permissions )
			if( !hasPermission( activity, permission ) )
				permissionsToRequest.add( permission.permission );
		if( !permissionsToRequest.isEmpty() )
			requestPermission( activity, permissionsToRequest.toArray( new String[ 0 ] ) );
	}

	public static boolean hasPermissions( Context context, Permission... permissions )
	{
		for( Permission permission : permissions )
			if( !hasPermission( context, permission ) )
				return false;
		return true;
	}

	public static boolean hasPermission( Context context, Permission permission )
	{
		boolean hasPermission = ActivityCompat.checkSelfPermission( context, permission.permission ) == PackageManager.PERMISSION_GRANTED;
		if( LogUtil.DEBUG ) LogUtil.logv( TAG, "permission: " + permission.name() + ", hasPermission? " + hasPermission );
		return hasPermission;
	}

	public static void checkPermissionsPhone( Activity activity )
	{
		checkPermission( activity, PHONE_PERMISSIONS );
	}

	public static boolean hasPermissionPhone( Context context )
	{
		return hasPermissions( context, PHONE_PERMISSIONS );
	}

	public static boolean shouldShowRequestRationale( Activity activity, Permission permission )
	{
		return ActivityCompat.shouldShowRequestPermissionRationale( activity, permission.permission );
	}

	private static void requestPermission( Activity activity, String[] permissions )
	{
		if( LogUtil.DEBUG )
			for( String permission : permissions )
				LogUtil.logv( TAG, "requestPermission: " + permission );
		ActivityCompat.requestPermissions( activity, permissions , PERMISSION_REQUEST );
	}





	public static void launchAppDetailSettings( Activity activity )
	{
		Intent intent = new Intent( Settings.ACTION_APPLICATION_DETAILS_SETTINGS );
		Uri uri = Uri.fromParts( "package", BuildConfig.APPLICATION_ID, null );
		intent.setData( uri );
		activity.startActivity( intent );
	}





	/**
	 * Check that all given permissions have been granted
	 * @see Activity#onRequestPermissionsResult(int, String[], int[])
	 */
	public static void onRequestPermissionsResult( int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, IDangerousPermissionResultListener listener )
	{
		switch( requestCode )
		{
			case PERMISSION_REQUEST:
				if( LogUtil.DEBUG ) LogUtil.logd( TAG, "Received response for permission request." );
				// Received permission result.
				if( grantResults.length == 1 ) // Check if the only required permission has been granted
				{
					if( grantResults[ 0 ] == PackageManager.PERMISSION_GRANTED )
					{
						if( LogUtil.DEBUG ) LogUtil.logd( TAG, "permission has been granted." );
						if( listener != null ) listener.onGrant();
					}
					else
					{
						if( LogUtil.DEBUG ) LogUtil.logd( TAG, "permission was NOT granted." );
						if( listener != null ) listener.onDenied();
					}
				}
				else
				{
					// TODO: process list permissions
					boolean allPermissionGrant = verifyPermissions( grantResults );
				}
				break;
			default:
				if( listener != null ) listener.onNoCodeFit();
				break;
		}
	}

	/**
	 * Check that all given permissions have been granted by verifying that each entry in the
	 * given array is of the value {@link PackageManager#PERMISSION_GRANTED}.
	 * @see Activity#onRequestPermissionsResult(int, String[], int[])
	 */
	private static boolean verifyPermissions( int[] grantResults )
	{
		// At least one result must be checked.
		if( grantResults.length < 1 ) return false;

		// Verify that each required permission has been granted, otherwise return false.
		for( int result : grantResults )
			if( result != PackageManager.PERMISSION_GRANTED )
				return false;

		return true;
	}





	public interface IDangerousPermissionResultListener extends IPermissionResultListener
	{
		void onDenied();
		void onNoCodeFit();
	}

	public interface IPermissionResultListener
	{
		void onGrant();
	}
}
