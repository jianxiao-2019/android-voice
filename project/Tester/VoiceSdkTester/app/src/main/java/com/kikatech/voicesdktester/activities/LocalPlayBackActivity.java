package com.kikatech.voicesdktester.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.service.VoiceConfiguration;
import com.kikatech.voice.service.VoiceService;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.util.log.Logger;
import com.kikatech.voice.util.request.RequestManager;
import com.kikatech.voicesdktester.LocalVoiceSource;
import com.kikatech.voicesdktester.R;
import com.kikatech.voicesdktester.ui.FileAdapter;
import com.kikatech.voicesdktester.ui.ResultAdapter;
import com.kikatech.voicesdktester.utils.PreferenceUtil;
import com.kikatech.voicesdktester.utils.WavHeaderHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.kikatech.voicesdktester.activities.MainActivity.PATH_FROM_MIC;

/**
 * Created by ryanlin on 03/01/2018.
 */

public class LocalPlayBackActivity extends AppCompatActivity implements
        VoiceService.VoiceRecognitionListener,
        VoiceService.VoiceStateChangedListener,
        VoiceService.VoiceActiveStateListener,
        LocalVoiceSource.EofListener,
        FileAdapter.OnItemCheckedListener {

    public static final String PATH_FROM_LOCAL = "/sdcard/voiceTester/fromLocal/";

    private TextView mTextView;

    private Button mStartButton;
    private Button mWavButton;

    private VoiceService mVoiceService;
    private AsrConfiguration mAsrConfiguration;
    private String mDebugFileName;

    private LocalVoiceSource mLocalVoiceSource;

    private RecyclerView mResultRecyclerView;
    private ResultAdapter mResultAdapter;

    private RecyclerView mFileRecyclerView;
    private FileAdapter mFileAdapter;

    private Handler mUiHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_playback);

        mLocalVoiceSource = new LocalVoiceSource();
        mLocalVoiceSource.setEofListener(this);
