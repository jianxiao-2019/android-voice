package com.kikatech.usb;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.extreamsd.usbtester.USBControl;
import com.kikatech.usb.driver.AudioSystemParams;
import com.kikatech.usb.driver.USBDeviceManager;
import com.kikatech.usb.driver.interfaces.IUsbAudioTransferListener;
import com.kikatech.usb.driver.interfaces.IUsbStatusListener;
import com.kikatech.usb.util.AudioUtil;
import com.kikatech.usb.util.FileUtil;
import com.kikatech.usb.util.ImageUtil;
import com.kikatech.usb.util.LogUtil;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by tianli on 17-10-23.
 */
public class UsbAudioService extends Service implements USBDeviceManager.IUsbDriverListener
{
	private static final String TAG = "UsbAudioService";

	private final class Commands {
		private static final String USB_DRIVER_SERVICE = "usb_driver_service_";
		private static final String START_FOREGROUND = USB_DRIVER_SERVICE + "start_foreground";
		private static final String STOP_FOREGROUND = USB_DRIVER_SERVICE + "stop_foreground";
		private static final String START_AUDIO_TRANSFER = USB_DRIVER_SERVICE + "start_audio_transfer";
		private static final String STOP_AUDIO_TRANSFER = USB_DRIVER_SERVICE + "stop_audio_transfer";
	}

	private static final int SERVICE_ID = 100;

	private final static int DEFAULT_OPEN_SL_ES_BUFFER_SIZE_IN_FRAMES = 4096;
	private final static int DEFAULT_USB_BUFFER_SIZE_IN_FRAMES = 4096;
	private final static int DEFAULT_SAMPLE_RATE = 16000;//48000;//44100;

	private USBControl mUsbControl;
	private USBDeviceManager mUsbDeviceManager;
	private static IUsbStatusListener mUsbStatusListener;
	private static IUsbAudioTransferListener mUsbAudioTransferListener;
	private FileOutputStream os;
	private String filePath;
	/** for reflection called by native usb library,
	 * do not remove the function or modify the name of the function */
	public void putAudioData( final short[] data )
	{
		if( data != null )
		{
			if( mUsbAudioTransferListener != null ) mUsbAudioTransferListener.onAudioTransferBufferResult( data );
			AudioUtil.getIns().playPcm( data );

			try
			{
				if( os == null )
				{
					filePath = FileUtil.getRecordFilePath();
					os = new FileOutputStream( filePath );
				}
				// writes the data to file from buffer stores the voice buffer
				byte bData[] = AudioUtil.getIns().short2byte( data );
				os.write( bData, 0, bData.length );
			}
			catch( Exception e )
			{
				if( LogUtil.DEBUG ) LogUtil.printStackTrace( TAG, e.getMessage(), e );
			}
		}
	}
	private void closeFile()
	{
		try
		{
			os.close();
			os = null;
		}
		catch( IOException e )
		{
			if( LogUtil.DEBUG ) LogUtil.printStackTrace( TAG, e.getMessage(), e );
		}
	}



	@Override
	public void onCreate()
	{
		super.onCreate();
		prepareDriver();
	}

	private void prepareDriver()
	{
		System.loadLibrary( "esdusb" );
		System.loadLibrary( "usbtestnative" );

		mUsbControl = new USBControl();
		mUsbControl.setObjectToPassArrayTo( this );

		// USBTestNative.setLogFileName( Environment.getExternalStorageDirectory().getAbsolutePath() + "/USBTesterLog.txt" );
	}

	@Override
	public int onStartCommand( Intent intent, int flags, int startId )
	{
		try
		{
			switch( intent.getAction() )
			{
				case Commands.START_FOREGROUND:
					startForeground( SERVICE_ID , getForegroundNotification() );
					if( mUsbStatusListener != null ) mUsbStatusListener.onServiceStarted();
					if( mUsbControl.initUSB( Build.VERSION.SDK_INT < 24 ) ) {
						mUsbDeviceManager = new USBDeviceManager( getApplicationContext(), mUsbControl, this );
						mUsbDeviceManager.getUSBAudioDevices();
					}
					else {
						if( LogUtil.DEBUG ) LogUtil.logw( TAG, "Error initialising USB!" );
					}
					break;
				case Commands.STOP_FOREGROUND:
					handleStopAudioTransfer( intent );
					if( mUsbStatusListener != null ) mUsbStatusListener.onServiceStopped();
					stopForeground( true );
					break;
				case Commands.START_AUDIO_TRANSFER:
					handleStartAudioTransfer( intent );
					break;
				case Commands.STOP_AUDIO_TRANSFER:
					handleStopAudioTransfer( intent );
					if( mUsbAudioTransferListener != null ) mUsbAudioTransferListener.onAudioTransferStop( filePath );
					break;
			}
		}
		catch( Exception e )
		{
			if( LogUtil.DEBUG ) LogUtil.printStackTrace( TAG, e.getMessage(), e );
		}

		return START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		mUsbControl.cleanUp();
		if( mUsbDeviceManager != null ) {
			mUsbDeviceManager.cleanUp();
		}
		super.onDestroy();
	}



	@Override
	public void onAttached()
	{
		if( LogUtil.DEBUG ) LogUtil.log( TAG, "onAttached" );
		if( mUsbStatusListener != null ) mUsbStatusListener.onAttached();
	}

	@Override
	public void onDetached()
	{
		if( LogUtil.DEBUG ) LogUtil.log( TAG, "onDetached" );
		mUsbControl.stopAudioTransfers();
		mUsbControl.stopMIDIInputTransfers();
		// mUsbControl.cleanUp();
		mUsbControl.restartUSBAudioManager( true );

		if( mUsbStatusListener != null ) mUsbStatusListener.onDetached();
	}

