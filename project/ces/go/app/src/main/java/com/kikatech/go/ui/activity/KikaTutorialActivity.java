package com.kikatech.go.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.kikatech.go.R;
import com.kikatech.go.services.DialogFlowForegroundService;
import com.kikatech.go.tutorial.TutorialManager;
import com.kikatech.go.tutorial.TutorialUtil;
import com.kikatech.go.view.GoLayout;

/**
 * @author SkeeterWang Created on 2018/5/11.
 */

public class KikaTutorialActivity extends BaseActivity {
    private static final String TAG = "KikaTutorialActivity";

    private View mBtnExit;
    private TutorialManager mManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kika_tutorial);
        mBtnExit = findViewById(R.id.tutorial_btn_exit);
        mBtnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitTutorial();
            }
        });

        GoLayout mGoLayout = (GoLayout) findViewById(R.id.go_layout);
        mGoLayout.setTouchWakeUpEnabled(false);

        initTutorialManager(KikaTutorialActivity.this, mGoLayout);
    }

    private void initTutorialManager(Context context, GoLayout goLayout) {
        mManager = new TutorialManager(context, goLayout, new TutorialManager.ITutorialListener() {
            @Override
            public void onLastStage() {
                mBtnExit.setVisibility(View.GONE);
            }

            @Override
            public void onDone() {
                TutorialUtil.setHasDoneTutorial();
                exitTutorial();
            }
        });
    }

    private void exitTutorial() {
        mManager.quitService();
        DialogFlowForegroundService.processStopTutorial();
        finish();
    }
}
