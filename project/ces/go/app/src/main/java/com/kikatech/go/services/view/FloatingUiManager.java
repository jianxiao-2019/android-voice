package com.kikatech.go.services.view;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.kikatech.go.R;
import com.kikatech.go.view.FlexibleOnTouchListener;
import com.kikatech.go.services.view.item.BtnClose;
import com.kikatech.go.services.view.item.BtnOpenApp;
import com.kikatech.go.services.view.item.ItemGMap;
import com.kikatech.go.services.view.item.ItemMsg;
import com.kikatech.go.services.view.item.ItemTip;
import com.kikatech.go.services.view.item.WindowFloatingButton;
import com.kikatech.go.services.view.item.WindowFloatingItem;
import com.kikatech.go.ui.ResolutionUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.view.GoLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SkeeterWang Created on 2017/12/18.
 */

@SuppressWarnings("SuspiciousNameCombination")
public class FloatingUiManager {
    private static final String TAG = "FloatingUiManager";

    private static final int GMAP_MARGIN_DP = 14;

    private int DEVICE_WIDTH;
    private int DEVICE_HEIGHT;

    private final Context mContext;
    private final WindowManager mWindowManager;
    private final LayoutInflater mLayoutInflater;
    private Configuration mConfiguration;
    private IOnFloatingItemAction mListener;
    private final Handler mUiHandler = new Handler(Looper.getMainLooper());

    private WindowManagerContainer mContainer;
    private ItemGMap mItemGMap;
    private ItemTip mItemTip;
    private ItemMsg mItemMsg;

    private BtnClose mBtnClose;
    private BtnOpenApp mBtnOpenApp;
    private List<WindowFloatingButton> mButtonList = new ArrayList<>();

    private boolean isTipViewShown;

    private int mGravity = Gravity.LEFT;


    private FlexibleOnTouchListener onGMapTouchListener = new FlexibleOnTouchListener(100, new FlexibleOnTouchListener.ITouchListener() {
        private int[] viewOriginalXY = new int[2];
        private float[] eventOriginalXY = new float[2];
        private int[] deltaXY = new int[2];
        private WindowFloatingButton mLastEnteredBtn;
        private boolean buttonShown;

        @Override
        public void onLongPress(View view, MotionEvent event) {
        }

        @Override
        public void onShortPress(View view, MotionEvent event) {
        }

        @Override
        public void onClick(View view, MotionEvent event) {
            if (mListener != null) {
                mListener.onGMapClicked();
            }
        }

        @Override
        public void onDown(View view, MotionEvent event) {
            viewOriginalXY = mItemGMap.getViewXY();
            eventOriginalXY = new float[]{event.getRawX(), event.getRawY()};
            mItemTip.setViewVisibility(View.GONE);
            mItemMsg.setViewVisibility(View.GONE);
            buttonShown = false;
        }

        @Override
        public void onMove(View view, MotionEvent event, long timeSpentFromStart) {
            deltaXY = new int[]{(int) (event.getRawX() - eventOriginalXY[0]), (int) (event.getRawY() - eventOriginalXY[1])};
            int targetX = getValidX(viewOriginalXY[0], deltaXY[0]);
            int targetY = getValidY(viewOriginalXY[1], deltaXY[1]);
            mContainer.moveItem(mItemGMap, targetX, targetY);

            if (!buttonShown) {
                if (mContainer.distance(deltaXY, viewOriginalXY) > ResolutionUtil.dp2px(mContext, 20)) {
                    showButtons();
                    buttonShown = true;
                }
            } else {
                WindowFloatingButton enteredItem = getNearestBtn(mItemGMap);
                if (enteredItem != null) {
                    mLastEnteredBtn = enteredItem;
                    mLastEnteredBtn.onEnter();
                    mItemGMap.setAlpha(0.5f);
                } else if (mLastEnteredBtn != null) {
                    mItemGMap.setAlpha(1.0f);
                    mLastEnteredBtn.onLeaved();
                }
            }
        }

        @Override
        public void onUp(View view, MotionEvent event, long timeSpentFromStart) {
            WindowFloatingButton enteredBtn = getNearestBtn(mItemGMap);
            if (enteredBtn != null) {
                enteredBtn.onSelected();
                enteredBtn.onLeaved();
            }
            mItemGMap.setAlpha(1.0f);
            int gmapX;
            int gmapY = ResolutionUtil.dp2px(mContext, GMAP_MARGIN_DP);
            if (mItemGMap.getViewX() > getDeviceWidthByOrientation() / 2) {
                mGravity = Gravity.LEFT;
                int deviceWidth = getDeviceWidthByOrientation();
                int itemWidth = mItemGMap.getMeasuredWidth();
                gmapX = deviceWidth - itemWidth - ResolutionUtil.dp2px(mContext, GMAP_MARGIN_DP);
            } else {
                mGravity = Gravity.RIGHT;
                gmapX = ResolutionUtil.dp2px(mContext, GMAP_MARGIN_DP);
            }
            mContainer.moveItem(mItemGMap, gmapX, gmapY);
            hideButtons();
            mItemTip.setViewVisibility(View.VISIBLE);
            mItemMsg.setViewVisibility(View.VISIBLE);
        }

        private int getValidX(int viewOriginalX, int deltaX) {
            int boundLeft = 0;
            int boundRight = getDeviceWidthByOrientation() - mItemGMap.getItemView().getMeasuredWidth();
            return (deltaX > 0)
                    ? (viewOriginalX + deltaX < boundRight) ? viewOriginalX + deltaX : boundRight
                    : (viewOriginalX + deltaX >= boundLeft) ? viewOriginalX + deltaX : boundLeft;
        }

        private int getValidY(int viewOriginalY, int deltaY) {
            int boundTop = 0;
            int boundBottom = getDeviceHeightByOrientation() - mItemGMap.getItemView().getMeasuredHeight();
            return (deltaY > 0)
                    ? (viewOriginalY + deltaY < boundBottom) ? viewOriginalY + deltaY : boundBottom
                    : (viewOriginalY + deltaY >= boundTop) ? viewOriginalY + deltaY : boundTop;
        }
    });


