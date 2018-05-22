package com.kikatech.go.ui.activity;

import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;

import com.kikatech.go.R;
import com.kikatech.go.databinding.ActivityKikaGoBinding;
import com.kikatech.go.dialogflow.model.DFServiceStatus;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.eventbus.DFServiceEvent;
import com.kikatech.go.navigation.location.LocationMgr;
import com.kikatech.go.services.DialogFlowForegroundService;
import com.kikatech.go.services.presenter.VoiceSourceHelper;
import com.kikatech.go.tutorial.TutorialUtil;
import com.kikatech.go.ui.fragment.DrawerAdvancedFragment;
import com.kikatech.go.ui.fragment.DrawerImFragment;
import com.kikatech.go.ui.fragment.DrawerMainFragment;
import com.kikatech.go.ui.fragment.DrawerMusicFragment;
import com.kikatech.go.ui.fragment.DrawerNavigationFragment;
import com.kikatech.go.ui.fragment.DrawerTipFragment;
import com.kikatech.go.util.AnimationUtils;
import com.kikatech.go.util.AsyncThreadPool;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.NetworkUtil;
import com.kikatech.go.util.StringUtil;
import com.kikatech.go.util.TipsHelper;
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

    private ActivityKikaGoBinding mBinding;
    private UiTaskManager mUiManager;

    private boolean triggerDialogViaClick;

    private boolean mIsUsbSource = false;
    private boolean mIsAudioDataCorrect = true;

    private int mIconTutorialVisibility;
    private int mIconHelpVisibility;

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
        boolean isFinished, proactive, isUsbSource, isAudioDataCorrect;
        String dbgAction = "[" + action.replace("action_on_", "") + "]";
        switch (action) {
            case DFServiceEvent.ACTION_ON_CONNECTIVITY_CHANGED:
                updateTopIconStatus();
                break;
            case DFServiceEvent.ACTION_EXIT_APP:
                finishAffinity();
                break;
            case DFServiceEvent.ACTION_ON_USB_NON_CHANGED:
                source = event.getExtras().getString(DFServiceEvent.PARAM_AUDIO_SOURCE);
                isUsbSource = VoiceSourceHelper.VOICE_SOURCE_USB.equals(source);
                isAudioDataCorrect = event.getExtras().getBoolean(DFServiceEvent.PARAM_IS_AUDIO_DATA_CORRECT, true);
                mIsUsbSource = isUsbSource;
                mIsAudioDataCorrect = isAudioDataCorrect;
                if (isUsbSource && isAudioDataCorrect) {
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
                                    DialogUtil.showUsbDetached(KikaGoActivity.this, null);
                                }
                            });
                        }
                    }, 1200);
                } else if (triggerDialogViaClick) {
                    triggerDialogViaClick = false;
                    DialogUtil.showUsbDetached(KikaGoActivity.this, null);
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
                if (!mIsAudioDataCorrect) {
                    mIsAudioDataCorrect = true;
                    updateTopIconStatus();
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
                source = event.getExtras().getString(DFServiceEvent.PARAM_AUDIO_SOURCE);
                isUsbSource = VoiceSourceHelper.VOICE_SOURCE_USB.equals(source);
                mIsUsbSource = isUsbSource;
                mIsAudioDataCorrect = true;
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
                    source = event.getExtras().getString(DFServiceEvent.PARAM_AUDIO_SOURCE);
                    isUsbSource = VoiceSourceHelper.VOICE_SOURCE_USB.equals(source);
                    Boolean isDataCorrect = serviceStatus.isAudioDataCorrect();
                    isAudioDataCorrect = isDataCorrect == null || isDataCorrect;
                    if (LogUtil.DEBUG) {
                        LogUtil.logv(TAG, String.format("%s, isUsbSource: %s, isAudioDataCorrect: %s", action, isUsbSource, isAudioDataCorrect));
                    }
                    mIsUsbSource = isUsbSource;
                    mIsAudioDataCorrect = isAudioDataCorrect;
                    updateTopIconStatus();
                }
                break;
            case DFServiceEvent.ACTION_ON_USB_DEVICE_DATA_STATUS_CHANGED:
                isAudioDataCorrect = event.getExtras().getBoolean(DFServiceEvent.PARAM_IS_AUDIO_DATA_CORRECT);
                source = event.getExtras().getString(DFServiceEvent.PARAM_AUDIO_SOURCE);
                isUsbSource = VoiceSourceHelper.VOICE_SOURCE_USB.equals(source);
                if (LogUtil.DEBUG) {
                    LogUtil.logv(TAG, String.format("%s, isUsbSource: %s, isAudioDataCorrect: %s", action, isUsbSource, isAudioDataCorrect));
                }
                mIsUsbSource = isUsbSource;
                mIsAudioDataCorrect = isAudioDataCorrect;
                updateTopIconStatus();
                break;
        }
    }

    /**
     * must called after GoLayout and DialogFlowService initialized
     **/
    private void initUiTaskManager() {
        mUiManager = new UiTaskManager(mBinding.goLayout, new UiTaskManager.IUiManagerFeedback() {
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
                        mBinding.goLayoutBtnOpenDrawer.setVisibility(View.GONE);
                        mIconHelpVisibility = View.INVISIBLE;
                        mIconTutorialVisibility = View.INVISIBLE;
                        updateTopIconStatus();
                        break;
                    case SLEEP:
                        mBinding.goLayoutBtnOpenDrawer.setVisibility(View.VISIBLE);
                        boolean hasDoneTutorial = TutorialUtil.hasDoneTutorial();
                        mIconHelpVisibility = hasDoneTutorial ? View.VISIBLE : View.INVISIBLE;
                        mIconTutorialVisibility = hasDoneTutorial ? View.INVISIBLE : View.VISIBLE;
                        updateTopIconStatus();
                        break;
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_kika_go);
        bindListener();

        @VersionControlUtil.AppVersionStatus int status = VersionControlUtil.checkAppVersion();
        switch (status) {
            case VersionControlUtil.AppVersionStatus.BLOCK:
                startAnotherActivity(KikaBlockActivity.class, true);
                break;
            case VersionControlUtil.AppVersionStatus.UPDATE:
                mBinding.goLayoutIcUpdateApp.setVisibility(View.VISIBLE);
            case VersionControlUtil.AppVersionStatus.LATEST:
                // TODO fine tune init timing
                ContactManager.getIns().init(this);
                LocationMgr.init(this);
                registerReceivers();
                initUiTaskManager();
                DialogFlowForegroundService.processStart(KikaGoActivity.this, DialogFlowForegroundService.class);
                DialogFlowForegroundService.processPingDialogFlowStatus();

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
        boolean hasDoneTutorial = TutorialUtil.hasDoneTutorial();
        mIconHelpVisibility = hasDoneTutorial ? View.VISIBLE : View.INVISIBLE;
        mIconTutorialVisibility = hasDoneTutorial ? View.INVISIBLE : View.VISIBLE;
        updateTopIconStatus();
        DialogFlowForegroundService.processScanUsbDevices();
        if (TipsHelper.shouldShowDialogMoreCommands()) {
            TipsHelper.showDialogMoreCommands(KikaGoActivity.this, new DialogUtil.IDialogListener() {
                @Override
                public void onApply(Bundle args) {
                    updateDrawerContent(mDrawerTipFragment);
                    openDrawer();
                }

                @Override
                public void onCancel() {
                }
            });
        }
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

    private void bindListener() {
        mBinding.goLayoutBtnOpenDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer();
            }
        });
        mBinding.goLayoutIcConnectionStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.showDialogConnectionError(KikaGoActivity.this, null);
            }
        });
        mBinding.goLayoutIcHardwareErr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.showAudioRecordError(KikaGoActivity.this, true, null);
            }
        });
        mBinding.goLayoutIcMicrophoneErr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.showAudioRecordError(KikaGoActivity.this, false, null);
            }
        });
        mBinding.goLayoutIcHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnotherActivity(KikaFAQsActivity.class, false);
            }
        });
        mBinding.goLayoutIcTutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFlowForegroundService.processDoTutorial();
            }
        });
        mBinding.goLayoutIcUpdateApp.setOnClickListener(new View.OnClickListener() {
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
            mBinding.goLayoutIcHardwareErr.setVisibility(View.GONE);
            mBinding.goLayoutIcMicrophoneErr.setVisibility(View.GONE);
            mBinding.goLayoutIcHelp.setVisibility(View.INVISIBLE);
            animateNetworkError();
        } else if (!mIsAudioDataCorrect) { // has network connection, but audio data incorrect
            mBinding.goLayoutIcConnectionStatus.clearAnimation();
            mBinding.goLayoutIcConnectionStatus.setVisibility(View.GONE);
            if (mIsUsbSource) {
                mBinding.goLayoutIcHardwareErr.setVisibility(View.VISIBLE);
                mBinding.goLayoutIcMicrophoneErr.setVisibility(View.GONE);
            } else {
                mBinding.goLayoutIcHardwareErr.setVisibility(View.GONE);
                mBinding.goLayoutIcMicrophoneErr.setVisibility(View.VISIBLE);
            }
            mBinding.goLayoutIcHelp.setVisibility(View.INVISIBLE);
        } else { // has network connection, and usb data correct
            mBinding.goLayoutIcConnectionStatus.clearAnimation();
            mBinding.goLayoutIcConnectionStatus.setVisibility(View.GONE);
            mBinding.goLayoutIcHardwareErr.setVisibility(View.GONE);
            mBinding.goLayoutIcMicrophoneErr.setVisibility(View.GONE);
            mBinding.goLayoutIcHelp.setVisibility(mIconHelpVisibility);
            mBinding.goLayoutIcTutorial.setVisibility(mIconTutorialVisibility);
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
        mBinding.goLayoutIcConnectionStatus.setVisibility(View.VISIBLE);
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(750);
        animation.setRepeatCount(1);
        AnimationUtils.getIns().animate(mBinding.goLayoutIcConnectionStatus, animation, new AnimationUtils.IAnimationEndCallback() {
            @Override
            public void onEnd(View view) {
                isAnimating = false;
            }
        });
    }

    private void onUsbAttachedStatusChanged(boolean isUsbAttach) {
        if (isUsbAttach) {
            boolean isStatusChanged = mBinding.goLayoutIcUsbAttached.getVisibility() != View.VISIBLE;
            if (isStatusChanged) {
                mBinding.goLayoutIcUsbAttached.setVisibility(View.VISIBLE);
                showToast("Smart Mic connected");
            }
        } else {
            mBinding.goLayoutIcUsbAttached.setVisibility(View.GONE);
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
        public void onItemMusicClicked() {
            updateDrawerContent(mDrawerMusicFragment);
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

    private Fragment mDrawerMusicFragment = DrawerMusicFragment.newInstance(new DrawerMusicFragment.IDrawerMusicListener() {
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