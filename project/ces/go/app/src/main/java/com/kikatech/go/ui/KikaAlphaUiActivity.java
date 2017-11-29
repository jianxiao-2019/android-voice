package com.kikatech.go.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.services.DialogFlowForegroundService;
import com.kikatech.go.services.DialogFlowForegroundService.SendBroadcastInfos;
import com.kikatech.go.view.GoLayout;
import com.kikatech.go.view.UiTaskManager;
import com.kikatech.go.view.UiTaskManager.DebugLogType;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.util.contact.ContactManager;

/**
 * @author SkeeterWang Created on 2017/11/10.
 */
public class KikaAlphaUiActivity extends BaseActivity {
    private static final String TAG = "KikaAlphaUiActivity";

    private GoLayout mGoLayout;
    private UiTaskManager mUiManager;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleReceive(context, intent);
        }

        private void handleReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            Bundle extras;
            String text, scene, sceneAction;
            SceneStage stage;
            boolean isFinished, isInterrupted;
            switch (action) {
                case SendBroadcastInfos.ACTION_ON_DIALOG_FLOW_INIT:
                    mGoLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            initUiTaskManager();
                            mUiManager.dispatchDefaultOptionsTask();
                            mUiManager.writeDebugLog(DebugLogType.ASR_LISTENING);
                        }
                    }, 800);
                    break;
                case SendBroadcastInfos.ACTION_ON_ASR_RESULT:
                    text = intent.getStringExtra(SendBroadcastInfos.PARAM_TEXT);
                    isFinished = intent.getBooleanExtra(SendBroadcastInfos.PARAM_IS_FINISHED, false);
                    String concat = String.valueOf(text.charAt(0)).toUpperCase() +
                            text.substring(1, text.length());
                    mUiManager.dispatchSpeechTask(concat, isFinished);
                    if (isFinished) {
                        mUiManager.writeDebugLog(DebugLogType.ASR_STOP);
                    } else {
                        mUiManager.writeDebugLog(DebugLogType.ASR_LISTENING);
                    }
                    break;
                case SendBroadcastInfos.ACTION_ON_TEXT:
                    text = intent.getStringExtra(SendBroadcastInfos.PARAM_TEXT);
                    extras = intent.getBundleExtra(SendBroadcastInfos.PARAM_EXTRAS);
                    mUiManager.dispatchTtsTask(text, extras);
                    break;
                case SendBroadcastInfos.ACTION_ON_TEXT_PAIRS:
                    text = intent.getStringExtra(SendBroadcastInfos.PARAM_TEXT);
                    extras = intent.getBundleExtra(SendBroadcastInfos.PARAM_EXTRAS);
                    mUiManager.dispatchTtsTask(text, extras);
                    break;
                case SendBroadcastInfos.ACTION_ON_STAGE_PREPARED:
                    stage = (SceneStage) intent.getSerializableExtra(SendBroadcastInfos.PARAM_SCENE_STAGE);
                    mUiManager.dispatchStageTask(stage);
                    break;
                case SendBroadcastInfos.ACTION_ON_STAGE_ACTION_DONE:
                    isInterrupted = intent.getBooleanExtra(SendBroadcastInfos.PARAM_IS_INTERRUPTED, false);
                    mUiManager.onStageActionDone(isInterrupted);
                    break;
                case SendBroadcastInfos.ACTION_ON_STAGE_EVENT:
                    extras = intent.getBundleExtra(SendBroadcastInfos.PARAM_EXTRAS);
                    mUiManager.dispatchEventTask(extras);
                    break;
                case SendBroadcastInfos.ACTION_ON_SCENE_EXIT:
                    mUiManager.onSceneExit();
                    break;
                case SendBroadcastInfos.ACTION_ON_AGENT_QUERY_START:
                    mUiManager.writeDebugLog(DebugLogType.API_AI_START);
                    break;
                case SendBroadcastInfos.ACTION_ON_AGENT_QUERY_STOP:
                    mUiManager.writeDebugLog(DebugLogType.API_AI_STOP);
                    break;
                case SendBroadcastInfos.ACTION_ON_AGENT_QUERY_ERROR:
                    mUiManager.writeDebugLog(DebugLogType.API_AI_ERROR);
                    break;
            }
        }
    };

    /**
     * must called after GoLayout and DialogFlowService initialized
     **/
    private void initUiTaskManager() {
        mUiManager = new UiTaskManager(mGoLayout, new UiTaskManager.IUiManagerFeedback() {
            @Override
            public void onOptionSelected(byte requestType, int index, Option option) {
                String textToSend;
                switch (requestType) {
                    case OptionList.REQUEST_TYPE_ORDINAL:
                        textToSend = String.valueOf(index + 1);
                        DialogFlowForegroundService.processDialogFlowTalk(KikaAlphaUiActivity.this, textToSend);
//                        mDialogFlowService.text(textToSend);
                        mUiManager.dispatchSpeechTask(textToSend);
                        break;
                    case OptionList.REQUEST_TYPE_TEXT:
                        textToSend = option.getDisplayText();
                        DialogFlowForegroundService.processDialogFlowTalk(KikaAlphaUiActivity.this, textToSend);
//                        mDialogFlowService.text(textToSend);
                        mUiManager.dispatchSpeechTask(textToSend);
                        break;
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kika_alpha_ui);
        bindView();
        // TODO fine tune init timing
        ContactManager.getIns().init(this);
        registerReceiver();
        DialogFlowForegroundService.processStart(KikaAlphaUiActivity.this, DialogFlowForegroundService.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(KikaAlphaUiActivity.this).unregisterReceiver(mReceiver);
        DialogFlowForegroundService.processStop(KikaAlphaUiActivity.this, DialogFlowForegroundService.class);
    }

    private void bindView() {
        mGoLayout = (GoLayout) findViewById(R.id.go_layout);
        mGoLayout.awake();
    }

    private void registerReceiver() {
        LocalBroadcastManager.getInstance(KikaAlphaUiActivity.this).registerReceiver(mReceiver, new IntentFilter(SendBroadcastInfos.ACTION_ON_DIALOG_FLOW_INIT));
        LocalBroadcastManager.getInstance(KikaAlphaUiActivity.this).registerReceiver(mReceiver, new IntentFilter(SendBroadcastInfos.ACTION_ON_ASR_RESULT));
        LocalBroadcastManager.getInstance(KikaAlphaUiActivity.this).registerReceiver(mReceiver, new IntentFilter(SendBroadcastInfos.ACTION_ON_TEXT));
        LocalBroadcastManager.getInstance(KikaAlphaUiActivity.this).registerReceiver(mReceiver, new IntentFilter(SendBroadcastInfos.ACTION_ON_TEXT_PAIRS));
        LocalBroadcastManager.getInstance(KikaAlphaUiActivity.this).registerReceiver(mReceiver, new IntentFilter(SendBroadcastInfos.ACTION_ON_STAGE_PREPARED));
        LocalBroadcastManager.getInstance(KikaAlphaUiActivity.this).registerReceiver(mReceiver, new IntentFilter(SendBroadcastInfos.ACTION_ON_STAGE_ACTION_DONE));
        LocalBroadcastManager.getInstance(KikaAlphaUiActivity.this).registerReceiver(mReceiver, new IntentFilter(SendBroadcastInfos.ACTION_ON_STAGE_EVENT));
        LocalBroadcastManager.getInstance(KikaAlphaUiActivity.this).registerReceiver(mReceiver, new IntentFilter(SendBroadcastInfos.ACTION_ON_SCENE_EXIT));
        LocalBroadcastManager.getInstance(KikaAlphaUiActivity.this).registerReceiver(mReceiver, new IntentFilter(SendBroadcastInfos.ACTION_ON_AGENT_QUERY_START));
        LocalBroadcastManager.getInstance(KikaAlphaUiActivity.this).registerReceiver(mReceiver, new IntentFilter(SendBroadcastInfos.ACTION_ON_AGENT_QUERY_STOP));
        LocalBroadcastManager.getInstance(KikaAlphaUiActivity.this).registerReceiver(mReceiver, new IntentFilter(SendBroadcastInfos.ACTION_ON_AGENT_QUERY_ERROR));
    }
}
