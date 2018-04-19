package com.kikatech.go.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.kikatech.go.R;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.UserReportUtil;

/**
 * @author SkeeterWang Created on 2018/4/17.
 */

public class KikaFAQsReportActivity extends BaseActivity {
    private static final String TAG = "KikaFAQsReportActivity";

    private String title;
    private String description;

    private EditText mEdtMail;
    private View mBtnSend;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq_report);
        bindData();
        bindView();
        bindListener();
    }

    private void bindData() {
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        Bundle args = intent.getExtras();
        if (args == null) {
            finish();
            return;
        }
        title = intent.getStringExtra(UserReportUtil.FAQReportReason.KEY_TITLE);
        description = intent.getStringExtra(UserReportUtil.FAQReportReason.KEY_DESCRIPTION);
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description)) {
            finish();
            return;
        }
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, String.format("title: %s, description: %s", title, description));
        }
    }

    private void bindView() {
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

        mEdtMail.addTextChangedListener(mAdjustBtnStatusWatcher);
    }

    private void adjustBtnSendStatus() {
        String mail = getEditTextContent(mEdtMail);
        boolean available = !TextUtils.isEmpty(mail);
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
        String mail = getEditTextContent(mEdtMail);
        String logFileUrl = "";
        UserReportUtil.report(title, description, mail, logFileUrl);
    }

    private void clearEditTexts() {
        mEdtMail.setText("");
    }

    private String getEditTextContent(EditText editText) {
        Editable editable = editText.getText();
        return editable != null ? editable.toString() : "";
    }
}
