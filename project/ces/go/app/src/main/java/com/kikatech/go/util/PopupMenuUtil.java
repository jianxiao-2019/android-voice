package com.kikatech.go.util;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.UserSettings;

/**
 * @author SkeeterWang Created on 2017/12/27.
 */

public class PopupMenuUtil {
    private static final String TAG = "PopupMenuUtil";

    public static void showDrawerAdvancedConfirmationPopup(Context context, View anchorView, final IPopupListener listener) {
        final PopupWindow popupWindow = new PopupWindow(context);

        View menuView = LayoutInflater.from(context).inflate(R.layout.go_layout_drawer_advanced_confirmation_popup_menu, null);

        menuView.findViewById(R.id.item_auto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserSettings.saveSettingConfirmCounter(true);
                popupWindow.dismiss();
            }
        });

        menuView.findViewById(R.id.item_manually).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserSettings.saveSettingConfirmCounter(false);
                popupWindow.dismiss();
            }
        });

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (listener != null) {
                    listener.onDismiss();
                }
            }
        });

        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setContentView(menuView);

        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);

        menuView.measure(0, 0);

        popupWindow.showAsDropDown(anchorView, 0 - menuView.getMeasuredWidth(), ResolutionUtil.dp2px(context, 10), Gravity.RIGHT);
    }

    public static void showDrawerNavigationConfirmPopup(Context context, View anchorView, final IPopupListener listener) {
        final PopupWindow popupWindow = new PopupWindow(context);

        View menuView = LayoutInflater.from(context).inflate(R.layout.go_layout_drawer_navigation_confirm_popup_menu, null);

        menuView.findViewById(R.id.item_skip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserSettings.saveSettingConfirmDestination(false);
                popupWindow.dismiss();
            }
        });

        menuView.findViewById(R.id.item_ask).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserSettings.saveSettingConfirmDestination(true);
                popupWindow.dismiss();
            }
        });

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (listener != null) {
                    listener.onDismiss();
                }
            }
        });

        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setContentView(menuView);

        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);

        menuView.measure(0, 0);

        int menuWidth = menuView.getMeasuredWidth();
        int menuHeight = menuView.getMeasuredHeight();

        int extraOff = ResolutionUtil.dp2px(context, 10);
        int deviceHeight = context.getResources().getDisplayMetrics().heightPixels;
        int xOff = 0 - menuWidth;
        int yOff;
        if (location[1] + menuHeight + extraOff > deviceHeight) {
            yOff = 0 - menuHeight - anchorView.getMeasuredHeight() - extraOff;
        } else {
            yOff = extraOff;
        }

        popupWindow.showAsDropDown(anchorView, xOff, yOff, Gravity.RIGHT);
    }


    public interface IPopupListener {
        void onDismiss();
    }
}
