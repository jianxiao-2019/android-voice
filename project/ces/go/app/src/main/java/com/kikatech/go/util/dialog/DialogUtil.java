package com.kikatech.go.util.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.UserSettings;
import com.kikatech.go.ui.adapter.FAQ1DialogAdapter;
import com.kikatech.go.util.IntentUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.MathUtil;
import com.kikatech.go.util.MediaPlayerUtil;
import com.kikatech.go.util.VersionControlUtil;

import java.util.List;

/**
 * @author SkeeterWang Created on 2017/12/20.
 */

@SuppressWarnings("SameParameterValue")
public class DialogUtil {
    private static final String TAG = "DialogUtil";

    private static Dialog mDialog;

    private static void safeDismissDialog() {
        safeDismissDialog(mDialog);
    }

    private static void safeDismissDialog(Dialog target) {
        try {
            if (target != null && target.isShowing()) {
                target.dismiss();
                target = null;
            }
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }


    public static void showDialogEditSettingDestination(final Context context, final String originalText, final IDialogListener listener) {
        safeDismissDialog();

        mDialog = new Dialog(context);

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(true);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_setting_destination_edit, null);

        final EditText mInput = (EditText) dialogView.findViewById(R.id.dialog_edit_text);
        View mBtnApply = dialogView.findViewById(R.id.dialog_btn_apply);
        View mBtnCancel = dialogView.findViewById(R.id.dialog_btn_cancel);

        if (!TextUtils.isEmpty(originalText)) {
            mInput.setText(originalText);
        }

        mBtnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newText = mInput.getText().toString();
                if (listener != null) {
                    Bundle args = new Bundle();
                    args.putString(DialogKeys.KEY_TEXT, newText);
                    listener.onApply(args);
                }
                safeDismissDialog();
            }
        });

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                safeDismissDialog();
            }
        });

        mDialog.setContentView(dialogView);

        Window window = mDialog.getWindow();
        setDefaultLayout(window);
//        setDimAlpha(window, 0.35f);
        enableSoftInput(window);

        mDialog.show();
    }

    public static void showDialogAddSettingDestination(final Context context, final IDialogListener listener) {
        safeDismissDialog();

        mDialog = new Dialog(context);

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(true);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_setting_destination_add, null);

        final EditText mInput = (EditText) dialogView.findViewById(R.id.dialog_edit_text);
        View mBtnApply = dialogView.findViewById(R.id.dialog_btn_apply);
        View mBtnCancel = dialogView.findViewById(R.id.dialog_btn_cancel);

        mBtnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newText = mInput.getText().toString();
                if (listener != null) {
                    Bundle args = new Bundle();
                    args.putString(DialogKeys.KEY_TEXT, newText);
                    listener.onApply(args);
                }
                safeDismissDialog();
            }
        });

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                safeDismissDialog();
            }
        });

        mDialog.setContentView(dialogView);

        Window window = mDialog.getWindow();
        setDefaultLayout(window);
