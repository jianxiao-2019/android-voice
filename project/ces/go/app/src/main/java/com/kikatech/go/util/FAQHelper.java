package com.kikatech.go.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.kikatech.go.eventbus.DFServiceEvent;
import com.kikatech.go.services.DialogFlowForegroundService;
import com.kikatech.go.services.presenter.VoiceSourceHelper;
import com.kikatech.go.ui.activity.BaseActivity;
import com.kikatech.go.ui.activity.KikaFAQsReportActivity;
import com.kikatech.go.ui.activity.KikaUserReportActivity;
import com.kikatech.go.util.dialog.DialogKeys;
import com.kikatech.go.util.dialog.DialogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author SkeeterWang Created on 2018/4/18.
 */

@SuppressLint("SwitchIntDef")
public class FAQHelper {
    private static final String TAG = "FAQHelper";

    private static final int FAQ_2_STEP_IDLE = -2;
    private static final int FAQ_2_STEP_START = -1;
    private static final int FAQ_2_STEP_DEVICE_FAILURE = 0;
    private static final int FAQ_2_STEP_DEVICE_DATA_INCORRECT = 1;
    private static final int FAQ_2_STEP_DEVICE_FINE = 2;

    @IntDef({FAQ_2_STEP_IDLE, FAQ_2_STEP_START, FAQ_2_STEP_DEVICE_FAILURE, FAQ_2_STEP_DEVICE_DATA_INCORRECT, FAQ_2_STEP_DEVICE_FINE})
    private @interface Faq2Step {
        int IDLE = FAQ_2_STEP_IDLE;
        int START = FAQ_2_STEP_START;
        int FAILURE = FAQ_2_STEP_DEVICE_FAILURE;
        int DATA_INCORRECT = FAQ_2_STEP_DEVICE_DATA_INCORRECT;
        int FINE = FAQ_2_STEP_DEVICE_FINE;
    }

    private Context mContext;

    @Faq2Step
    private int mFaq2Step = Faq2Step.IDLE;

