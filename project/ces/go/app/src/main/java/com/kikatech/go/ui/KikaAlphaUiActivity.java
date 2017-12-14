package com.kikatech.go.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.eventbus.DFServiceEvent;
import com.kikatech.go.navigation.location.LocationMgr;
import com.kikatech.go.services.DialogFlowForegroundService;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.StringUtil;
import com.kikatech.go.view.GoLayout;
import com.kikatech.go.view.UiTaskManager;
import com.kikatech.go.view.UiTaskManager.DebugLogType;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.core.hotword.SnowBoyConfig;
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

    private boolean mDbgLogFirstAsrResult = false;
    private boolean mIsAsrFinished = false;
    private long mDbgLogAPIQueryUITime = 0;
    private long mDbgLogASRRecogStartTime = 0;
    private long mDbgLogResumeStartTime = 0;
    private long mDbgLogASRRecogFullTime = 0;

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
        String dbgAction = "[" + action.replace("action_on_", "") + "]";
        switch (action) {
            case DFServiceEvent.ACTION_EXIT_APP:
                if(GoLayout.ENABLE_LOG_VIEW) mUiManager.writeDebugLog(dbgAction, "Exit App, Goodbye !");
                finishAffinity();
                break;
            case DFServiceEvent.ACTION_ON_DIALOG_FLOW_INIT:
                initUiTaskManager();
                if(GoLayout.ENABLE_LOG_VIEW) mUiManager.writeDebugLog(dbgAction, "init UI Done");
                break;
            case DFServiceEvent.ACTION_ON_WAKE_UP:
                if(GoLayout.ENABLE_LOG_VIEW) mUiManager.writeDebugLog(dbgAction, "Hi Kika Wake Up");
                mUiManager.dispatchWakeUp();
                if(GoLayout.ENABLE_LOG_VIEW) mUiManager.writeDebugLog(DebugLogType.ASR_LISTENING);
                break;
            case DFServiceEvent.ACTION_ON_SLEEP:
                if(GoLayout.ENABLE_LOG_VIEW) mUiManager.writeDebugLog(dbgAction, "Hi Kika Sleep");
                mUiManager.dispatchSleep();
                break;
            case DFServiceEvent.ACTION_ON_ASR_PAUSE:
                if(GoLayout.ENABLE_LOG_VIEW) {
                    long spend = System.currentTimeMillis() - mDbgLogResumeStartTime;
                    int per = (int) (100 * ((float)mDbgLogASRRecogFullTime / spend));
                    mUiManager.writeDebugLog(dbgAction, "asr section over (" + spend + " ms, " + per + "%)");
                }
                break;
            case DFServiceEvent.ACTION_ON_ASR_RESUME:
                if(GoLayout.ENABLE_LOG_VIEW) {
                    mUiManager.writeDebugLogSeparator();
                    mDbgLogResumeStartTime = System.currentTimeMillis();
                    mUiManager.writeDebugLog(dbgAction, "asr section start");
                }
                break;
            case DFServiceEvent.ACTION_ON_ASR_RESULT:
                text = event.getExtras().getString(DFServiceEvent.PARAM_TEXT);
                isFinished = event.getExtras().getBoolean(DFServiceEvent.PARAM_IS_FINISHED, false);
                String concat = StringUtil.upperCaseFirstWord(text);

                if(GoLayout.ENABLE_LOG_VIEW) {
                    mIsAsrFinished = isFinished;
                    if (!mDbgLogFirstAsrResult) {
                        mDbgLogFirstAsrResult = true;
                        mDbgLogASRRecogStartTime = System.currentTimeMillis();
                    }
                    String finishMsg = isFinished ? "[OK]" : "";
                    mDbgLogASRRecogFullTime = System.currentTimeMillis() - mDbgLogASRRecogStartTime;
                    String spendTime = " (" + mDbgLogASRRecogFullTime + " ms)";
                    mUiManager.writeDebugLog(dbgAction + finishMsg, concat + spendTime);
                }

                mUiManager.dispatchSpeechTask(concat, isFinished);
                break;
            case DFServiceEvent.ACTION_ON_TEXT:
                text = event.getExtras().getString(DFServiceEvent.PARAM_TEXT);
                extras = event.getExtras().getBundle(DFServiceEvent.PARAM_EXTRAS);
                mUiManager.dispatchTtsTask(text, extras);
                if(GoLayout.ENABLE_LOG_VIEW) mUiManager.writeDebugLog(dbgAction, text);
                break;
            case DFServiceEvent.ACTION_ON_TEXT_PAIRS:
                text = event.getExtras().getString(DFServiceEvent.PARAM_TEXT);
                extras = event.getExtras().getBundle(DFServiceEvent.PARAM_EXTRAS);
                if(GoLayout.ENABLE_LOG_VIEW) mUiManager.writeDebugLog(dbgAction, text);
                mUiManager.dispatchTtsTask(text, extras);
                break;
            case DFServiceEvent.ACTION_ON_STAGE_PREPARED:
                stage = (SceneStage) event.getExtras().getSerializable(DFServiceEvent.PARAM_SCENE_STAGE);
                if(GoLayout.ENABLE_LOG_VIEW) mUiManager.writeDebugLog(dbgAction, stage.toString());
                mUiManager.dispatchStageTask(stage);
                break;
            case DFServiceEvent.ACTION_ON_STAGE_ACTION_DONE:
                isInterrupted = event.getExtras().getBoolean(DFServiceEvent.PARAM_IS_INTERRUPTED, false);
                if(GoLayout.ENABLE_LOG_VIEW) mUiManager.writeDebugLog(dbgAction, "isInterrupted:" + isInterrupted);
                mUiManager.onStageActionDone(isInterrupted);
                break;
            case DFServiceEvent.ACTION_ON_STAGE_EVENT:
                extras = event.getExtras().getBundle(DFServiceEvent.PARAM_EXTRAS);
                if(GoLayout.ENABLE_LOG_VIEW) mUiManager.writeDebugLog(dbgAction, "Parameters:" + extras);
                mUiManager.dispatchEventTask(extras);
                break;
            case DFServiceEvent.ACTION_ON_SCENE_EXIT:
                boolean proactive = event.getExtras().getBoolean(DFServiceEvent.PARAM_IS_PROACTIVE);
                if(GoLayout.ENABLE_LOG_VIEW) mUiManager.writeDebugLog(dbgAction, "proactive:" + proactive);
                mUiManager.onSceneExit(proactive);
                break;
            case DFServiceEvent.ACTION_ON_AGENT_QUERY_START:
                mUiManager.dispatchAsrStart();
                mDbgLogAPIQueryUITime = System.currentTimeMillis();
                if(GoLayout.ENABLE_LOG_VIEW) {
                    mUiManager.writeDebugLogSeparator();
                    mUiManager.writeDebugLog(dbgAction, "");
                }
                break;
            case DFServiceEvent.ACTION_ON_AGENT_QUERY_COMPLETE:
                if(GoLayout.ENABLE_LOG_VIEW) {
                    mDbgLogAPIQueryUITime = System.currentTimeMillis() - mDbgLogAPIQueryUITime;
                    String intentAction = event.getExtras().getString(DFServiceEvent.PARAM_DBG_INTENT_ACTION);
                    String intentParms = event.getExtras().getString(DFServiceEvent.PARAM_DBG_INTENT_PARMS);
                    mUiManager.writeDebugLog(dbgAction + " (" + mDbgLogAPIQueryUITime + "ms)", "\n" + intentAction + "\n" + intentParms);
                    mUiManager.writeDebugLogSeparator();
                }
                break;
            case DFServiceEvent.ACTION_ON_AGENT_QUERY_ERROR:
                if(GoLayout.ENABLE_LOG_VIEW) {
                    mUiManager.writeDebugLog(DebugLogType.API_AI_ERROR);
                    mUiManager.writeDebugLogSeparator();
                    mUiManager.writeDebugLog(dbgAction, "");
                }
                break;
            case DFServiceEvent.ACTION_ON_ASR_CONFIG:
                if(GoLayout.ENABLE_LOG_VIEW) {
                    text = event.getExtras().getString(DFServiceEvent.PARAM_TEXT);
                    mUiManager.writeDebugLogSeparator();
                    mUiManager.writeDebugLog(dbgAction, text);
                    mUiManager.writeDebugLogSeparator();
                }
                break;
        }

        if (GoLayout.ENABLE_LOG_VIEW && mIsAsrFinished) {
            mDbgLogFirstAsrResult = false;
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
                        DialogFlowForegroundService.processDialogFlowTalk(textToSend);
                        mUiManager.dispatchSpeechTask(textToSend);
                        break;
                    case OptionList.REQUEST_TYPE_TEXT:
                        textToSend = option.getDisplayText();
                        DialogFlowForegroundService.processDialogFlowTalk(textToSend);
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
        LocationMgr.init(this);
        registerReceivers();
        DialogFlowForegroundService.processStart(KikaAlphaUiActivity.this, DialogFlowForegroundService.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DialogFlowForegroundService.processOnAppForeground();

        if(LogUtil.DEBUG) {
            String sen = SnowBoyConfig.getSensitivity();
            Toast.makeText(this, "Snowboy Sensitivity : " + sen, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        DialogFlowForegroundService.processOnAppBackground();
    }

    @Override
    protected void onDestroy() {
        if (mUiManager != null) mUiManager.release();
        unregisterReceivers();
        DialogFlowForegroundService.processStop(KikaAlphaUiActivity.this, DialogFlowForegroundService.class);
        super.onDestroy();
    }

    private void bindView() {
        mGoLayout = (GoLayout) findViewById(R.id.go_layout);
    }

    private void registerReceivers() {
        try {
            EventBus.getDefault().register(this);
        } catch (Exception ignore) {
        }
    }

    private void unregisterReceivers() {
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception ignore) {
        }
    }
}