    private FloatingUiManager(Context context, WindowManager manager, LayoutInflater inflater, Configuration configuration, IOnFloatingItemAction listener) {
        this.mContext = context;
        this.mWindowManager = manager;
        this.mLayoutInflater = inflater;
        this.mListener = listener;

        mConfiguration = configuration;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);

        switch (mConfiguration.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                DEVICE_HEIGHT = displayMetrics.widthPixels;
                DEVICE_WIDTH = displayMetrics.heightPixels;
                break;
            default:
            case Configuration.ORIENTATION_PORTRAIT:
                DEVICE_WIDTH = displayMetrics.widthPixels;
                DEVICE_HEIGHT = displayMetrics.heightPixels;
                break;
        }

        mContainer = new WindowManagerContainer(mWindowManager);

        initItems();
        initButtons();
    }

    private void initItems() {
        mItemGMap = new ItemGMap(inflate(R.layout.go_layout_gmap), onGMapTouchListener);
        mItemTip = new ItemTip(inflate(R.layout.go_layout_gmap_tip), null);
        mItemMsg = new ItemMsg(inflate(R.layout.go_layout_gmap_msg), null);
    }

    private void initButtons() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "initButtons");
        }
        mBtnClose = new BtnClose(inflate(R.layout.go_layout_gmap_btn_close), null);
        mBtnOpenApp = new BtnOpenApp(inflate(R.layout.go_layout_gmap_btn_open_app), null);

        mButtonList.add(mBtnClose);
        mButtonList.add(mBtnOpenApp);

        mBtnClose.setGravity(Gravity.TOP | Gravity.LEFT);
        mBtnOpenApp.setGravity(Gravity.TOP | Gravity.LEFT);

        mContainer.addItem(mBtnClose);
        mContainer.addItem(mBtnOpenApp);
    }

    private void resetButtons() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "resetButtons");
        }

        final int BUTTON_SIZE_DP = 76;

        int deviceWidth = getDeviceWidthByOrientation();
        int yOffset = ResolutionUtil.dp2px(mContext, 18 + BUTTON_SIZE_DP) + ResolutionUtil.getStatusBarHeight(mContext);
        int y = getDeviceHeightByOrientation() - yOffset;
        int fixedDistance, firstBtnX, secondBtnX;

