package com.kikatech.go.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.go.dialogflow.DialogFlowConfig;
import com.kikatech.go.dialogflow.navigation.NaviSceneManager;
import com.kikatech.go.dialogflow.stop.SceneStopIntentManager;
import com.kikatech.go.dialogflow.telephony.TelephonySceneManager;
import com.kikatech.voice.service.DialogFlowService;
import com.kikatech.voice.service.IDialogFlowService;

import java.util.ArrayList;
import java.util.List;


/**
 * @author SkeeterWang Created on 2017/11/4.
 */
public class KikaDialogFlowActivity extends BaseActivity {

    private static final String TAG = "KikaDialogFlowActivity";

    private EditText mWordsInput;
    private View mBtnResetContexts;
    private View mBtnQuery;
    private View mBtnResetAll;
    private View mBtnClearInputs;
    private TextView mTvScene;
    private TextView mTvName;
    private TextView mTvAction;
    private TextView mTvExtras;
    private View[] mInteractiveViews;

    private IDialogFlowService mDialogFlowService;
    private final List<BaseSceneManager> mSceneManagers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kika_dialog_flow);
        bindView();
        setViewEnable(false);

        initDialogFlowService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (BaseSceneManager bcm : mSceneManagers) {
            if (bcm != null) bcm.close();
        }
        if (mDialogFlowService != null) {
            mDialogFlowService.quitService();
        }
    }

    private void initDialogFlowService() {
        mDialogFlowService = DialogFlowService.queryService(this,
                DialogFlowConfig.queryDemoConfig(this),
                new IDialogFlowService.IServiceCallback() {
                    @Override
                    public void onInitComplete() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showLongToast("Dialog Service Init Completed");
                                setViewEnable(true);
                            }
                        });
                    }

                    @Override
                    public void onSpeechSpokenDone(final String speechText) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mWordsInput.setText(speechText);
                            }
                        });
                    }
                });

        // Register all scenes from scene mangers
        mSceneManagers.add(new TelephonySceneManager(this, mDialogFlowService));
        mSceneManagers.add(new NaviSceneManager(this, mDialogFlowService));
        mSceneManagers.add(new SceneStopIntentManager(this, mDialogFlowService));
    }

    private void resetLogs() {
        mTvScene.setText("");
        mTvName.setText("");
        mTvAction.setText("");
        mTvExtras.setText("");
    }

    private void bindView() {
        mWordsInput = (EditText) findViewById(R.id.edit_words);

        mBtnResetContexts = findViewById(R.id.btn_reset_contexts);
        mBtnQuery = findViewById(R.id.btn_query);
        mBtnResetAll = findViewById(R.id.btn_reset_all);
        mBtnClearInputs = findViewById(R.id.btn_clear_words);

        mTvScene = (TextView) findViewById(R.id.log_tv_scene);
        mTvName = (TextView) findViewById(R.id.log_tv_name);
        mTvAction = (TextView) findViewById(R.id.log_tv_action);
        mTvExtras = (TextView) findViewById(R.id.log_tv_extras);

        mBtnResetContexts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogFlowService.resetContexts();
            }
        });

        mBtnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String words = mWordsInput.getText().toString();
                if (!TextUtils.isEmpty(words)) {
                    mDialogFlowService.talk(words);
                }
            }
        });

        mBtnClearInputs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWordsInput.setText("");
            }
        });

        mBtnResetAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWordsInput.setText("");
                resetLogs();
                mDialogFlowService.resetContexts();
            }
        });

        mInteractiveViews = new View[]{mWordsInput, mBtnResetContexts, mBtnQuery, mBtnResetAll, mBtnClearInputs};
    }

    private void setViewEnable(boolean enable) {
        if (mInteractiveViews != null) {
            for (View v : mInteractiveViews) {
                v.setEnabled(enable);
            }
        }
    }
}