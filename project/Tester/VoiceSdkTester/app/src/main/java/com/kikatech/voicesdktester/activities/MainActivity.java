package com.kikatech.voicesdktester.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import com.kikatech.voice.core.debug.DebugUtil;
import com.kikatech.voice.core.tts.TtsService;
import com.kikatech.voice.core.tts.TtsSource;
import com.kikatech.voice.core.webservice.message.EditTextMessage;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voice.service.VoiceConfiguration;
import com.kikatech.voice.service.VoiceService;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.util.ReportUtil;
import com.kikatech.voice.util.log.Logger;
import com.kikatech.voice.util.request.RequestManager;
import com.kikatech.voicesdktester.R;
import com.kikatech.voicesdktester.ui.ResultAdapter;
import com.kikatech.voicesdktester.utils.PreferenceUtil;
import com.xiao.usbaudio.AudioPlayBack;

import java.util.Locale;

import static com.kikatech.voice.service.VoiceService.ERR_REASON_NOT_CREATED;

public class MainActivity extends AppCompatActivity implements
        VoiceService.VoiceRecognitionListener,
        VoiceService.VoiceStateChangedListener,
        VoiceService.VoiceActiveStateListener,
        TtsSource.TtsStateChangedListener {

    private static final boolean IS_WAKE_UP_MODE = false;
    private static final String DEBUG_FILE_TAG = "voiceTester";

    public static final String WEB_SOCKET_URL_DEV = "ws://speech0-dev-mvp.kikakeyboard.com/v3/speech";

    private static final Locale[] LOCALE_LIST = new Locale[]{
            new Locale("en", "US"),
            new Locale("zh", "CN"),
    };

    private Button mPermissionButton;

    private Button mStartButton;
    private Button mStopButton;
    private Button mWavButton;
    private Button mReportButton;
    private Button mCurServerButton;

    private TextView mAudioIdText;
    private TextView mTextView;
    private TextView mStatus2TextView;
    private TextView mVadTextView;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_testing);

        Logger.i("onCreate");
        mPermissionButton = (Button) findViewById(R.id.button_permission);
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
                ReportUtil.getInstance().startTimeStamp("start record");
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
                ReportUtil.getInstance().logTimeStamp("stop record");
                if (mVadTextView != null) {
                    mVadTextView.setText("0.0");
                }
                if (mVoiceService != null) {
                    mVoiceService.stop();
                }
                if (mTimerHandler.hasMessages(MSG_FINAL_RESULT_TIMEOUT)) {
                    mTimerHandler.removeMessages(MSG_FINAL_RESULT_TIMEOUT);
                }
                Logger.w("onMessage 1.0 send 2adㄎ000");
                mTimerHandler.sendEmptyMessageDelayed(MSG_FINAL_RESULT_TIMEOUT, 2000);
            }
        });
        mStopButton.setEnabled(false);

        mWavButton = (Button) findViewById(R.id.button_to_wav);
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
        mVadTextView = (TextView) findViewById(R.id.vad_text);
        mResultAdapter = new ResultAdapter(this);
        mResultRecyclerView = (RecyclerView) findViewById(R.id.result_recycler);
        mResultRecyclerView.setAdapter(mResultAdapter);
        mResultRecyclerView.setLayoutManager(new LinearLayoutManager(this));

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

        mReportButton = (Button) findViewById(R.id.button_report_log);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReportActivity.class);
                startActivity(intent);
            }
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

        ((TextView) findViewById(R.id.text_version)).setText("version : " + getVersionName(this));

        findViewById(R.id.button_volume_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUsbAudioSource != null) {
                    int volume = mUsbAudioSource.volumeUp();
                    Logger.d("button_volume_up volume = " + volume);
                    if (mTextView != null) {
                        mTextView.setText("volume : " + volume);
                    }
                }
            }
        });

        findViewById(R.id.button_volume_down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUsbAudioSource != null) {
                    int volume = mUsbAudioSource.volumeDown();
                    Logger.d("button_volume_down volume = " + volume);
                    if (mTextView != null) {
                        mTextView.setText("volume : " + volume);
                    }
                }
            }
        });

        findViewById(R.id.button_check_volume).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUsbAudioSource != null) {
                    int volume = mUsbAudioSource.checkVolumeState();
                    Logger.d("button_check_volume volume = " + volume);
                    if (mTextView != null) {
                        mTextView.setText("volume : " + volume);
                    }
                }
            }
        });

        Message.register("INTERMEDIATE", IntermediateMessage.class);
        Message.register("ALTER", EditTextMessage.class);
        Message.register("ASR", TextMessage.class);
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
            mVoiceService.destroy();
            mVoiceService = null;
        }
        // Debug
