package com.kikatech.voicesdktester.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kikatech.usb.IUsbAudioListener;
import com.kikatech.usb.UsbAudioService;
import com.kikatech.usb.UsbAudioSource;
import com.kikatech.usb.nc.KikaNcBuffer;
import com.kikatech.voice.core.tts.TtsService;
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
import com.kikatech.voicesdktester.R;
import com.kikatech.voicesdktester.ui.ResultAdapter;
import com.kikatech.voicesdktester.utils.PreferenceUtil;
import com.kikatech.voicesdktester.utils.WavHeaderHelper;
import com.xiao.usbaudio.AudioPlayBack;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.kikatech.voice.service.VoiceService.ERR_REASON_NOT_CREATED;

public class MainActivity extends AppCompatActivity implements
        VoiceService.VoiceRecognitionListener,
        VoiceService.VoiceStateChangedListener,
        VoiceService.VoiceActiveStateListener,
        TtsSource.TtsStateChangedListener {

    public static final String PATH_FROM_MIC = "/sdcard/voiceTester/fromMic/";

    private static final boolean IS_WAKE_UP_MODE = false;

    public static final String WEB_SOCKET_URL_DEV = "ws://speech0-dev-mvp.kikakeyboard.com/v3/speech";

    private static final Locale[] LOCALE_LIST = new Locale[]{
            new Locale("en", "US"),
            new Locale("zh", "CN"),
    };

    private Button mPermissionButton;

    private Button mStartButton;
    private Button mStopButton;
    private Button mWavButton;
    private Button mCurServerButton;

    private TextView mAudioIdText;
    private TextView mTextView;
    private TextView mStatus2TextView;

    private RecyclerView mResultRecyclerView;
    private ResultAdapter mResultAdapter;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean mPermissionToRecordAccepted = false;
    private String[] mPermissions = {Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private VoiceService mVoiceService;
    private TtsSource mTtsSource;

    private AsrConfiguration mAsrConfiguration;

    private UsbAudioSource mUsbAudioSource;

    private String mDebugFileName;

    private UsbAudioService mUsbAudioService;

    private Spinner mSpinner;

    private SeekBar mSeekAngle;
    private TextView mTextAngle;
    private Button mButtonAngle;
    private SeekBar mSeekNc;
    private TextView mTextNc;
    private Button mButtonNc;
    private SeekBar mSeekMode;
    private TextView mTextMode;
    private Button mButtonMode;

    private View mNcParamLayout;

    private BufferedWriter mBufferedWriter;
    private boolean mIsListening = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_testing);

        mPermissionButton = (Button) findViewById(R.id.button_permission);
        updatePermissionButtonState();
        mPermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.RECORD_AUDIO);
                int storagePermissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (permissionCheck != PackageManager.PERMISSION_GRANTED
                        || storagePermissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            mPermissions, REQUEST_RECORD_AUDIO_PERMISSION);
                } else {
                    showToast("You already have these permission.");
                }
                updatePermissionButtonState();
            }
        });

        mSpinner = (Spinner) findViewById(R.id.spinner);
        final String[] select = {"1", "2", "3", "4", "5"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, select);
        mSpinner.setAdapter(arrayAdapter);
        mSpinner.setSelection(2);

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

        mStopButton = (Button) findViewById(R.id.button_stop);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVoiceService != null) {
                    mVoiceService.stop();
                }
            }
        });
        mStopButton.setEnabled(false);

        mWavButton = (Button) findViewById(R.id.button_to_wav);
        mWavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addWavHeader();
            }
        });
        mWavButton.setEnabled(false);

        if (IS_WAKE_UP_MODE) {
            findViewById(R.id.button_wake).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mVoiceService != null) {
                        mVoiceService.wakeUp();
                    }
                }
            });

            findViewById(R.id.button_sleep).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mVoiceService != null) {
                        mVoiceService.sleep();
                    }
                }
            });
        } else {
            findViewById(R.id.button_wake).setVisibility(View.GONE);
            findViewById(R.id.button_sleep).setVisibility(View.GONE);
        }

        findViewById(R.id.button_tts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mResultRecyclerView == null || mResultRecyclerView.getChildCount() == 0) {
                    mTtsSource.speak("Hi! Nice to meet you.");
                } else {
                    // mTtsSource.speak(((TextView) mResultRecyclerView.getChildAt(0).findViewById(R.id.text_result)).getText().toString());
                    Pair<String, Integer>[] playList = new Pair[3];
                    playList[0] = new Pair<>("The top one in your result list is :", TtsSource.TTS_SPEAKER_1);
                    playList[1] = new Pair<>(((TextView) mResultRecyclerView.getChildAt(0).findViewById(R.id.text_result)).getText().toString(), TtsSource.TTS_SPEAKER_2);
                    playList[2] = new Pair<>("Is that correct?", TtsSource.TTS_SPEAKER_1);
                    mTtsSource.speak(playList);
                }

            }
        });

        findViewById(R.id.button_interrupt_tts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTtsSource.interrupt();
            }
        });

        findViewById(R.id.button_update_asr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVoiceService != null) {
                    mAsrConfiguration.setSpeechMode(((CheckBox) findViewById(R.id.check_one_shot)).isChecked()
                            ? AsrConfiguration.SpeechMode.ONE_SHOT
                            : AsrConfiguration.SpeechMode.CONVERSATION);
                    mAsrConfiguration.setAlterEnabled(
                            ((CheckBox) findViewById(R.id.check_alter)).isChecked());
                    mAsrConfiguration.setEmojiEnabled(
                            ((CheckBox) findViewById(R.id.check_emoji)).isChecked());
                    mAsrConfiguration.setPunctuationEnabled(
                            ((CheckBox) findViewById(R.id.check_punctuation)).isChecked());
                    mAsrConfiguration.setSpellingEnabled(
                            ((CheckBox) findViewById(R.id.check_spelling)).isChecked());
                    mAsrConfiguration.setVprEnabled(
                            ((CheckBox) findViewById(R.id.check_vpr)).isChecked());
                    mAsrConfiguration.setEosPackets(mSpinner.getSelectedItemPosition() + 1);
                    Logger.d("onMessage conf = " + mAsrConfiguration.toJsonString());
                    mVoiceService.updateAsrSettings(mAsrConfiguration);
                }
            }
        });

        mAudioIdText = (TextView) findViewById(R.id.audio_file_id_text);
        mTextView = (TextView) findViewById(R.id.status_text);
        mStatus2TextView = (TextView) findViewById(R.id.status_right_text);
        mResultAdapter = new ResultAdapter(this);
        mResultRecyclerView = (RecyclerView) findViewById(R.id.result_recycler);
        mResultRecyclerView.setAdapter(mResultAdapter);
        mResultRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (mTtsSource == null) {
            mTtsSource = TtsService.getInstance().getSpeaker(TtsService.TtsSourceType.KIKA_WEB);
            mTtsSource.init(this, null);
            mTtsSource.setTtsStateChangedListener(MainActivity.this);
        }

        mCurServerButton = (Button) findViewById(R.id.server_button);
        mCurServerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setSingleChoiceItems(R.array.hosts, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PreferenceUtil.setString(MainActivity.this,
                                        PreferenceUtil.KEY_SERVER_LOCATION,
                                        getResources().getStringArray(R.array.hosts)[which]);
                                dialog.dismiss();

                                updateServerButtonText();
                            }
                        }).show();
            }
        });

        findViewById(R.id.button_source_usb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUsbAudioService = UsbAudioService.getInstance(MainActivity.this);
                mUsbAudioService.setListener(mIUsbAudioListener);
                mUsbAudioService.scanDevices();
            }
        });

        findViewById(R.id.button_source_android).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUsbAudioSource = null;
                attachService();
            }
        });

        findViewById(R.id.button_alignment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVoiceService != null) {
                    mVoiceService.sendAlignment(new String[]{"Yes", "No"});
                }
            }
        });

        findViewById(R.id.button_enter_local_playback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LocalPlayBackActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.button_enter_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlayActivity.class);
                startActivity(intent);
            }
        });

        // TODO : this panel will be hidden when using android.
        mNcParamLayout = findViewById(R.id.nc_parameters_layout);
        mNcParamLayout.setVisibility(View.GONE);

        mSeekAngle = (SeekBar) findViewById(R.id.seek_angle);
        mSeekAngle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mTextAngle != null) {
                    mTextAngle.setText(String.valueOf(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mTextAngle = (TextView) findViewById(R.id.text_angle);
        mButtonAngle = (Button) findViewById(R.id.button_angle);
        mButtonAngle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUsbAudioSource != null) {
                    mUsbAudioSource.setNoiseCancellationParameters(
                            KikaNcBuffer.CONTROL_ANGLE, mSeekAngle.getProgress());
                    if (mTextView != null) {
                        mTextView.setText("Set the Angle to " + mSeekAngle.getProgress());
                    }
                }
            }
        });
        mButtonAngle.setEnabled(false);

        mSeekNc = (SeekBar) findViewById(R.id.seek_nc);
        mSeekNc.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mTextNc != null) {
                    mTextNc.setText(String.valueOf(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mTextNc = (TextView) findViewById(R.id.text_nc);
        mButtonNc = (Button) findViewById(R.id.button_nc);
        mButtonNc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUsbAudioSource != null) {
                    mUsbAudioSource.setNoiseCancellationParameters(
                            KikaNcBuffer.CONTROL_NC, mSeekNc.getProgress());
                }
                if (mTextView != null) {
                    mTextView.setText("Set the NC to " + mSeekNc.getProgress());
                }
            }
        });
        mButtonNc.setEnabled(false);

        mSeekMode = (SeekBar) findViewById(R.id.seek_mode);
        mSeekMode.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mTextMode != null) {
                    mTextMode.setText(String.valueOf(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mTextMode = (TextView) findViewById(R.id.text_mode);
        mButtonMode = (Button) findViewById(R.id.button_mode);
        mButtonMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUsbAudioSource != null) {
                    mUsbAudioSource.setNoiseCancellationParameters(
                            KikaNcBuffer.CONTROL_MODE, mSeekMode.getProgress());
                }
                if (mTextView != null) {
                    mTextView.setText("Set the Mode to " + mSeekMode.getProgress());
                }
            }
        });
        mButtonMode.setEnabled(false);

        Message.register("INTERMEDIATE", IntermediateMessage.class);
        Message.register("ALTER", EditTextMessage.class);
        Message.register("ASR", TextMessage.class);

        ((TextView) findViewById(R.id.text_version)).setText("version : " + getVersionName(this));

        AudioPlayBack.setListener(new AudioPlayBack.OnAudioPlayBackWriteListener() {
            @Override
            public void onWrite(final int len) {
                mStatus2TextView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mStatus2TextView != null) {
                            mStatus2TextView.setText("size : " + len);
                        }
                    }
                });
            }
        });
    }

    private void updatePermissionButtonState() {
        if (mPermissionButton == null) {
            return;
        }
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.RECORD_AUDIO);
        int storagePermissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED
                || storagePermissionCheck != PackageManager.PERMISSION_GRANTED) {
            mPermissionButton.setEnabled(true);
            mPermissionButton.setText("Need permission.");
        } else {
            mPermissionButton.setEnabled(false);
            mPermissionButton.setText("Permission granted");
        }
    }

    public String getVersionName(Context context) {
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

    private void updateServerButtonText() {
        if (mCurServerButton == null) {
            return;
        }

        String server = PreferenceUtil.getString(this, PreferenceUtil.KEY_SERVER_LOCATION, WEB_SOCKET_URL_DEV);
        mCurServerButton.setText("Now is : " + server);

        attachService();
    }

    private void attachService() {
        if (mVoiceService != null) {
            mVoiceService.stop();
            mVoiceService.destroy();
            mVoiceService = null;
        }
        // Debug
        mDebugFileName = getDebugFilePath(this);
        AudioPlayBack.sFilePath = mDebugFileName;       // For debug.
        mAudioIdText.setText("File : " + mDebugFileName.substring(mDebugFileName.lastIndexOf("/") + 1));
        AsrConfiguration.Builder builder = new AsrConfiguration.Builder();
        mAsrConfiguration = builder
                .setSpeechMode(((CheckBox) findViewById(R.id.check_one_shot)).isChecked()
                        ? AsrConfiguration.SpeechMode.ONE_SHOT
                        : AsrConfiguration.SpeechMode.CONVERSATION)
                .setAlterEnabled(((CheckBox) findViewById(R.id.check_alter)).isChecked())
                .setEmojiEnabled(((CheckBox) findViewById(R.id.check_emoji)).isChecked())
                .setPunctuationEnabled(((CheckBox) findViewById(R.id.check_punctuation)).isChecked())
                .setSpellingEnabled(((CheckBox) findViewById(R.id.check_spelling)).isChecked())
                .setVprEnabled(((CheckBox) findViewById(R.id.check_vpr)).isChecked())
                .setEosPackets(mSpinner.getSelectedItemPosition() + 1)
                .build();
        VoiceConfiguration conf = new VoiceConfiguration();
        conf.setDebugFilePath(mDebugFileName);
        conf.source(mUsbAudioSource);
        conf.setSupportWakeUpMode(IS_WAKE_UP_MODE);
        conf.setConnectionConfiguration(new VoiceConfiguration.ConnectionConfiguration.Builder()
                .setAppName("KikaGoTest")
                .setUrl(PreferenceUtil.getString(
                        MainActivity.this,
                        PreferenceUtil.KEY_SERVER_LOCATION,
                        WEB_SOCKET_URL_DEV))
                .setLocale(getCurrentLocale())
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mVoiceService != null) {
            mVoiceService.stop();
            mVoiceService.destroy();
            mVoiceService = null;
        }
        if (mTtsSource != null) {
            mTtsSource.close();
            mTtsSource = null;
        }

        Logger.d("MainActivity onDestroy mUsbAudioService = " + mUsbAudioService);
        if (mUsbAudioSource != null) {
            mUsbAudioSource.close();
        }
        if (mUsbAudioService != null) {
            mUsbAudioService.setListener(null);
        }

        AudioPlayBack.setListener(null);

        Message.unregisterAll();
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
            if (file.isDirectory() || file.getName().contains("wav") || file.getName().contains("txt")) {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                mPermissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!mPermissionToRecordAccepted) finish();
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

    public String getCurrentLocale() {
        int current = PreferenceUtil.getInt(this, PreferenceUtil.KEY_LANGUAGE, 0);
        if (current >= LOCALE_LIST.length) {
            return LOCALE_LIST[0].toString();
        }
        return LOCALE_LIST[current].toString();
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
        Logger.d("MainActivity onCreated");
        if (mTextView != null) {
            String text = mUsbAudioSource == null ? "Using Android source" : "Using Usb source";
            mTextView.setText(text);
            mNcParamLayout.setVisibility(mUsbAudioSource == null ? View.GONE : View.VISIBLE);
        }
        mStartButton.setEnabled(true);
        mStopButton.setEnabled(false);
        mWavButton.setEnabled(false);
        if (mUsbAudioSource != null) {
            mButtonAngle.setEnabled(true);
            mButtonNc.setEnabled(true);
            mButtonMode.setEnabled(true);
        }
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
        mStopButton.setEnabled(true);
        mWavButton.setEnabled(false);

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
        Logger.d("MainActivity onStopListening");
        if (mTextView != null) {
            mTextView.setText("stopped.");
        }
        mStartButton.setEnabled(true);
        mStopButton.setEnabled(false);
        mWavButton.setEnabled(true);

        mIsListening = false;
    }

    @Override
    public void onDestroyed() {
        Logger.d("MainActivity onDestroyed");
        if (mTextView != null) {
            mTextView.setText("Disconnected.");
        }
        mStartButton.setEnabled(false);
        mStopButton.setEnabled(false);
        mWavButton.setEnabled(false);
        mButtonAngle.setEnabled(false);
        mButtonNc.setEnabled(false);
        if (mBufferedWriter != null) {
            try {
                mBufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onError(int reason) {
        if (reason == ERR_REASON_NOT_CREATED) {
            Logger.e("ERR_REASON_NOT_CREATED");
            if (mTextView != null) {
                mTextView.setText("Select an audio source first.");
            }
        }
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
    public void onTtsStart() {
        Logger.d("Tts onTtsStart");
    }

    @Override
    public void onTtsComplete() {
        Logger.d("Tts onTtsComplete");
    }

    @Override
    public void onTtsInterrupted() {
        Logger.d("Tts onTtsInterrupted");
    }

    @Override
    public void onTtsError() {
        Logger.d("Tts onTtsError");
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onWakeUp() {
        Logger.d("Tts onWakeUp");
    }

    @Override
    public void onSleep() {
        Logger.d("Tts onSleep");
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

        }
    };
}
