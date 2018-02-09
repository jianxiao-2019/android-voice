package com.kikatech.go.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.kikatech.go.R;
import com.kikatech.go.util.preference.GlobalPref;

/**
 * @author SkeeterWang Created on 2018/2/8.
 */

public class KikaFeatureHighlightActivity extends BaseActivity {
    private static final String TAG = "KikaFeatureHighlightActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kika_feature_highlight);
        bindListener();
    }

    private void bindListener() {
        findViewById(R.id.feature_highlight_btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalPref.getIns().setIsFirstLaunch(false);
                startAnotherActivity(KikaPermissionsActivity.class, true);
            }
        });
    }
}
