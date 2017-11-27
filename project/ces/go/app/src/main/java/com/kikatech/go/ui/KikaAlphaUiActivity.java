package com.kikatech.go.ui;

import android.os.Bundle;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.go.dialogflow.DialogFlowConfig;
import com.kikatech.go.dialogflow.im.IMSceneManager;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.dialogflow.navigation.NaviSceneManager;
import com.kikatech.go.dialogflow.sms.SmsSceneManager;
import com.kikatech.go.dialogflow.stop.SceneStopIntentManager;
import com.kikatech.go.dialogflow.telephony.TelephonySceneManager;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.view.GoLayout;
import com.kikatech.go.view.UiTaskManager;
import com.kikatech.go.view.UiTaskManager.DebugLogType;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.service.DialogFlowService;
import com.kikatech.voice.service.IDialogFlowService;
import com.kikatech.voice.util.contact.ContactManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SkeeterWang Created on 2017/11/10.
 */
public class KikaAlphaUiActivity extends BaseActivity {
    private static final String TAG = "KikaAlphaUiActivity";

    private GoLayout mGoLayout;
    private UiTaskManager mUiManager;
    private IDialogFlowService mDialogFlowService;
    private final List<BaseSceneManager> mSceneManagers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kika_alpha_ui);
        bindView();
        // TODO fine tune init timing
        ContactManager.getIns().init(this);
        //initDialogFlowService();
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
        if (mUiManager != null) {
            mUiManager.release();
        }
    }

    private void bindView() {
        mGoLayout = (GoLayout) findViewById(R.id.go_layout);
        mGoLayout.awake();
//        mGoLayout.setOnModeChangedListener(new GoLayout.IOnModeChangedListener() {
//            @Override
//            public void onChanged(GoLayout.DisplayMode mode) {
//                switch (mode) {
//                    case AWAKE:
//                        if (mDialogFlowService == null) {
//                            initDialogFlowService();
//                        }
//                        break;
//                    case SLEEP:
//                        break;
//                }
//            }
//        });
        initDialogFlowService();
    }

    private void initDialogFlowService() {

        mDialogFlowService = DialogFlowService.queryService(this,
                DialogFlowConfig.queryDemoConfig(this),
                new IDialogFlowService.IServiceCallback() {
                    @Override
                    public void onInitComplete() {
                        mGoLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                initUiTaskManager();
                                mUiManager.dispatchDefaultOptionsTask();
                                mUiManager.writeDebugLog(DebugLogType.ASR_LISTENING);
                            }
                        }, 800);
                    }

                    @Override
                    public void onASRResult(final String speechText, boolean isFinished) {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, String.format("speechText: %1$s, isFinished: %2$s", speechText, isFinished));
                        }
                        String concat =
                                String.valueOf(speechText.charAt(0)).toUpperCase() +
                                        speechText.substring(1, speechText.length());
                        mUiManager.dispatchSpeechTask(concat, isFinished);
                        if (isFinished) {
                            mUiManager.writeDebugLog(DebugLogType.ASR_STOP);
                        } else {
                            mUiManager.writeDebugLog(DebugLogType.ASR_LISTENING);
                        }
                    }

                    @Override
                    public void onText(String text, Bundle extras) {
                        mUiManager.dispatchTtsTask(text, extras);
                    }

                    @Override
                    public void onStagePrepared(String scene, String action, SceneStage stage) {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, String.format("scene: %1$s, action: %2$s, stage: %3$s", scene, action, stage.getClass().getSimpleName()));
                        }
                        mUiManager.dispatchStageTask(stage);
                    }

                    @Override
                    public void onStageActionDone(boolean isEndOfScene, boolean isInterrupted) {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, String.format("isEndOfScene: %1$s, isInterrupted: %2$s", isEndOfScene, isInterrupted));
                        }
                        mUiManager.onStageActionDone(isEndOfScene, isInterrupted);
                    }
                }, new IDialogFlowService.IAgentQueryStatus() {
                    @Override
                    public void onStart() {
                        if (LogUtil.DEBUG) LogUtil.log(TAG, "IAgentQueryStatus::onStart");
                        mUiManager.writeDebugLog(DebugLogType.API_AI_START);
                    }

                    @Override
                    public void onComplete() {
                        if (LogUtil.DEBUG) LogUtil.log(TAG, "IAgentQueryStatus::onComplete");
                        mUiManager.writeDebugLog(DebugLogType.API_AI_STOP);
                    }

                    @Override
                    public void onError(Exception e) {
                        if (LogUtil.DEBUG) LogUtil.log(TAG, "IAgentQueryStatus::onError" + e);
                        mUiManager.writeDebugLog(DebugLogType.API_AI_ERROR);
                    }
                });

        // Register all scenes from scene mangers
        mSceneManagers.add(new TelephonySceneManager(this, mDialogFlowService));
        mSceneManagers.add(new NaviSceneManager(this, mDialogFlowService));
        mSceneManagers.add(new SceneStopIntentManager(this, mDialogFlowService, KikaAlphaUiActivity.class));
        mSceneManagers.add(new SmsSceneManager(this, mDialogFlowService));
        mSceneManagers.add(new IMSceneManager(this, mDialogFlowService));
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
                        mDialogFlowService.text(textToSend);
                        mUiManager.dispatchSpeechTask(textToSend);
                        break;
                    case OptionList.REQUEST_TYPE_TEXT:
                        textToSend = option.getDisplayText();
                        mDialogFlowService.text(textToSend);
                        mUiManager.dispatchSpeechTask(textToSend);
                        break;
                }
            }
        });
    }
}
