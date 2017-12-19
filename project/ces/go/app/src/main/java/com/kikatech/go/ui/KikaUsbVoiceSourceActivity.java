package com.kikatech.go.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.kikatech.go.R;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.PreferenceUtil;
import com.kikatech.usb.IUsbAudioListener;
import com.kikatech.usb.UsbAudioService;
import com.kikatech.usb.UsbAudioSource;
import com.kikatech.voice.core.tts.TtsService;
import com.kikatech.voice.core.tts.TtsSource;
import com.kikatech.voice.core.webservice.message.EditTextMessage;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voice.service.VoiceConfiguration;
import com.kikatech.voice.service.VoiceService;
import com.kikatech.voice.service.conf.AsrConfiguration;
//import com.kikatech.voice.util.log.Logger;
import com.kikatech.voice.util.request.RequestManager;
import com.xiao.usbaudio.AudioPlayBack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.kikatech.voice.service.VoiceService.REASON_NOT_CREATED;

/**
 * Created by brad_chang on 2017/12/19.
 */

public class KikaUsbVoiceSourceActivity extends BaseActivity implements VoiceService.VoiceRecognitionListener, VoiceService.VoiceStateChangedListener, VoiceService.VoiceActiveStateListener,
        TtsSource.TtsStateChangedListener {
    private static final String WEB_SOCKET_URL_DEV = "ws://speech0-dev-mvp.kikakeyboard.com/v3/speech";
//    private static final String WEB_SOCKET_URL_DEV = "ws://172.16.3.168:8080/v3/speech";
//    private static final String WEB_SOCKET_URL_DEV = "ws://speech0-poc.kikakeyboard.com/v3/speech";

    
    private static final String TAG = "KikaUsbVoiceSourceActivity";
    
    private static final String SERVER_COMMAND_CONTENT = "CONTENT";

    private static final Locale[] LOCALE_LIST = new Locale[]{
            new Locale("en", "US"),
            new Locale("zh", "CN"),
    };

    private Button mStartButton;
    private Button mStopButton;
    private Button mCurServerButton;

    private TextView mTextView;
    private EditText mEditText;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean mPermissionToRecordAccepted = false;
    private String[] mPermissions = {Manifest.permission.RECORD_AUDIO};

    private VoiceService mVoiceService;
    private TtsSource mTtsSource;

    private AsrConfiguration mAsrConfiguration;

    private UsbAudioSource mUsbAudioSource;

    private String mDebugFileName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_testing_2);

        findViewById(R.id.button_permission).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(KikaUsbVoiceSourceActivity.this,
                        Manifest.permission.RECORD_AUDIO);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(KikaUsbVoiceSourceActivity.this,
                            mPermissions, REQUEST_RECORD_AUDIO_PERMISSION);
                } else {
                    showToast("You already have this permission.");
                }
            }
        });

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

        findViewById(R.id.button_alter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVoiceService != null && mEditText != null) {
                    mVoiceService.sleep();
                }
            }
        });

        findViewById(R.id.button_tts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditText == null || TextUtils.isEmpty(mEditText.getText())) {
                    return;
                }
                mTtsSource.speak(mEditText.getText().toString());
