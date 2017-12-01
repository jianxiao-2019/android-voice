package com.kikatech.go.ui;

import android.os.Bundle;
import android.text.TextUtils;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.eventbus.DFServiceEvent;
import com.kikatech.go.services.DialogFlowForegroundService;
import com.kikatech.go.util.StringUtil;
import com.kikatech.go.view.GoLayout;
import com.kikatech.go.view.UiTaskManager;
import com.kikatech.go.view.UiTaskManager.DebugLogType;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.util.contact.ContactManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author SkeeterWang Created on 2017/11/10.
 */
public class KikaAlphaUiActivity extends BaseActivity {
    private static final String TAG = "KikaAlphaUiActivity";

    private GoLayout mGoLayout;
    private UiTaskManager mUiManager;

    /**
     * <p>Reflection subscriber method used by EventBus,
     * <p>do not remove this except the subscriber is no longer needed.
     *
     * @param event event from {@link com.kikatech.go.services.DialogFlowForegroundService}
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServiceEvent(DFServiceEvent event) {
        if (event == null) {
            return;
        }
        String action = event.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        Bundle extras;
        String text, scene, sceneAction;
        SceneStage stage;
        boolean isFinished, isInterrupted;
        switch (action) {
            case DFServiceEvent.ACTION_ON_DIALOG_FLOW_INIT:
                mGoLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initUiTaskManager();
                        mUiManager.dispatchDefaultOptionsTask();
                        mUiManager.writeDebugLog(DebugLogType.ASR_LISTENING);
                    }
                }, 800);
                break;
            case DFServiceEvent.ACTION_ON_ASR_RESULT:
                text = event.getExtras().getString(DFServiceEvent.PARAM_TEXT);
                isFinished = event.getExtras().getBoolean(DFServiceEvent.PARAM_IS_FINISHED, false);
                String concat = StringUtil.upperCaseFirstWord(text);
                mUiManager.dispatchSpeechTask(concat, isFinished);
                if (isFinished) {
                    mUiManager.writeDebugLog(DebugLogType.ASR_STOP);
                } else {
                    mUiManager.writeDebugLog(DebugLogType.ASR_LISTENING);
                }
                break;
            case DFServiceEvent.ACTION_ON_TEXT:
                text = event.getExtras().getString(DFServiceEvent.PARAM_TEXT);
                extras = event.getExtras().getBundle(DFServiceEvent.PARAM_EXTRAS);
                mUiManager.dispatchTtsTask(text, extras);
                break;
            case DFServiceEvent.ACTION_ON_TEXT_PAIRS:
                text = event.getExtras().getString(DFServiceEvent.PARAM_TEXT);
                extras = event.getExtras().getBundle(DFServiceEvent.PARAM_EXTRAS);
                mUiManager.dispatchTtsTask(text, extras);
                break;
            case DFServiceEvent.ACTION_ON_STAGE_PREPARED:
                stage = (SceneStage) event.getExtras().getSerializable(DFServiceEvent.PARAM_SCENE_STAGE);
                mUiManager.dispatchStageTask(stage);
                break;
            case DFServiceEvent.ACTION_ON_STAGE_ACTION_DONE:
                isInterrupted = event.getExtras().getBoolean(DFServiceEvent.PARAM_IS_INTERRUPTED, false);
                mUiManager.onStageActionDone(isInterrupted);
                break;
            case DFServiceEvent.ACTION_ON_STAGE_EVENT:
                extras = event.getExtras().getBundle(DFServiceEvent.PARAM_EXTRAS);
                mUiManager.dispatchEventTask(extras);
                break;
            case DFServiceEvent.ACTION_ON_SCENE_EXIT:
                mUiManager.onSceneExit();
                break;
            case DFServiceEvent.ACTION_ON_AGENT_QUERY_START:
                mUiManager.writeDebugLog(DebugLogType.API_AI_START);
                break;
            case DFServiceEvent.ACTION_ON_AGENT_QUERY_STOP:
                mUiManager.writeDebugLog(DebugLogType.API_AI_STOP);
                break;
            case DFServiceEvent.ACTION_ON_AGENT_QUERY_ERROR:
                mUiManager.writeDebugLog(DebugLogType.API_AI_ERROR);
                break;
        }
    }

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
                        mUiManager.dispatchSpeechTask(textToSend);
                        break;
                    case OptionList.REQUEST_TYPE_TEXT:
                        textToSend = option.getDisplayText();
                        DialogFlowForegroundService.processDialogFlowTalk(KikaAlphaUiActivity.this, textToSend);
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
        EventBus.getDefault().register(this);
        DialogFlowForegroundService.processStart(KikaAlphaUiActivity.this, DialogFlowForegroundService.class);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        DialogFlowForegroundService.processStop(KikaAlphaUiActivity.this, DialogFlowForegroundService.class);
        super.onDestroy();
    }

    private void bindView() {
        mGoLayout = (GoLayout) findViewById(R.id.go_layout);
        mGoLayout.awake();
    }
}
