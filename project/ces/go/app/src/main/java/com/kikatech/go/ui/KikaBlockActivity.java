package com.kikatech.go.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.kikatech.go.R;
import com.kikatech.go.util.VersionControlUtil;

/**
 * @author SkeeterWang Created on 2018/4/20.
 */

public class KikaBlockActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kika_block);
        bindView();
        bindListener();
    }

    private void bindView() {
    }

    private void bindListener() {
        findViewById(R.id.block_btn_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VersionControlUtil.openUpdatePage(KikaBlockActivity.this);
            }
        });
    }
}