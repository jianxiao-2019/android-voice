package com.kikatech.go.util.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.kikatech.go.R;
import com.kikatech.go.util.IntentUtil;
import com.kikatech.go.util.LogUtil;

/**
 * @author SkeeterWang Created on 2017/12/20.
 */

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
                IntentUtil.openBrowser(context, "http://www.kika.ai/#home-section");
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
}