//        mLocalVoiceSource.selectFile(MainActivity.PATH_FROM_MIC + "kika_voice_20171230_204859_USB");

        mTextView = (TextView) findViewById(R.id.status_text);
        mFileRecyclerView = (RecyclerView) findViewById(R.id.files_recycler);
        mFileRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mResultAdapter = new ResultAdapter(this);
        mResultRecyclerView = (RecyclerView) findViewById(R.id.result_recycler);
        mResultRecyclerView.setAdapter(mResultAdapter);
        mResultRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mStartButton = (Button) findViewById(R.id.button_start);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVoiceService != null) {
                    mVoiceService.start();
                } else {
                    if (mTextView != null) {
                        mTextView.setText("Select an audio source first.");
                    }
                }
            }
        });
        mStartButton.setEnabled(false);

        mWavButton = (Button) findViewById(R.id.button_to_wav);
        mWavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addWavHeader();
            }
        });
        mWavButton.setEnabled(false);

        attachService();
        scanFiles();

        mUiHandler = new Handler();

        findViewById(R.id.button_enter_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LocalPlayBackActivity.this, PlayActivity.class);
                startActivity(intent);
            }
        });
    }

    private void scanFiles() {
        File folder = new File(PATH_FROM_MIC);
        if (!folder.exists() || !folder.isDirectory()) {
            return;
        }

        List<String> fileNames = new ArrayList<>();
        for (final File file : folder.listFiles()) {
            if (file.isDirectory()
                    || !file.getName().contains("USB")
                    || file.getName().contains("wav")) {
                continue;
            }
            fileNames.add(file.getName());
        }

        if (mFileAdapter == null) {
            mFileAdapter = new FileAdapter(PATH_FROM_MIC, fileNames);
            mFileAdapter.setOnItemCheckedListener(this);
        } else {
            mFileAdapter.notifyDataSetChanged();
        }
        mFileRecyclerView.setAdapter(mFileAdapter);
    }

    private void attachService() {
        if (mVoiceService != null) {
            mVoiceService.destroy();
            mVoiceService = null;
        }
        // Debug
        mDebugFileName = getDebugFilePath(this);
        AsrConfiguration.Builder builder = new AsrConfiguration.Builder();
        mAsrConfiguration = builder
//                .setSpeechMode(((CheckBox) findViewById(R.id.check_one_shot)).isChecked()
//                        ? AsrConfiguration.SpeechMode.ONE_SHOT
//                        : AsrConfiguration.SpeechMode.CONVERSATION)
//                .setAlterEnabled(((CheckBox) findViewById(R.id.check_alter)).isChecked())
//                .setEmojiEnabled(((CheckBox) findViewById(R.id.check_emoji)).isChecked())
//                .setPunctuationEnabled(((CheckBox) findViewById(R.id.check_punctuation)).isChecked())
//                .setSpellingEnabled(((CheckBox) findViewById(R.id.check_spelling)).isChecked())
//                .setVprEnabled(((CheckBox) findViewById(R.id.check_vpr)).isChecked())
                .setEosPackets(3)
                .build();
        VoiceConfiguration conf = new VoiceConfiguration();
        conf.setDebugFilePath(mDebugFileName);
        conf.source(mLocalVoiceSource);
        conf.setSupportWakeUpMode(false);
        conf.setConnectionConfiguration(new VoiceConfiguration.ConnectionConfiguration.Builder()
                .setAppName("KikaGoTest")
                .setUrl(PreferenceUtil.getString(
                        LocalPlayBackActivity.this,
                        PreferenceUtil.KEY_SERVER_LOCATION,
                        MainActivity.WEB_SOCKET_URL_DEV))
                .setLocale("en_US")
                .setSign(RequestManager.getSign(this))
                .setUserAgent(RequestManager.generateUserAgent(this))
                .setEngine("google")
                .setAsrConfiguration(mAsrConfiguration)
                .build());
        mVoiceService = VoiceService.getService(this, conf);
        mVoiceService.setVoiceRecognitionListener(this);
        mVoiceService.setVoiceStateChangedListener(this);
        mVoiceService.create();
    }

    @UiThread
    private String getDebugFilePath(Context context) {
        if (context == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", new Locale("en"));
        Date resultDate = new Date(System.currentTimeMillis());
        String timeStr = sdf.format(resultDate);

        return getCacheDir(context).toString() + "/kika_voice_" + timeStr;
    }

    @UiThread
    private File getCacheDir(@NonNull Context context) {
        try {
//            File[] files = ContextCompat.getExternalCacheDirs(context);
//            if (files != null && files.length > 0) {
//                File file = files[0];
//                if (file != null) {
            File file = new File(PATH_FROM_LOCAL);
            createFolderIfNecessary(file);
            return file;
//                }
//            }
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
    public void onRecognitionResult(final Message message) {
        if (message instanceof IntermediateMessage) {
            if (mTextView != null) {
                mTextView.setText("Intermediate Result : " + ((IntermediateMessage) message).text);
            }
            return;
        }
        if (mTextView != null) {
            mTextView.setText("Final Result");
        }
        mResultAdapter.addResult(message);
        mResultAdapter.notifyDataSetChanged();
    }

    private boolean mIsCreated = false;

    @Override
    public void onCreated() {
        Logger.d("MainActivity onCreated");
        if (mTextView != null) {
            mTextView.setText("Created.");
        }
        mIsCreated = true;
        mWavButton.setEnabled(false);
    }

    @Override
    public void onStartListening() {
        Logger.d("MainActivity onStartListening");
        mResultAdapter.clearResults();
        mResultAdapter.notifyDataSetChanged();

        if (mTextView != null) {
            mTextView.setText("starting.");
        }
        mStartButton.setEnabled(false);
        mWavButton.setEnabled(false);
    }

    @Override
    public void onStopListening() {
        Logger.d("MainActivity onStopListening");
        if (mTextView != null) {
            mTextView.setText("stopped.");
        }
        mStartButton.setEnabled(true);
        mWavButton.setEnabled(true);
    }

    @Override
    public void onDestroyed() {
        if (mTextView != null) {
            mTextView.setText("Disconnected.");
        }
        mIsCreated = false;
        mWavButton.setEnabled(false);
    }

    @Override
    public void onError(int reason) {

    }

    @Override
    public void onVadBos() {

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

    @Override
    public void onEndOfFile() {
        Logger.d("LocalPlayBackActivity onEndOfFile");

        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mVoiceService.stop();
            }
        }, 500);
    }

    private void addWavHeader() {
        Logger.i("-----addWavHeader mDebugFileName = " + mDebugFileName);
        if (TextUtils.isEmpty(mDebugFileName)) {
            return;
        }
        String fileName = mDebugFileName.substring(mDebugFileName.lastIndexOf("/") + 1);
        Logger.i("-----addWavHeader fileName = " + fileName);
        if (TextUtils.isEmpty(fileName)) {
            return;
        }

        File folder = new File(mDebugFileName.substring(0, mDebugFileName.lastIndexOf("/")));
        if (!folder.exists() || !folder.isDirectory()) {
            return;
        }

        Logger.d("addWavHeader folder = " + folder.getPath());
        boolean isConverted = false;
        for (final File file : folder.listFiles()) {
            if (file.isDirectory() || file.getName().contains("wav")) {
                continue;
            }
            if (file.getName().contains(fileName) && !file.getName().contains("speex")) {
                Logger.d("addWavHeader found file = " + file.getPath());
                WavHeaderHelper.addWavHeader(file, !file.getName().contains("USB"));
                isConverted = true;
            }
        }
        if (isConverted) {
            if (mTextView != null) {
                mTextView.setText("Convert the file: '" + fileName + "' succeed!");
            }
        } else {
            if (mTextView != null) {
                mTextView.setText("Some error occurred!");
            }
        }
    }

    @Override
    public void onItemChecked(String itemStr) {
        if (mLocalVoiceSource != null) {
            mLocalVoiceSource.selectFile(MainActivity.PATH_FROM_MIC + itemStr);
        }
        if (mStartButton != null) {
            mStartButton.setEnabled(mIsCreated);
        }
    }

    @Override
    public void onNothongChecked() {
        if (mStartButton != null) {
            mStartButton.setEnabled(false);
        }
    }
}
