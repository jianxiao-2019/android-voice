package com.kikatech.go.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kikatech.go.BuildConfig;
import com.kikatech.go.R;
import com.kikatech.go.eventbus.DFServiceEvent;
import com.kikatech.go.util.BackgroundThread;
import com.kikatech.go.util.LogOnViewUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.view.GoLayout;
import com.kikatech.voice.util.log.FileLoggerUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by brad_chang on 2017/12/8.
 */

public class KikaDebugLogActivity extends Activity {

    private static final String LOG_FOLDER = com.kikatech.voice.util.log.LogUtil.LOG_FOLDER;

    private TextView tvLogAppVersion;
    private TextView tvLogContent;
    private int mCurrentCheckedId = R.id.log_display;

    private final static String[] MAIL_RECEIVER = new String[]{"brad.chang@kikatech.com", "skeeter.wang@kikatech.com", "daniel.huang@kikatech.com"};
    private final static String SUBJECT = "[" + FileLoggerUtil.getIns().getDisplayInitTime() + "][" + BuildConfig.VERSION_NAME + "] KiKaGO Debug Log";
    private final static String MAIL_CONTENT =
            "Please kindly describe the reproduce steps of the issue, Thank you !! \n\n\n" +
                    "What is the problem ?\n\n\n\n" +
                    "Reproduce Steps :\n1.\n2.\n3.\n";

    private final static int[] VIEW_ID = new int[]{
            R.id.radioGroupLog,

            R.id.log_display,
            R.id.log_kikago,
            R.id.log_voice_sdk,
            R.id.log_voice_mvp,

            R.id.button_copy,
            R.id.button_send
    };


    /**
     * <p>Reflection subscriber method used by EventBus,
     * <p>do not remove this except the subscriber is no longer needed.
     *
     * @param event event from {@link com.kikatech.go.services.DialogFlowForegroundService}
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServiceEvent(DFServiceEvent event) {
        if (event == null) {
            return;
        }
        String action = event.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        switch (action) {
            case DFServiceEvent.ACTION_ON_VOICE_SRC_CHANGE:
                if (GoLayout.ENABLE_LOG_VIEW) {
                    String text = event.getExtras().getString(DFServiceEvent.PARAM_TEXT);
                    updateVoiceSourceInfo(text);
                }
                break;
        }
    }

    public void updateVoiceSourceInfo(String text) {
        StringBuilder builder = new StringBuilder(BuildConfig.VERSION_NAME);
        if (!TextUtils.isEmpty(text)) {
            builder.append("  <Record From:").append(text).append(">");
        }
        tvLogAppVersion.setText(builder.toString());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_kika_debug_log);

        tvLogAppVersion = (TextView) findViewById(R.id.log_app_version);
        tvLogContent = (TextView) findViewById(R.id.log_content);

        ((RadioGroup) findViewById(R.id.radioGroupLog)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mCurrentCheckedId = checkedId;
                loadLog();
            }
        });

        findViewById(R.id.button_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadLogContent(new ILoadCallback() {
                    @Override
                    public void onLoadComplete(String logTitle, String log) {
                        copyText(logTitle, log);
                    }
                });
            }
        });

        findViewById(R.id.button_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMail();
            }
        });

        registerReceivers();

        updateVoiceSourceInfo(LogOnViewUtil.getIns().getVoiceSourceInfo());

        loadLog();
    }


    private void registerReceivers() {
        try {
            EventBus.getDefault().register(this);
        } catch (Exception ignore) {
        }
    }

    private void unregisterReceivers() {
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception ignore) {
        }
    }


    @Override
    protected void onDestroy() {
        unregisterReceivers();
        super.onDestroy();
    }

    private final Runnable mLockUI = new Runnable() {
        @Override
        public void run() {
            tvLogContent.setText("Loading ...");
            for (int id : VIEW_ID) {
                findViewById(id).setEnabled(false);
            }
        }
    };

    private final Runnable mReleaseUI = new Runnable() {
        @Override
        public void run() {
            for (int id : VIEW_ID) {
                findViewById(id).setEnabled(true);
            }
        }
    };

    private void copyText(String logTitle, String log) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(logTitle, log);
        clipboard.setPrimaryClip(clip);
    }

    private void sendMail() {
        Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
        i.setType("vnd.android.cursor.dir/email");
        i.putExtra(Intent.EXTRA_EMAIL, MAIL_RECEIVER);
        i.putExtra(Intent.EXTRA_SUBJECT, SUBJECT);
        i.putExtra(Intent.EXTRA_TEXT, MAIL_CONTENT);

        ArrayList<Uri> uris = new ArrayList<>();
        //convert from paths to Android friendly Parcelable Uri's
        File[] filePaths = new File[]{
                FileLoggerUtil.getIns().getLogFullPath(LOG_FOLDER, LogOnViewUtil.LOG_FILE),
                FileLoggerUtil.getIns().getLogFullPath(LOG_FOLDER, LogUtil.LOG_FILE),
                FileLoggerUtil.getIns().getLogFullPath(LOG_FOLDER, com.kikatech.voice.util.log.LogUtil.LOG_FILE),
                FileLoggerUtil.getIns().getLogFullPath(LOG_FOLDER, com.kikatech.voice.util.log.Logger.LOG_FILE),
        };
        for (File file : filePaths) {
            LogUtil.log("KikaDebugLogActivity", file.getAbsolutePath() + ":" + file.exists());
            Uri u = Uri.fromFile(file);
            uris.add(u);
        }
        i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(KikaDebugLogActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    interface ILoadCallback {
        void onLoadComplete(String logTitle, String log);
    }

    private void loadLogContent(final ILoadCallback callback) {
        BackgroundThread.post(new Runnable() {
            @Override
            public void run() {
                final String log;
                final String logTitle;
                switch (mCurrentCheckedId) {
                    case R.id.log_display:
                        log = FileLoggerUtil.getIns().loadLogFile(LOG_FOLDER, LogOnViewUtil.LOG_FILE);
                        logTitle = "Log Display";
                        break;
                    case R.id.log_kikago:
                        log = FileLoggerUtil.getIns().loadLogFile(LOG_FOLDER, com.kikatech.go.util.LogUtil.LOG_FILE);
                        logTitle = "Log Display";
                        break;
                    case R.id.log_voice_sdk:
                        log = FileLoggerUtil.getIns().loadLogFile(LOG_FOLDER, com.kikatech.voice.util.log.LogUtil.LOG_FILE);
                        logTitle = "Voice SDK Log";
                        break;
                    case R.id.log_voice_mvp:
                        log = FileLoggerUtil.getIns().loadLogFile(LOG_FOLDER, com.kikatech.voice.util.log.Logger.LOG_FILE);
                        logTitle = "Voice MVP Log";
                        break;
                    default:
                        logTitle = "<Id Error : " + mCurrentCheckedId + ">";
                        log = logTitle;
                        break;
                }
                callback.onLoadComplete(logTitle, log);
            }
        });
    }

    private void loadLog() {
        LogUtil.log("KikaDebugLogActivity", "start");
        tvLogContent.post(mLockUI);
        loadLogContent(new ILoadCallback() {
            @Override
            public void onLoadComplete(String logTitle, final String log) {
                tvLogContent.post(new Runnable() {
                    @Override
                    public void run() {
                        tvLogContent.setText(log);
                        mReleaseUI.run();
                    }
                });
            }
        });
    }
}