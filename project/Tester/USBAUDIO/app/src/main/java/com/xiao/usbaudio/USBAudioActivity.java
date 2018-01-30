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

import android.media.AudioManager;

public class USBAudioActivity extends Activity
{

    private static final String TAG = "UsbAudio";
	
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    PendingIntent mPermissionIntent = null;
    UsbManager mUsbManager = null;
    UsbDevice mAudioDevice = null;
    
    UsbAudio mUsbAudio = null;
    private UsbDeviceConnection m_connection = null;

    Button startButton;
    Button stopButton;
    Button closeButton;

    Button checkButton;
    Button upButton;
    Button downButton;

    boolean m_audioDeviceOpened = false;
    private AudioManager myAudioManager;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

	// Grab the USB Device so we can get permission
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        // Load native lib
        System.loadLibrary("usbaudio");
       
	startButton = (Button) findViewById(R.id.button1);
	stopButton = (Button) findViewById(R.id.button2);
	closeButton = (Button) findViewById(R.id.button3);
	checkButton = (Button) findViewById(R.id.button4);
	upButton = (Button) findViewById(R.id.button5);
	downButton = (Button) findViewById(R.id.button6);

	myAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
	AudioPlayBack.setup();
	
	startButton.setEnabled(true);
	stopButton.setEnabled(false);
	
	startButton.setOnClickListener(new View.OnClickListener() {
		public void onClick(View v) {
		    mUsbAudio.start();
		    startButton.setEnabled(false);
		    stopButton.setEnabled(true);
		}
	    });
	
	stopButton.setOnClickListener(new View.OnClickListener() {
		public void onClick(View v) {
		    Log.d(TAG, "Stop pressed");
		    mUsbAudio.stop();
		    startButton.setEnabled(true);
		    stopButton.setEnabled(false);
		}
	    });

	closeButton.setOnClickListener(new View.OnClickListener() {
		public void onClick(View v) {
		    Log.d(TAG, "close pressed");
		    //myAudioManager.setSpeakerphoneOn(true);
		    if(m_audioDeviceOpened) {
		    	mUsbAudio.stop();
		    	mUsbAudio.close();
		    }
		    m_audioDeviceOpened = false;
		}
	    });

	checkButton.setOnClickListener(new View.OnClickListener() {
		public void onClick(View v) {
		    int state = 0;
		    if(m_audioDeviceOpened) {
		    	state = mUsbAudio.checkVolumeState();
			Log.d(TAG, "volume state = "+state);
		    }
		}
	    });

	upButton.setOnClickListener(new View.OnClickListener() {
		public void onClick(View v) {
		    int state = 0;
		    if(m_audioDeviceOpened) {
		    	state = mUsbAudio.volumeUp();
			Log.d(TAG, "volume_up = "+state);
		    }
		}
	    });

	downButton.setOnClickListener(new View.OnClickListener() {
		public void onClick(View v) {
		    int state = 0;
		    if(m_audioDeviceOpened) {
		    	state = mUsbAudio.volumeDown();
			Log.d(TAG, "volume_down = "+state);
		    }
		}
	    });

        // Register for permission
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);

    }

    public void onResume(){
	super.onResume();
    	mUsbAudio = new UsbAudio();    	
	tryGetUsbPermission();
    }
    
    public void onPause() {
	if(m_audioDeviceOpened) {
	    mUsbAudio.stop();
	    mUsbAudio.close();
	}
	m_audioDeviceOpened = false;
	super.onPause();
    }
    
    public void onDestroy() {
	unregisterReceiver(mUsbReceiver);
	AudioPlayBack.stop();
        super.onDestroy();
    }
    
    private void tryGetUsbPermission(){
	mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

	for (final UsbDevice usbDevice : mUsbManager.getDeviceList().values()) {
	    if(mUsbManager.hasPermission(usbDevice)){
		afterGetUsbPermission(usbDevice);
	    }else{
		mUsbManager.requestPermission(usbDevice, mPermissionIntent);
	    }
	}
    }


    private void afterGetUsbPermission(UsbDevice usbDevice){
	//call method to set up device communication
	HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while(deviceIterator.hasNext()){
            UsbDevice device = deviceIterator.next();
    		UsbInterface intf = device.getInterface(0);
    		if (intf.getInterfaceClass() == UsbConstants.USB_CLASS_AUDIO) {
    			mAudioDevice = device;
			m_connection = mUsbManager.openDevice(device);
			
			if (m_connection != null) {
			    int fd = m_connection.getFileDescriptor();
			}

    		}
        }

	
	if ( !m_audioDeviceOpened &&
	    mUsbAudio.setup(mAudioDevice.getDeviceName(),
			    m_connection.getFileDescriptor(),
			    mAudioDevice.getProductId(),
			    mAudioDevice.getVendorId()) == true) {

	    m_audioDeviceOpened = true;
	    
	    new Thread(new Runnable() {
		public void run() {
		    mUsbAudio.loop();
		}
	    }).start();

	}  
	
	
    }


    
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            try
            {
                String action = intent.getAction();
                if (ACTION_USB_PERMISSION.equals(action))
                {
                    synchronized (this)
                    {
                        if (m_audioDeviceOpened)
                        {
                            Log.d(TAG, "USB audio device already opened!");
                            return;
                        }
                        
                        UsbDevice device = (UsbDevice) intent.getParcelableExtra("device"); //UsbManager.EXTRA_DEVICE);
                    
                        if (intent.getBooleanExtra("permission" /*UsbManager.EXTRA_PERMISSION_GRANTED*/, false))
                        {
                            if (device != null)
                            {
                                mAudioDevice = device;
                                
                                //tryGetUsbPermission();
                            }
                        } 
                        else
                        {
                            Log.d(TAG, "permission denied for device " + device);
                        }
                    }
                }
                else if (action.equals("android.hardware.usb.action.USB_DEVICE_DETACHED")) // UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action))
                {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra("device"); //UsbManager.EXTRA_DEVICE);
                    
                    int detachedDeviceVendorId = device.getVendorId();
                    int attachedDeviceVendorId = mAudioDevice.getVendorId();
                    
                    int detachedProductId = device.getProductId();
                    int attachedProductId = mAudioDevice.getProductId();
                    
                    if (device != null &&
                        detachedDeviceVendorId == attachedDeviceVendorId &&
                        detachedProductId == attachedProductId)
                    {
                        if (m_connection != null)
                        {
                            m_connection.close();
                            m_connection = null;
                        }
                        
                        mAudioDevice = null;

			if(m_audioDeviceOpened) {
			    mUsbAudio.stop();
			    mUsbAudio.close();
			}
                        m_audioDeviceOpened = false;
		    }
                }
                else if (action.equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) // UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action))
                {
                    Object device = (Object) intent.getParcelableExtra("device"); //UsbManager.EXTRA_DEVICE);
                    
                    if (device != null)
                    {
                        Log.d(TAG, "Attached in USBDeviceManager received!");
			tryGetUsbPermission();
                    }
                }
            }
            catch (Exception e)
            {
            }
        }
    };

}
