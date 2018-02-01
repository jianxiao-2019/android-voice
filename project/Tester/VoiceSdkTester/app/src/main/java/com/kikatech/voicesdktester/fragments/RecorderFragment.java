package com.kikatech.voicesdktester.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import com.kikatech.voicesdktester.AudioPlayerTask;
import com.kikatech.voicesdktester.R;
import com.xiao.usbaudio.AudioPlayBack;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ryanlin on 23/01/2018.
 */

public class RecorderFragment extends Fragment implements
        View.OnClickListener,
        VoiceService.VoiceRecognitionListener,
        VoiceService.VoiceStateChangedListener,
        VoiceService.VoiceActiveStateListener,
        TtsSource.TtsStateChangedListener  {

    public static final String PATH_FROM_MIC = "/sdcard/voiceTesterUi/fromMic/";
    public static final String WEB_SOCKET_URL_DEV = "ws://speech0-dev.kikakeyboard.com/v3/speech";

    private View mStartRecordView;
    private View mStopRecordView;
    private TextView mRecordingTimerText;
    private View mUsingKikago;
    private ImageView mKikagoSignal;
    private View mUsingAndroid;
    private ImageView mAndroidSignal;
    private TextView mErrorHintText;
    private FrameLayout mRecentArea;

    private VoiceService mVoiceService;
    private AsrConfiguration mAsrConfiguration;
    private UsbAudioSource mUsbAudioSource;
    private UsbAudioService mUsbAudioService;

    private String mDebugFileName;
    private BufferedWriter mBufferedWriter;
    private boolean mIsListening = false;

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

        mErrorHintText = (TextView) view.findViewById(R.id.error_hint);

        mUsingKikago = view.findViewById(R.id.device_button_right);
        mUsingKikago.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUsingAndroid.setSelected(false);
                mUsingKikago.setSelected(true);
                mKikagoSignal.setImageResource(R.drawable.signal_point_empty);
                mAndroidSignal.setImageResource(R.drawable.signal_point_empty);
                mErrorHintText.setVisibility(View.GONE);

                if (mUsbAudioSource != null) {
                    mUsbAudioSource.close();
                }
                mUsbAudioSource = null;
                attachService();
            }
        });
        mKikagoSignal = (ImageView) view.findViewById(R.id.signal_kikago);

        mUsingAndroid = view.findViewById(R.id.device_button_left);
        mUsingAndroid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUsingAndroid.setSelected(true);
                mUsingKikago.setSelected(false);
                mKikagoSignal.setImageResource(R.drawable.signal_point_empty);
                mAndroidSignal.setImageResource(R.drawable.signal_point_empty);
                mErrorHintText.setVisibility(View.GONE);

                mUsbAudioService = UsbAudioService.getInstance(getActivity());
                mUsbAudioService.setListener(mIUsbAudioListener);
                mUsbAudioService.scanDevices();
            }
        });
        mAndroidSignal = (ImageView) view.findViewById(R.id.signal_phone);

        mRecordingTimerText = (TextView) view.findViewById(R.id.recording_timer_text);

        mRecentArea = (FrameLayout) view.findViewById(R.id.recent_area);

        attachService();
        refreshRecentView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mUsbAudioService != null) {
            mUsbAudioService.setListener(null);
        }
        if (mUsbAudioSource != null) {
            mUsbAudioSource.close();
        }
    }

    private void attachService() {
        mKikagoSignal.setImageResource(R.drawable.signal_point_empty);
        mAndroidSignal.setImageResource(R.drawable.signal_point_empty);
        mStartRecordView.setAlpha(0.2f);
        mStartRecordView.setEnabled(false);
        mErrorHintText.setVisibility(View.GONE);

        if (mVoiceService != null) {
            mVoiceService.stop();
            mVoiceService.destroy();
            mVoiceService = null;
        }
        mDebugFileName = getDebugFilePath(getActivity());
        AudioPlayBack.sFilePath = mDebugFileName;       // For debug.
        AsrConfiguration.Builder builder = new AsrConfiguration.Builder();
        mAsrConfiguration = builder
                .setSpeechMode(AsrConfiguration.SpeechMode.CONVERSATION)
                .setAlterEnabled(false)
                .setEmojiEnabled(false)
                .setPunctuationEnabled(false)
                .setSpellingEnabled(false)
                .setVprEnabled(false)
                .setEosPackets(3)
                .build();
        VoiceConfiguration conf = new VoiceConfiguration();
        conf.setDebugFilePath(mDebugFileName);
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
    }

    @UiThread
    private String getDebugFilePath(Context context) {
        if (context == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd_HHmmss", new Locale("en"));
        Date resultDate = new Date(System.currentTimeMillis());
        String timeStr = sdf.format(resultDate);

        return getCacheDir(context).toString() + (mUsbAudioSource == null ? "/Phone_" : "/Kikago_") + timeStr;
    }

    @UiThread
    private File getCacheDir(@NonNull Context context) {
        try {
            File file = new File(PATH_FROM_MIC);
            createFolderIfNecessary(file);
            return file;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return context.getCacheDir();
    }

    @UiThread
    private boolean createFolderIfNecessary(File folder) {
        if (folder != null) {
            if (!folder.exists() || !folder.isDirectory()) {
                return folder.mkdirs();
            }
            return true;
        }
        return false;
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
                attachService();
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
        logResultToFile(message);
    }

    private void logResultToFile(Message message) {
        Logger.d("logResultToFile mBufferedWriter = " + mBufferedWriter);
        if (mBufferedWriter != null) {
            long cid = 0;
            String text = "";
            if (message instanceof TextMessage) {
                text = ((TextMessage) message).text[0];
                cid = ((TextMessage) message).cid;
            } else if (message instanceof EditTextMessage) {
                text = ((EditTextMessage) message).text[0];
                cid = ((EditTextMessage) message).cid;
            } else {
                return;
            }
            Logger.d("logResultToFile cid = " + cid + " text = " + text);
            try {
                mBufferedWriter.write("cid:" + cid);
                mBufferedWriter.newLine();
                mBufferedWriter.write("result:" + text);
                mBufferedWriter.newLine();
                mBufferedWriter.write("-----------------------");
                mBufferedWriter.newLine();
                Logger.d("logResultToFile mIsListening = " + mIsListening);
                if (!mIsListening) {
                    mBufferedWriter.close();
                    mBufferedWriter = null;
                } else {
                    mBufferedWriter.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreated() {
        Log.d("Ryan", "onCreated mUsbAudioSource = " + mUsbAudioSource);
        if (mUsbAudioSource != null) {
            if (mKikagoSignal != null) {
                mKikagoSignal.setImageResource(R.drawable.signal_point_yellow);
                mUsingAndroid.setSelected(true);
                mUsingKikago.setSelected(false);
            }
        } else {
            if (mAndroidSignal != null) {
                mAndroidSignal.setImageResource(R.drawable.signal_point_yellow);
                mUsingAndroid.setSelected(false);
                mUsingKikago.setSelected(true);
            }
        }

        mStartRecordView.setAlpha(1.0f);
        mStartRecordView.setEnabled(true);
    }

    @Override
    public void onStartListening() {
        mStartRecordView.setVisibility(View.GONE);
        mStopRecordView.setVisibility(View.VISIBLE);

        mRecordingTimerText.setText("00:00");
        mTimerHandler.sendEmptyMessageDelayed(0, 1000);

        try {
            if (mBufferedWriter != null) {
                mBufferedWriter.close();
            }
            mBufferedWriter = new BufferedWriter(new FileWriter(mDebugFileName + ".txt", true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mIsListening = true;
    }

    @Override
    public void onStopListening() {
        mStartRecordView.setVisibility(View.VISIBLE);
        mStopRecordView.setVisibility(View.GONE);

        mTimerHandler.removeMessages(0);
        mTimeInSec = 0;
        mIsListening = false;

        refreshRecentView();
    }

    @Override
    public void onDestroyed() {

    }

    @Override
    public void onError(int reason) {
        mKikagoSignal.setImageResource(R.drawable.signal_point_empty);
        mAndroidSignal.setImageResource(R.drawable.signal_point_empty);
        mStartRecordView.setAlpha(0.2f);
        mStartRecordView.setEnabled(false);
        if (mErrorHintText != null) {
            mErrorHintText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onVadBos() {

    }

    @Override
    public void onConnectionClosed() {
        mKikagoSignal.setImageResource(R.drawable.signal_point_empty);
        mAndroidSignal.setImageResource(R.drawable.signal_point_empty);
        mStartRecordView.setAlpha(0.2f);
        mStartRecordView.setEnabled(false);
        if (mErrorHintText != null) {
            mErrorHintText.setVisibility(View.VISIBLE);
        }
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
        }

        @Override
        public void onDeviceDetached() {
            Logger.d("onDeviceDetached.");
        }

        @Override
        public void onDeviceError(int errorCode) {
            if (errorCode == ERROR_NO_DEVICES) {
                Logger.d("onDeviceError ERROR_NO_DEVICES");
                mUsbAudioSource = null;
                attachService();
                Toast.makeText(getContext(), "KikaGo mic isn’t plugged-in.", Toast.LENGTH_SHORT).show();
            } else if (errorCode == ERROR_DRIVER_INIT_FAIL) {
                Logger.d("onDeviceError ERROR_DRIVER_INIT_FAIL");
                if (mErrorHintText != null) {
                    mErrorHintText.setVisibility(View.VISIBLE);
                }
                mKikagoSignal.setImageResource(R.drawable.signal_point_red);
            }
        }
    };

    private long mTimeInSec = 0;
    private Handler mTimerHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            mTimeInSec++;
            if (mRecordingTimerText != null) {
                String result = String.format("%02d:%02d", mTimeInSec / 60, mTimeInSec % 60);
                mRecordingTimerText.setText(result);
                mTimerHandler.sendEmptyMessageDelayed(0, 1000);
            }

        }
    };

    private void refreshRecentView() {
        if (mRecentArea != null) {
            RecognizeItem item = scanLatestFile(PATH_FROM_MIC);
            if (item == null) {
                return;
            }
            mRecentArea.removeAllViews();

            View recentView = LayoutInflater.from(getContext()).inflate(R.layout.item_recorded, mRecentArea, false);

            recentView.findViewById(R.id.expanded_layout).setVisibility(View.GONE);
            ((ImageView) recentView.findViewById(R.id.source_icon)).setImageResource(item.isSourceUsb ?
                     R.drawable.ic_list_usbcable : R.drawable.ic_list_phone);;
            ((TextView) recentView.findViewById(R.id.file_name)).setText(item.fileName);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/");
            long duration = item.file.length() / 2 / 16000;
            ((TextView) recentView.findViewById(R.id.file_time)).setText(sdf.format(item.file.lastModified()) + " | " + String.format("%02d:%02d", duration / 60, duration % 60));
            View controlNc = recentView.findViewById(R.id.control_nc);
            controlNc.setVisibility(item.isSourceUsb ? View.VISIBLE : View.INVISIBLE);
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

    private class RecognizeItem {
        File file;
        String fileName;
        String filePath;
        boolean isSourceUsb;
    }

    private class PlayButtonClickListener implements View.OnClickListener {

        private String mFilePath;
        private AudioPlayerTask mTask;

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
            if (mTask == null || !mTask.isPlaying()) {
                mTask = new AudioPlayerTask(mFilePath, (TextView) v, mDrawablePlay, mDrawableSource);
                mTask.execute();
                ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(
                        0, mDrawableStop, 0, mDrawableSource);
            } else {
                if (mTask.isPlaying()) {
                    mTask.stop();
                    mTask = null;
                }
                ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(
                        0, mDrawablePlay, 0, mDrawableSource);
            }
        }
    }
}
