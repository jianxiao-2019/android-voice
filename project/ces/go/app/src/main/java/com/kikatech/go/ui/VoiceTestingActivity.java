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
import android.view.View;

import com.kikatech.go.R;
import com.kikatech.voice.VoiceConfiguration;
import com.kikatech.voice.VoiceService;
import com.kikatech.voice.util.PreferenceUtil;
import com.kikatech.voice.util.request.RequestManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ryanlin on 03/11/2017.
 */

public class VoiceTestingActivity extends BaseActivity {

    private static final String WEB_SOCKET_URL_DEV = "ws://speech0-dev-mvp.kikakeyboard.com/v2/speech";

    private static final Locale[] LOCALE_LIST = new Locale[] {
            new Locale("en", "US"),
            new Locale("zh", "CN"),
    };

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean mPermissionToRecordAccepted = false;
    private String[] mPermissions = {Manifest.permission.RECORD_AUDIO};

    private VoiceService mVoiceService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_testing);

        VoiceConfiguration conf = new VoiceConfiguration();
        conf.setDebugFilePath(getDebugFilePath(this));
        conf.setConnectionConfiguration(new VoiceConfiguration.ConnectionConfiguration.Builder()
                .setUrl(WEB_SOCKET_URL_DEV)
                .setLocale(getCurrentLocale())
                .setSign(RequestManager.getSign(this))
                .setUserAgent(RequestManager.generateUserAgent(this))
                .build());
        mVoiceService = VoiceService.getService(this, conf);

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
}
