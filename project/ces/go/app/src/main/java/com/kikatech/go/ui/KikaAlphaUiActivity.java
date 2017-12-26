package com.kikatech.go.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.View;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.eventbus.DFServiceEvent;
import com.kikatech.go.navigation.location.LocationMgr;
import com.kikatech.go.services.DialogFlowForegroundService;
import com.kikatech.go.ui.fragment.DrawerImFragment;
import com.kikatech.go.ui.fragment.DrawerMainFragment;
import com.kikatech.go.ui.fragment.DrawerNavigationFragment;
import com.kikatech.go.util.StringUtil;
import com.kikatech.go.view.GoLayout;
import com.kikatech.go.view.UiTaskManager;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.util.contact.ContactManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author SkeeterWang Created on 2017/11/10.
 */
public class KikaAlphaUiActivity extends BaseDrawerActivity {
    private static final String TAG = "KikaAlphaUiActivity";

    private GoLayout mGoLayout;
    private UiTaskManager mUiManager;
    private View mBtnOpenDrawer;


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
                finishAffinity();
                break;
            case DFServiceEvent.ACTION_ON_DIALOG_FLOW_INIT:
                initUiTaskManager();
                break;
            case DFServiceEvent.ACTION_ON_WAKE_UP:
                mUiManager.dispatchWakeUp();
                break;
            case DFServiceEvent.ACTION_ON_SLEEP:
                mUiManager.dispatchSleep();
                break;
            case DFServiceEvent.ACTION_ON_ASR_PAUSE:
                break;
            case DFServiceEvent.ACTION_ON_ASR_RESUME:
                break;
            case DFServiceEvent.ACTION_ON_ASR_RESULT:
                text = event.getExtras().getString(DFServiceEvent.PARAM_TEXT);
                isFinished = event.getExtras().getBoolean(DFServiceEvent.PARAM_IS_FINISHED, false);
                String concat = StringUtil.upperCaseFirstWord(text);
                mUiManager.dispatchSpeechTask(concat, isFinished);
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
                int bosDuration = event.getExtras().getInt(DFServiceEvent.PARAM_BOS_DURATION, 0);
                mUiManager.onStageActionDone(isInterrupted, bosDuration);
                break;
            case DFServiceEvent.ACTION_ON_STAGE_EVENT:
                extras = event.getExtras().getBundle(DFServiceEvent.PARAM_EXTRAS);
                mUiManager.dispatchEventTask(extras);
                break;
            case DFServiceEvent.ACTION_ON_SCENE_EXIT:
                boolean proactive = event.getExtras().getBoolean(DFServiceEvent.PARAM_IS_PROACTIVE);
                mUiManager.onSceneExit(proactive);
                break;
            case DFServiceEvent.ACTION_ON_AGENT_QUERY_START:
                mUiManager.dispatchAsrStart();
                break;
            case DFServiceEvent.ACTION_ON_AGENT_QUERY_COMPLETE:
                break;
            case DFServiceEvent.ACTION_ON_AGENT_QUERY_ERROR:
                break;
            case DFServiceEvent.ACTION_ON_ASR_CONFIG:
                break;
            case DFServiceEvent.ACTION_ON_VOICE_SRC_CHANGE:
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

            @Override
            public void onLayoutModeChanged(GoLayout.DisplayMode mode) {
                switch (mode) {
                    case AWAKE:
                        mBtnOpenDrawer.setVisibility(View.GONE);
                        break;
                    case SLEEP:
                        mBtnOpenDrawer.setVisibility(View.VISIBLE);
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

//        if(LogUtil.DEBUG) {
//            String sen = SnowBoyConfig.getSensitivity();
//            Toast.makeText(this, "Snowboy Sensitivity : " + sen, Toast.LENGTH_SHORT).show();
//        }
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
        mBtnOpenDrawer = findViewById(R.id.go_layout_btn_open_drawer);
        mBtnOpenDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer();
            }
        });
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

    @Override
    protected DrawerLayout getDrawerLayout() {
        return (DrawerLayout) findViewById(R.id.go_drawer_layout);
    }

    @Override
    protected View getDrawerView() {
        return findViewById(R.id.go_layout_drawer);
    }

    @Override
    protected Fragment getMainDrawerFragment() {
        return mDrawerMainFragment;
    }

    private Fragment mDrawerMainFragment = DrawerMainFragment.newInstance(new DrawerMainFragment.IDrawerMainListener() {
        @Override
        public void onCloseDrawer() {
            closeDrawer();
        }

        @Override
        public void onItemNavigationClick() {
            updateDrawerContent(mDrawerNavigationFragment);
        }

        @Override
        public void onItemImClicked() {
            updateDrawerContent(mDrawerImFragment);
        }

        @Override
        public void onItemExitClicked() {
            finishAffinity();
        }
    });

    private Fragment mDrawerNavigationFragment = DrawerNavigationFragment.newInstance(new DrawerNavigationFragment.IDrawerNavigationListener() {
        @Override
        public void onBackClicked() {
            updateDrawerContent(mDrawerMainFragment);
        }
    });

    private Fragment mDrawerImFragment = DrawerImFragment.newInstance(new DrawerImFragment.IDrawerImListener() {
        @Override
        public void onBackClicked() {
            updateDrawerContent(mDrawerMainFragment);
        }
    });
}
