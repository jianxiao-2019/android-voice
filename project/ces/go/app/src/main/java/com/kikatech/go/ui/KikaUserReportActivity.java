package com.kikatech.go.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.kikatech.go.R;
import com.kikatech.go.util.BackgroundThread;
import com.kikatech.go.util.UserReportUtil;

/**
 * @author SkeeterWang Created on 2018/3/30.
 */

public class KikaUserReportActivity extends BaseActivity {
    private static final String TAG = "KikaUserReportActivity";

    private EditText mEdtTitle;
    private EditText mEdtDescription;
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
    }

    private void adjustBtnSendStatus() {
        String title = getEditTextContent(mEdtTitle);
        String description = getEditTextContent(mEdtDescription);
        boolean available = !TextUtils.isEmpty(title) && !TextUtils.isEmpty(description);
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
        String title = getEditTextContent(mEdtTitle);
        String description = getEditTextContent(mEdtDescription);
        String logFileUrl = "";
        UserReportUtil.report(title, description, logFileUrl);
    }

    private void clearEditTexts() {
        mEdtTitle.setText("");
        mEdtDescription.setText("");
    }

    private String getEditTextContent(EditText editText) {
        Editable editable = editText.getText();
        return editable != null ? editable.toString() : "";
    }
}
