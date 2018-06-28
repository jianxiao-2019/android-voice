package com.kikatech.voicesdktester.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kikatech.usb.IUsbAudioListener;
import com.kikatech.usb.UsbAudioService;
import com.kikatech.usb.datasource.KikaGoVoiceSource;
import com.kikatech.usb.nc.KikaNcBuffer;
import com.kikatech.voice.core.debug.DebugUtil;
import com.kikatech.voice.core.tts.TtsService;
import com.kikatech.voice.core.tts.TtsSource;
import com.kikatech.voice.core.webservice.command.SocketCommand;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.service.conf.VoiceConfiguration;
import com.kikatech.voice.service.voice.VoiceService;
import com.kikatech.voice.util.log.Logger;
import com.kikatech.voice.util.request.RequestManager;
import com.kikatech.voice.wakeup.SnowBoyDetector;
import com.kikatech.voicesdktester.R;
import com.kikatech.voicesdktester.ui.ResultAdapter;
import com.kikatech.voicesdktester.utils.PreferenceUtil;
import com.kikatech.voicesdktester.utils.VoiceConfig;
import com.kikatech.voicesdktester.wave.draw.WaveCanvas;
import com.kikatech.voicesdktester.wave.view.WaveSurfaceView;
import com.xiao.usbaudio.AudioPlayBack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.kikatech.voice.service.voice.VoiceService.ERR_CONNECTION_ERROR;
import static com.kikatech.voice.service.voice.VoiceService.ERR_NO_SPEECH;
import static com.kikatech.voice.service.voice.VoiceService.ERR_REASON_NOT_CREATED;
import static com.kikatech.voice.service.voice.VoiceService.ERR_RECORD_DATA_FAIL;
import static com.kikatech.voice.service.voice.VoiceService.ERR_RECORD_OPEN_FAIL;

