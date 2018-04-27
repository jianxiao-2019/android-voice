package com.kikatech.go.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.kikatech.go.R;
import com.kikatech.go.services.DialogFlowForegroundService;
import com.kikatech.go.util.FAQHelper;

/**
 * @author SkeeterWang Created on 2018/3/30.
 */

public class KikaFAQsActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "KikaFAQsActivity";

    private FAQHelper mFAQHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kika_faq);
        bindView();
        bindListener();
        mFAQHelper = new FAQHelper(this);
        DialogFlowForegroundService.processDisableWakeUpDetector();
    }

    @Override
    protected void onDestroy() {
        mFAQHelper.release();
        DialogFlowForegroundService.processEnableWakeUpDetector();
        super.onDestroy();
    }

    private void bindView() {
    }

    private void bindListener() {
        findViewById(R.id.drawer_title_icon).setOnClickListener(this);
        findViewById(R.id.faq_q1).setOnClickListener(this);
        findViewById(R.id.faq_q2).setOnClickListener(this);
        findViewById(R.id.faq_q3).setOnClickListener(this);
        findViewById(R.id.faq_t1).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == null) {
            return;
        }
        switch (view.getId()) {
            case R.id.drawer_title_icon:
                finish();
                break;
            case R.id.faq_q1:
                mFAQHelper.doFAQ1();
                break;
            case R.id.faq_q2:
                mFAQHelper.doFAQ2();
                break;
            case R.id.faq_q3:
                mFAQHelper.doFAQ3();
                break;
            case R.id.faq_t1:
                mFAQHelper.doFAQT1();
                break;
        }
    }
}