// TODO :       mAudioIdText.setText("File : " + mDebugFileName.substring(mDebugFileName.lastIndexOf("/") + 1));
        AsrConfiguration.Builder builder = new AsrConfiguration.Builder();
        mAsrConfiguration = builder
                .setAlterEnabled(((CheckBox) findViewById(R.id.check_alter)).isChecked())
                .setEmojiEnabled(((CheckBox) findViewById(R.id.check_emoji)).isChecked())
                .setPunctuationEnabled(((CheckBox) findViewById(R.id.check_punctuation)).isChecked())
                .setSpellingEnabled(((CheckBox) findViewById(R.id.check_spelling)).isChecked())
                .setVprEnabled(((CheckBox) findViewById(R.id.check_vpr)).isChecked())
                .setEosPackets(mSpinner.getSelectedItemPosition() + 1)
                .build();
        VoiceConfiguration conf = new VoiceConfiguration();
        conf.setIsDebugMode(true);
        conf.setDebugFileTag(DEBUG_FILE_TAG);
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

        if (mUsbAudioSource != null) {
            mUsbAudioSource.closeDevice();
        }
        if (mUsbAudioService != null) {
            mUsbAudioService.setListener(null);
        }
        AudioPlayBack.setListener(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.i("onDestroy");
        Message.unregisterAll();
    }

    private class ConvertWavButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            mWavButton.setEnabled(false);

            boolean isConverted = DebugUtil.convertCurrentPcmToWav();
            if (isConverted) {
                if (mTextView != null) {
                    mTextView.setText("Convert the file: '" + DebugUtil.getDebugFilePath() + "' succeed!");
                }
            } else {
                if (mTextView != null) {
                    mTextView.setText("Some error occurred!");
                }
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
            ReportUtil.getInstance().logTimeStamp(message.toString());
            if (mTextView != null) {
                mTextView.setText("Intermediate Result : " + ((IntermediateMessage) message).text);
            }
            if (mTimerHandler.hasMessages(MSG_FINAL_RESULT_TIMEOUT)) {
                mTimerHandler.removeMessages(MSG_FINAL_RESULT_TIMEOUT);
                Logger.w("onMessage 2.0 send 5000");
                mTimerHandler.sendEmptyMessageDelayed(MSG_FINAL_RESULT_TIMEOUT, 5000);
            }
            return;
        }
        ReportUtil.getInstance().logTimeStamp(message.toString());
        if (mTextView != null) {
            mTextView.setText("Final Result");
        }
        mResultAdapter.addResult(message);
        mResultAdapter.notifyDataSetChanged();

        if (mTimerHandler.hasMessages(MSG_FINAL_RESULT_TIMEOUT)) {
            mTimerHandler.removeMessages(MSG_FINAL_RESULT_TIMEOUT);
            Logger.w("onMessage 3.0 end 3000");
            mTimerHandler.sendEmptyMessageDelayed(MSG_FINAL_RESULT_TIMEOUT, 3000);
        }
    }

    @Override
    public void onCreated() {
        Logger.d("MainActivity onCreated");
        if (mTextView != null) {
            String text = mUsbAudioSource == null ? "Using Android source" : "Using Usb source";
            mTextView.setText(text);
        }
        if (mUsbAudioSource != null) {
            mNcParamLayout.setVisibility(View.VISIBLE);
            if (mSeekAngle != null) {
                mSeekAngle.setProgress(mUsbAudioSource.getNoiseSuppressionParameters(0));
            }
            if (mSeekNc != null) {
                mSeekNc.setProgress(mUsbAudioSource.getNoiseSuppressionParameters(1));
            }
            if (mSeekMode != null) {
                mSeekMode.setProgress(mUsbAudioSource.getNoiseSuppressionParameters(2));
            }
        } else {
            mNcParamLayout.setVisibility(View.GONE);
        }
        mStartButton.setEnabled(true);
        mStopButton.setEnabled(false);
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
        mReportButton.setEnabled(false);
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
        mWavButton.setOnClickListener(new ConvertWavButtonListener());
        mReportButton.setEnabled(true);
    }

    @Override
    public void onDestroyed() {
        Logger.d("MainActivity onDestroyed");
        if (mTextView != null) {
            mTextView.setText("Disconnected.");
        }
        mStartButton.setEnabled(false);
        mStopButton.setEnabled(false);
        mButtonAngle.setEnabled(false);
    }

    @Override
    public void onError(int reason) {
        Logger.e("MainActivity onError reason = " + reason);
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
        Logger.e("MainActivity onConnectionClosed");
        if (mVoiceService != null) {
            mVoiceService.stop();
        }
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

    private static final int MSG_FINAL_RESULT_TIMEOUT = 3;
    private Handler mTimerHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_FINAL_RESULT_TIMEOUT) {
                Logger.w("onMessage MSG_FINAL_RESULT_TIMEOUT");
                attachService();
            }
        }
    };
}