	@Override
	public void onNoDevices()
	{
		if( LogUtil.DEBUG ) LogUtil.log( TAG, "onNoDevices" );
		if( mUsbStatusListener != null ) mUsbStatusListener.onNoDevices();
	}

	@Override
	public void onInitialized()
	{
		if( LogUtil.DEBUG ) LogUtil.log( TAG, "onInitialized" );
		if( mUsbStatusListener != null ) mUsbStatusListener.onInitialized();
	}

	@Override
	public void onInitializedFailed( String errorMsg )
	{
		if( LogUtil.DEBUG ) LogUtil.log( TAG, "onInitializedFailed" );
		makeToast( "USB audio interface initialize failed!" );
		if( mUsbStatusListener != null ) mUsbStatusListener.onInitializedFailed( errorMsg );
	}

	@Override
	public void onOpenFailed()
	{
		if( LogUtil.DEBUG ) LogUtil.log( TAG, "onOpenFailed" );
		makeToast( "USB audio interface open failed!" );
		if( mUsbStatusListener != null ) mUsbStatusListener.onOpenFailed();
	}



	private void handleStartAudioTransfer( Intent intent )
	{
		try
		{
			AudioUtil.getIns().playSilence( UsbAudioService.this );

			/*
			AudioManager audioManager = ( AudioManager ) getApplicationContext().getSystemService( Context.AUDIO_SERVICE );
			if( audioManager != null )
			{
				audioManager.setMode( AudioManager.MODE_NORMAL );
				audioManager.setSpeakerphoneOn( true );
			}
			*/

			int openSLESBufferSize = getOpenSLESBufferSize( DEFAULT_SAMPLE_RATE );
			mUsbControl.startAudioTransfers( false, true, DEFAULT_SAMPLE_RATE, false, DEFAULT_USB_BUFFER_SIZE_IN_FRAMES, openSLESBufferSize );

			if( mUsbAudioTransferListener != null ) mUsbAudioTransferListener.onAudioTransferStart();
		}
		catch( Exception ignore ) {}
	}

	@SuppressLint( "ObsoleteSdkInt" )
	private int getOpenSLESBufferSize( int i_requestedSampleRate )
	{
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 )
		{
			try
			{
				AudioSystemParams params = AudioSystemParams.createInstance( this );

				if( LogUtil.DEBUG )
				{
					LogUtil.logv( TAG, "Native sample rate: " + Integer.toString( params.getSampleRate() ) );
					LogUtil.logv( TAG, "Native buffer size: " + Integer.toString( params.getBufferSize() ) );
				}

				if( params.getSampleRate() == i_requestedSampleRate )
				{
					if( params.getBufferSize() > 0 )
					{
						return params.getBufferSize();
					}
				}
			}
			catch( Exception e )
			{
				if( LogUtil.DEBUG ) LogUtil.printStackTrace( TAG, e.getMessage(), e );
			}
		}

		return DEFAULT_OPEN_SL_ES_BUFFER_SIZE_IN_FRAMES;
	}

	private void handleStopAudioTransfer( Intent intent )
	{
		try
		{
			mUsbControl.stopAudioTransfers();

			closeFile();
		}
		catch( Exception ignore ) {}
	}



	private void makeToast( String msg ) {
		Toast.makeText( UsbAudioService.this, msg, Toast.LENGTH_SHORT ).show();
	}



	public static void setUsbStatusListener( IUsbStatusListener listener )
	{
		mUsbStatusListener = listener;
	}

	public static void processStart( Context context, IUsbStatusListener listener )
	{
		mUsbStatusListener = listener;
		Bundle args = new Bundle();
		launchCommend( context, Commands.START_FOREGROUND, args );
	}

	public static void processStartAudioTransfer( Context context, IUsbAudioTransferListener listener )
	{
		mUsbAudioTransferListener = listener;
		Bundle args = new Bundle();
		launchCommend( context, Commands.START_AUDIO_TRANSFER, args );
	}

	public static void processStopAudioTransfer( Context context )
	{
		Bundle args = new Bundle();
		launchCommend( context, Commands.STOP_AUDIO_TRANSFER, args );
	}

	private static void launchCommend( Context context, String action, Bundle args )
	{
		try
		{
			Context appCtx = context.getApplicationContext();
			Intent notifyIntent = new Intent( appCtx, UsbAudioService.class );
			notifyIntent.setAction( action );
			notifyIntent.putExtras( args );
			appCtx.startService( notifyIntent );
		}
		catch( Exception e ) { if( LogUtil.DEBUG ) LogUtil.printStackTrace( TAG, e.getMessage(), e ); }
	}



	private Notification getForegroundNotification()
	{
		Intent closeIntent = new Intent( UsbAudioService.this, UsbAudioService.class );
		closeIntent.setAction( Commands.STOP_FOREGROUND );
		PendingIntent closePendingIntent = PendingIntent.getService( UsbAudioService.this, SERVICE_ID, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT );

		return new NotificationCompat.Builder( UsbAudioService.this )
				.setSmallIcon( R.mipmap.ic_launcher_round )
				.setLargeIcon( ImageUtil.safeDecodeFile( getResources(), R.mipmap.ic_launcher ) )
				.setContentTitle( TAG )
				.setContentText( "Click to close service." )
				.setContentIntent( closePendingIntent )
				.setAutoCancel( true )
				// .setColor( appCtx.getResources().getColor( R.color.gela_green ) )
				.build();
	}

	@Override
	public IBinder onBind( Intent intent ) {
		return null;
	}
}
