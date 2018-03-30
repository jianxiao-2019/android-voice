package com.kika.usbasrtester.fragment;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.kika.usbasrtester.R;
import com.kika.usbasrtester.wave.draw.WaveCanvas;
import com.kika.usbasrtester.wave.view.WaveSurfaceView;
import com.kikatech.usb.IUsbAudioListener;
import com.kikatech.usb.UsbAudioService;
import com.kikatech.usb.UsbAudioSource;
import com.kikatech.voice.core.tts.TtsSource;
import com.kikatech.voice.core.webservice.message.EditTextMessage;
import com.kikatech.voice.core.webservice.message.EosMessage;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voice.service.VoiceConfiguration;
import com.kikatech.voice.service.VoiceService;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.util.log.Logger;
import com.kikatech.voice.util.request.RequestManager;
import com.xiao.usbaudio.AudioPlayBack;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ryanlin on 23/01/2018.
 */

public class RecorderFragment extends PageFragment implements
        View.OnClickListener,
        VoiceService.VoiceRecognitionListener,
        VoiceService.VoiceStateChangedListener,
        VoiceService.VoiceActiveStateListener,
        VoiceService.VoiceDataListener,
        UsbAudioSource.SourceDataCallback,
        TtsSource.TtsStateChangedListener {

    private static final String DEBUG_FILE_TAG = "UsbTester";
    public static final String WEB_SOCKET_URL_DEV = "ws://speech0-dev.kikakeyboard.com/v3/speech";

    private View mStartRecordView;
    private View mStopRecordView;
    private TextView mRecordingTimerText;
    private TextView mStatusTextView;

    private TextView mIntermediateView;
    private TextView mResultsView;
    private TextView mSizeText;
    private TextView mVolumeText;
    private TextView mVersionText;
    private final StringBuilder mResultStr = new StringBuilder();

    private VoiceService mVoiceService;
    private AsrConfiguration mAsrConfiguration;
    private UsbAudioSource mUsbAudioSource;
    private UsbAudioService mUsbAudioService;

    private WaveCanvas mLeftWaveCanvas;
    private WaveSurfaceView mLeftWaveSurfaceView;
    private WaveCanvas mRightWaveCanvas;
    private WaveSurfaceView mRightWaveSurfaceView;
    private WaveCanvas mWaveCanvas;
    private WaveSurfaceView mWaveSurfaceView;

    private static final int MSG_TIMER = 0;
    private static final int MSG_CLEAR_READ_SIZE = 1;
    private long mTimeInSec = 0;

    private long mServerEosTime;
    private long mEndOfSpeechTime;
    private float mPreProb;
    private long receiveFirstResultTime = -1;

    private static final String[] VOLUME_TABLE = new String[] {
            "error",
            "-16.5",// level 1
            "-6.5", // level 2
            "0",    // level 3
            "5",    // level 4
            "10",   // level 5
            "15",   // level 6
            "20",   // level 7
            "25",   // level 8
            "30",   // level 9
    };

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recorder, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Message.register("INTERMEDIATE", IntermediateMessage.class);
        Message.register("ALTER", EditTextMessage.class);
        Message.register("ASR", TextMessage.class);
        Message.register("EOS", EosMessage.class);

        mStartRecordView = view.findViewById(R.id.start_record);
        mStartRecordView.setOnClickListener(this);
        mStopRecordView = view.findViewById(R.id.stop_record);
        mStopRecordView.setOnClickListener(this);

        mStatusTextView = view.findViewById(R.id.status_text);
        mSizeText = view.findViewById(R.id.text_size);
        mIntermediateView = view.findViewById(R.id.intermediate_text);
        mResultsView = view.findViewById(R.id.result_text);
        mVolumeText = view.findViewById(R.id.text_volume);
        mVersionText = view.findViewById(R.id.text_version);

        checkVersion();

        view.findViewById(R.id.button_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUsbAudioSource != null) {
                    mUsbAudioSource.volumeUp();

                    checkVolume();
                }
            }
        });

        view.findViewById(R.id.button_down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUsbAudioSource != null) {
                    mUsbAudioSource.volumeDown();

                    checkVolume();
                }
            }
        });

        mRecordingTimerText = view.findViewById(R.id.recording_timer_text);

        mWaveSurfaceView = view.findViewById(R.id.wave_view_nc);
        mLeftWaveSurfaceView = view.findViewById(R.id.wave_view_left);
        mRightWaveSurfaceView = view.findViewById(R.id.wave_view_right);
        waveCreateView();

        setRecordViewEnabled(false);
    }

    private void checkVersion() {
        StringBuilder version = new StringBuilder();
        version.append("[app : ").append(getVersionName(getContext())).append("]\n");
        if (mUsbAudioSource != null) {
            String fwVersion = mUsbAudioSource.checkFwVersion() == 0xFFFF ? "error"
                    : String.valueOf(mUsbAudioSource.checkFwVersion());
            version.append("[fw : ").append(fwVersion).append("]\n");
            version.append("[driver : ").append(mUsbAudioSource.checkDriverVersion()).append("]\n");
            version.append("[nc : ").append(mUsbAudioSource.getNcVersion()).append("]\n");
        }
        mVersionText.setText("version : \n" + version);
    }

    @Override
    public void onStart() {
        super.onStart();

        mUsbAudioService = UsbAudioService.getInstance(getActivity());
        mUsbAudioService.setListener(mIUsbAudioListener);
        mUsbAudioService.scanDevices();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mVoiceService != null) {
            mVoiceService.destroy();
        }

        if (mUsbAudioService != null) {
            mUsbAudioService.setListener(null);
        }
        if (mUsbAudioSource != null) {
            mUsbAudioSource.closeDevice();
            mUsbAudioSource.setSourceDataCallback(null);
        }

        AudioPlayBack.setListener(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setRecordViewEnabled(boolean enabled) {
        if (mStartRecordView != null) {
            mStartRecordView.setAlpha(enabled ? 1.0f : 0.2f);
            mStartRecordView.setEnabled(enabled);
        }
    }

    private void attachService() {
        if (mVoiceService != null) {
            mVoiceService.destroy();
            mVoiceService = null;
        }
        AsrConfiguration.Builder builder = new AsrConfiguration.Builder();
        mAsrConfiguration = builder
                .setAlterEnabled(false)
                .setEmojiEnabled(false)
                .setPunctuationEnabled(false)
                .setSpellingEnabled(false)
                .setVprEnabled(false)
                .setEosPackets(10)
                .build();
        VoiceConfiguration conf = new VoiceConfiguration();
        conf.setIsDebugMode(true);
        conf.setDebugFileTag(DEBUG_FILE_TAG);
        conf.source(mUsbAudioSource);
        conf.setBosDuration(Integer.MAX_VALUE);
        conf.setConnectionConfiguration(new VoiceConfiguration.ConnectionConfiguration.Builder()
                .setAppName("KikaGoTest")
                .setUrl(WEB_SOCKET_URL_DEV)
                .setLocale("en_US")
                .setSign(RequestManager.getSign(getActivity()))
                .setUserAgent(RequestManager.generateUserAgent(getActivity()))
                .setEngine("google")
                .setAsrConfiguration(mAsrConfiguration)
                .build());
        mVoiceService = VoiceService.getService(getActivity(), conf);
        mVoiceService.setVoiceRecognitionListener(this);
        mVoiceService.setVoiceStateChangedListener(this);
        mVoiceService.setVoiceDataListener(this);
        mVoiceService.create();

        AudioPlayBack.setListener(mListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_record:
                if (mVoiceService != null) {
                    mVoiceService.start();
                }
                break;
            case R.id.stop_record:
                if (mVoiceService != null) {
                    mVoiceService.stop();
                }
                break;
        }
    }

    @Override
    public void onTtsStart() {

    }

    @Override
    public void onTtsComplete() {

    }

    @Override
    public void onTtsInterrupted() {

    }

    @Override
    public void onTtsError() {

    }

    @Override
    public void onRecognitionResult(Message message) {
        if (message instanceof TextMessage) {
            mResultStr.insert(0, "\n").insert(0, ((TextMessage) message).text[0]);
            mResultsView.setText(mResultStr.toString());

            long end = System.currentTimeMillis();
            mIntermediateView.setText("Requite " + (end - mEndOfSpeechTime) + "ms");

            Logger.i("5566 First result to final result = " + (end - receiveFirstResultTime) + "ms");
            Logger.w("5566 Server Eos   to final result = " + (end - mServerEosTime) + "ms");
            Logger.e("5566 local  Eos   to final result = " + (end - mEndOfSpeechTime) + "ms");
            receiveFirstResultTime = -1;
        } else if (message instanceof IntermediateMessage) {
            mIntermediateView.setText(((IntermediateMessage) message).text);
            if (receiveFirstResultTime == -1) {
                receiveFirstResultTime = System.currentTimeMillis();
            } else {
                Logger.i("5566 Intermediate result [" + ((IntermediateMessage) message).text + "] duration = " + (System.currentTimeMillis() - receiveFirstResultTime));
            }
        } else if (message instanceof EosMessage) {
            mServerEosTime = System.currentTimeMillis();
            Logger.d("5566 Receive Eos message");
            Logger.i("5566 First result to Eos message = " + (System.currentTimeMillis() - receiveFirstResultTime));
        }
    }

    @Override
    public void onCreated() {

    }

    @Override
    public void onStartListening() {
        if (mStartRecordView != null) {
            mStartRecordView.setVisibility(View.GONE);
        }
        if (mStopRecordView != null) {
            mStopRecordView.setVisibility(View.VISIBLE);
        }

        mRecordingTimerText.setText("00:00");
        mTimerHandler.sendEmptyMessageDelayed(MSG_TIMER, 1000);

        mResultStr.setLength(0);
        mResultsView.setText(mResultStr.toString());
        waveStartDraw();
    }

    @Override
    public void onStopListening() {
        if (mStartRecordView != null) {
            mStartRecordView.setVisibility(View.VISIBLE);
        }
        if (mStopRecordView != null) {
            mStopRecordView.setVisibility(View.GONE);
        }

        mTimerHandler.removeMessages(MSG_TIMER);
        mTimeInSec = 0;
        waveStopDraw();
    }

    @Override
    public void onDestroyed() {

    }

    @Override
    public void onError(int reason) {
        Logger.e("onError reason = " + reason);
        if (mUsbAudioSource != null) {
            mUsbAudioSource.setSourceDataCallback(null);
        }
    }

    @Override
    public void onConnectionClosed() {

    }

    @Override
    public void onSpeechProbabilityChanged(float prob) {
        Logger.d("55667 onSpeechProbabilityChanged prob = " + prob);
        if (mPreProb > 0.8 && prob == 0) {
            mEndOfSpeechTime = System.currentTimeMillis();
        }

        mPreProb = prob;
    }

    @Override
    public void onWakeUp() {

    }

    @Override
    public void onSleep() {

    }

    private IUsbAudioListener mIUsbAudioListener = new IUsbAudioListener() {

        @Override
        public void onDeviceAttached(UsbAudioSource audioSource) {
            Logger.d("onDeviceAttached.");
            mUsbAudioSource = audioSource;
            mUsbAudioSource.setSourceDataCallback(RecorderFragment.this);
            attachService();

            checkVolume();
            checkVersion();

            mStatusTextView.setText("Usb Device Attached.");
            setRecordViewEnabled(true);
        }

        @Override
        public void onDeviceDetached() {
            Logger.d("onDeviceDetached.");
            if (mUsbAudioSource != null) {
                mUsbAudioSource.setSourceDataCallback(null);
            }

            mStatusTextView.setText("Usb Device Detached.");
            setRecordViewEnabled(false);
        }

        @Override
        public void onDeviceError(int errorCode) {
            if (errorCode == ERROR_NO_DEVICES) {
                Logger.d("onDeviceError ERROR_NO_DEVICES");
                mStatusTextView.setText("No usb devices.");
            } else if (errorCode == ERROR_DRIVER_INIT_FAIL) {
                Logger.d("onDeviceError ERROR_DRIVER_INIT_FAIL");
                mStatusTextView.setText("Device init fail.");
            } else if (errorCode == ERROR_DRIVER_MONO) {
                Logger.d("onDeviceError ERROR_DRIVER_INIT_FAIL");
                mStatusTextView.setText("Device is MONO.");
            }

            if (mUsbAudioSource != null) {
                mUsbAudioSource.setSourceDataCallback(null);
            }
            setRecordViewEnabled(false);
        }
    };

    private void checkVolume() {
        if (mVolumeText != null) {
            int volumeLevel = mUsbAudioSource.checkVolumeState();
            String volume = (volumeLevel >= VOLUME_TABLE.length | volumeLevel < 0) ? "error" : VOLUME_TABLE[volumeLevel];
            mVolumeText.setText(String.format(getString(R.string.current_volume), volume));
        }
    }

    private Handler mTimerHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_TIMER) {
                mTimeInSec++;
                if (mRecordingTimerText != null) {
                    String result = String.format("%02d:%02d", mTimeInSec / 60, mTimeInSec % 60);
                    mRecordingTimerText.setText(result);
                    mTimerHandler.sendEmptyMessageDelayed(MSG_TIMER, 1000);
                }
            } else if (msg.what == MSG_CLEAR_READ_SIZE) {
                if (mSizeText != null) {
                    mSizeText.setText("");
                }
            }
        }
    };

    @Override
    public void onPagePause() {
        if (mVoiceService != null) {
            mVoiceService.stop();
        }
    }

    @Override
    public void onPageResume() {
    }

    private AudioPlayBack.OnAudioPlayBackWriteListener mListener = new AudioPlayBack.OnAudioPlayBackWriteListener() {

        @Override
        public void onWrite(final int len) {
            mSizeText.post(new Runnable() {
                @Override
                public void run() {
                    if (mSizeText != null) {
                        mSizeText.setText(String.valueOf(len));
                        mTimerHandler.removeMessages(MSG_CLEAR_READ_SIZE);
                        mTimerHandler.sendEmptyMessageDelayed(MSG_CLEAR_READ_SIZE, 1000);
                    }
                }
            });
        }
    };

    @Override
    public void onData(byte[] data, int readSize) {
//        if (mWaveCanvas != null) {
//            mWaveCanvas.onData(data, readSize);
//        }
    }

    private void waveCreateView() {
        if (mWaveSurfaceView != null) {
            mWaveSurfaceView.setLine_off(42);
            mWaveSurfaceView.setZOrderOnTop(true);
            mWaveSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            mWaveSurfaceView.setVisibility(View.GONE);
        }
        if (mLeftWaveSurfaceView != null) {
            mLeftWaveSurfaceView.setLine_off(42);
            mLeftWaveSurfaceView.setZOrderOnTop(true);
            mLeftWaveSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        }
        if (mRightWaveSurfaceView != null) {
            mRightWaveSurfaceView.setLine_off(42);
            mRightWaveSurfaceView.setZOrderOnTop(true);
            mRightWaveSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        }
    }

    private void waveStartDraw() {
        if (mWaveCanvas == null) {
            mWaveCanvas = new WaveCanvas();
            mWaveCanvas.baseLine = mWaveSurfaceView.getHeight() / 2;
            mWaveCanvas.startDraw(mWaveSurfaceView, new Handler.Callback() {
                @Override
                public boolean handleMessage(android.os.Message msg) {
                    return true;
                }
            });
        }
        if (mLeftWaveCanvas == null) {
            mLeftWaveCanvas = new WaveCanvas();
            mLeftWaveCanvas.baseLine = mLeftWaveSurfaceView.getHeight() / 2;
            mLeftWaveCanvas.startDraw(mLeftWaveSurfaceView, new Handler.Callback() {
                @Override
                public boolean handleMessage(android.os.Message msg) {
                    return true;
                }
            });
        }
        if (mRightWaveCanvas == null) {
            mRightWaveCanvas = new WaveCanvas();
            mRightWaveCanvas.baseLine = mRightWaveSurfaceView.getHeight() / 2;
            mRightWaveCanvas.startDraw(mRightWaveSurfaceView, new Handler.Callback() {
                @Override
                public boolean handleMessage(android.os.Message msg) {
                    return true;
                }
            });
        }
    }

    private void waveStopDraw() {
        if (mWaveCanvas != null) {
            mWaveCanvas.stopDraw();
            mWaveCanvas = null;
        }
        if (mLeftWaveCanvas != null) {
            mLeftWaveCanvas.stopDraw();
            mLeftWaveCanvas = null;
        }
        if (mRightWaveCanvas != null) {
            mRightWaveCanvas.stopDraw();
            mRightWaveCanvas = null;
        }
    }

    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    @Override
    public void onSource(final byte[] leftData, final byte[] rightData) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mLeftWaveCanvas != null) {
                    mLeftWaveCanvas.onData(leftData, leftData.length);
                }
                if (mRightWaveCanvas != null) {
                    mRightWaveCanvas.onData(rightData, rightData.length);
                }
            }
        });
    }

    private String getVersionName(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);

            return pi.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
