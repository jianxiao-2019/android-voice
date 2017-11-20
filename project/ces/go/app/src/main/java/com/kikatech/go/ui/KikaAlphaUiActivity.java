package com.kikatech.go.ui;

import android.os.Bundle;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.go.dialogflow.DialogFlowConfig;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.dialogflow.navigation.NaviSceneManager;
import com.kikatech.go.dialogflow.sms.SmsSceneManager;
import com.kikatech.go.dialogflow.stop.SceneStopIntentManager;
import com.kikatech.go.dialogflow.telephony.TelephonySceneManager;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.view.GoLayout;
import com.kikatech.go.view.UiTaskManager;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.service.DialogFlowService;
import com.kikatech.voice.service.IDialogFlowService;

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
    }

    private void bindView() {
        mGoLayout =(GoLayout) findViewById(R.id.go_layout);
        mGoLayout.sleep();
        mGoLayout.displayOptions(OptionList.getSleepOptionList(), new GoLayout.IOnOptionSelectListener() {
            @Override
            public void onSelected(byte requestType, int index, Option option) {
                switch (requestType){
                    case OptionList.REQUEST_TYPE_AWAKE:
                        initDialogFlowService();
                        break;
                }
            }
        });
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
                                mGoLayout.awake();
                                mUiManager.dispatchDefaultOptionsTask();
                            }
                        }, 800);
                    }

                    @Override
                    public void onSpeechSpokenDone(final String speechText) {
                        mUiManager.dispatchSpeechTask(speechText);
                    }

                    @Override
                    public void onText(String text, Bundle extras) {
                        mUiManager.dispatchTtsTask(text, extras);
                    }

                    @Override
                    public void onSceneExit(String scene) {
                        mUiManager.dispatchDefaultOptionsTask();
                    }

                    @Override
                    public void onStagePrepared(String scene, String action, SceneStage stage) {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "scene: " + scene + ", action: " + action + ", stage: " + stage.getClass().getSimpleName());
                        }
                        mUiManager.dispatchStageTask(stage);
                    }

                    @Override
                    public void onStageActionDone() {
                        mUiManager.onStageActionDone();
                    }
                });

        // Register all scenes from scene mangers
        mSceneManagers.add(new TelephonySceneManager(this, mDialogFlowService));
        mSceneManagers.add(new NaviSceneManager(this, mDialogFlowService));
        mSceneManagers.add(new SceneStopIntentManager(this, mDialogFlowService));
        mSceneManagers.add(new SmsSceneManager(this, mDialogFlowService));
    }

    /**
     * must called after GoLayout and DialogFlowService initialized
     **/
    private void initUiTaskManager() {
        mUiManager = new UiTaskManager(mGoLayout, new UiTaskManager.IUiManagerFeedback() {
            @Override
            public void onOptionSelected(byte requestType, int index, Option option) {
                switch (requestType) {
                    case OptionList.REQUEST_TYPE_ORDINAL:
                        mDialogFlowService.text(String.valueOf(index + 1));
                        break;
                    case OptionList.REQUEST_TYPE_TEXT:
                        mDialogFlowService.text(option.getDisplayText());
                        break;
                }
            }
        });
    }
}