public class MainActivity extends AppCompatActivity implements
        VoiceService.VoiceRecognitionListener,
        VoiceService.VoiceWakeUpListener,
        VoiceService.VoiceDataListener,
        TtsSource.TtsStateChangedListener {

    private static final boolean IS_WAKE_UP_MODE = false;
    private static final String DEBUG_FILE_TAG = "voiceTester";

    private static final Locale[] LOCALE_LIST = new Locale[]{
            new Locale("en", "US"),
            new Locale("zh", "CN"),
    };

    private static final String[] mServerList = {
            VoiceConfiguration.HostUrl.HAO_DEV,
            VoiceConfiguration.HostUrl.API_DEV,
            VoiceConfiguration.HostUrl.API_SQ,
            VoiceConfiguration.HostUrl.API_PRODUCTION,
            VoiceConfiguration.HostUrl.KIKAGO_SQ,
            VoiceConfiguration.HostUrl.KIKAGO_PRODUCTION,
    };

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

    private Button mPermissionButton;

    private Button mStartButton;
    private Button mStopButton;
    private Button mReportButton;
    private Button mCurServerButton;

    private TextView mAudioIdText;
    private TextView mTextView;
    private TextView mStatus2TextView;
    private TextView mVadTextView;
    private TextView mDbTextView;
    private TextView mDbTitleView;

    private RecyclerView mResultRecyclerView;
    private ResultAdapter mResultAdapter;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean mPermissionToRecordAccepted = false;
    private String[] mPermissions = {Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private VoiceService mVoiceService;
    private TtsSource mTtsSource;

    private AsrConfiguration mAsrConfiguration;

    private KikaGoVoiceSource mKikaGoVoiceSource;

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
    private TextView mVolumeText;
    private EditText mNoteText;

    private View mNcParamLayout;

    private WaveCanvas mWaveCanvas;
    private WaveSurfaceView mWavesfv;

    private VoiceConfiguration.SpeechMode mSpeechMode = VoiceConfiguration.SpeechMode.CONVERSATION;

    private KikaGoVoiceSource.SourceDataCallback mSourceDataCallback = new KikaGoVoiceSource.SourceDataCallback() {
        @Override
        public void onSource(byte[] leftData, byte[] rightData) {

        }

        @Override
        public void onCurrentDB(int curDB) {
            runOnUiThread(() -> {
                if (mDbTextView != null) {
                    mDbTextView.setText(String.valueOf(curDB));
                }
            });
        }
    };

    private KikaGoVoiceSource.OnOpenedCallback mOnOpenedCallback = state -> runOnUiThread(() -> {
        checkVersions();
        checkVolume();
        checkNsParameters();
    });

    private void checkNsParameters() {
        if (mKikaGoVoiceSource != null) {
            if (mSeekAngle != null) {
                mSeekAngle.setProgress(mKikaGoVoiceSource.getNoiseSuppressionParameters(0));
            }
            if (mSeekNc != null) {
                mSeekNc.setProgress(mKikaGoVoiceSource.getNoiseSuppressionParameters(1));
            }
            if (mSeekMode != null) {
                mSeekMode.setProgress(mKikaGoVoiceSource.getNoiseSuppressionParameters(2));
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_testing);

        Logger.i("onCreate");
        mPermissionButton = (Button) findViewById(R.id.button_permission);
        mPermissionButton.setOnClickListener(v -> {
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
        });

        mSpinner = (Spinner) findViewById(R.id.spinner);
        final String[] select = {"3", "6", "9", "12", "15", "18", "21", "24", "27", "30", "33", "36", "39", "42"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, select);
        mSpinner.setAdapter(arrayAdapter);
        mSpinner.setSelection(2);

        mStartButton = (Button) findViewById(R.id.button_start);
        mStartButton.setOnClickListener(v -> {
            if (mVoiceService != null) {
                mVoiceService.start();

                Logger.d("MainActivity onStartListening");
                waveStartDraw();
                mResultAdapter.clearResults();
                mResultAdapter.notifyDataSetChanged();

                writeTimeToFile();
                writeNoteToFile();
                writeVersionsToFile();
                writeVolumeToFile();

                if (mTextView != null) {
                    mTextView.setText("starting.");
                }
                mStartButton.setEnabled(false);
                mStopButton.setEnabled(true);
                mReportButton.setEnabled(false);
            } else {
                if (mTextView != null) {
                    mTextView.setText("Select an audio source first.");
                }
            }
        });
        mStartButton.setEnabled(false);

        mStopButton = (Button) findViewById(R.id.button_stop);
        mStopButton.setOnClickListener(v -> {
            if (mVadTextView != null) {
                mVadTextView.setText("0.0");
            }
            if (mNoteText!= null) {
                String note = mNoteText.getText().toString();
                String originalHint = getResources().getString(R.string.note_hint);
                mNoteText.setHint((note!=null && note.length()>0)? originalHint + note : originalHint);
                mNoteText.setText("", TextView.BufferType.EDITABLE);
            }
            if (mVoiceService != null) {
                writeTimeToFile();
                mVoiceService.stop(VoiceService.StopType.NORMAL);

                Logger.d("MainActivity onStopListening");
                waveStopDraw();
                if (mTextView != null) {
                    mTextView.setText("stopped.");
                }
                mStartButton.setEnabled(true);
                mStopButton.setEnabled(false);
                mReportButton.setEnabled(true);
            }
        });
        mStopButton.setEnabled(false);

        if (IS_WAKE_UP_MODE) {
            findViewById(R.id.button_wake).setOnClickListener(v -> {
                if (mVoiceService != null) {
                    mVoiceService.wakeUp();
                }
            });

            findViewById(R.id.button_sleep).setOnClickListener(v -> {
                if (mVoiceService != null) {
                    mVoiceService.sleep();
                }
            });
        } else {
            findViewById(R.id.button_wake).setVisibility(View.GONE);
            findViewById(R.id.button_sleep).setVisibility(View.GONE);
        }

        findViewById(R.id.button_tts).setOnClickListener(v -> {
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

        });

        findViewById(R.id.button_interrupt_tts).setOnClickListener(v -> mTtsSource.interrupt());

        findViewById(R.id.button_update_asr).setOnClickListener(v -> {
            if (mVoiceService != null) {
                mAsrConfiguration.setEmojiEnabled(
                        ((CheckBox) findViewById(R.id.check_emoji)).isChecked());
                mAsrConfiguration.setPunctuationEnabled(
                        ((CheckBox) findViewById(R.id.check_punctuation)).isChecked());
                mAsrConfiguration.setSpellingEnabled(
                        ((CheckBox) findViewById(R.id.check_spelling)).isChecked());
                mAsrConfiguration.setVprEnabled(
                        ((CheckBox) findViewById(R.id.check_vpr)).isChecked());
                mAsrConfiguration.setEosPackets((mSpinner.getSelectedItemPosition() + 1) * 3);
                Logger.d("onMessage conf = " + mAsrConfiguration.toJsonString());
                mVoiceService.updateAsrSettings(mAsrConfiguration);
            }
        });

        mAudioIdText = (TextView) findViewById(R.id.audio_file_id_text);
        mTextView = (TextView) findViewById(R.id.status_text);
        mStatus2TextView = (TextView) findViewById(R.id.status_right_text);
        mVadTextView = (TextView) findViewById(R.id.vad_text);
        mDbTitleView = (TextView) findViewById(R.id.db_title);
        mDbTextView = (TextView) findViewById(R.id.db_text);
        mNoteText = (EditText) findViewById(R.id.note_text);
        mResultAdapter = new ResultAdapter(this);
        mResultRecyclerView = (RecyclerView) findViewById(R.id.result_recycler);
        mResultRecyclerView.setAdapter(mResultAdapter);
        mResultRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mCurServerButton = (Button) findViewById(R.id.server_button);
        mCurServerButton.setOnClickListener(v -> {
            int checkedItem = findCheckedServer(PreferenceUtil.getString(MainActivity.this, PreferenceUtil.KEY_SERVER_LOCATION, VoiceConfiguration.HostUrl.KIKAGO_SQ));
            new AlertDialog.Builder(MainActivity.this)
                    .setSingleChoiceItems(mServerList, checkedItem, (dialog, which) -> {
                        PreferenceUtil.setString(MainActivity.this,
                                PreferenceUtil.KEY_SERVER_LOCATION,
                                mServerList[which]);
                        dialog.dismiss();

                        updateServerButtonText();
                        attachService();
                    }).show();
        });

        findViewById(R.id.button_source_usb).setOnClickListener(v -> scanUsbDevices());

        findViewById(R.id.button_source_android).setOnClickListener(v -> {
            mKikaGoVoiceSource = null;
            attachService();

            mTextView.setText("Using Android source");
            mNcParamLayout.setVisibility(View.GONE);
            if (mDbTitleView != null) {
                mDbTitleView.setVisibility(View.GONE);
            }
            if (mDbTextView != null) {
                mDbTextView.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.button_use_note).setOnClickListener(v -> {
            if (mNoteText != null) {
                String note = mNoteText.getText().toString();
                if (note != null && note.length() > 0) {
                    try {
                        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(mNoteText.getWindowToken(), 0);
                    } catch (Exception e) {
                    }
                } else {
                    String hint = mNoteText.getHint().toString();
                    String originalHint = getResources().getString(R.string.note_hint);
                    if (hint != null && originalHint != null) {
                        mNoteText.setText(hint.replace(originalHint,""), TextView.BufferType.EDITABLE);
                    }
                }
            }
        });

        findViewById(R.id.button_alignment).setOnClickListener(v -> {
            if (mVoiceService != null) {
                mVoiceService.sendAlignment(new String[]{"Yes", "No"});
            }
        });

        findViewById(R.id.button_altering).setOnClickListener(v -> {
            if (mVoiceService != null) {
                mVoiceService.sendCommand(SocketCommand.ALTERING, "Send me a taxi in 5 minutes");
            }
        });

        findViewById(R.id.button_enter_local_playback).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LocalPlayBackActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.button_enter_play).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PlayActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.button_auto_testr).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AutoTestActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.button_wake_up_tester).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WakeUpTestActivity.class);
            startActivity(intent);
        });

        mReportButton = (Button) findViewById(R.id.button_report_log);
        mReportButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReportActivity.class);
            startActivity(intent);
        });
        mReportButton.setEnabled(false);

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
        mButtonAngle.setOnClickListener(v -> {
            if (mKikaGoVoiceSource != null) {
                mKikaGoVoiceSource.setNoiseCancellationParameters(
                        KikaNcBuffer.CONTROL_ANGLE, mSeekAngle.getProgress());
                if (mTextView != null) {
                    mTextView.setText("Set the Angle to " + mSeekAngle.getProgress());
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
        mButtonNc.setOnClickListener(v -> {
            if (mKikaGoVoiceSource != null) {
                mKikaGoVoiceSource.setNoiseCancellationParameters(
                        KikaNcBuffer.CONTROL_NC, mSeekNc.getProgress());
            }
            if (mTextView != null) {
                mTextView.setText("Set the NC to " + mSeekNc.getProgress());
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
        mButtonMode.setOnClickListener(v -> {
            if (mKikaGoVoiceSource != null) {
                mKikaGoVoiceSource.setNoiseCancellationParameters(
                        KikaNcBuffer.CONTROL_MODE, mSeekMode.getProgress());
            }
            if (mTextView != null) {
                mTextView.setText("Set the Mode to " + mSeekMode.getProgress());
            }
        });
        mButtonMode.setEnabled(false);

        mVolumeText = (TextView) findViewById(R.id.text_volume);

        findViewById(R.id.button_volume_up).setOnClickListener(v -> {
            if (mKikaGoVoiceSource != null) {
                mKikaGoVoiceSource.volumeUp();
                checkVolume();
            }
        });

        findViewById(R.id.button_volume_down).setOnClickListener(v -> {
            if (mKikaGoVoiceSource != null) {
                mKikaGoVoiceSource.volumeDown();
                checkVolume();
            }
        });

        findViewById(R.id.button_check_volume).setOnClickListener(v -> checkVolume());

        checkVersions();
        waveCreateView();

        mStartButton.setEnabled(true);
        mStopButton.setEnabled(false);
        mTextView.setText("Using Android source");
        mNcParamLayout.setVisibility(View.GONE);
    }

    private void scanUsbDevices() {
        if (mKikaGoVoiceSource == null) {
            mUsbAudioService = UsbAudioService.getInstance(MainActivity.this);
            mUsbAudioService.setListener(mIUsbAudioListener);
            mUsbAudioService.scanDevices();
        }
    }

    private void checkVersions() {
        StringBuilder version = new StringBuilder();
        version.append("[app : ").append(getVersionName(this)).append("] ");
        if (mKikaGoVoiceSource != null && mKikaGoVoiceSource.mIsOpened()) {
            version.append("[fw : 0x").append(Integer.toHexString(mKikaGoVoiceSource.checkFwVersion())).append("] ");
            version.append("[driver : 0x").append(String.format("%03X", mKikaGoVoiceSource.checkDriverVersion())).append("] ");
            version.append("[nc : ").append(mKikaGoVoiceSource.getNcVersion()).append("] ");
        }
        ((TextView) findViewById(R.id.text_version)).setText("version : \n" + version);
    }

    private void checkVolume() {
        if (mVolumeText != null) {
            if (mKikaGoVoiceSource != null && mKikaGoVoiceSource.mIsOpened()) {
                int volumeLevel = mKikaGoVoiceSource.checkVolumeState();
                String volume = (volumeLevel >= VOLUME_TABLE.length | volumeLevel < 0) ? "error" : VOLUME_TABLE[volumeLevel];
                mVolumeText.setText(String.format(getString(R.string.current_volume), volume));
            } else {
                mVolumeText.setText("");
            }
        }
    }

    private void writeTimeToFile(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateAndTime = sdf.format(new Date());
        DebugUtil.logTextToFile("time", currentDateAndTime);
    }

    private void writeNoteToFile() {
        if (mNoteText!= null) {
            String note = mNoteText.getText().toString();
            if (note != null && note.length() > 0) {
                DebugUtil.logTextToFile("note", note);
            }
        }
    }

    private void writeVersionsToFile() {
        DebugUtil.logTextToFile("app", getVersionName(this));
        if (mKikaGoVoiceSource != null) {
            DebugUtil.logTextToFile("fw", String.valueOf(Integer.toHexString(mKikaGoVoiceSource.checkFwVersion())));
            DebugUtil.logTextToFile("driver", String.valueOf(mKikaGoVoiceSource.checkDriverVersion()));
            DebugUtil.logTextToFile("nc", String.valueOf(mKikaGoVoiceSource.getNcVersion()));
        }
    }

    private void writeVolumeToFile() {
        if (mKikaGoVoiceSource != null) {
            int volumeLevel = mKikaGoVoiceSource.checkVolumeState();
            String volume = (volumeLevel >= VOLUME_TABLE.length | volumeLevel < 0) ? "error" : VOLUME_TABLE[volumeLevel];
            if (!(volumeLevel >= VOLUME_TABLE.length | volumeLevel < 0)) {
                DebugUtil.logTextToFile("volume", String.format(getString(R.string.current_volume), volume).replace("Volume : ", ""));
            }
        }
    }

    private int findCheckedServer(String url) {
        for (int i = 0; i < mServerList.length; i++) {
            if (mServerList[i].equals(url)) {
                return i;
            }
        }
        return 0;
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
            mPermissionButton.setText("Need\npermission.");
        } else {
            mPermissionButton.setEnabled(false);
            mPermissionButton.setText("Permission\ngranted");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.i("onStart");
        updatePermissionButtonState();
        if (mTtsSource == null) {
            mTtsSource = TtsService.getInstance().getSpeaker(TtsService.TtsSourceType.KIKA_WEB);
            mTtsSource.init(this, null);
            mTtsSource.setTtsStateChangedListener(MainActivity.this);
        }
        AudioPlayBack.setListener(len -> mStatus2TextView.post(() -> {
            if (mStatus2TextView != null) {
                mStatus2TextView.setText("size : " + len);
            }
        }));

        scanUsbDevices();
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

        String server = PreferenceUtil.getString(this, PreferenceUtil.KEY_SERVER_LOCATION, VoiceConfiguration.HostUrl.KIKAGO_SQ);
        mCurServerButton.setText("Now is : " + server);
    }

    private void attachService() {
        if (mVoiceService != null) {
            mVoiceService.destroy();
            mVoiceService = null;
        }
        AsrConfiguration.Builder builder = new AsrConfiguration.Builder();
        mAsrConfiguration = builder
                .setAlterEnabled(((CheckBox) findViewById(R.id.check_alter)).isChecked())
                .setEmojiEnabled(((CheckBox) findViewById(R.id.check_emoji)).isChecked())
                .setPunctuationEnabled(((CheckBox) findViewById(R.id.check_punctuation)).isChecked())
                .setSpellingEnabled(((CheckBox) findViewById(R.id.check_spelling)).isChecked())
                .setVprEnabled(((CheckBox) findViewById(R.id.check_vpr)).isChecked())
                .setEosPackets((mSpinner.getSelectedItemPosition() + 1) * 3)
                .build();
        VoiceConfiguration conf = new VoiceConfiguration();
        conf.setIsDebugMode(true);
        conf.setDebugFileTag(DEBUG_FILE_TAG);
        conf.source(mKikaGoVoiceSource);
        conf.setWakeUpDetector(IS_WAKE_UP_MODE ? new SnowBoyDetector(this) : null);
        conf.setBosDuration(14500);
        conf.setEosDuration(10000);
        conf.setSpeechMode(mSpeechMode);
        conf.setConnectionConfiguration(new VoiceConfiguration.ConnectionConfiguration.Builder()
                .setAppName("KikaGoTest")
                .setUrl(PreferenceUtil.getString(
                        MainActivity.this,
                        PreferenceUtil.KEY_SERVER_LOCATION,
                        VoiceConfiguration.HostUrl.KIKAGO_SQ))
                .setLocale(getCurrentLocale())
                .setSign(RequestManager.getSign(this))
                .setUserAgent(RequestManager.generateUserAgent(this))
                .setEngine("google")
                .setAsrConfiguration(mAsrConfiguration)
                .build());

        VoiceConfig.getVoiceConfig(this, conf, config -> {
            mVoiceService = VoiceService.getService(this, config);
            mVoiceService.setVoiceDataListener(this);
            mVoiceService.setVoiceRecognitionListener(this);
            mVoiceService.setVoiceDataListener(this);
            mVoiceService.create();

            if (mKikaGoVoiceSource != null) {
                mKikaGoVoiceSource.updateFileWriter();
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.i("onStop");
        if (mVoiceService != null) {
            mVoiceService.destroy();
            mVoiceService = null;
        }
        if (mTtsSource != null) {
            mTtsSource.close();
            mTtsSource = null;
        }

        if (mUsbAudioService != null) {
            mUsbAudioService.closeDevice();
            mUsbAudioService.setListener(null);
        }
        AudioPlayBack.setListener(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.i("onDestroy");
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

        if (mSpeechMode == VoiceConfiguration.SpeechMode.ONE_SHOT) {
            waveStopDraw();
            if (mTextView != null) {
                mTextView.setText("stopped.");
            }
            mStartButton.setEnabled(true);
            mStopButton.setEnabled(false);
            mReportButton.setEnabled(true);
        }
    }

    @Override
    public void onError(int reason) {
        Logger.e("MainActivity onError reason = " + reason);
        if (reason == ERR_REASON_NOT_CREATED) {
            Logger.e("ERR_REASON_NOT_CREATED");
            if (mTextView != null) {
                mTextView.setText("Select an audio source first.");
            }
        } else if (reason == ERR_CONNECTION_ERROR) {
            if (mTextView != null) {
                mTextView.setText("Connection error.");
            }
        } else if (reason == ERR_NO_SPEECH) {
            if (mTextView != null) {
                mTextView.setText("No Speech timeout.");
            }
        } else if (reason == ERR_RECORD_DATA_FAIL) {
            if (mTextView != null) {
                mTextView.setText("Record data fail.");
            }
        } else if (reason == ERR_RECORD_OPEN_FAIL) {
            mUsbAudioService.closeDevice();
            if (mKikaGoVoiceSource != null) {
                mKikaGoVoiceSource.close();
                mKikaGoVoiceSource = null;
            }
            attachService();
            Toast.makeText(this, "Device open fail", Toast.LENGTH_SHORT).show();
        }
        waveStopDraw();
        mStartButton.setEnabled(true);
        mStopButton.setEnabled(false);
        mReportButton.setEnabled(true);
    }

    @Override
    public void onSpeechProbabilityChanged(float prob) {
        if (mVadTextView != null) {
            mVadTextView.setText(String.format("%.1f", prob));
        }
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
        mTextView.setText("Waking up!");
    }

    @Override
    public void onSleep() {
        Logger.d("Tts onSleep");
        mTextView.setText("Sleeping!");
    }

    private IUsbAudioListener mIUsbAudioListener = new IUsbAudioListener() {

        @Override
        public void onDeviceAttached(KikaGoVoiceSource audioSource) {
            Logger.d("onDeviceAttached.");
            mKikaGoVoiceSource = audioSource;
            attachService();

            Logger.d("onDeviceAttached. mKikaGoVoiceSource = " + mKikaGoVoiceSource);
            if (mKikaGoVoiceSource == null) {
                return;
            }

            mKikaGoVoiceSource.setSourceDataCallback(mSourceDataCallback);
            mKikaGoVoiceSource.setOnOpenedCallback(mOnOpenedCallback);

            mNcParamLayout.setVisibility(View.VISIBLE);

            if (mDbTitleView != null) {
                mDbTitleView.setVisibility(View.VISIBLE);
            }
            if (mDbTextView != null) {
                mDbTextView.setVisibility(View.VISIBLE);
            }

            mButtonAngle.setEnabled(true);
            mButtonNc.setEnabled(true);
            mButtonMode.setEnabled(true);

            mTextView.setText("Using Usb source");
        }

        @Override
        public void onDeviceDetached() {
            Logger.d("onDeviceDetached.");
            mKikaGoVoiceSource = null;
            attachService();

            checkVersions();
            checkVolume();

            mTextView.setText("Using Android source");
            mNcParamLayout.setVisibility(View.GONE);
        }

        @Override
        public void onDeviceError(int errorCode) {
            Logger.d("onDeviceError errorCode = " + errorCode);
            mKikaGoVoiceSource = null;
            attachService();

            checkVersions();
            checkVolume();

            mTextView.setText("Using Android source");
            mNcParamLayout.setVisibility(View.GONE);
        }
    };

    @Override
    public void onData(byte[] data, int readSize) {
        if (mWaveCanvas != null) {
            mWaveCanvas.onData(data, readSize);
        }
    }

    private void waveCreateView() {
        mWavesfv = (WaveSurfaceView) findViewById(R.id.wavesfv);
        if(mWavesfv != null) {
            mWavesfv.setLine_off(42);
            //解决surfaceView黑色闪动效果
            mWavesfv.setZOrderOnTop(true);
            mWavesfv.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        }
    }

    private void waveStartDraw() {
        if (mWaveCanvas == null) {
            mWaveCanvas = new WaveCanvas();
            mWaveCanvas.baseLine = mWavesfv.getHeight() / 2;
            mWaveCanvas.startDraw(mWavesfv, new Handler.Callback() {
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
    }
}
