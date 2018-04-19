package com.kikatech.voicesdktester.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kikatech.usb.IUsbAudioListener;
import com.kikatech.usb.UsbAudioService;
import com.kikatech.usb.UsbAudioSource;
import com.kikatech.voice.core.debug.DebugUtil;
import com.kikatech.voice.core.tts.TtsSource;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.service.conf.VoiceConfiguration;
import com.kikatech.voice.service.voice.VoiceService;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.util.log.Logger;
import com.kikatech.voice.util.request.RequestManager;
import com.kikatech.voicesdktester.AudioPlayerTask;
import com.kikatech.voicesdktester.R;
import com.kikatech.voicesdktester.utils.PreferenceUtil;

import java.io.File;
import java.text.SimpleDateFormat;

import static com.kikatech.voicesdktester.utils.PreferenceUtil.KEY_ENABLE_DEBUG_APP;

/**
 * Created by ryanlin on 23/01/2018.
 */

public class RecorderFragment extends PageFragment implements
        View.OnClickListener,
        VoiceService.VoiceRecognitionListener,
        VoiceService.VoiceWakeUpListener,
        TtsSource.TtsStateChangedListener {

    private static final String DEBUG_FILE_TAG = "voiceTesterUi";

    private View mStartRecordView;
    private View mStopRecordView;
    private TextView mRecordingTimerText;
    private View mUsingAndroid;
    private ImageView mKikagoSignal;
    private View mUsingKikaGo;
    private ImageView mAndroidSignal;
    private TextView mErrorHintText;
    private FrameLayout mRecentArea;

    private VoiceService mVoiceService;
    private AsrConfiguration mAsrConfiguration;
    private UsbAudioSource mUsbAudioSource;
    private UsbAudioService mUsbAudioService;

    private static final int MSG_TIMER = 0;
    private static final int MSG_CHECK_DEBUG = 1;
    private static final int MSG_VAD_TIMER = 2;
    private long mTimeInSec = 0;

    private int mDebugCount = 0;

    private TextView mPrevPlayingView = null;
    private AudioPlayerTask mTask;

//    private boolean mStartSpeech = false;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recorder, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mStartRecordView = view.findViewById(R.id.start_record);
        mStartRecordView.setOnClickListener(this);
        mStopRecordView = view.findViewById(R.id.stop_record);
        mStopRecordView.setOnClickListener(this);

        mErrorHintText = (TextView) view.findViewById(R.id.error_hint);

        mUsingAndroid = view.findViewById(R.id.device_button_right);
        mUsingAndroid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mUsingKikaGo.setSelected(false);
                mUsingAndroid.setSelected(true);

                mUsbAudioSource = null;
                attachService();
            }
        });
        mKikagoSignal = (ImageView) view.findViewById(R.id.signal_kikago);

        mUsingKikaGo = view.findViewById(R.id.device_button_left);
        mUsingKikaGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUsbAudioSource == null) {
                    mUsingKikaGo.setSelected(true);
                    mUsingAndroid.setSelected(false);
                    setViewToDisableState();
                    if (mErrorHintText != null) {
                        mErrorHintText.setVisibility(View.GONE);
                    }

                    if (mUsbAudioService != null) {
                        mUsbAudioService.scanDevices();
                    }
                } else {
                    Toast.makeText(getContext(), "You'er already using kikiGO device.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mAndroidSignal = (ImageView) view.findViewById(R.id.signal_phone);

        mRecordingTimerText = (TextView) view.findViewById(R.id.recording_timer_text);

        mRecentArea = (FrameLayout) view.findViewById(R.id.recent_area);

        view.findViewById(R.id.enter_debug_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.d("onClick mDebugCount = " + mDebugCount);
                Boolean isDebug = PreferenceUtil.getBoolean(getContext(), KEY_ENABLE_DEBUG_APP, false);
                if (isDebug) {
                    if (++mDebugCount >= 3) {
                        Toast.makeText(getContext(), "You'er already in the developer mode", Toast.LENGTH_SHORT).show();
                        mTimerHandler.removeMessages(MSG_CHECK_DEBUG);
                        mDebugCount = 0;
                    } else  {
                        mTimerHandler.removeMessages(MSG_CHECK_DEBUG);
                        mTimerHandler.sendEmptyMessageDelayed(MSG_CHECK_DEBUG, 1000);
                    }
                } else {
                    if (++mDebugCount >= 10) {
                        PreferenceUtil.setBoolean(getContext(), KEY_ENABLE_DEBUG_APP, true);
                        Toast.makeText(getContext(), "You'er entering the developer mode", Toast.LENGTH_SHORT).show();
                        mTimerHandler.removeMessages(MSG_CHECK_DEBUG);
                        mDebugCount = 0;
                    } else {
                        mTimerHandler.removeMessages(MSG_CHECK_DEBUG);
                        mTimerHandler.sendEmptyMessageDelayed(MSG_CHECK_DEBUG, 1000);
                    }
                }
            }
        });

        attachService();
        refreshRecentView();
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
            mUsbAudioService.closeDevice();
            mUsbAudioService.setListener(null);
            mUsbAudioService.setReqPermissionOnReceiver(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void attachService() {
        if (mErrorHintText != null) {
            mErrorHintText.setVisibility(View.GONE);
        }

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
                .setEosPackets(9)
                .build();
        VoiceConfiguration conf = new VoiceConfiguration();
        conf.setIsDebugMode(true);
        conf.setDebugFileTag(DEBUG_FILE_TAG);
        conf.source(mUsbAudioSource);
        conf.setConnectionConfiguration(new VoiceConfiguration.ConnectionConfiguration.Builder()
                .setAppName("KikaGoTest")
                .setUrl(VoiceConfiguration.HostUrl.DEV_KIKAGO)
                .setLocale("en_US")
                .setSign(RequestManager.getSign(getActivity()))
                .setUserAgent(RequestManager.generateUserAgent(getActivity()))
                .setEngine("google")
                .setAsrConfiguration(mAsrConfiguration)
                .build());
        mVoiceService = VoiceService.getService(getActivity(), conf);
        mVoiceService.setVoiceRecognitionListener(this);
        mVoiceService.create();
    }

    private void setViewToDisableState() {
        if (mKikagoSignal != null) {
            mKikagoSignal.setImageResource(R.drawable.signal_point_empty);
        }
        if (mAndroidSignal != null) {
            mAndroidSignal.setImageResource(R.drawable.signal_point_empty);
        }
        if (mStartRecordView != null) {
            mStartRecordView.setAlpha(0.2f);
            mStartRecordView.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_record:
                if (mVoiceService != null) {
                    mVoiceService.start();

                    if (mStartRecordView != null) {
                        mStartRecordView.setVisibility(View.GONE);
                    }
                    if (mStopRecordView != null) {
                        mStopRecordView.setVisibility(View.VISIBLE);
                    }

                    mRecordingTimerText.setText("00:00");
                    mTimerHandler.sendEmptyMessageDelayed(MSG_TIMER, 1000);
                }
                break;
            case R.id.stop_record:
                if (mVoiceService != null) {
                    mVoiceService.stop(VoiceService.StopType.NORMAL);
                }
                if (mStartRecordView != null) {
                    mStartRecordView.setVisibility(View.VISIBLE);
                }
                if (mStopRecordView != null) {
                    mStopRecordView.setVisibility(View.GONE);
                }

                mTimerHandler.removeMessages(MSG_TIMER);
                mTimeInSec = 0;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshRecentView();
                    }
                }, 1000);
                break;
            case R.id.device_button_left:
                break;
            case R.id.device_button_right:
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
    }

    @Override
    public void onError(int reason) {
        Logger.e("onError reason = " + reason);
        // setViewToDisableState();
        if (mErrorHintText != null) {
            mErrorHintText.setVisibility(View.VISIBLE);
        }
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

            if (mKikagoSignal != null) {
                mKikagoSignal.setImageResource(R.drawable.signal_point_yellow);
                mUsingKikaGo.setSelected(true);
                mUsingAndroid.setSelected(false);
            }
        }

        @Override
        public void onDeviceDetached() {
            Logger.d("onDeviceDetached.");
            mUsbAudioSource = null;
            attachService();

            if (mAndroidSignal != null) {
                mAndroidSignal.setImageResource(R.drawable.signal_point_yellow);
                mUsingKikaGo.setSelected(false);
                mUsingAndroid.setSelected(true);
            }
        }

        @Override
        public void onDeviceError(int errorCode) {
            if (errorCode == ERROR_NO_DEVICES) {
                Logger.d("onDeviceError ERROR_NO_DEVICES");
                mUsbAudioSource = null;
                attachService();
                Toast.makeText(getContext(), "KikaGo mic isn’t plugged-in.", Toast.LENGTH_SHORT).show();

                if (mAndroidSignal != null) {
                    mAndroidSignal.setImageResource(R.drawable.signal_point_yellow);
                    mUsingKikaGo.setSelected(false);
                    mUsingAndroid.setSelected(true);
                }
            } else if (errorCode == ERROR_DRIVER_CONNECTION_FAIL) {
                Logger.d("onDeviceError ERROR_DRIVER_INIT_FAIL");
                if (mErrorHintText != null) {
                    mErrorHintText.setVisibility(View.VISIBLE);
                }
                if (mKikagoSignal != null) {
                    mKikagoSignal.setImageResource(R.drawable.signal_point_red);
                }
            }
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
            } else if (msg.what == MSG_CHECK_DEBUG) {
                mDebugCount = 0;
//            } else if (msg.what == MSG_VAD_TIMER) {
//                if (mVoiceService != null) {
//                    mVoiceService.stop();
            }
        }
    };

    private void refreshRecentView() {
        if (mRecentArea != null) {
            Activity activity = getActivity();
            if (activity == null || activity.isDestroyed()) {
                return;
            }
            RecognizeItem item = scanLatestFile(DebugUtil.getDebugFolderPath());
            if (item == null) {
                return;
            }
            mRecentArea.removeAllViews();

            View recentView = LayoutInflater.from(activity).inflate(R.layout.item_recorded, mRecentArea, false);

            recentView.findViewById(R.id.expanded_layout).setVisibility(View.GONE);
            ((ImageView) recentView.findViewById(R.id.source_icon)).setImageResource(item.isSourceUsb ?
                    R.drawable.ic_list_usbcable : R.drawable.ic_list_phone);
            ((TextView) recentView.findViewById(R.id.file_name)).setText(item.fileName);

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd hh:mm");
            long duration = item.file.length() / 2 / 16000;
            ((TextView) recentView.findViewById(R.id.file_time)).setText(sdf.format(item.file.lastModified()) + " | " + String.format("%02d:%02d", duration / 60, duration % 60));
            View controlNc = recentView.findViewById(R.id.control_nc);
            controlNc.getLayoutParams().width = item.isSourceUsb ? ViewGroup.LayoutParams.WRAP_CONTENT : 0;
            controlNc.setOnClickListener(new PlayButtonClickListener(
                    item.filePath + "_NC",
                    R.drawable.ic_source_nc_play,
                    R.drawable.ic_source_nc_pause,
                    R.drawable.ic_source_tag_nc));
            recentView.findViewById(R.id.control_raw).setOnClickListener(new PlayButtonClickListener(
                    item.filePath + (item.isSourceUsb ? "_USB" : "_SRC"),
                    R.drawable.ic_source_raw_play,
                    R.drawable.ic_source_raw_pause,
                    R.drawable.ic_source_tag_raw));

            mRecentArea.addView(recentView);
        }
    }

    private RecognizeItem scanLatestFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        File folder = new File(path);
        if (!folder.exists() || !folder.isDirectory() || folder.listFiles() == null) {
            return null;
        }

        File latestFile = null;
        RecognizeItem latestItem = null;
        for (final File file : folder.listFiles()) {
            if (file.isDirectory()) {
                continue;
            }

            if (file.getName().contains("_NC") || file.getName().contains("_SRC")) {
                if (latestFile == null || file.lastModified() > latestFile.lastModified()) {
                    String simpleFileName = file.getPath().substring(0, file.getPath().lastIndexOf("_"));
                    latestFile = file;
                    latestItem = new RecognizeItem();
                    latestItem.file = file;
                    latestItem.filePath = simpleFileName;
                    latestItem.fileName = simpleFileName.substring(simpleFileName.lastIndexOf("/") + 1);
                    latestItem.isSourceUsb = file.getName().contains("_NC");
                }
            }
        }

        return latestItem;
    }

    @Override
    public void onPagePause() {
        if (mTask != null && mTask.isPlaying()) {
            mTask.stop();
        }
        if (mVoiceService != null) {
            mVoiceService.stop(VoiceService.StopType.NORMAL);
        }
    }

    @Override
    public void onPageResume() {
        refreshRecentView();
    }

    private class RecognizeItem {
        File file;
        String fileName;
        String filePath;
        boolean isSourceUsb;
    }

    private class PlayButtonClickListener implements View.OnClickListener {

        private String mFilePath;

        private int mDrawablePlay;
        private int mDrawableStop;
        private int mDrawableSource;

        public PlayButtonClickListener(String filePath,
                                       int drawablePlay, int drawableStop, int drawableSource) {
            mFilePath = filePath;

            mDrawablePlay = drawablePlay;
            mDrawableStop = drawableStop;
            mDrawableSource = drawableSource;
        }

        @Override
        public void onClick(View v) {
            if (!(v instanceof TextView)) {
                return;
            }
            TextView currentView = (TextView) v;
            if (v == mPrevPlayingView) {
                if (mTask == null || !mTask.isPlaying()) {
                    mTask = new AudioPlayerTask(mFilePath, currentView, mDrawablePlay, mDrawableSource);
                    mTask.execute();
                    currentView.setCompoundDrawablesWithIntrinsicBounds(
                            0, mDrawableStop, 0, mDrawableSource);
                } else {
                    if (mTask.isPlaying()) {
                        mTask.stop();
                        mTask = null;
                    }
                    currentView.setCompoundDrawablesWithIntrinsicBounds(
                            0, mDrawablePlay, 0, mDrawableSource);
                }
            } else {
                if (mTask != null && mTask.isPlaying()) {
                    mTask.stop();
//                    mPrevPlayingView.setCompoundDrawablesWithIntrinsicBounds(
//                            0, mDrawablePlay, 0, mDrawableSource);
                    mTask = new AudioPlayerTask(mFilePath, (TextView) v, mDrawablePlay, mDrawableSource);
                    mTask.execute();
                    currentView.setCompoundDrawablesWithIntrinsicBounds(
                            0, mDrawableStop, 0, mDrawableSource);
                } else {
                    mTask = new AudioPlayerTask(mFilePath, (TextView) v, mDrawablePlay, mDrawableSource);
                    mTask.execute();
                    currentView.setCompoundDrawablesWithIntrinsicBounds(
                            0, mDrawableStop, 0, mDrawableSource);
                }

                mPrevPlayingView = (TextView) v;
            }
        }
    }
}
