package com.kika.usbasrtester.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kika.usbasrtester.R;
import com.kikatech.usb.IUsbAudioListener;
import com.kikatech.usb.UsbAudioService;
import com.kikatech.usb.UsbAudioSource;
import com.kikatech.voice.core.tts.TtsSource;
import com.kikatech.voice.core.webservice.message.EditTextMessage;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voice.service.VoiceConfiguration;
import com.kikatech.voice.service.VoiceService;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.util.log.Logger;
import com.kikatech.voice.util.request.RequestManager;
import com.xiao.usbaudio.AudioPlayBack;

/**
 * Created by ryanlin on 23/01/2018.
 */

public class RecorderFragment extends PageFragment implements
        View.OnClickListener,
        VoiceService.VoiceRecognitionListener,
        VoiceService.VoiceStateChangedListener,
        VoiceService.VoiceActiveStateListener,
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
    private final StringBuilder mResultStr = new StringBuilder();

    private VoiceService mVoiceService;
    private AsrConfiguration mAsrConfiguration;
    private UsbAudioSource mUsbAudioSource;
    private UsbAudioService mUsbAudioService;

    private static final int MSG_TIMER = 0;
    private long mTimeInSec = 0;

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

        mStartRecordView = view.findViewById(R.id.start_record);
        mStartRecordView.setOnClickListener(this);
        mStopRecordView = view.findViewById(R.id.stop_record);
        mStopRecordView.setOnClickListener(this);

        mStatusTextView = view.findViewById(R.id.status_text);
        mSizeText = view.findViewById(R.id.text_size);
        mIntermediateView = view.findViewById(R.id.intermediate_text);
        mResultsView = view.findViewById(R.id.result_text);

        mRecordingTimerText = view.findViewById(R.id.recording_timer_text);

        setRecordViewEnabled(false);
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
                .setEosPackets(3)
                .build();
        VoiceConfiguration conf = new VoiceConfiguration();
        conf.setIsDebugMode(true);
        conf.setDebugFileTag(DEBUG_FILE_TAG);
        conf.source(mUsbAudioSource);
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
            mIntermediateView.setText("");

            mResultStr.append("\n").append(((TextMessage) message).text[0]);
            mResultsView.setText(mResultStr.toString());
        } else if (message instanceof IntermediateMessage) {
            mIntermediateView.setText(((IntermediateMessage) message).text);
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

        mIntermediateView.setText("");
    }

    @Override
    public void onDestroyed() {

    }

    @Override
    public void onError(int reason) {
        Logger.e("onError reason = " + reason);
    }

    @Override
    public void onVadBos() {

    }

    @Override
    public void onVadEos() {

    }

    @Override
    public void onConnectionClosed() {

    }

    @Override
    public void onSpeechProbabilityChanged(float prob) {
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
            attachService();

            mStatusTextView.setText("Usb Device Attached.");
            setRecordViewEnabled(true);
        }

        @Override
        public void onDeviceDetached() {
            Logger.d("onDeviceDetached.");

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

            setRecordViewEnabled(false);
        }
    };

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
                    }
                }
            });
        }
    };
}