//                Pair<String, Integer>[] playList = new Pair[3];
//                playList[0] = new Pair<>("Your speak is :", TtsSource.TTS_SPEAKER_1);
//                playList[1] = new Pair<>(mEditText.getText().toString(), TtsSource.TTS_SPEAKER_2);
//                playList[2] = new Pair<>("Is that correct?", TtsSource.TTS_SPEAKER_1);
//                mTtsSource.speak(playList);
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
                    LogUtil.log(TAG, "onMessage conf = " + mAsrConfiguration.toJsonString());
                    mVoiceService.updateAsrSettings(mAsrConfiguration);
                }
            }
        });

        mTextView = (TextView) findViewById(R.id.status_text);
        mEditText = (EditText) findViewById(R.id.edit_text);

        if (mTtsSource == null) {
            mTtsSource = TtsService.getInstance().getSpeaker(TtsService.TtsSourceType.KIKA_WEB);
            mTtsSource.init(this, null);
            mTtsSource.setTtsStateChangedListener(KikaUsbVoiceSourceActivity.this);
        }

        mCurServerButton = (Button) findViewById(R.id.server_button);
        mCurServerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(KikaUsbVoiceSourceActivity.this)
                        .setSingleChoiceItems(R.array.hosts, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PreferenceUtil.setString(KikaUsbVoiceSourceActivity.this,
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
                UsbAudioService audioService = UsbAudioService.getInstance(KikaUsbVoiceSourceActivity.this);
                audioService.setListener(mIUsbAudioListener);
                audioService.scanDevices();
            }
        });

        findViewById(R.id.button_source_android).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUsbAudioSource = null;
                attachService();
            }
        });

        Message.register("INTERMEDIATE", IntermediateMessage.class);
        Message.register("ALTER", EditTextMessage.class);
        Message.register("ASR", TextMessage.class);

    }

    private void updateServerButtonText() {
        if (mCurServerButton == null) {
            return;
        }

        String server = PreferenceUtil.getString(this, PreferenceUtil.KEY_SERVER_LOCATION, WEB_SOCKET_URL_DEV);
        mCurServerButton.setText("Now is : " + server);

        if (mVoiceService != null) {
            mVoiceService.stop();
            mVoiceService.destroy();
        }
        attachService();
    }

    private void attachService() {

        // Debug
        AudioPlayBack.sFilePath = getDebugFilePath(this);
        mDebugFileName = getDebugFilePath(this);
        AsrConfiguration.Builder builder = new AsrConfiguration.Builder();
        mAsrConfiguration = builder
                .setSpeechMode(((CheckBox) findViewById(R.id.check_one_shot)).isChecked()
                        ? AsrConfiguration.SpeechMode.ONE_SHOT
                        : AsrConfiguration.SpeechMode.CONVERSATION)
                .setAlterEnabled(((CheckBox) findViewById(R.id.check_alter)).isChecked())
                .setEmojiEnabled(((CheckBox) findViewById(R.id.check_emoji)).isChecked())
                .setPunctuationEnabled(((CheckBox) findViewById(R.id.check_punctuation)).isChecked())
                .setSpellingEnabled(((CheckBox) findViewById(R.id.check_spelling)).isChecked())
                .build();
        VoiceConfiguration conf = new VoiceConfiguration();
        conf.setDebugFilePath(mDebugFileName);
        conf.source(mUsbAudioSource);
        conf.setConnectionConfiguration(new VoiceConfiguration.ConnectionConfiguration.Builder()
                .setAppName("KikaGoTest")
                .setUrl(PreferenceUtil.getString(
                        KikaUsbVoiceSourceActivity.this,
                        PreferenceUtil.KEY_SERVER_LOCATION,
                        WEB_SOCKET_URL_DEV))
                .setLocale(getCurrentLocale())
                .setSign(RequestManager.getSign(this))
                .setUserAgent(RequestManager.generateUserAgent(this))
                .setEngine("google")
                .setAsrConfiguration(mAsrConfiguration)
                .build());
//        conf.setSupportWakeUpMode(true);
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
        }
        if (mTtsSource != null) {
            mTtsSource.close();
            mTtsSource = null;
        }

        Message.unregisterAll();
        addWavHeader();
    }

    private void addWavHeader() {
        if (TextUtils.isEmpty(mDebugFileName)) {
            return;
        }
        String fileName = mDebugFileName.substring(mDebugFileName.lastIndexOf("/") + 1);
        LogUtil.log(TAG, "addWavHeader fileName = " + fileName);
        if (TextUtils.isEmpty(fileName)) {
            return;
        }

        File folder = new File(mDebugFileName.substring(0, mDebugFileName.lastIndexOf("/")));
        if (!folder.exists() || !folder.isDirectory()) {
            return;
        }

        LogUtil.log(TAG, "addWavHeader folder = " + folder.getPath());
        for (final File file : folder.listFiles()) {
            if (file.isDirectory()) {
                continue;
            }
            if (file.getName().contains(fileName) && !file.getName().contains("speex")) {
                LogUtil.log(TAG, "addWavHeader found file = " + file.getPath());
                addWavHeader(file, file.getName().contains("NC"));
            }
        }
    }

    private void addWavHeader(File file, boolean isNC) {
        try {
            byte[] bytesArray = new byte[(int) file.length()];
            LogUtil.log(TAG, "addWavHeader file.length = " + file.length());
            FileInputStream fis = new FileInputStream(file);
            int readSize = fis.read(bytesArray); //read file into bytes[]
            LogUtil.log(TAG, "addWavHeader readSize = " + readSize);
            fis.close();

            FileOutputStream fos = new FileOutputStream(file.getPath() + ".wav");
            fos.write(addHeader(bytesArray, 16000, 16, mUsbAudioSource == null || isNC ? 1 : 2));
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] addHeader(byte[] pcm, int sampleRate, int bitsPerSample, int channel) {
        byte[] header = new byte[44];

        long totalDataLen = pcm.length + 36;
        long bitrate = sampleRate * channel * bitsPerSample;
        long dataLen = pcm.length;
        LogUtil.log(TAG, "addWavHeader dataLen = " + dataLen);

        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = (byte) 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;
        header[21] = 0;
        header[22] = (byte) channel;
        header[23] = 0;
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        header[28] = (byte) ((bitrate / 8) & 0xff);
        header[29] = (byte) (((bitrate / 8) >> 8) & 0xff);
        header[30] = (byte) (((bitrate / 8) >> 16) & 0xff);
        header[31] = (byte) (((bitrate / 8) >> 24) & 0xff);
        header[32] = (byte) ((channel * bitsPerSample) / 8);
        header[33] = 0;
        header[34] = (byte) bitsPerSample;
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (dataLen & 0xff);
        header[41] = (byte) ((dataLen >> 8) & 0xff);
        header[42] = (byte) ((dataLen >> 16) & 0xff);
        header[43] = (byte) ((dataLen >> 24) & 0xff);

        byte[] result = new byte[header.length + pcm.length];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(pcm, 0, result, header.length, pcm.length);

        return result;
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
            File[] files = ContextCompat.getExternalCacheDirs(context);
            if (files != null && files.length > 0) {
                File file = files[0];
                if (file != null) {
                    createFolderIfNecessary(file);
                    return file;
                }
            }
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
        if (mEditText != null) {
            mEditText.post(new Runnable() {
                @Override
                public void run() {
                    if (message instanceof TextMessage) {
                        mEditText.setText(((TextMessage) message).text[0]);
                        mTextView.setText(String.format(getString(R.string.cid_text), ((TextMessage) message).cid));
                    } else if (message instanceof IntermediateMessage) {
                        mEditText.setText(((IntermediateMessage) message).text);
                        mTextView.setText(String.format(getString(R.string.cid_text), ((IntermediateMessage) message).cid));
                    } else if (message instanceof EditTextMessage) {
                        mEditText.setText(((EditTextMessage) message).text[0]);
                        mTextView.setText(String.format(getString(R.string.cid_text), ((EditTextMessage) message).cid));
                    }
                }
            });
        }
    }

    @Override
    public void onCreated() {
        LogUtil.log(TAG, "KikaUsbVoiceSourceActivity onCreated");
        if (mTextView != null) {
            String text = mUsbAudioSource == null ? "Using Android source" : "Using Usb source";
            mTextView.setText(text);
        }
    }

    @Override
    public void onStartListening() {
        LogUtil.log(TAG, "KikaUsbVoiceSourceActivity onStartListening");
        mStartButton.setEnabled(false);
        mStopButton.setEnabled(true);
    }

    @Override
    public void onStopListening() {
        LogUtil.log(TAG, "KikaUsbVoiceSourceActivity onStopListening");
        mStartButton.setEnabled(true);
        mStopButton.setEnabled(false);
    }

    @Override
    public void onDestroyed() {
        LogUtil.log(TAG, "KikaUsbVoiceSourceActivity onDestroyed");
    }

    @Override
    public void onSpeechProbabilityChanged(float prob) {
    }

    @Override
    public void onError(int reason) {
        if (reason == REASON_NOT_CREATED) {
            if (mTextView != null) {
                mTextView.setText("Select an audio source first.");
            }
        }
    }

    @Override
    public void onVadBos() {

    }

    @Override
    public void onTtsStart() {
        LogUtil.log(TAG, "Tts onTtsStart");
    }

    @Override
    public void onTtsComplete() {
        LogUtil.log(TAG, "Tts onTtsComplete");
    }

    @Override
    public void onTtsInterrupted() {
        LogUtil.log(TAG, "Tts onTtsInterrupted");
    }

    @Override
    public void onTtsError() {
        LogUtil.log(TAG, "Tts onTtsError");
    }

    @Override
    public void onWakeUp() {
        LogUtil.log(TAG, "Tts onWakeUp");
    }

    @Override
    public void onSleep() {
        LogUtil.log(TAG, "Tts onSleep");
    }

    private IUsbAudioListener mIUsbAudioListener = new IUsbAudioListener() {

        @Override
        public void onDeviceAttached(UsbAudioSource audioSource) {
            LogUtil.log(TAG, "onDeviceAttached.");
            mUsbAudioSource = audioSource;
            attachService();
        }

        @Override
        public void onDeviceDetached() {

        }
    };
}