    public FAQHelper(Context context) {
        mContext = context;
    }

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
        switch (action) {
            case DFServiceEvent.ACTION_ON_USB_NO_DEVICES:
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, String.format("onServiceEvent, action: %s", action));
                }
                switch (mFaq2Step) {
                    case Faq2Step.IDLE:
                    case Faq2Step.DATA_INCORRECT:
                    case Faq2Step.FINE:
                        break;
                    case Faq2Step.FAILURE:
                        DialogUtil.dismiss();
                        break;
                    case Faq2Step.START:
                        if (LogUtil.DEBUG) {
                            LogUtil.logv(TAG, "case 1: can not find usb devices");
                        }
                        // case 1: can not find usb devices
                        showDialogFAQ2(Faq2Step.FAILURE);
                        break;
                }
                break;
            case DFServiceEvent.ACTION_ON_USB_NON_CHANGED:
            case DFServiceEvent.ACTION_ON_VOICE_SRC_CHANGE:
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, String.format("onServiceEvent, action: %s", action));
                }
                if (LogUtil.DEBUG) {
                    LogUtil.logd(TAG, String.format("mFaq2Step: %s", mFaq2Step));
                }
                String source = event.getExtras().getString(DFServiceEvent.PARAM_AUDIO_SOURCE);
                boolean isUsbSource = VoiceSourceHelper.VOICE_SOURCE_USB.equals(source);
                boolean isDataCorrect = event.getExtras().getBoolean(DFServiceEvent.PARAM_IS_AUDIO_DATA_CORRECT, true);
                switch (mFaq2Step) {
                    case Faq2Step.IDLE:
                        break;
                    case Faq2Step.DATA_INCORRECT:
                    case Faq2Step.FINE:
                    case Faq2Step.FAILURE:
                        DialogUtil.dismiss();
                        break;
                    case Faq2Step.START:
                        if (!isUsbSource) {
                            if (LogUtil.DEBUG) {
                                LogUtil.logv(TAG, "case 1: can not find usb devices");
                            }
                            // case 1: can not find usb devices
                            showDialogFAQ2(Faq2Step.FAILURE);
                        } else {
                            if (isDataCorrect) {
                                if (LogUtil.DEBUG) {
                                    LogUtil.logv(TAG, "case 3: usb device works fine");
                                }
                                // case 3: usb device works fine
                                showDialogFAQ2(Faq2Step.FINE);
                            } else {
                                if (LogUtil.DEBUG) {
                                    LogUtil.logv(TAG, "case 2: usb device data error");
                                }
                                // case 2: usb device data error
                                showDialogFAQ2(Faq2Step.DATA_INCORRECT);
                            }
                        }
                        break;
                }
                break;
        }
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

    private void showDialogFAQ2(@Faq2Step final int idxPage) {
        if (LogUtil.DEBUG) {
            LogUtil.logw(TAG, "showDialogFAQ2");
        }
        DialogUtil.showFAQ2(mContext, idxPage, new DialogUtil.IDialogLeftRightListener() {
            @Override
            public void onShow() {
                if (LogUtil.DEBUG) {
                    LogUtil.logw(TAG, "onShow");
                }
                mFaq2Step = idxPage;
            }

            @Override
            public void onDismiss() {
                if (LogUtil.DEBUG) {
                    LogUtil.logw(TAG, "onDismiss");
                }
                mFaq2Step = Faq2Step.IDLE;
                unregisterReceivers();
            }

            @Override
            public void onLeftBtnClicked(Bundle args) {
                int idxPage = args != null ? args.getInt(DialogKeys.KEY_PAGE_INDEX, Integer.MIN_VALUE) : Integer.MIN_VALUE;
                switch (idxPage) {
                    case Faq2Step.FAILURE:
                        IntentUtil.openKikaGoProductWeb(mContext);
                        break;
                }
            }

            @Override
            public void onRightBtnClicked(Bundle args) {
                int idxPage = args != null ? args.getInt(DialogKeys.KEY_PAGE_INDEX, Integer.MIN_VALUE) : Integer.MIN_VALUE;
                switch (idxPage) {
                    case Faq2Step.FAILURE:
                        openFaqReport(UserReportUtil.FAQReportReason.ERROR_HARDWARE_FAILURE);
                        break;
                    case Faq2Step.DATA_INCORRECT:
                        openFaqReport(UserReportUtil.FAQReportReason.ERROR_HARDWARE_DATA_INCORRECT);
                        break;
                    case Faq2Step.FINE:
                        openUserReport();
                        break;
                }
            }

            @Override
            public void onCancel() {
            }
        });
    }

    private void openFaqReport(UserReportUtil.FAQReportReason reason) {
        if (mContext instanceof BaseActivity) {
            BaseActivity activity = ((BaseActivity) mContext);
            Bundle args = new Bundle();
            args.putString(UserReportUtil.FAQReportReason.KEY_TITLE, reason.title);
            args.putString(UserReportUtil.FAQReportReason.KEY_DESCRIPTION, reason.description);
            activity.getIntent().putExtras(args);
            activity.startAnotherActivity(KikaFAQsReportActivity.class, false);
        }
    }

    private void openUserReport() {
        if (mContext instanceof BaseActivity) {
            BaseActivity activity = ((BaseActivity) mContext);
            activity.startAnotherActivity(KikaUserReportActivity.class, false);
        }
    }


    public void doFAQ1() {
        DialogUtil.showFAQ1(mContext, null);
    }

    public void doFAQ2() {
        registerReceivers();
        mFaq2Step = Faq2Step.START;
        DialogFlowForegroundService.processScanUsbDevices();
    }

    public void doFAQ3() {
        DialogUtil.showFAQ3(mContext, 0, new DialogUtil.IDialogLeftRightListener() {
            @Override
            public void onShow() {
            }

            @Override
            public void onDismiss() {
            }

            @Override
            public void onLeftBtnClicked(Bundle args) {
                int idxPage = args != null ? args.getInt(DialogKeys.KEY_PAGE_INDEX, Integer.MIN_VALUE) : Integer.MIN_VALUE;
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, String.format("onLeftBtnClicked, idxPage: %s", idxPage));
                }
                switch (idxPage) {
                    case 0:
                        DialogUtil.showFAQ3(mContext, 1, this);
                        break;
                    case 1:
                        openFaqReport(UserReportUtil.FAQReportReason.ERROR_WAKE_UP_DETECTOR_FAILED);
                        break;
                    case 2:
                        break;
                }
            }

            @Override
            public void onRightBtnClicked(Bundle args) {
                int idxPage = args != null ? args.getInt(DialogKeys.KEY_PAGE_INDEX, Integer.MIN_VALUE) : Integer.MIN_VALUE;
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, String.format("onRightBtnClicked, idxPage: %s", idxPage));
                }
                switch (idxPage) {
                    case 0:
                        DialogUtil.showFAQ3(mContext, 2, this);
                        break;
                    case 1:
                        doFAQ2();
                        break;
                    case 2:
                        DialogUtil.showFAQ3Record(mContext, 1, new DialogUtil.IDialogListener() {
                            @Override
                            public void onApply(Bundle args) {
                                int recordTimes = args != null ? args.getInt(DialogKeys.KEY_COUNTER, Integer.MIN_VALUE) : Integer.MIN_VALUE;
                                if (recordTimes > 0 && recordTimes < 3) {
                                    DialogUtil.showFAQ3Record(mContext, recordTimes + 1, this);
                                } else if (recordTimes == 3) {
                                    // TODO: DONE
                                    DialogUtil.showFAQ3RecordDone(mContext, null);
                                }
                            }

                            @Override
                            public void onCancel() {
                            }
                        });
                        break;
                }
            }

            @Override
            public void onCancel() {
            }
        });
    }

    public void doFAQT1() {
        openUserReport();
    }

    public void release() {
        unregisterReceivers();
        mContext = null;
    }
}
