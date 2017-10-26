package com.kikatech.usb.driver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.extreamsd.usbtester.USBControl;
import com.kikatech.usb.util.LogUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class USBDeviceManager
{
	private static final String TAG = "USBDeviceManager";

	private Context mContext;

	private boolean mRetried = false;
	final String UsbManagerClassName = "android.hardware.usb.UsbManager";
	final String UsbDeviceClassName = "android.hardware.usb.UsbDevice";

	private UsbDevice mUsbDevice = null;
	private UsbDeviceConnection mConnection = null;

	private USBControl mUsbControl = null;

	private boolean m_audioDeviceOpened = false;
	private int m_devicesToQuery = 0;
	private int m_queriedDevices = 0;

	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	private static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
	private static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
	private static final String EXTRA_DEVICE = "device";
	private static final String EXTRA_PERMISSION_GRANTED = "permission";
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver()
	{
		public void onReceive( Context context, Intent intent )
		{
			performOnUsbActionReceived( context, intent );
		}

		private void performOnUsbActionReceived( Context context, Intent intent )
		{
			try
			{
				String action = intent.getAction();
				Log.v( TAG, "action = " + action );
				switch( action )
				{
					case ACTION_USB_PERMISSION:
						synchronized( this )
						{
							//Log.v(TAG, "Permission response");
							if( m_audioDeviceOpened )
							{
								Log.v( TAG, "USB audio device already opened!" );
								return;
							}

							m_queriedDevices++;
							UsbDevice device = intent.getParcelableExtra( EXTRA_DEVICE ); //UsbManager.EXTRA_DEVICE);

							if( intent.getBooleanExtra( EXTRA_PERMISSION_GRANTED /*UsbManager.EXTRA_PERMISSION_GRANTED*/, false ) )
							{
								if( device != null )
								{
									mUsbDevice = device;

									openDevice( context, device );
								}
							}
							else
							{
								Log.d( TAG, "permission denied for device " + device );
							}
						}
						break;
					case ACTION_USB_DETACHED:
						//Log.v(TAG, "DETACHED!");
						UsbDevice device = intent.getParcelableExtra( EXTRA_DEVICE ); //UsbManager.EXTRA_DEVICE);
						if( device != null )
						{
							int detachedDeviceVendorId = device.getVendorId();
							int attachedDeviceVendorId = mUsbDevice.getVendorId();

							int detachedProductId = device.getProductId();
							int attachedProductId = mUsbDevice.getProductId();

							Log.d( TAG, "detachedDeviceVendorId: " + detachedDeviceVendorId );
							Log.d( TAG, "attachedDeviceVendorId: " + attachedDeviceVendorId );
							Log.d( TAG, "detachedProductId: " + detachedProductId );
							Log.d( TAG, "attachedProductId: " + attachedProductId );

							if( detachedDeviceVendorId == attachedDeviceVendorId && detachedProductId == attachedProductId )
							{
								if( mConnection != null )
								{
									mConnection.close();
									mConnection = null;
								}

								mUsbDevice = null;
								m_audioDeviceOpened = false;

								mUsbDriverHandler.sendMsg( UsbDriverHandler.MsgCommands.USB_DETACHED );
							}
						}
						break;
					case ACTION_USB_ATTACHED:
						mUsbDriverHandler.sendMsg( UsbDriverHandler.MsgCommands.USB_ATTACHED );
						Object deviceO = intent.getParcelableExtra( EXTRA_DEVICE );
						if( deviceO != null )
						{
							Log.v( TAG, "Attached in USBDeviceManager received!" );
							getUSBAudioDevices();
						}
						break;
				}
			}
			catch( Exception ignore ){}
		}
	};

	private static IUsbDriverListener mUsbDriverListener;
	private UsbDriverHandler mUsbDriverHandler = new UsbDriverHandler();
	private static class UsbDriverHandler extends Handler
	{
		private final class MsgCommands
		{
			private static final int USB_INITIALIZED = 1;
			private static final int USB_DETACHED = 2;
			private static final int USB_ATTACHED = 3;
			private static final int USB_NO_DEVICES = 4;
			private static final int USB_INITIALIZED_FAILED = 5;
			private static final int USB_OPEN_FAILED = 6;
		}

		@Override
		public void handleMessage( Message msg )
		{
			int command = msg.what;

			switch( command )
			{
				case MsgCommands.USB_ATTACHED:
					if( mUsbDriverListener != null ) mUsbDriverListener.onAttached();
					break;
				case MsgCommands.USB_DETACHED:
					if( mUsbDriverListener != null ) mUsbDriverListener.onDetached();
					break;

				case MsgCommands.USB_NO_DEVICES:
					if( mUsbDriverListener != null ) mUsbDriverListener.onNoDevices();
					break;

				case MsgCommands.USB_INITIALIZED:
					if( mUsbDriverListener != null ) mUsbDriverListener.onInitialized();
					break;
				case MsgCommands.USB_INITIALIZED_FAILED:
					String errorMsg = msg.obj != null && msg.obj instanceof String ? ( String ) msg.obj : null;
					if( mUsbDriverListener != null ) mUsbDriverListener.onInitializedFailed( errorMsg );
					break;

				case MsgCommands.USB_OPEN_FAILED:
					if( mUsbDriverListener != null ) mUsbDriverListener.onOpenFailed();
					break;
			}
		}

		private void sendMsg( int what )
		{
			sendMsg( what, null );
		}

		private void sendMsg( int what, String command )
		{
			if( !TextUtils.isEmpty( command ) )
				sendMessage( obtainMessage( what, command ) );
			else
				sendEmptyMessage( what );
		}
	}

	private void openDevice( Context context, UsbDevice device )
	{
		Log.i( TAG, "openDevice" );

		UsbManager manager = ( UsbManager ) context.getSystemService( Context.USB_SERVICE );
		mConnection = manager.openDevice( device );
		mUsbDevice = device;

		Log.d( TAG, "mConnection != null ? " + String.valueOf( mConnection != null ) );

		if( mConnection != null )
		{
			int fileDescriptor = mConnection.getFileDescriptor();

			boolean initUSBOk;

			if( android.os.Build.VERSION.SDK_INT >= 24 )
			{
				initUSBOk = mUsbControl.initUSBDeviceByName( fileDescriptor, device.getDeviceName(), device.getProductId(), device.getVendorId(), mConnection.getRawDescriptors(), mConnection.getRawDescriptors().length );
			}
			else
			{
				initUSBOk = mUsbControl.initUSBDevice( fileDescriptor, device.getProductId(), device.getVendorId() );
			}

			if( !initUSBOk )
			{
				Log.v( TAG, "Fail: product id = " + device.getProductId() );
				Log.d( TAG, "m_queriedDevices: " + m_queriedDevices );
				Log.d( TAG, "m_devicesToQuery: " + m_devicesToQuery );

				if( !mRetried && ( device.getVendorId() == 0x2466 && device.getProductId() == 0x0003 ) ) // Axe-Fx II
				{
					Log.v( TAG, "DOING ANOTHER ROUND!!!" );
					mConnection.close();
					mUsbDevice = null;
					mRetried = true;
					getUSBAudioDevices();
				}
				else if( m_queriedDevices == m_devicesToQuery )
				{
					if( mUsbControl.getOpenDeviceErrorMessage().length() > 0 )
					{
						mUsbDriverHandler.sendMsg( UsbDriverHandler.MsgCommands.USB_INITIALIZED_FAILED, mUsbControl.getOpenDeviceErrorMessage() );
					}
					else
					{
						mUsbDriverHandler.sendMsg( UsbDriverHandler.MsgCommands.USB_INITIALIZED_FAILED );
					}
				}
				else
				{
					if( LogUtil.DEBUG ) LogUtil.logw( TAG, "Silently skipping device!" );
					mUsbDriverHandler.sendMsg( UsbDriverHandler.MsgCommands.USB_INITIALIZED_FAILED );
				}
			}
			else
			{
				m_audioDeviceOpened = true;
				mUsbDriverHandler.sendMsg( UsbDriverHandler.MsgCommands.USB_INITIALIZED );
			}
		}
		else
		{
			if( LogUtil.DEBUG ) LogUtil.logw( TAG, "Failed to open USB device" );
			mUsbDriverHandler.sendMsg( UsbDriverHandler.MsgCommands.USB_OPEN_FAILED );
		}
	}

	public USBDeviceManager( Context context, USBControl usbControl, IUsbDriverListener listener )
	{
		mContext = context;
		mUsbControl = usbControl;
		mUsbDriverListener = listener;

		IntentFilter filter = new IntentFilter();
		filter.addAction( UsbManager.ACTION_USB_DEVICE_ATTACHED );
		filter.addAction( UsbManager.ACTION_USB_DEVICE_DETACHED );
		mContext.registerReceiver( mUsbReceiver, filter );
	}


	public int getUSBAudioDevices()
	{
		@SuppressWarnings( "WrongConstant" )
		UsbManager manager = ( UsbManager ) mContext.getSystemService( "usb" ); //Context.USB_SERVICE);

		if( manager == null )
		{
			Log.e( TAG, "UsbManager was null!" );
			return 0;
		}

		try // see if we can cast to an Activity
		{
			//Activity activity = (Activity) m_context;
			mUsbDevice = null; //(UsbDevice) activity.getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
		}
		catch( Exception e )
		{
			Log.e( TAG, "Exception in getUSBAudioDevices! " + e );
		}

		int possibleAudioDevices = 0;
		if( LogUtil.DEBUG ) LogUtil.log( TAG, "getUSBAudioDevices" );

		if( mUsbDevice == null )
		{
			if( LogUtil.DEBUG ) LogUtil.log( TAG, "getDevices" );

			PendingIntent mPermissionIntent = PendingIntent.getBroadcast( mContext, 0, new Intent( ACTION_USB_PERMISSION ), 0 );
			IntentFilter filter = new IntentFilter( ACTION_USB_PERMISSION );
			mContext.registerReceiver( mUsbReceiver, filter );

			HashMap< String, UsbDevice > devices = manager.getDeviceList();

			if( devices.size() == 0 )
			{
				Log.v( "USBTester", "NO devices found!" );
				if( LogUtil.DEBUG ) LogUtil.logw( TAG, "NO devices found, calling initUSBDevice() in hope for root access" );
				/*
				boolean linuxSeesAudioDevice = mUsbControl.isAudioDevicePresentForLinux();
				if (linuxSeesAudioDevice)
				{
					bundle.putString( "ExtraErrorMsg", mContext.getResources().getString( R.string.AndroidDidNotFindAnyDevice ) );
				}
				else
				{
					bundle.putString( "ExtraErrorMsg", mContext.getResources().getString( R.string.AndroidAndLinuxDidNotFindAnyDevice ) );
				}
				*/
				mUsbDriverHandler.sendMsg( UsbDriverHandler.MsgCommands.USB_NO_DEVICES );
			}

			boolean hasClass2Vendor5401 = false;

			m_devicesToQuery = 0;
			Iterator< ? > it = devices.entrySet().iterator();
			while( it.hasNext() )
			{
				@SuppressWarnings( "rawtypes" ) Map.Entry pairs = ( Map.Entry ) it.next();
				UsbDevice dev = ( UsbDevice ) pairs.getValue();

				if( dev != null )
				{
					int deviceClass = dev.getDeviceClass();

					if( ( ( deviceClass == 1 ) || ( deviceClass == 0 ) || ( deviceClass == 239 ) || ( deviceClass == 255 ) ) && // inspect interface device class, misc = 239, 255 = vendor specific
						dev.getVendorId() != 0x05C6 && dev.getVendorId() != 0x05E1 && // Symantec bluetooth and video cameras
						dev.getVendorId() != 0x0A5C ) // Broadcom (bluetooth)
					{
						m_devicesToQuery++;
					}

					if( dev.getVendorId() == 0x1519 && dev.getProductId() == 0x443 ) // Samsung dock?
					{
						//appendLog(m_context, "   hasClass2Vendor5401");
						hasClass2Vendor5401 = true;
					}
				}
			}
			if( LogUtil.DEBUG ) LogUtil.log( TAG, "Devices to query = " + m_devicesToQuery + ", hasClass2Vendor5401 = " + hasClass2Vendor5401 );

			it = devices.entrySet().iterator();
			while( it.hasNext() )
			{
				@SuppressWarnings( "rawtypes" ) Map.Entry pairs = ( Map.Entry ) it.next();
				UsbDevice dev = ( UsbDevice ) pairs.getValue();

				if( dev != null )
				{
					int deviceClass = dev.getDeviceClass();
					//Log.v(TAG, "deviceClass = " + deviceClass + ", dev.getVendorId() = " + dev.getVendorId());
					if( LogUtil.DEBUG ) LogUtil.log( TAG, "deviceClass = " + deviceClass + ", dev.getVendorId() = " + dev.getVendorId() );

					if( ( ( deviceClass == 1 ) || ( deviceClass == 0 ) || ( deviceClass == 239 ) || ( deviceClass == 255 ) ) && dev.getVendorId() != 0x05C6 && dev.getVendorId() != 0x05E1 && // Symantec bluetooth and video cameras
						dev.getVendorId() != 0x0A5C )
					{
						if( hasClass2Vendor5401 && dev.getVendorId() == 0x8BB && m_devicesToQuery >= 2 )
						{
							if( LogUtil.DEBUG ) LogUtil.logw( TAG, "Skipping dock audio!" );
							continue;
						}
						else
						{
							//appendLog(m_context, "Not skipping: hasClass2Vendor5401 = " + hasClass2Vendor5401 + ", dev.getVendorId() = " + dev.getVendorId() + ", m_devicesToQuery = " + m_devicesToQuery);
						}

						possibleAudioDevices++;
						//Log.v(TAG, "----> requestPermission");
						if( LogUtil.DEBUG ) LogUtil.log( TAG, "requestPermission" );

						if( manager.hasPermission( dev ) == false )
						{
							manager.requestPermission( dev, mPermissionIntent );
						}
						else
						{
							openDevice( mContext, dev );
						}
					}
				}
			}
		}
		else
		{
			Log.v( TAG, "USB device already exists" );
			mConnection = manager.openDevice( mUsbDevice );

			if( mConnection != null )
			{
				int fd = mConnection.getFileDescriptor();
			}
			else
			{
				mUsbDriverHandler.sendMsg( UsbDriverHandler.MsgCommands.USB_OPEN_FAILED );
			}


			Toast.makeText( mContext, "mUsbDevice != null", Toast.LENGTH_SHORT ).show();
		}

		return possibleAudioDevices;
	}


	public void cleanUp()
	{
		//Log.v(TAG, "cleanUp");
		if( mContext != null && mUsbReceiver != null )
		{
			try
			{
				//Log.v(TAG, "unregisterReceiver");
				mContext.unregisterReceiver( mUsbReceiver );
			}
			catch( Exception e )
			{
				Log.e( TAG, "Exception in USB cleanup" );
			}
		}

		if( mConnection != null )
		{
			mConnection.close();
		}
	}


	public interface IUsbDriverListener
	{
		void onAttached();
		void onDetached();
		void onNoDevices();
		void onInitialized();
		void onInitializedFailed( String errorMsg );
		void onOpenFailed();
	}
}

