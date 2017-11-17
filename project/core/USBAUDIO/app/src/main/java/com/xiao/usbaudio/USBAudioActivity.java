package com.xiao.usbaudio;

import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.os.Bundle;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbDeviceConnection;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class USBAudioActivity extends Activity
{

    private static final String TAG = "UsbAudio";
	
    private static final String ACTION_USB_PERMISSION = "com.minelab.droidspleen.USB_PERMISSION";
    PendingIntent mPermissionIntent = null;
    UsbManager mUsbManager = null;
    UsbDevice mAudioDevice = null;
    
    UsbAudio mUsbAudio = null;
    private UsbDeviceConnection m_connection = null;

    private UsbReciever mUsbPermissionReciever;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

	        // Grab the USB Device so we can get permission
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while(deviceIterator.hasNext()){
            UsbDevice device = deviceIterator.next();
    		UsbInterface intf = device.getInterface(0);
		Log.d(TAG, "Audio intf.getInterfaceClass() : " + intf.getInterfaceClass());
    		if (intf.getInterfaceClass() == UsbConstants.USB_CLASS_AUDIO) {
    			Log.d(TAG, "Audio class device: " + device);
    			mAudioDevice = device;
			Log.d(TAG, "Audio class device name: " + mAudioDevice.getDeviceName());
			m_connection = mUsbManager.openDevice(device);
			
			if (m_connection != null) {
			    int fd = m_connection.getFileDescriptor();
			    Log.d(TAG, "1jx getFileDescriptor: " + fd);
			}

    		}
        }
    	
        // Load native lib
        System.loadLibrary("usbaudio");
       
    	mUsbAudio = new UsbAudio();
    	
    	AudioPlayBack.setup();
    	
    	// Buttons
	final Button startButton = (Button) findViewById(R.id.button1);
	final Button stopButton = (Button) findViewById(R.id.button2);
	
	startButton.setEnabled(true);
	stopButton.setEnabled(false);
	
	startButton.setOnClickListener(new View.OnClickListener() {
		public void onClick(View v) {
		    Log.d(TAG, "Start pressed");
		    
		    if (mUsbAudio.setup(mAudioDevice.getDeviceName(),
					m_connection.getFileDescriptor(),
					mAudioDevice.getProductId(),
					mAudioDevice.getVendorId()) == true) {
			startButton.setEnabled(false);
			stopButton.setEnabled(true);
		    }
		    
		    new Thread(new Runnable() {
		            public void run() {
		            	while (true) {
				    mUsbAudio.loop();
		            	}
		            }
		        }).start();
		}
	    });
	
	stopButton.setOnClickListener(new View.OnClickListener() {
		public void onClick(View v) {
		    Log.d(TAG, "Stop pressed");
		    AudioPlayBack.stop();
		    mUsbAudio.stop();
		    //mUsbAudio.close();
		    
		    startButton.setEnabled(true);
		    stopButton.setEnabled(false);
		}
	    });
        
        // Register for permission
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        mUsbPermissionReciever = new UsbReciever();
        registerReceiver(mUsbPermissionReciever, filter);
        
        // Request permission from user
        if (mAudioDevice != null && mPermissionIntent != null) {
        	mUsbManager.requestPermission(mAudioDevice, mPermissionIntent);
        } else {
        	Log.e(TAG, "Device not present? Can't request peremission");
        }

    }

    public void onDestroy() {
        super.onDestroy();
	mUsbAudio.stop();
	mUsbAudio.close();
    }
    
    private void setDevice(UsbDevice device) {
    	// Set button to enabled when permission is obtained
    	((Button) findViewById(R.id.button1)).setEnabled(device != null);
    }
    
    private class UsbReciever extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
			if (ACTION_USB_PERMISSION.equals(action)) {
				if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
					setDevice(device);
					m_connection = mUsbManager.openDevice(device);
					
					if (m_connection != null) {
					    int fd = m_connection.getFileDescriptor();
					    Log.d(TAG, "jx getFileDescriptor: " + fd);
					}

				} else {
					Log.d(TAG, "Permission denied for device " + device);
				}
			}
		}
    }

}
