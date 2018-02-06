package com.kikatech.go.services.view.manager;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.kikatech.go.R;
import com.kikatech.go.services.view.item.BtnClose;
import com.kikatech.go.services.view.item.BtnOpenApp;
import com.kikatech.go.services.view.item.ItemAsrResult;
import com.kikatech.go.services.view.item.ItemGMap;
import com.kikatech.go.services.view.item.ItemMsg;
import com.kikatech.go.services.view.item.ItemTip;
import com.kikatech.go.services.view.item.WindowFloatingButton;
import com.kikatech.go.services.view.item.WindowFloatingItem;
import com.kikatech.go.ui.ResolutionUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.view.FlexibleOnTouchListener;
import com.kikatech.go.view.GoLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SkeeterWang Created on 2017/12/18.
 */

@SuppressWarnings("SuspiciousNameCombination")
public class FloatingUiManager extends BaseFloatingManager {
    private static final String TAG = "FloatingUiManager";

    private static final int GMAP_MARGIN_DP = 14;

    private IOnFloatingItemAction mListener;

    private ItemGMap mItemGMap;
    private ItemTip mItemTip;
    private ItemAsrResult mItemAsrResult;
    private ItemMsg mItemMsg;

    private BtnClose mBtnClose;
    private BtnOpenApp mBtnOpenApp;
    private List<WindowFloatingButton> mButtonList = new ArrayList<>();

