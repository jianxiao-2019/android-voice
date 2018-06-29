package com.kikatech.go.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.kikatech.go.R;
import com.kikatech.go.util.storage.FileUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.UserReportUtil;
import com.kikatech.go.util.amazon.S3TransferUtil;
import com.kikatech.voice.util.log.FileLoggerUtil;

import java.io.File;

/**
 * @author SkeeterWang Created on 2018/3/30.
 */

public class KikaUserReportActivity extends BaseActivity {
    private static final String TAG = "KikaUserReportActivity";

    private EditText mEdtTitle;
    private EditText mEdtDescription;
    private EditText mEdtMail;
    private View mBtnSend;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kika_user_report);
        bindView();
        bindListener();
    }

    private void bindView() {
        mEdtTitle = (EditText) findViewById(R.id.report_edt_title);
        mEdtDescription = (EditText) findViewById(R.id.report_edt_des);
        mEdtMail = (EditText) findViewById(R.id.report_edt_mail);
        mBtnSend = findViewById(R.id.report_btn_send);
    }

    private void bindListener() {
        findViewById(R.id.drawer_title_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.report_btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doReport();
            }
        });

        TextWatcher mAdjustBtnStatusWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                adjustBtnSendStatus();
            }
        };

        mEdtTitle.addTextChangedListener(mAdjustBtnStatusWatcher);
        mEdtDescription.addTextChangedListener(mAdjustBtnStatusWatcher);
        mEdtMail.addTextChangedListener(mAdjustBtnStatusWatcher);
    }

    private void adjustBtnSendStatus() {
        String title = getEditTextContent(mEdtTitle);
        String description = getEditTextContent(mEdtDescription);
        String mail = getEditTextContent(mEdtMail);
        boolean available = !TextUtils.isEmpty(title) && !TextUtils.isEmpty(description) && !TextUtils.isEmpty(mail);
        mBtnSend.setEnabled(available);
    }

    private void doReport() {
        performReport();
        clearEditTexts();
        showToastWithApplicationContext(getString(R.string.report_toast_sent));
        finish();
    }

    @SuppressWarnings("ConstantConditions")
    private void performReport() {
        final String title = getEditTextContent(mEdtTitle);
        final String description = getEditTextContent(mEdtDescription);
        final String mail = getEditTextContent(mEdtMail);
        File originLogFile = FileLoggerUtil.getIns().getLogFullPath(LogUtil.LOG_FOLDER, LogUtil.LOG_FILE);
        File copyLogFile = FileUtil.copy(originLogFile);
        if (copyLogFile.exists()) {
            final String key = FileUtil.getS3LogFileKey(mail, copyLogFile.getName());
            S3TransferUtil.getIns().uploadFile(copyLogFile.getAbsolutePath(), key, new S3TransferUtil.IUploadListener() {
                @Override
                public void onUploaded(long remainTime) {
                    if (LogUtil.DEBUG) {
                        LogUtil.logd("S3TransferUtil", String.format("remainTime: %s", remainTime));
                    }
                    UserReportUtil.report(title, description, mail, key);
                }

                @Override
                public void onFailed() {
                    if (LogUtil.DEBUG) {
                        LogUtil.logd("S3TransferUtil", "onFailed");
                    }
                    UserReportUtil.report(title, description, mail, UserReportUtil.LOG_FILE_UPLOAD_FAILED);
                }
            });
        } else {
            UserReportUtil.report(title, description, mail, UserReportUtil.LOG_FILE_NOT_EXIST);
        }
    }

    private void clearEditTexts() {
        mEdtTitle.setText("");
        mEdtDescription.setText("");
        mEdtMail.setText("");
    }

    private String getEditTextContent(EditText editText) {
        Editable editable = editText.getText();
        return editable != null ? editable.toString() : "";
    }
}
