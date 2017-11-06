package com.kikatech.go.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.kikatech.go.R;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.VoiceConfiguration;
import com.kikatech.voice.core.dialogflow.DialogFlow;
import com.kikatech.voice.core.dialogflow.DialogObserver;
import com.kikatech.voice.core.dialogflow.agent.apiai.ApiAiAgentCreator;
import com.kikatech.voice.core.dialogflow.constant.Scene;
import com.kikatech.voice.core.dialogflow.intent.Intent;

/**
 * @author SkeeterWang Created on 2017/11/4.
 */
public class KikaDialogFlowActivity extends BaseActivity {

    private static final String TAG = "KikaDialogFlowActivity";

    private EditText mWordsInput;
    private View mBtnResetContexts;
    private View mBtnQuery;
    private TextView mTvScene;
    private TextView mTvName;
    private TextView mTvAction;
    private TextView mTvExtras;


    private DialogFlow mDialogFlow;
    private DialogObserver mDialogObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kika_dialog_flow);
        initDialogFlow();
        bindView();
    }

    private void initDialogFlow()
    {
        VoiceConfiguration config = new VoiceConfiguration();
        config.agent(new ApiAiAgentCreator());
        mDialogFlow = DialogFlow.getInstance(KikaDialogFlowActivity.this, config);
        mDialogObserver = new DialogObserver() {
            @Override
            public void onIntent(Intent intent) {
                if (intent != null) {
                    if (LogUtil.DEBUG) {
                        LogUtil.log(TAG, "===== START ON INTENT =====");
                        LogUtil.log(TAG, "Scene: " + intent.getScene());
                        LogUtil.log(TAG, "Name: " + intent.getName());
                        LogUtil.log(TAG, "Action: " + intent.getAction());
                        Bundle args = intent.getExtra();
                        if (args != null && !args.isEmpty()) {
                            for (String key : args.keySet()) {
                                LogUtil.log(TAG, "[params] " + key + ": " + args.getString(key));
                            }
                        }
                        LogUtil.log(TAG, "===== STOP ON INTENT =====");
                    }
                    handleLog(intent);
                }
            }
        };
        mDialogFlow.register(Scene.DEFAULT.toString(), mDialogObserver);
        mDialogFlow.register(Scene.NAVIGATION.toString(), mDialogObserver);
    }

    private void handleLog(final Intent intent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resetLogs();
                if (intent != null) {
                    mTvScene.setText(intent.getScene());
                    mTvName.setText(intent.getName());
                    mTvAction.setText(intent.getAction());
                    Bundle args = intent.getExtra();
                    if (args != null && !args.isEmpty()) {
                        String extras = "";
                        for (String key : args.keySet()) {
                            extras = extras + key + ": " + args.getString(key) + "\n";
                        }
                        mTvExtras.setText(extras);
                    }
                }
            }
        });
    }

    private void resetLogs()
    {
        mTvScene.setText("");
        mTvName.setText("");
        mTvAction.setText("");
        mTvExtras.setText("");
    }

    private void bindView()
    {
        mWordsInput = (EditText) findViewById(R.id.edit_words);

        mBtnResetContexts = findViewById(R.id.btn_reset_contexts);
        mBtnQuery = findViewById(R.id.btn_query);

        mTvScene = (TextView) findViewById(R.id.log_tv_scene);
        mTvName = (TextView) findViewById(R.id.log_tv_name);
        mTvAction = (TextView) findViewById(R.id.log_tv_action);
        mTvExtras = (TextView) findViewById(R.id.log_tv_extras);

        mBtnResetContexts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogFlow.resetContexts();
            }
        });

        mBtnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String words = mWordsInput.getText().toString();
                if (!TextUtils.isEmpty(words)) {
                    mDialogFlow.talk(words);
                }
            }
        });
    }
}
