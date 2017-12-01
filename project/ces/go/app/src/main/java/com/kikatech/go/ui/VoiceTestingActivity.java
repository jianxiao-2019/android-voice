package com.kikatech.go.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;

import com.kikatech.go.R;
import com.kikatech.usb.IUsbAudioListener;
import com.kikatech.usb.UsbAudioService;
import com.kikatech.usb.UsbAudioSource;
import com.kikatech.voice.core.tts.TtsService;
import com.kikatech.voice.core.tts.TtsSpeaker;
import com.kikatech.voice.core.webservice.message.EditTextMessage;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voice.service.VoiceConfiguration;
import com.kikatech.voice.service.VoiceService;
import com.kikatech.voice.util.PreferenceUtil;
import com.kikatech.voice.util.log.Logger;
import com.kikatech.voice.util.request.RequestManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ryanlin on 03/11/2017.
 */

public class VoiceTestingActivity extends BaseActivity
        implements VoiceService.VoiceRecognitionListener, VoiceService.VoiceStateChangedListener,
        TtsSpeaker.TtsStateChangedListener, IUsbAudioListener {

    private static final String WEB_SOCKET_URL_DEV = "ws://speech0-dev-mvp.kikakeyboard.com/v2/speech";
    private static final String SERVER_COMMAND_CONTENT = "CONTENT";

    private static final Locale[] LOCALE_LIST = new Locale[]{
            new Locale("en", "US"),
            new Locale("zh", "CN"),
    };

    private EditText mEditText;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean mPermissionToRecordAccepted = false;
    private String[] mPermissions = {Manifest.permission.RECORD_AUDIO};

    private VoiceService mVoiceService;
    private TtsSpeaker mTtsSpeaker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_testing);

        UsbAudioService audioService = UsbAudioService.getInstance(this);
        audioService.setListener(this);
        audioService.scanDevices();
//
//         attachService(null);

        findViewById(R.id.button_permission).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(VoiceTestingActivity.this,
                        Manifest.permission.RECORD_AUDIO);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(VoiceTestingActivity.this,
                            mPermissions, REQUEST_RECORD_AUDIO_PERMISSION);
                } else {
                    showToast("You already have this permission.");
                }
            }
        });

        findViewById(R.id.button_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVoiceService != null) {
                    mVoiceService.start();
                }
            }
        });

        findViewById(R.id.button_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVoiceService != null) {
                    mVoiceService.stop();
                }
            }
        });

        findViewById(R.id.button_alter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVoiceService != null && mEditText != null) {
                    mVoiceService.sendCommand(
                            SERVER_COMMAND_CONTENT,
                            mEditText.getText().toString());
                }
            }
        });

        findViewById(R.id.button_tts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditText == null || TextUtils.isEmpty(mEditText.getText())) {
                    return;
                }
                // mTtsSpeaker.speak(mEditText.getText().toString());
                Pair<String, Integer>[] playList = new Pair[3];
                playList[0] = new Pair<>("Your speak is :", TtsSpeaker.TTS_VOICE_2);
                playList[1] = new Pair<>(mEditText.getText().toString(), TtsSpeaker.TTS_VOICE_1);
                playList[2] = new Pair<>("Is that correct?", TtsSpeaker.TTS_VOICE_2);
                mTtsSpeaker.speak(playList);
            }
        });

        findViewById(R.id.button_interrupt_tts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTtsSpeaker.interrupt();
            }
        });

        mEditText = (EditText) findViewById(R.id.edit_text);

        if (mTtsSpeaker == null) {
            mTtsSpeaker = TtsService.getInstance().getSpeaker();
            mTtsSpeaker.init(this, null);
            mTtsSpeaker.setTtsStateChangedListener(VoiceTestingActivity.this);
        }

        Message.register("INTERMEDIATE", IntermediateMessage.class);
        Message.register("ALTER", EditTextMessage.class);
        Message.register("ASR", TextMessage.class);
    }

    private void attachService(UsbAudioSource audioSource) {
        VoiceConfiguration conf = new VoiceConfiguration();
        conf.setDebugFilePath(getDebugFilePath(this));
        conf.source(audioSource);
        conf.setConnectionConfiguration(new VoiceConfiguration.ConnectionConfiguration.Builder()
                .setAppName("KikaGoTest")
                .setUrl(WEB_SOCKET_URL_DEV)
                .setLocale(getCurrentLocale())
                .setSign(RequestManager.getSign(this))
                .setUserAgent(RequestManager.generateUserAgent(this))
                .setEngine("google")
                .setAlterEnabled(true)
                .setEmojiEnabled(true)
                .setPunctuationEnabled(true)
                .build());
        mVoiceService = VoiceService.getService(this, conf);
        mVoiceService.setVoiceRecognitionListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mVoiceService != null) {
            mVoiceService.stop();
        }
        if (mTtsSpeaker != null) {
            mTtsSpeaker.close();
            mTtsSpeaker = null;
        }

        Message.unregisterAll();
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
                        mEditText.setText(((TextMessage) message).text);
                    } else if (message instanceof IntermediateMessage) {
                        mEditText.setText(((IntermediateMessage) message).text);
                    } else if (message instanceof EditTextMessage) {
                        mEditText.setText(((EditTextMessage) message).text);
                    }
                }
            });
        }
    }

    @Override
    public void onStartListening() {
    }

    @Override
    public void onStopListening() {
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


    @Override
    public void onDeviceAttached(UsbAudioSource audioSource) {
        Logger.d("VoiceTestingActivity onDeviceAttached audioSource = " + audioSource);
        attachService(audioSource);
    }

    @Override
    public void onDeviceDetached() {

    }
}
