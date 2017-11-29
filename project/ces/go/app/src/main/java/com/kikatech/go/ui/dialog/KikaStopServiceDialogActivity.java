package com.kikatech.go.ui.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.kikatech.go.R;
import com.kikatech.go.services.DialogFlowForegroundService;

/**
 * @author SkeeterWang Created on 2017/11/29.
 */

public class KikaStopServiceDialogActivity extends Activity {
    private static final String TAG = "KikaDialogStopServiceActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_kika_stop_service);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        View mBtnApply = findViewById(R.id.activity_dialog_stop_service_btn_apply);
        View mBtnCancel = findViewById(R.id.activity_dialog_stop_service_btn_cancel);

        mBtnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFlowForegroundService.processStop(KikaStopServiceDialogActivity.this, DialogFlowForegroundService.class);
                finish();
            }
        });

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
