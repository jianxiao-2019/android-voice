package com.kikatech.go.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.model.DFServiceStatus;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.eventbus.DFServiceEvent;
import com.kikatech.go.navigation.location.LocationMgr;
import com.kikatech.go.services.DialogFlowForegroundService;
import com.kikatech.go.ui.fragment.DrawerAdvancedFragment;
import com.kikatech.go.ui.fragment.DrawerImFragment;
import com.kikatech.go.ui.fragment.DrawerMainFragment;
import com.kikatech.go.ui.fragment.DrawerNavigationFragment;
import com.kikatech.go.ui.fragment.DrawerTipFragment;
import com.kikatech.go.util.AnimationUtils;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.NetworkUtil;
import com.kikatech.go.util.StringUtil;
import com.kikatech.go.util.dialog.DialogUtil;
import com.kikatech.go.util.preference.GlobalPref;
import com.kikatech.go.view.GoLayout;
import com.kikatech.go.view.UiTaskManager;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.util.CustomConfig;
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
    private ImageView mBtnOpenDrawer;
    private View mIconConnectionStatus;
    private View mIconUsbHardwareStatus;
    private View mIconUsbAttached;

    private boolean triggerDialogViaClick;

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
        boolean isFinished, isInterrupted, proactive;
        String dbgAction = "[" + action.replace("action_on_", "") + "]";
        switch (action) {
            case DFServiceEvent.ACTION_ON_CONNECTIVITY_CHANGED:
                onConnectivityChanged();
                break;
            case DFServiceEvent.ACTION_EXIT_APP:
                finishAffinity();
                break;
            case DFServiceEvent.ACTION_ON_USB_NO_DEVICES:
                if (!GlobalPref.getIns().getHasShowDialogUsbIllustration()) {
                    GlobalPref.getIns().setHasShowDialogUsbIllustration(true);
                    DialogUtil.showDialogAlertUsbInstallation(KikaAlphaUiActivity.this, null);
                } else if (triggerDialogViaClick) {
                    triggerDialogViaClick = false;
                    DialogUtil.showDialogAlertUsbInstallation(KikaAlphaUiActivity.this, null);
                }
                break;
            case DFServiceEvent.ACTION_ON_DIALOG_FLOW_INIT:
                break;
            case DFServiceEvent.ACTION_ON_WAKE_UP:
                String wakeUpFrom = event.getExtras().getString(DFServiceEvent.PARAM_WAKE_UP_FROM);
                mUiManager.dispatchWakeUp(wakeUpFrom);
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
                proactive = event.getExtras().getBoolean(DFServiceEvent.PARAM_IS_PROACTIVE);
                mUiManager.onSceneExit(proactive);
                break;
            case DFServiceEvent.ACTION_ON_AGENT_QUERY_START:
                proactive = event.getExtras().getBoolean(DFServiceEvent.PARAM_IS_PROACTIVE);
                mUiManager.dispatchAgentQueryStart(proactive);
                break;
            case DFServiceEvent.ACTION_ON_AGENT_QUERY_COMPLETE:
                break;
            case DFServiceEvent.ACTION_ON_AGENT_QUERY_ERROR:
                break;
            case DFServiceEvent.ACTION_ON_ASR_CONFIG:
                break;
            case DFServiceEvent.ACTION_ON_VOICE_SRC_CHANGE:
                String source = event.getExtras().getString(DFServiceEvent.PARAM_TEXT);
                boolean isUsbSource = DialogFlowForegroundService.VOICE_SOURCE_USB.equals(source);
                onUsbAttachedStatusChanged(isUsbSource);
                break;
            case DFServiceEvent.ACTION_ON_PING_SERVICE_STATUS:
                DFServiceStatus serviceStatus = (DFServiceStatus) event.getExtras().getSerializable(DFServiceEvent.PARAM_SERVICE_STATUS);
                if (serviceStatus != null && serviceStatus.isInit()) {
                    if (serviceStatus.isAwake()) {
                        mUiManager.dispatchWakeUp(null);
                    } else {
                        mUiManager.dispatchSleep();
                    }
                    Boolean isDataCorrect = serviceStatus.isUsbDeviceDataCorrect();
                    boolean isUsbDataCorrect = isDataCorrect == null || isDataCorrect;
                    if (LogUtil.DEBUG) {
                        LogUtil.logv(TAG, String.format("ACTION_ON_PING_SERVICE_STATUS, isUsbDataCorrect: %s", isUsbDataCorrect));
                    }
                    onUsbHardwareStatusChanged(isUsbDataCorrect);
                }
                break;
            case DFServiceEvent.ACTION_ON_USB_DEVICE_DATA_STATUS_CHANGED:
                boolean isUsbDataCorrect = event.getExtras().getBoolean(DFServiceEvent.PARAM_IS_USB_DEVICE_DATA_CORRECT);
                if (LogUtil.DEBUG) {
                    LogUtil.logv(TAG, String.format("ACTION_ON_USB_DEVICE_DATA_STATUS_CHANGED, isUsbDataCorrect: %s", isUsbDataCorrect));
                }
                onUsbHardwareStatusChanged(isUsbDataCorrect);
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
                        textToSend = option.getActionText();
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
        initUiTaskManager();
        DialogFlowForegroundService.processPingDialogFlowStatus();
        onConnectivityChanged();

        if (GlobalPref.getIns().isFirstLaunch()) {
            CustomConfig.removeAllCustomConfigFiles();
        }


//        if (LogUtil.DEBUG) {
//            String sen = CustomConfig.getSnowboySensitivity();
//            int timeout = CustomConfig.getKikaTtsServerTimeout();
//            String msg = "[config] Sensitivity: " + sen + " , Timeout: " + timeout + " ms";
//            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
//            LogUtil.log(TAG, msg);
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        DialogFlowForegroundService.processScanUsbDevices();
    }

    @Override
    protected void onDestroy() {
        if (mUiManager != null) {
            mUiManager.release();
        }
        unregisterReceivers();
        DialogFlowForegroundService.processStop(KikaAlphaUiActivity.this, DialogFlowForegroundService.class);
        super.onDestroy();
    }

    private void bindView() {
        mGoLayout = (GoLayout) findViewById(R.id.go_layout);
        mBtnOpenDrawer = (ImageView) findViewById(R.id.go_layout_btn_open_drawer);
        mIconConnectionStatus = findViewById(R.id.go_layout_ic_connection_status);
        mIconUsbHardwareStatus = findViewById(R.id.go_layout_ic_hardware_status);
        mIconUsbAttached = findViewById(R.id.go_layout_ic_usb_attached);

        mBtnOpenDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer();
            }
        });
        mIconConnectionStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.showDialogConnectionError(KikaAlphaUiActivity.this, null);
            }
        });
        mIconUsbHardwareStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.showDialogUsbHardwareError(KikaAlphaUiActivity.this, null);
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

    private synchronized void onConnectivityChanged() {
        boolean isNetworkAvailable = NetworkUtil.isNetworkAvailable(KikaAlphaUiActivity.this);
        if (isNetworkAvailable) {
            mIconConnectionStatus.clearAnimation();
            mIconConnectionStatus.setVisibility(View.GONE);
        } else {
            mIconUsbHardwareStatus.setVisibility(View.GONE);
            animateNetworkError();
        }
        mUiManager.dispatchConnectionStatusChanged(isNetworkAvailable);
    }

    private boolean isAnimating;

    private synchronized void animateNetworkError() {
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, String.format("isAnimating: %s", isAnimating));
        }
        if (isAnimating) {
            return;
        }
        isAnimating = true;
        mIconConnectionStatus.setVisibility(View.VISIBLE);
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(600);
        animation.setRepeatCount(2);
        AnimationUtils.getIns().animate(mIconConnectionStatus, animation, new AnimationUtils.IAnimationEndCallback() {
            @Override
            public void onEnd(View view) {
                isAnimating = false;
            }
        });
    }

    private void onUsbHardwareStatusChanged(boolean isUsbDataCorrect) {
        if (isUsbDataCorrect) {
            mIconUsbHardwareStatus.setVisibility(View.GONE);
        } else if (mIconConnectionStatus.getVisibility() != View.VISIBLE) {
            mIconUsbHardwareStatus.setVisibility(View.VISIBLE);
        }
    }

    private void onUsbAttachedStatusChanged(boolean isUsbAttach) {
        if (isUsbAttach) {
            boolean isStatusChanged = mIconUsbAttached.getVisibility() != View.VISIBLE;
            if (isStatusChanged) {
                mIconUsbAttached.setVisibility(View.VISIBLE);
                showToast("Smart Mic connected");
            }
        } else {
            mIconUsbAttached.setVisibility(View.GONE);
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
        public void onItemMicClicked() {
            triggerDialogViaClick = true;
            closeDrawer();
            DialogFlowForegroundService.processScanUsbDevices();
        }

        @Override
        public void onItemTipClicked() {
            updateDrawerContent(mDrawerTipFragment);
        }

        @Override
        public void onItemAdvancedClicked() {
            updateDrawerContent(mDrawerAdvancedFragment);
        }

        @Override
        public void onItemFAQsClicked() {
            startAnotherActivity(KikaFAQsActivity.class, false);
        }

        @Override
        public void onItemReportClicked() {
            startAnotherActivity(KikaUserReportActivity.class, false);
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

    private Fragment mDrawerTipFragment = DrawerTipFragment.newInstance(new DrawerTipFragment.IDrawerTipListener() {
        @Override
        public void onBackClicked() {
            updateDrawerContent(mDrawerMainFragment);
        }
    });

    private Fragment mDrawerAdvancedFragment = DrawerAdvancedFragment.newInstance(new DrawerAdvancedFragment.IDrawerAdvancedListener() {
        @Override
        public void onBackClicked() {
            updateDrawerContent(mDrawerMainFragment);
        }
    });

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, "keyboard:" + newConfig.keyboard + ", hardKeyboardHidden:" + newConfig.hardKeyboardHidden + ", keyboardHidden:" + newConfig.keyboardHidden);
        }
    }
}