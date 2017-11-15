package com.kikatech.go.ui;

import android.os.Bundle;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.go.dialogflow.BaseSceneStage;
import com.kikatech.go.dialogflow.DialogFlowConfig;
import com.kikatech.go.dialogflow.navigation.NaviSceneManager;
import com.kikatech.go.dialogflow.stop.SceneStopIntentManager;
import com.kikatech.go.dialogflow.telephony.TelephonySceneManager;
import com.kikatech.go.view.GoLayout;
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

    private IDialogFlowService mDialogFlowService;
    private final List<BaseSceneManager> mSceneManagers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kika_alpha_ui);
        bindView();
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

    private void bindView() {
        mGoLayout = (GoLayout) findViewById(R.id.go_layout);
    }

    private void initDialogFlowService() {

        mDialogFlowService = DialogFlowService.queryService(this,
                DialogFlowConfig.queryDemoConfig(this),
                new IDialogFlowService.IServiceCallback() {
                    @Override
                    public void onInitComplete() {
                        // Register all scenes from scene mangers
                    }

                    @Override
                    public void onSpeechSpokenDone(final String speechText) {
                        listenOnLayout(speechText);
                    }

                    @Override
                    public void onText(String text, Bundle extras) {
                        BaseSceneStage.OptionList optionList = null;
                        String title = text;
                        if (extras != null && extras.containsKey(BaseSceneStage.EXTRA_OPTIONS_LIST)) {
                            optionList = extras.getParcelable(BaseSceneStage.EXTRA_OPTIONS_LIST);
                            title = extras.getString(BaseSceneStage.EXTRA_OPTIONS_TITLE);
                        }
                        if (optionList != null && !optionList.isEmpty()) {
                            displayOptionOnLayout(title, optionList);
                        } else {
                            speakOnLayout(title);
                        }
                    }
                });

        // Register all scenes from scene mangers
        mSceneManagers.add(new TelephonySceneManager(this, mDialogFlowService));
        mSceneManagers.add(new NaviSceneManager(this, mDialogFlowService));
        mSceneManagers.add(new SceneStopIntentManager(this, mDialogFlowService));
    }

    private void speakOnLayout(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGoLayout.speak(text);
            }
        });
    }

    private void listenOnLayout(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGoLayout.listen(text);
            }
        });
    }

    private void displayOptionOnLayout(final String text, final BaseSceneStage.OptionList optionList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGoLayout.displayOptions(text, optionList, new GoLayout.IOnOptionSelectListener() {
                    @Override
                    public void onSelected(byte requestType, BaseSceneStage.Option option) {
                        switch (requestType) {
                            case BaseSceneStage.OptionList.REQUEST_TYPE_ORDINAL:
                                mDialogFlowService.talk(String.valueOf(optionList.indexOf(option) + 1));
                                break;
                            case BaseSceneStage.OptionList.REQUEST_TYPE_TEXT:
                                mDialogFlowService.talk(option.getDisplayText());
                                break;
                        }
                    }
                });
            }
        });
    }
}