    private boolean isTipViewShown;
    private boolean isGMapShown;

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
            mItemAsrResult.setViewVisibility(View.GONE);
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
            mItemAsrResult.setViewVisibility(View.VISIBLE);
            mItemMsg.setViewVisibility(View.VISIBLE);
        }

        private int getValidX(int viewOriginalX, int deltaX) {
            int boundLeft = 0;
            int boundRight = getDeviceWidthByOrientation() - mItemGMap.getMeasuredWidth();
            return (deltaX > 0)
                    ? (viewOriginalX + deltaX < boundRight) ? viewOriginalX + deltaX : boundRight
                    : (viewOriginalX + deltaX >= boundLeft) ? viewOriginalX + deltaX : boundLeft;
        }

        private int getValidY(int viewOriginalY, int deltaY) {
            int boundTop = 0;
            int boundBottom = getDeviceHeightByOrientation() - mItemGMap.getMeasuredHeight();
            return (deltaY > 0)
                    ? (viewOriginalY + deltaY < boundBottom) ? viewOriginalY + deltaY : boundBottom
                    : (viewOriginalY + deltaY >= boundTop) ? viewOriginalY + deltaY : boundTop;
        }
    });


    private FloatingUiManager(Context context, WindowManager manager, LayoutInflater inflater, Configuration configuration, IOnFloatingItemAction listener) {
        super(context, manager, inflater, configuration);
        this.mListener = listener;
        initItems();
        initButtons();
    }

    private void initItems() {
        mItemGMap = new ItemGMap(inflate(R.layout.go_layout_gmap), onGMapTouchListener);
        mItemTip = new ItemTip(inflate(R.layout.go_layout_gmap_tip), null);
        mItemAsrResult = new ItemAsrResult(inflate(R.layout.go_layout_gmap_asr_result), null);
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
            mItemGMap.hideMsgStatusView();
        }
    };

    private Runnable removeAsrResultViewRunnable = new Runnable() {
        @Override
        public void run() {
            mContainer.removeItem(mItemAsrResult);
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
            showAllItems();
            return;
        }

        int deviceWidth = getDeviceWidthByOrientation();
        int itemWidth = mItemGMap.getMeasuredWidth();

        mItemGMap.setGravity(Gravity.TOP | Gravity.LEFT);
        mItemGMap.setViewX(deviceWidth - itemWidth - ResolutionUtil.dp2px(mContext, GMAP_MARGIN_DP));
        mItemGMap.setViewY(ResolutionUtil.dp2px(mContext, GMAP_MARGIN_DP));
        mContainer.addItem(mItemGMap);
        mItemGMap.updateStatus(mContext, GoLayout.ViewStatus.STAND_BY_SLEEP);

        isTipViewShown = false;
    }

    public synchronized void removeGMap() {
        mContainer.removeItem(mItemGMap);
        mContainer.removeItem(mItemTip);
        mContainer.removeItem(mItemAsrResult);
        mContainer.removeItem(mItemMsg);
        removeCallbacks(removeTipViewRunnable);
        removeCallbacks(removeAsrResultViewRunnable);
        removeCallbacks(removeMsgViewRunnable);
    }

    private synchronized void showTipView() {
        showTipView("You can wake me up by saying...", "\"Hi Kika\"", 5000);
    }

    private synchronized void showTipView(String title, String text, long displayMillis) {
        if (mContainer.isViewAdded(mItemTip)) {
            mContainer.removeItem(mItemTip);
            removeCallbacks(removeTipViewRunnable);
        }

        mItemTip.setTitle(title);
        mItemTip.setText(text);

        int deviceWidth = getDeviceWidthByOrientation();
        int itemWidth = mItemTip.getMeasuredWidth();

        mItemTip.setGravity(Gravity.TOP | mGravity);
        mItemTip.setViewX(deviceWidth - itemWidth - ResolutionUtil.dp2px(mContext, 82));
        mItemTip.setViewY(ResolutionUtil.dp2px(mContext, 78) - ResolutionUtil.getStatusBarHeight(mContext));
        mItemTip.setAnimation(android.R.style.Animation_Toast);
        mItemTip.updateBackgroundRes(mGravity);

        mContainer.addItem(mItemTip);

        postDelay(removeTipViewRunnable, displayMillis);
    }

    private synchronized void showAsrResult(String text) {
        boolean isViewShowing = mContainer.isViewAdded(mItemAsrResult);
        if (isViewShowing) {
            removeCallbacks(removeAsrResultViewRunnable);
        }
        if (mContainer.isViewAdded(mItemMsg)) {
            mContainer.removeItem(mItemMsg);
            removeCallbacks(removeMsgViewRunnable);
        }

        mItemAsrResult.setText(String.format("\"%s\"", text));

        int deviceWidth = getDeviceWidthByOrientation();
        int itemWidth = mItemAsrResult.getMeasuredWidth();

        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("deviceWidth: %1$s, itemWidth: %2$s", deviceWidth, itemWidth));
        }

        mItemAsrResult.setGravity(Gravity.TOP | mGravity);
        mItemAsrResult.setViewX(deviceWidth - itemWidth - ResolutionUtil.dp2px(mContext, 82));
        mItemAsrResult.setViewY(ResolutionUtil.dp2px(mContext, 78) - ResolutionUtil.getStatusBarHeight(mContext));
        mItemAsrResult.setAnimation(android.R.style.Animation_Toast);
        mItemAsrResult.updateBackgroundRes(mGravity);

        if (!isViewShowing) {
            mContainer.addItem(mItemAsrResult);
        } else {
            mContainer.requestLayout(mItemAsrResult);
        }

        postDelay(removeAsrResultViewRunnable, 2800);
    }

    private synchronized void showMsgView(String text) {
        if (mContainer.isViewAdded(mItemAsrResult)) {
            mContainer.removeItem(mItemAsrResult);
            removeCallbacks(removeAsrResultViewRunnable);
        }
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


    public synchronized void handleStatusChanged(GoLayout.ViewStatus status) {
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

    public synchronized void handleAsrResult(String text) {
        if (!mContainer.isViewAdded(mItemGMap) || TextUtils.isEmpty(text)) {
            return;
        }
        showAsrResult(text);
    }

    public synchronized void handleMsgChanged(String text) {
        if (!mContainer.isViewAdded(mItemGMap) || TextUtils.isEmpty(text)) {
            return;
        }
        showMsgView(text);
    }

    private synchronized void handleTipView(String title, String text) {
        if (!mContainer.isViewAdded(mItemGMap) || TextUtils.isEmpty(title) || TextUtils.isEmpty(text)) {
            return;
        }
        showTipView(title, text, 2800);
    }

    public synchronized void handleMsgSentStatusChanged(final boolean isSucceed) {
        if (!mContainer.isViewAdded(mItemGMap)) {
            return;
        }
        post(new Runnable() {
            @Override
            public void run() {
                handleTipView("Message", isSucceed ? "Sent successfully" : "Contact not found");
                mItemGMap.showMsgStatusView(mContext, isSucceed);
            }
        });
    }


    public synchronized void hideAllItems() {
        if (mContainer.isViewAdded(mItemGMap)) {
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    mItemGMap.setViewVisibility(View.GONE);
                    mItemTip.setViewVisibility(View.GONE);
                    mItemAsrResult.setViewVisibility(View.GONE);
                    mItemMsg.setViewVisibility(View.GONE);
                }
            });
        }
    }

    public synchronized void showAllItems() {
        if (mContainer.isViewAdded(mItemGMap)) {
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    mItemGMap.setViewVisibility(View.VISIBLE);
                    mItemTip.setViewVisibility(View.VISIBLE);
                    mItemAsrResult.setViewVisibility(View.VISIBLE);
                    mItemMsg.setViewVisibility(View.VISIBLE);
                }
            });
        }
    }

    public synchronized void setShowGMap(boolean shown) {
        isGMapShown = shown;
    }

    public synchronized void updateGMapVisibility() {
        if (isGMapShown) {
            showGMap();
        } else {
            hideAllItems();
        }
    }



    public static final class Builder extends BaseFloatingManager.Builder<Builder> {
        private IOnFloatingItemAction mListener;

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