//        switch (mConfiguration.orientation) {
//            case Configuration.ORIENTATION_LANDSCAPE:
//                fixedDistance = ResolutionUtil.dp2px(mContext, 30);
//                firstBtnX = deviceWidth / 3 - mBtnClose.getMeasuredWidth() / 2 + fixedDistance;
//                secondBtnX = deviceWidth * 2 / 3 - mBtnOpenApp.getMeasuredWidth() / 2;
//                break;
//            case Configuration.ORIENTATION_PORTRAIT:
//            default:
//                fixedDistance = ResolutionUtil.dp2px(mContext, 5);
//                firstBtnX = (int) (deviceWidth / 3 - mBtnClose.getMeasuredWidth() / 2 - (fixedDistance * 1.5f));
//                secondBtnX = deviceWidth * 2 / 3 - mBtnOpenApp.getMeasuredWidth() / 2;
//                break;
//        }

        firstBtnX = (deviceWidth - ResolutionUtil.dp2px(mContext, BUTTON_SIZE_DP * 2 + 70)) / 2;
        secondBtnX = firstBtnX + ResolutionUtil.dp2px(mContext, BUTTON_SIZE_DP + 70);

        mBtnClose.setViewXY(firstBtnX, y);
        mBtnClose.setViewHeight(yOffset);

        mBtnOpenApp.setViewXY(secondBtnX, y);
        mBtnOpenApp.setViewHeight(yOffset);

        mContainer.requestLayout(mBtnClose);
        mContainer.requestLayout(mBtnOpenApp);
    }

    private void showButtons() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "showButtons");
        }
        resetButtons();
        for (WindowFloatingButton btn : mButtonList) {
            btn.show();
        }
    }

    private void hideButtons() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "hideButtons");
        }
        for (WindowFloatingButton btn : mButtonList) {
            btn.hide();
        }
    }

    private WindowFloatingButton getNearestBtn(WindowFloatingItem item) {
        double minDistance = Double.MAX_VALUE;
        WindowFloatingButton nearestItem = null;
        for (WindowFloatingButton btn : mButtonList) {
            if (btn.isShowing()) {
                double MIN_DISTANCE = (btn.getMeasuredWidth() / 2 + item.getMeasuredWidth() / 2);
                double distance = mContainer.distance(item, btn);
                if (distance < MIN_DISTANCE && distance < minDistance) {
                    minDistance = distance;
                    nearestItem = btn;
                }
            }
        }
        return nearestItem;
    }


    private Runnable removeTipViewRunnable = new Runnable() {
        @Override
        public void run() {
            mContainer.removeItem(mItemTip);
        }
    };

    private Runnable removeMsgViewRunnable = new Runnable() {
        @Override
        public void run() {
            mContainer.removeItem(mItemMsg);
        }
    };


    public synchronized void showGMap() {
        if (mContainer.isViewAdded(mItemGMap)) {
            return;
        }

        int deviceWidth = getDeviceWidthByOrientation();
        int itemWidth = mItemGMap.getMeasuredWidth();

        mItemGMap.setGravity(Gravity.TOP | Gravity.LEFT);
        mItemGMap.setViewX(deviceWidth - itemWidth - ResolutionUtil.dp2px(mContext, GMAP_MARGIN_DP));
        mItemGMap.setViewY(ResolutionUtil.dp2px(mContext, GMAP_MARGIN_DP));
        mContainer.addItem(mItemGMap);

        isTipViewShown = false;
    }

    public synchronized void removeGMap() {
        mContainer.removeItem(mItemGMap);
        mContainer.removeItem(mItemTip);
        mContainer.removeItem(mItemMsg);
        removeCallbacks(removeTipViewRunnable);
        removeCallbacks(removeMsgViewRunnable);
    }

    public synchronized void showTipView() {
        if (mContainer.isViewAdded(mItemTip)) {
            mContainer.removeItem(mItemTip);
            removeCallbacks(removeTipViewRunnable);
        }

        int deviceWidth = getDeviceWidthByOrientation();
        int itemWidth = mItemTip.getMeasuredWidth();

        mItemTip.setGravity(Gravity.TOP | mGravity);
        mItemTip.setViewX(deviceWidth - itemWidth - ResolutionUtil.dp2px(mContext, 82));
        mItemTip.setViewY(ResolutionUtil.dp2px(mContext, 78) - ResolutionUtil.getStatusBarHeight(mContext));
        mItemTip.setAnimation(android.R.style.Animation_Toast);
        mItemTip.updateBackgroundRes(mGravity);

        mContainer.addItem(mItemTip);

        postDelay(removeTipViewRunnable, 4000);
    }

    public synchronized void showMsgView(String text) {
        if (mContainer.isViewAdded(mItemMsg)) {
            mContainer.removeItem(mItemMsg);
            removeCallbacks(removeMsgViewRunnable);
        }

        mItemMsg.setText(text);

        int deviceWidth = getDeviceWidthByOrientation();
        int itemWidth = mItemMsg.getMeasuredWidth();

        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("deviceWidth: %1$s, itemWidth: %2$s", deviceWidth, itemWidth));
        }

        mItemMsg.setGravity(Gravity.TOP | mGravity);
        mItemMsg.setViewX(deviceWidth - itemWidth - ResolutionUtil.dp2px(mContext, 82));
        mItemMsg.setViewY(ResolutionUtil.dp2px(mContext, 78) - ResolutionUtil.getStatusBarHeight(mContext));
        mItemMsg.setAnimation(android.R.style.Animation_Toast);
        mItemMsg.updateBackgroundRes(mGravity);

        mContainer.addItem(mItemMsg);

        postDelay(removeMsgViewRunnable, 2800);
    }


    public void handleStatusChanged(GoLayout.ViewStatus status) {
        if (!mContainer.isViewAdded(mItemGMap) || status == null) {
            return;
        }

        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, "handleStatusChanged: status: " + status.name());
        }

        mItemGMap.updateStatus(mContext, status);

        if (!isTipViewShown) {
            showTipView();
            isTipViewShown = true;
        }
    }

    public void handleMsgChanged(String text) {
        if (!mContainer.isViewAdded(mItemGMap) || TextUtils.isEmpty(text)) {
            return;
        }
        showMsgView(text);
    }


    public synchronized void hideAllItems() {
        if (mContainer.isViewAdded(mItemGMap)) {
            mItemGMap.setViewVisibility(View.GONE);
            mItemTip.setViewVisibility(View.GONE);
            mItemMsg.setViewVisibility(View.GONE);
        }
    }

    public synchronized void showAllItems() {
        if (mContainer.isViewAdded(mItemGMap)) {
            mItemGMap.setViewVisibility(View.VISIBLE);
            mItemTip.setViewVisibility(View.VISIBLE);
            mItemMsg.setViewVisibility(View.VISIBLE);
        }
    }


    public synchronized void updateConfiguration(Configuration configuration) {
        this.mConfiguration = configuration;
    }

    private int getDeviceWidthByOrientation() {
        try {
            switch (mConfiguration.orientation) {
                default:
                case Configuration.ORIENTATION_PORTRAIT:
                    return DEVICE_WIDTH;
                case Configuration.ORIENTATION_LANDSCAPE:
                    return DEVICE_HEIGHT;
            }
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
        return DEVICE_WIDTH;
    }

    private int getDeviceHeightByOrientation() {
        try {
            switch (mConfiguration.orientation) {
                case Configuration.ORIENTATION_PORTRAIT:
                    return DEVICE_HEIGHT;
                case Configuration.ORIENTATION_LANDSCAPE:
                    return DEVICE_WIDTH;
            }
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
        return DEVICE_HEIGHT;
    }


    private synchronized View inflate(int resId) {
        return inflate(resId, null);
    }

    @SuppressWarnings("SameParameterValue")
    private synchronized View inflate(int resId, ViewGroup root) {
        return mLayoutInflater.inflate(resId, root);
    }


    private synchronized void post(Runnable runnable) {
        mUiHandler.post(runnable);
    }

    private synchronized void postDelay(Runnable runnable, long delayMillis) {
        mUiHandler.postDelayed(runnable, delayMillis);
    }

    private synchronized void removeCallbacks(Runnable runnable) {
        mUiHandler.removeCallbacks(runnable);
    }


    public static final class Builder {
        private WindowManager mWindowManager;
        private LayoutInflater mLayoutInflater;
        private Configuration mConfiguration;
        private IOnFloatingItemAction mListener;

        public Builder setWindowManager(WindowManager manager) {
            this.mWindowManager = manager;
            return this;
        }

        public Builder setLayoutInflater(LayoutInflater inflater) {
            this.mLayoutInflater = inflater;
            return this;
        }

        public Builder setConfiguration(Configuration configuration) {
            this.mConfiguration = configuration;
            return this;
        }

        public Builder setOnFloatingItemAction(IOnFloatingItemAction listener) {
            this.mListener = listener;
            return this;
        }

        public FloatingUiManager build(Context context) {
            return new FloatingUiManager(context, mWindowManager, mLayoutInflater, mConfiguration, mListener);
        }
    }


    public interface IOnFloatingItemAction {
        void onGMapClicked();
    }
}
