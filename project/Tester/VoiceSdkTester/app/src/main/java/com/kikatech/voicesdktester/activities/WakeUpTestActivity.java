package com.kikatech.voicesdktester.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kikatech.usb.IUsbAudioListener;
import com.kikatech.usb.KikaBuffer;
import com.kikatech.usb.UsbAudioService;
import com.kikatech.usb.UsbAudioSource;
import com.kikatech.voice.core.debug.DebugUtil;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.service.VoiceConfiguration;
import com.kikatech.voice.service.VoiceService;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.util.log.Logger;
import com.kikatech.voice.util.request.RequestManager;
import com.kikatech.voicesdktester.R;
import com.kikatech.voicesdktester.ui.FileAdapter;
import com.kikatech.voicesdktester.utils.PreferenceUtil;
import com.xiao.usbaudio.AudioPlayBack;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ryanlin on 03/01/2018.
 */

public class WakeUpTestActivity extends AppCompatActivity implements
        VoiceService.VoiceRecognitionListener,
        VoiceService.VoiceStateChangedListener,
        VoiceService.VoiceActiveStateListener,
        FileAdapter.OnItemCheckedListener{

    private static final String DEBUG_FILE_PATH = "voiceTesterWakeUp";

    private TextView mTextView;
    private TextView mResultText;

    private Button mStartButton;

    private VoiceService mVoiceService;
    private AsrConfiguration mAsrConfiguration;

    private UsbAudioSource mUsbAudioSource;
    private UsbAudioService mUsbAudioService;

    private RecyclerView mFileRecyclerView;
    private FileAdapter mFileAdapter;

    private boolean mIsUsingNc = true;

    private Map<String,Boolean> mWakeUpResult = new HashMap<String, Boolean>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wake_up_test);

        mTextView = (TextView) findViewById(R.id.status_text);
        mResultText = (TextView) findViewById(R.id.result_text);

        mStartButton = (Button) findViewById(R.id.button_start);
        mStartButton.setOnClickListener(v -> {
            if (mVoiceService != null) {
                mVoiceService.start();
            }
        });

        findViewById(R.id.button_source_usb).setOnClickListener(v -> {
            mTextView.setText("Preparing...");
            mIsUsingNc = true;

            mUsbAudioService = UsbAudioService.getInstance(WakeUpTestActivity.this);
            mUsbAudioService.setListener(mIUsbAudioListener);
            mUsbAudioService.scanDevices();
        });

        findViewById(R.id.button_source_usb_no_nc).setOnClickListener(v -> {
            mTextView.setText("Preparing...");
            mIsUsingNc = false;

            mUsbAudioService = UsbAudioService.getInstance(WakeUpTestActivity.this);
            mUsbAudioService.setListener(mIUsbAudioListener);
            mUsbAudioService.scanDevices();
        });

        findViewById(R.id.button_source_android).setOnClickListener(v -> {
            mTextView.setText("Preparing...");

            mUsbAudioSource = null;
            attachService();
        });

        mFileRecyclerView = (RecyclerView) findViewById(R.id.files_recycler);
        mFileRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        mUsbAudioSource = null;
        attachService();
        scanFiles();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.i("onStop");
        if (mVoiceService != null) {
            mVoiceService.destroy();
            mVoiceService = null;
        }

        if (mUsbAudioSource != null) {
            mUsbAudioSource.closeDevice();
        }
        if (mUsbAudioService != null) {
            mUsbAudioService.setListener(null);
            mUsbAudioService.setReqPermissionOnReceiver(false);
        }
        AudioPlayBack.setListener(null);
    }

    private void attachService() {
        if (mVoiceService != null) {
            mVoiceService.destroy();
            mVoiceService = null;
        }
        // Debug
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
        conf.setDebugFileTag(DEBUG_FILE_PATH);
        conf.setIsDebugMode(true);
        conf.setSupportWakeUpMode(true);
        conf.source(mUsbAudioSource);
        conf.setConnectionConfiguration(new VoiceConfiguration.ConnectionConfiguration.Builder()
                .setAppName("KikaGoTest")
                .setUrl(PreferenceUtil.getString(
                        WakeUpTestActivity.this,
                        PreferenceUtil.KEY_SERVER_LOCATION,
                        VoiceConfiguration.HostUrl.DEV_MVP))
                .setLocale("en_US")
                .setSign(RequestManager.getSign(this))
                .setUserAgent(RequestManager.generateUserAgent(this))
                .setEngine("google")
                .setAsrConfiguration(mAsrConfiguration)
                .build());
        mVoiceService = VoiceService.getService(this, conf);
        mVoiceService.setVoiceRecognitionListener(this);
        mVoiceService.setVoiceStateChangedListener(this);
        mVoiceService.setVoiceActiveStateListener(this);
        mVoiceService.create();

        mHandler.post(() -> mTextView.setText(mUsbAudioSource == null ? "Using Android" : mIsUsingNc ? "Using KikaGO No NC" : "Using KikaGO with NC"));
    }

    @Override
    public void onRecognitionResult(final Message message) {
    }

    @Override
    public void onCreated() {

    }

    @Override
    public void onStartListening() {
        Logger.d("WakeUpTestActivity onStartListening");
        mTextView.setText("starting.");
        mResultText.setText("");
        mStartButton.setEnabled(false);

        mHandler.sendEmptyMessageDelayed(MSG_WAKE_UP_BOS, 3000);
    }

    @Override
    public void onStopListening() {
        Logger.d("WakeUpTestActivity onStopListening");
        mTextView.setText("stopped.");
        mStartButton.setEnabled(true);
    }

    @Override
    public void onDestroyed() {

    }

    @Override
    public void onError(int reason) {

    }

    @Override
    public void onConnectionClosed() {

    }

    @Override
    public void onSpeechProbabilityChanged(float prob) {

    }

    @Override
    public void onWakeUp() {
        Logger.d("onWakeUp");
        String path = DebugUtil.getDebugFilePath();
        mVoiceService.stop();
        mVoiceService.sleep();
        mTextView.setText("");
        mResultText.setText("SUCCESS!");
        mResultText.setTextColor(Color.GREEN);

        mHandler.removeMessages(MSG_WAKE_UP_BOS);
        if (mUsbAudioSource != null) {
            renameSuccessFile(path, "_USB");
            renameSuccessFile(path, "_COMMAND");
        }
        if (mUsbAudioSource == null) {
            renameSuccessFile(path, "_SRC");
            renameSuccessFile(path, "_COMMAND");
        }
        scanFiles();
    }

    private void renameSuccessFile(final String path, final String pattern) {
        new Thread(() -> {
            Logger.i("renameSuccessFile path = " + path);
            File oldFile = new File(path + pattern);
            Logger.i("renameSuccessFile oldFile = " + oldFile);
            File newFile = new File(path + "_s" + pattern);
            Logger.i("renameSuccessFile newFile = " + newFile);

            if (oldFile.exists() && !newFile.exists()) {
                oldFile.renameTo(newFile);
            }
        }).start();
    }

    @Override
    public void onSleep() {
        Logger.d("onSleep");
    }

    private static final int MSG_WAKE_UP_BOS = 1;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_WAKE_UP_BOS) {
                mVoiceService.stop();
                mTextView.setText("-end-");
                mResultText.setText("Fail!");
                mResultText.setTextColor(Color.RED);
                scanFiles();
            }
        }
    };

    private void scanFiles() {
        String path = DebugUtil.getDebugFolderPath();
        Logger.d("WakeUpTestActivity scanFiles path = " + path);
        if (TextUtils.isEmpty(path)) {
            return;
        }
        File folder = new File(path);
        if (!folder.exists() || !folder.isDirectory()) {
            return;
        }

        List<File> fileNames = new ArrayList<>();
        for (final File file : folder.listFiles()) {
            if (file.isDirectory()
                    || (!file.getName().contains("USB") && !file.getName().contains("COMMAND"))
                    || file.getName().contains("wav")) {
                continue;
            }
            fileNames.add(file);
        }

        if (mFileAdapter == null) {
            mFileAdapter = new FileAdapter(path, fileNames);
            mFileRecyclerView.setAdapter(mFileAdapter);
        } else {
            mFileAdapter.updateContent(fileNames);
            mFileAdapter.notifyDataSetChanged();
        }
    }

    private IUsbAudioListener mIUsbAudioListener = new IUsbAudioListener() {

        @Override
        public void onDeviceAttached(UsbAudioSource audioSource) {
            Logger.d("onDeviceAttached. mIsUsingNc = " + mIsUsingNc);
            mUsbAudioSource = audioSource;
            mUsbAudioSource.setKikaBuffer(mIsUsingNc ? KikaBuffer.TYPE_NOISC_CANCELLATION : KikaBuffer.TYPE_STEREO_TO_MONO);

            attachService();
        }

        @Override
        public void onDeviceDetached() {
            Logger.d("onDeviceDetached.");
            mUsbAudioSource = null;
            attachService();
        }

        @Override
        public void onDeviceError(int errorCode) {
            mUsbAudioSource = null;
            attachService();
        }
    };

    @Override
    public void onItemChecked(String itemStr) {

    }

    @Override
    public void onNothingChecked() {

    }
}
