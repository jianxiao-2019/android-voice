package com.kikatech.go.util.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.UserSettings;
import com.kikatech.go.util.IntentUtil;
import com.kikatech.go.util.LogUtil;

import java.util.ArrayList;
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

    public static void showFAQ(final Context context, final IDialogListener listener) {
        safeDismissDialog();

        mDialog = new Dialog(context);

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(false);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_faq, null);

        final ViewPager mPager = (ViewPager) dialogView.findViewById(R.id.dialog_faq_pager);
        TabLayout mIndicatorView = (TabLayout) dialogView.findViewById(R.id.dialog_faq_pager_indicators);
        final TextView mContent = (TextView) dialogView.findViewById(R.id.dialog_faq_content);
        View mBtnApply = dialogView.findViewById(R.id.dialog_btn_apply);
        View mBtnCancel = dialogView.findViewById(R.id.dialog_btn_cancel);

        final String[] descriptions = context.getResources().getStringArray(R.array.dialog_faq_pager_item_des);

        final FAQsAdapter mAdapter = new FAQsAdapter(context);
        mPager.setAdapter(mAdapter);
        mIndicatorView.setupWithViewPager(mPager);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position < descriptions.length) {
                    mContent.setText(descriptions[position]);
                }
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

    private static class FAQsAdapter extends PagerAdapter {
        private ArrayList<View> mList = new ArrayList<>();

        public FAQsAdapter(Context context) {
            View pagerItem1 = LayoutInflater.from(context).inflate(R.layout.dialog_faq_pager_item_1, null);
            View pagerItem2 = LayoutInflater.from(context).inflate(R.layout.dialog_faq_pager_item_1, null);
            View pagerItem3 = LayoutInflater.from(context).inflate(R.layout.dialog_faq_pager_item_1, null);
            View pagerItem4 = LayoutInflater.from(context).inflate(R.layout.dialog_faq_pager_item_1, null);

            Drawable drawable1 = context.getResources().getDrawable(R.drawable.bg_dialog_usb_detached);
            drawable1.setColorFilter(Color.BLUE, PorterDuff.Mode.SRC);
            pagerItem1.setBackground(drawable1);

            Drawable drawable2 = context.getResources().getDrawable(R.drawable.bg_dialog_usb_detached);
            drawable2.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC);
            pagerItem2.setBackground(drawable2);

            Drawable drawable3 = context.getResources().getDrawable(R.drawable.bg_dialog_usb_detached);
            drawable3.setColorFilter(Color.RED, PorterDuff.Mode.SRC);
            pagerItem3.setBackground(drawable3);

            Drawable drawable4 = context.getResources().getDrawable(R.drawable.bg_dialog_usb_detached);
            drawable4.setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC);
            pagerItem4.setBackground(drawable4);

            mList.add(pagerItem1);
            mList.add(pagerItem2);
            mList.add(pagerItem3);
            mList.add(pagerItem4);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mList.get(position));
            return mList.get(position);
        }
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