//        setDimAlpha(window, 0.35f);
        enableSoftInput(window);

        mDialog.show();
    }

    public static void showDialogAlertUsbInstallation(final Context context, final IDialogListener listener) {
        safeDismissDialog();

        mDialog = new Dialog(context);

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(false);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_usb_detached_alert, null);

        View mBtnApply = dialogView.findViewById(R.id.dialog_btn_apply);
        View mBtnCancel = dialogView.findViewById(R.id.dialog_btn_cancel);

        mBtnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    Bundle args = new Bundle();
                    listener.onApply(args);
                }
                safeDismissDialog();
            }
        });

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentUtil.openKikaGoProductWeb(context);
                safeDismissDialog();
            }
        });

        mDialog.setContentView(dialogView);

        Window window = mDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        setDimAlpha(window, 0.35f);

        mDialog.show();
    }

    public static void showDialogConnectionError(final Context context, final IDialogListener listener) {
        safeDismissDialog();

        mDialog = new Dialog(context);

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(false);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_connection_status_error, null);

        View mBtnApply = dialogView.findViewById(R.id.dialog_btn_apply);

        mBtnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    Bundle args = new Bundle();
                    listener.onApply(args);
                }
                safeDismissDialog();
            }
        });

        mDialog.setContentView(dialogView);

        Window window = mDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        setDimAlpha(window, 0.35f);

        mDialog.show();
    }

    public static void showDialogUsbHardwareError(final Context context, final IDialogListener listener) {
        safeDismissDialog();

        mDialog = new Dialog(context);

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(false);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_usb_hardware_status_error, null);

        View mBtnApply = dialogView.findViewById(R.id.dialog_btn_apply);

        mBtnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    Bundle args = new Bundle();
                    listener.onApply(args);
                }
                safeDismissDialog();
            }
        });

        mDialog.setContentView(dialogView);

        Window window = mDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        setDimAlpha(window, 0.35f);

        mDialog.show();
    }

    public static void showDbgAsrServerList(final Context context, final IDialogListener listener) {
        safeDismissDialog();

        mDialog = new Dialog(context);

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(true);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_dbg_server_list, null);
        final RadioGroup group = (RadioGroup) dialogView.findViewById(R.id.server_list_group);

        String currentServer = UserSettings.getDbgAsrServer();
        List<String> serverList = UserSettings.getDbgAsrServerList();
        for (String serverUrl : serverList) {
            RadioButton radioButton = (RadioButton) LayoutInflater.from(context).inflate(R.layout.dialog_dbg_server_list_radiobutton, null);
            radioButton.setText(serverUrl);
            group.addView(radioButton);
            if (currentServer.equals(serverUrl)) {
                group.check(radioButton.getId());
            }
        }

        View mBtnApply = dialogView.findViewById(R.id.dialog_btn_apply);
        View mBtnCancel = dialogView.findViewById(R.id.dialog_btn_cancel);

        mBtnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentServer = UserSettings.getDbgAsrServer();
                int checkedRadioBtnId = group.getCheckedRadioButtonId();
                String newServer = ((RadioButton) group.findViewById(checkedRadioBtnId)).getText().toString();
                if (!currentServer.equals(newServer)) {
                    UserSettings.saveDbgAsrServer(newServer);
                    if (listener != null) {
                        Bundle args = new Bundle();
                        listener.onApply(args);
                    }
                    safeDismissDialog();
                }
            }
        });

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onCancel();
                }
                safeDismissDialog();
            }
        });

        mDialog.setContentView(dialogView);

        Window window = mDialog.getWindow();
        setDefaultLayout(window);
        setDimAlpha(window, 0.35f);

        mDialog.show();
    }

    public static void showFAQ1(final Context context, final IDialogListener listener) {
        safeDismissDialog();

        mDialog = new Dialog(context);

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(true);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_faq_1, null);

        final ViewPager mPager = (ViewPager) dialogView.findViewById(R.id.dialog_faq_pager);
        TabLayout mIndicatorView = (TabLayout) dialogView.findViewById(R.id.dialog_faq_pager_indicators);
        final TextView mSubtitle = (TextView) dialogView.findViewById(R.id.dialog_faq_subtitle);
        final TextView mContent = (TextView) dialogView.findViewById(R.id.dialog_faq_content);
        final TextView mBtnApply = (TextView) dialogView.findViewById(R.id.dialog_btn_apply);
        View mBtnCancel = dialogView.findViewById(R.id.dialog_btn_cancel);

        Resources resources = context.getResources();
        final int[] IMAGE_RESOURCE = new int[]{R.drawable.ic_settings_faq_illu_headup,
                R.drawable.ic_settings_faq_illu_facingdriver,
                R.drawable.ic_settings_faq_illu_2inches};
        final String[] SUBTITLES = resources.getStringArray(R.array.faq_q1_subtitles);
        final String[] DESCRIPTIONS = resources.getStringArray(R.array.faq_q1_descriptions);
        final String[] BUTTON_TEXTS = resources.getStringArray(R.array.faq_q1_buttons);
        int LIST_SIZE = MathUtil.min(IMAGE_RESOURCE.length, SUBTITLES.length, DESCRIPTIONS.length, BUTTON_TEXTS.length);

        final FAQ1DialogAdapter mAdapter = new FAQ1DialogAdapter(context, IMAGE_RESOURCE, LIST_SIZE);
        mPager.setAdapter(mAdapter);
        mIndicatorView.setupWithViewPager(mPager);

        mSubtitle.setText(SUBTITLES.length > 0 ? SUBTITLES[0] : "");
        mContent.setText(DESCRIPTIONS.length > 0 ? DESCRIPTIONS[0] : "");
        mBtnApply.setText(BUTTON_TEXTS.length > 0 ? BUTTON_TEXTS[0] : "");

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mSubtitle.setText(position < SUBTITLES.length ? SUBTITLES[position] : "");
                mContent.setText(position < DESCRIPTIONS.length ? DESCRIPTIONS[position] : "");
                mBtnApply.setText(position < BUTTON_TEXTS.length ? BUTTON_TEXTS[position] : "");
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mBtnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentPosition = mPager.getCurrentItem();
                if (currentPosition + 1 < mAdapter.getCount()) {
                    mPager.setCurrentItem(currentPosition + 1);
                } else {
                    if (listener != null) {
                        Bundle args = new Bundle();
                        listener.onApply(args);
                    }
                    safeDismissDialog();
                }
            }
        });

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onCancel();
                }
                safeDismissDialog();
            }
        });
        mDialog.setContentView(dialogView);

        Window window = mDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        setDimAlpha(window, 0.35f);

        mDialog.show();
    }

    public static void showFAQ2(final Context context, final int idxPage, final IDialogLeftRightListener listener) {
        safeDismissDialog();

        mDialog = new Dialog(context);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(true);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_faq_2, null);

        final TextView mContent = (TextView) dialogView.findViewById(R.id.dialog_faq_content);
        final TextView mBtnLeft = (TextView) dialogView.findViewById(R.id.dialog_btn_left);
        final TextView mBtnRight = (TextView) dialogView.findViewById(R.id.dialog_btn_right);

        Resources resources = context.getResources();
        final String[] DESCRIPTIONS = resources.getStringArray(R.array.faq_q2_descriptions);
        final String[] BUTTON_TEXTS_L = resources.getStringArray(R.array.faq_q2_buttons_left);
        final String[] BUTTON_TEXTS_R = resources.getStringArray(R.array.faq_q2_buttons_right);

        mContent.setText(idxPage < DESCRIPTIONS.length ? DESCRIPTIONS[idxPage] : "");
        mBtnLeft.setText(idxPage < BUTTON_TEXTS_L.length ? BUTTON_TEXTS_L[idxPage] : "");
        mBtnRight.setText(idxPage < BUTTON_TEXTS_R.length ? BUTTON_TEXTS_R[idxPage] : "");

        mBtnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                safeDismissDialog();
                if (listener != null) {
                    Bundle args = new Bundle();
                    args.putInt(DialogKeys.KEY_PAGE_INDEX, idxPage);
                    listener.onLeftBtnClicked(args);
                }
            }
        });

        mBtnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                safeDismissDialog();
                if (listener != null) {
                    Bundle args = new Bundle();
                    args.putInt(DialogKeys.KEY_PAGE_INDEX, idxPage);
                    listener.onRightBtnClicked(args);
                }
            }
        });

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if (listener != null) {
                    listener.onShow();
                }
            }
        });

        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (listener != null) {
                    listener.onDismiss();
                }
            }
        });

        mDialog.setContentView(dialogView);

        Window window = mDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        setDimAlpha(window, 0.35f);

        mDialog.show();
    }

    public static void showFAQ3(final Context context, final int idxPage, final IDialogLeftRightListener listener) {
        safeDismissDialog();

        mDialog = new Dialog(context);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(true);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_faq_3, null);

        final TextView mContent = (TextView) dialogView.findViewById(R.id.dialog_faq_content);
        final TextView mBtnLeft = (TextView) dialogView.findViewById(R.id.dialog_btn_left);
        final TextView mBtnRight = (TextView) dialogView.findViewById(R.id.dialog_btn_right);
        View mBtnExtra = dialogView.findViewById(R.id.dialog_faq_extra_btn);

        Resources resources = context.getResources();
        final String[] DESCRIPTIONS = resources.getStringArray(R.array.faq_q3_descriptions);
        final String[] BUTTON_TEXTS_L = resources.getStringArray(R.array.faq_q3_buttons_left);
        final String[] BUTTON_TEXTS_R = resources.getStringArray(R.array.faq_q3_buttons_right);

        mContent.setText(idxPage < DESCRIPTIONS.length ? DESCRIPTIONS[idxPage] : "");
        mBtnLeft.setText(idxPage < BUTTON_TEXTS_L.length ? BUTTON_TEXTS_L[idxPage] : "");
        mBtnRight.setText(idxPage < BUTTON_TEXTS_R.length ? BUTTON_TEXTS_R[idxPage] : "");

        if (idxPage == 2) {
            mBtnExtra.setVisibility(View.VISIBLE);
            mBtnExtra.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MediaPlayerUtil.playAlert(context, R.raw.wake_up_example, null);
                }
            });
        }

        mBtnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                safeDismissDialog();
                if (listener != null) {
                    Bundle args = new Bundle();
                    args.putInt(DialogKeys.KEY_PAGE_INDEX, idxPage);
                    listener.onLeftBtnClicked(args);
                }
            }
        });

        mBtnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                safeDismissDialog();
                if (listener != null) {
                    Bundle args = new Bundle();
                    args.putInt(DialogKeys.KEY_PAGE_INDEX, idxPage);
                    listener.onRightBtnClicked(args);
                }
            }
        });

        mDialog.setContentView(dialogView);

        Window window = mDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        setDimAlpha(window, 0.35f);

        mDialog.show();
    }

    public static void showFAQ3Record(final Context context, final int recordTime, final IDialogListener listener) {
        safeDismissDialog();

        mDialog = new Dialog(context);

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(false);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_faq_3_record, null);

        final TextView mContent = (TextView) dialogView.findViewById(R.id.dialog_faq_record_content);
        TextView mBtnApply = (TextView) dialogView.findViewById(R.id.dialog_btn_apply);

        mContent.post(new Runnable() {
            private long milliseconds = 0;

            @Override
            public void run() {
                milliseconds += 1000;
                int duration = (int) (milliseconds / 1000);
                int sec = duration % 60;
                int min = duration / 60;
                String durationText = ((min > 0 ? min : "00") + ":" + (sec > 9 ? sec : ("0" + sec)));
                mContent.setText(durationText);
                mContent.postDelayed(this, 1000);
            }
        });

        if (recordTime == 3) {
            mBtnApply.setText("DONE (3/3)");
        } else {
            mBtnApply.setText(String.format("OK (%s/3)", recordTime));
        }

        mBtnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                safeDismissDialog();
                if (listener != null) {
                    Bundle args = new Bundle();
                    args.putInt(DialogKeys.KEY_COUNTER, recordTime);
                    listener.onApply(args);
                }
            }
        });

        mDialog.setContentView(dialogView);

        Window window = mDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        setDimAlpha(window, 0.35f);

        mDialog.show();
    }

    public static void showFAQ3RecordDone(final Context context, final IDialogListener listener) {
        safeDismissDialog();

        mDialog = new Dialog(context);

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(false);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_faq_3_record_done, null);

        View mBtnApply = dialogView.findViewById(R.id.dialog_btn_apply);

        mBtnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                safeDismissDialog();
                if (listener != null) {
                    Bundle args = new Bundle();
                    listener.onApply(args);
                }
            }
        });

        mDialog.setContentView(dialogView);

        Window window = mDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        setDimAlpha(window, 0.35f);

        mDialog.show();
    }

    public static void showUpdateApp(final Context context, final IDialogListener listener) {
        safeDismissDialog();

        mDialog = new Dialog(context);

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(true);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_update_app, null);

        View mBtnApply = dialogView.findViewById(R.id.dialog_btn_apply);

        mBtnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VersionControlUtil.openUpdatePage(context);
                safeDismissDialog();
                if (listener != null) {
                    Bundle args = new Bundle();
                    listener.onApply(args);
                }
            }
        });

        mDialog.setContentView(dialogView);

        Window window = mDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        setDimAlpha(window, 0.35f);

        mDialog.show();
    }

    public static void showMoreCommands(final Context context, final IDialogListener listener) {
        safeDismissDialog();

        mDialog = new Dialog(context);

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(true);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_more_commands, null);

        View mBtnApply = dialogView.findViewById(R.id.dialog_btn_apply);
        View mBtnCancel = dialogView.findViewById(R.id.dialog_btn_cancel);

        mBtnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                safeDismissDialog();
                if (listener != null) {
                    Bundle args = new Bundle();
                    listener.onApply(args);
                }
            }
        });

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                safeDismissDialog();
                if (listener != null) {
                    listener.onCancel();
                }
            }
        });

        mDialog.setContentView(dialogView);

        Window window = mDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        setDimAlpha(window, 0.35f);

        mDialog.show();
    }

    public static void dismiss() {
        safeDismissDialog();
    }


    private static void setDefaultLayout(Window window) {
        try {
            if (window != null) {
                window.setBackgroundDrawableResource(android.R.color.transparent);
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            }
        } catch (Exception ignore) {
        }
    }

    /**
     * @param alpha from 0.0f to 1.0f
     **/
    private static void setDimAlpha(Window window, float alpha) {
        try {
            if (window != null) {
                WindowManager.LayoutParams layoutParams = window.getAttributes();
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                layoutParams.dimAmount = alpha;
                window.setAttributes(layoutParams);
            }
        } catch (Exception ignore) {
        }
    }

    private static void enableSoftInput(Window window) {
        try {
            if (window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }
        } catch (Exception ignore) {
        }
    }


    public interface IExpandCancelDialogListener extends IBaseDialogListener {
        void onApply(Bundle args);

        void onCancel(Bundle args);
    }

    public interface IDialogListener extends IBaseDialogListener {
        void onApply(Bundle args);

        void onCancel();
    }

    private interface IBaseDialogListener {
        void onApply(Bundle args);
    }

    public interface IDialogLeftRightListener {
        void onShow();

        void onDismiss();

        void onLeftBtnClicked(Bundle args);

        void onRightBtnClicked(Bundle args);

        void onCancel();
    }
}
