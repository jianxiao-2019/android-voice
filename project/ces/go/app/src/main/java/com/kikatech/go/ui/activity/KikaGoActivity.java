package com.kikatech.go.ui.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
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
import com.kikatech.go.util.AsyncThreadPool;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.NetworkUtil;
import com.kikatech.go.util.StringUtil;
import com.kikatech.go.util.VersionControlUtil;
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
public class KikaGoActivity extends BaseDrawerActivity {
    private static final String TAG = "KikaGoActivity";

    private GoLayout mGoLayout;
    private UiTaskManager mUiManager;
    private ImageView mBtnOpenDrawer;
    private View mIconConnectionStatus;
    private View mIconUsbHardwareStatus;
    private View mIconUsbAttached;
    private View mIconHelp;
    private View mIconUpdateApp;

    private boolean triggerDialogViaClick;

    private boolean mIsUsbDataCorrect = true;

    private int mIconHelpVisibility = View.VISIBLE;

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
        String text, scene, sceneAction, source;
        SceneStage stage;
        boolean isFinished, proactive, isUsbSource, isUsbDataCorrect;
        String dbgAction = "[" + action.replace("action_on_", "") + "]";
        switch (action) {
            case DFServiceEvent.ACTION_ON_CONNECTIVITY_CHANGED:
                updateTopIconStatus();
                break;
            case DFServiceEvent.ACTION_EXIT_APP:
                finishAffinity();
                break;
            case DFServiceEvent.ACTION_ON_USB_NON_CHANGED:
                source = event.getExtras().getString(DFServiceEvent.PARAM_TEXT);
                isUsbSource = DialogFlowForegroundService.VOICE_SOURCE_USB.equals(source);
                isUsbDataCorrect = event.getExtras().getBoolean(DFServiceEvent.PARAM_IS_USB_DEVICE_DATA_CORRECT, true);
                if (isUsbSource && isUsbDataCorrect) {
                    break;
                }
            case DFServiceEvent.ACTION_ON_USB_NO_DEVICES:
                if (!GlobalPref.getIns().getHasShowDialogUsbIllustration()) {
                    GlobalPref.getIns().setHasShowDialogUsbIllustration(true);
                    AsyncThreadPool.getIns().executeDelay(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DialogUtil.showDialogAlertUsbInstallation(KikaGoActivity.this, null);
                                }
                            });
                        }
                    }, 1200);
                } else if (triggerDialogViaClick) {
                    triggerDialogViaClick = false;
                    DialogUtil.showDialogAlertUsbInstallation(KikaGoActivity.this, null);
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
                int bosDuration = event.getExtras().getInt(DFServiceEvent.PARAM_BOS_DURATION, 0);
                mUiManager.onStageActionDone(bosDuration);
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
                source = event.getExtras().getString(DFServiceEvent.PARAM_TEXT);
                isUsbSource = DialogFlowForegroundService.VOICE_SOURCE_USB.equals(source);
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
                    isUsbDataCorrect = isDataCorrect == null || isDataCorrect;
                    if (LogUtil.DEBUG) {
                        LogUtil.logv(TAG, String.format("ACTION_ON_PING_SERVICE_STATUS, isUsbDataCorrect: %s", isUsbDataCorrect));
                    }
                    mIsUsbDataCorrect = isUsbDataCorrect;
                    updateTopIconStatus();
                }
                break;
            case DFServiceEvent.ACTION_ON_USB_DEVICE_DATA_STATUS_CHANGED:
                isUsbDataCorrect = event.getExtras().getBoolean(DFServiceEvent.PARAM_IS_USB_DEVICE_DATA_CORRECT);
                if (LogUtil.DEBUG) {
                    LogUtil.logv(TAG, String.format("ACTION_ON_USB_DEVICE_DATA_STATUS_CHANGED, isUsbDataCorrect: %s", isUsbDataCorrect));
                }
                mIsUsbDataCorrect = isUsbDataCorrect;
                updateTopIconStatus();
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
                String textToSend = null;
                switch (requestType) {
                    case OptionList.REQUEST_TYPE_ORDINAL:
                        textToSend = String.valueOf(index + 1);
                        if (!TextUtils.isEmpty(textToSend)) {
                            DialogFlowForegroundService.processDialogFlowTalk(textToSend);
                            mUiManager.dispatchSpeechTask(textToSend);
                        }
                        break;
                    case OptionList.REQUEST_TYPE_TEXT:
                        textToSend = option.getActionText();
                        if (!TextUtils.isEmpty(textToSend)) {
                            DialogFlowForegroundService.processDialogFlowTalk(textToSend);
                            mUiManager.dispatchSpeechTask(textToSend);
                        }
                        break;
                    case OptionList.REQUEST_TYPE_ORDINAL_TO_TEXT:
                        switch (index) {
                            case 0:
                                textToSend = "The first one";
                                break;
                            case 1:
                                textToSend = "The second one";
                                break;
                        }
                        if (!TextUtils.isEmpty(textToSend)) {
                            DialogFlowForegroundService.processDialogFlowTalk(textToSend);
                            mUiManager.dispatchSpeechTask(textToSend);
                        }
                        break;
                }
            }

            @Override
            public void onLayoutModeChanged(GoLayout.DisplayMode mode) {
                switch (mode) {
                    case AWAKE:
                        mBtnOpenDrawer.setVisibility(View.GONE);
                        mIconHelpVisibility = View.INVISIBLE;
                        updateTopIconStatus();
                        break;
                    case SLEEP:
                        mBtnOpenDrawer.setVisibility(View.VISIBLE);
                        mIconHelpVisibility = View.VISIBLE;
                        updateTopIconStatus();
                        break;
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kika_go);
        bindView();
        bindListener();

        @VersionControlUtil.AppVersionStatus int status = VersionControlUtil.checkAppVersion();
        switch (status) {
            case VersionControlUtil.AppVersionStatus.BLOCK:
                startAnotherActivity(KikaBlockActivity.class, true);
                break;
            case VersionControlUtil.AppVersionStatus.UPDATE:
                mIconUpdateApp.setVisibility(View.VISIBLE);
            case VersionControlUtil.AppVersionStatus.LATEST:
                // TODO fine tune init timing
                ContactManager.getIns().init(this);
                LocationMgr.init(this);
                registerReceivers();
                initUiTaskManager();
                DialogFlowForegroundService.processStart(KikaGoActivity.this, DialogFlowForegroundService.class);
                DialogFlowForegroundService.processPingDialogFlowStatus();
                updateTopIconStatus();

                if (GlobalPref.getIns().isFirstLaunch()) {
                    CustomConfig.removeAllCustomConfigFiles();
                }

//                if (LogUtil.DEBUG) {
//                    String sen = CustomConfig.getSnowboySensitivity();
//                    int timeout = CustomConfig.getKikaTtsServerTimeout();
//                    String msg = "[config] Sensitivity: " + sen + " , Timeout: " + timeout + " ms";
//                    showLongToast(msg);
//                    LogUtil.log(TAG, msg);
//                }
                break;
        }
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
        DialogFlowForegroundService.processStop(KikaGoActivity.this, DialogFlowForegroundService.class);
        super.onDestroy();
    }

    private void bindView() {
        mGoLayout = (GoLayout) findViewById(R.id.go_layout);
        mBtnOpenDrawer = (ImageView) findViewById(R.id.go_layout_btn_open_drawer);
        mIconConnectionStatus = findViewById(R.id.go_layout_ic_connection_status);
        mIconUsbHardwareStatus = findViewById(R.id.go_layout_ic_hardware_status);
        mIconUsbAttached = findViewById(R.id.go_layout_ic_usb_attached);
        mIconHelp = findViewById(R.id.go_layout_ic_help);
        mIconUpdateApp = findViewById(R.id.go_layout_ic_update_app);
    }

    private void bindListener() {
        mBtnOpenDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer();
            }
        });
        mIconConnectionStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.showDialogConnectionError(KikaGoActivity.this, null);
            }
        });
        mIconUsbHardwareStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.showDialogUsbHardwareError(KikaGoActivity.this, null);
            }
        });
        mIconHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnotherActivity(KikaFAQsActivity.class, false);
            }
        });
        mIconUpdateApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.showUpdateApp(KikaGoActivity.this, null);
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

    private synchronized void updateTopIconStatus() {
        boolean hasNetwork = NetworkUtil.isNetworkAvailable(KikaGoActivity.this);
        if (!hasNetwork) { // do not have network connection
            mIconUsbHardwareStatus.setVisibility(View.GONE);
            mIconHelp.setVisibility(View.INVISIBLE);
            animateNetworkError();
        } else if (!mIsUsbDataCorrect) { // has network connection, but usb data incorrect
            mIconConnectionStatus.clearAnimation();
            mIconConnectionStatus.setVisibility(View.GONE);
            mIconUsbHardwareStatus.setVisibility(View.VISIBLE);
            mIconHelp.setVisibility(View.INVISIBLE);
        } else { // has network connection, and usb data correct
            mIconConnectionStatus.clearAnimation();
            mIconConnectionStatus.setVisibility(View.GONE);
            mIconUsbHardwareStatus.setVisibility(View.GONE);
            mIconHelp.setVisibility(mIconHelpVisibility);
        }
        mUiManager.dispatchConnectionStatusChanged(hasNetwork);
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
        animation.setDuration(750);
        animation.setRepeatCount(1);
        AnimationUtils.getIns().animate(mIconConnectionStatus, animation, new AnimationUtils.IAnimationEndCallback() {
            @Override
            public void onEnd(View view) {
                isAnimating = false;
            }
        });
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
            closeDrawer();
            startAnotherActivity(KikaFAQsActivity.class, false);
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