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
import com.kikatech.go.services.view.item.ItemWakeUpTip;
import com.kikatech.go.services.view.item.WindowFloatingButton;
import com.kikatech.go.services.view.item.WindowFloatingItem;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.MathUtil;
import com.kikatech.go.util.ResolutionUtil;
import com.kikatech.go.util.TipsHelper;
import com.kikatech.go.util.preference.GlobalPref;
import com.kikatech.go.view.FlexibleOnTouchListener;
import com.kikatech.go.view.GoLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SkeeterWang Created on 2017/12/18.
 */

@SuppressWarnings({"SuspiciousNameCombination", "RtlHardcoded", "SameParameterValue", "WeakerAccess"})
public class FloatingUiManager extends BaseFloatingManager {
    private static final String TAG = "FloatingUiManager";

    private static final int GMAP_MARGIN_DP = 14;

    private IOnFloatingItemAction mListener;

    private ItemGMap mItemGMap;
    private ItemWakeUpTip mItemWakeUpTip;
    private ItemTip mItemTip;
    private ItemAsrResult mItemAsrResult;
    private ItemMsg mItemMsg;

    private BtnClose mBtnClose;
    private BtnOpenApp mBtnOpenApp;
    private List<WindowFloatingButton> mButtonList = new ArrayList<>();

    private boolean isWakeUpTipViewShown;
    private boolean isGMapShown;

    private int mGravity = Gravity.RIGHT;


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
            mItemWakeUpTip.setViewVisibility(View.GONE);
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
                if (MathUtil.distance(deltaXY, viewOriginalXY) > ResolutionUtil.dp2px(mContext, 20)) {
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
            int gmapY = mItemGMap.getViewY();
            if (mItemGMap.getViewX() > getDeviceWidthByOrientation() / 2) {
                mGravity = Gravity.RIGHT;
                int deviceWidth = getDeviceWidthByOrientation();
                int itemWidth = mItemGMap.getMeasuredWidth();
                gmapX = deviceWidth - itemWidth - ResolutionUtil.dp2px(mContext, GMAP_MARGIN_DP);
            } else {
                mGravity = Gravity.LEFT;
                gmapX = ResolutionUtil.dp2px(mContext, GMAP_MARGIN_DP);
            }
            mContainer.moveItem(mItemGMap, gmapX, gmapY);
            hideButtons();

            updateItemWakeUpTipOnUp();
            updateItemOnUp(mItemTip);
            updateItemOnUp(mItemAsrResult);
            updateItemOnUp(mItemMsg);
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
    });


    private FloatingUiManager(Context context, WindowManager manager, LayoutInflater inflater, Configuration configuration, IOnFloatingItemAction listener) {
        super(context, manager, inflater, configuration);
        this.mListener = listener;
        initItems();
        initButtons();
    }


    @Override
    public synchronized void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        updateGmapByOrientation(mConfiguration.orientation);
    }

    private void updateGmapByOrientation(int orientation) {
        int newX, newY;
        // calculate X position
        switch (mGravity) {
            case Gravity.LEFT:
                newX = ResolutionUtil.dp2px(mContext, GMAP_MARGIN_DP);
                break;
            default:
            case Gravity.RIGHT:
                int deviceWidth = getDeviceWidthByOrientation();
                int itemWidth = mItemGMap.getMeasuredWidth();
                newX = deviceWidth - itemWidth - ResolutionUtil.dp2px(mContext, GMAP_MARGIN_DP);
                break;
        }
        // calculate Y position
        float originalY = (float) mItemGMap.getViewY();
        float scaleRate;
        switch (orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                scaleRate = originalY / getDeviceHeight();
                newY = (int) (scaleRate * getDeviceHeightByOrientation());
                break;
            default:
            case Configuration.ORIENTATION_PORTRAIT:
                scaleRate = originalY / getDeviceWidth();
                newY = (int) (scaleRate * getDeviceHeightByOrientation());
                break;
        }
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, String.format("newX: %s, newY: %s, originalY: %s, scaleRate: %s", newX, newY, originalY, scaleRate));
        }
        mContainer.moveItem(mItemGMap, newX, newY);

        updateItemWakeUpTipOnUp();
        updateItemOnUp(mItemTip);
        updateItemOnUp(mItemAsrResult);
        updateItemOnUp(mItemMsg);
    }

    private void updateItemOnUp(final WindowFloatingItem item) {
        updateItemPosition(item, mItemGMap.getViewY());
    }

    private void updateItemWakeUpTipOnUp() {
        int itemHeight = mItemWakeUpTip.getMeasuredHeight();
        int gmapHeight = mItemGMap.getMeasuredHeight();
        int y = mItemGMap.getViewY() + gmapHeight - itemHeight;
        updateItemPosition(mItemWakeUpTip, y);
    }

    private void updateItemPosition(final WindowFloatingItem item, final int y) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mContainer.isViewAdded(item)) {
                    item.setGravity(Gravity.TOP | mGravity);
                    item.setViewY(y);
                    mContainer.requestLayout(item);
                }
                item.updateBackgroundRes(mGravity);
                item.setViewVisibility(View.VISIBLE);
            }
        });
    }


    private void initItems() {
        mItemGMap = new ItemGMap(inflate(R.layout.go_layout_gmap), onGMapTouchListener);
        mItemWakeUpTip = new ItemWakeUpTip(inflate(R.layout.go_layout_gmap_msg), null);
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
        int firstBtnX, secondBtnX;

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


    private Runnable removeWakeUpTipViewRunnable = new Runnable() {
        @Override
        public void run() {
            mContainer.removeItem(mItemWakeUpTip);
            mItemGMap.hideMsgStatusView();
        }
    };

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

    private synchronized void showGMap() {
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

        isWakeUpTipViewShown = false;
        showAllItems();
    }

    public synchronized void removeGMap() {
        mContainer.removeItem(mItemGMap);
        mContainer.removeItem(mItemWakeUpTip);
        mContainer.removeItem(mItemTip);
        mContainer.removeItem(mItemAsrResult);
        mContainer.removeItem(mItemMsg);
        removeCallbacks(removeTipViewRunnable);
        removeCallbacks(removeAsrResultViewRunnable);
        removeCallbacks(removeMsgViewRunnable);
    }

    private synchronized void showWakeUpTipView() {
        int showsCount = TipsHelper.getFloatingWakeUpTipShowingCount();
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, String.format("Wake up tip has shown %s times", showsCount));
        }
        boolean shouldSticky = showsCount < TipsHelper.WAKE_UP_TIP_STICKY_COUNT;
        long displayMillis;
        if (shouldSticky) {
            TipsHelper.setFloatingWakeUpTipShowingCount(showsCount + 1);
            displayMillis = 0;
        } else {
            displayMillis = 5000;
        }
        showWakeUpTipView(displayMillis);
    }

    private synchronized void showWakeUpTipView(long displayMillis) {
        removeWakeUpTipView();

        mItemWakeUpTip.setText("Say \"Hi Kika\"");

        int deviceWidth = getDeviceWidthByOrientation();
        int itemWidth = mItemWakeUpTip.getMeasuredWidth();
        int itemHeight = mItemWakeUpTip.getMeasuredHeight();
        int gmapWidth = mItemGMap.getMeasuredWidth();
        int gmapHeight = mItemGMap.getMeasuredHeight();


        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("deviceWidth: %1$s, itemWidth: %2$s", deviceWidth, itemWidth));
        }

        mItemWakeUpTip.setGravity(Gravity.TOP | mGravity);
        mItemWakeUpTip.setViewX(gmapWidth + 2 * ResolutionUtil.dp2px(mContext, GMAP_MARGIN_DP));
        mItemWakeUpTip.setViewY(mItemGMap.getViewY() + gmapHeight - itemHeight);
        mItemWakeUpTip.setAnimation(android.R.style.Animation_Toast);
        mItemWakeUpTip.updateBackgroundRes(mGravity);

        mContainer.addItem(mItemWakeUpTip);

        if (displayMillis > 0) {
            postDelay(removeWakeUpTipViewRunnable, displayMillis);
        }
    }

    private synchronized void removeWakeUpTipView() {
        if (mContainer.isViewAdded(mItemWakeUpTip)) {
            mContainer.removeItem(mItemWakeUpTip);
            removeCallbacks(removeWakeUpTipViewRunnable);
        }
    }

    private synchronized void showTipView(String title, String text) {
        removeTipView();

        mItemTip.setTitle(title);
        mItemTip.setText(text);

        int gmapWidth = mItemGMap.getMeasuredWidth();

        mItemTip.setGravity(Gravity.TOP | mGravity);
        mItemTip.setViewX(gmapWidth + 2 * ResolutionUtil.dp2px(mContext, GMAP_MARGIN_DP));
        mItemTip.setViewY(mItemGMap.getViewY());
        mItemTip.setAnimation(android.R.style.Animation_Toast);
        mItemTip.updateBackgroundRes(mGravity);

        mContainer.addItem(mItemTip);

        postDelay(removeTipViewRunnable, 2800);
    }

    private synchronized void removeTipView() {
        if (mContainer.isViewAdded(mItemTip)) {
            mContainer.removeItem(mItemTip);
            removeCallbacks(removeTipViewRunnable);
        }
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
        int gmapWidth = mItemGMap.getMeasuredWidth();

        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("deviceWidth: %1$s, itemWidth: %2$s", deviceWidth, itemWidth));
        }

        mItemAsrResult.setGravity(Gravity.TOP | mGravity);
        mItemAsrResult.setViewX(gmapWidth + 2 * ResolutionUtil.dp2px(mContext, GMAP_MARGIN_DP));
        mItemAsrResult.setViewY(mItemGMap.getViewY());
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
        int gmapWidth = mItemGMap.getMeasuredWidth();

        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("deviceWidth: %1$s, itemWidth: %2$s", deviceWidth, itemWidth));
        }

        mItemMsg.setGravity(Gravity.TOP | mGravity);
        mItemMsg.setViewX(gmapWidth + 2 * ResolutionUtil.dp2px(mContext, GMAP_MARGIN_DP));
        mItemMsg.setViewY(mItemGMap.getViewY());
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

        if (GoLayout.ViewStatus.STAND_BY_AWAKE.equals(status)) {
            removeWakeUpTipView();
        }

        mItemGMap.updateStatus(mContext, status);
    }

    public synchronized void handleAsrResult(String text) {
        if (!mContainer.isViewAdded(mItemGMap) || TextUtils.isEmpty(text)) {
            return;
        }
        removeTipView();
        showAsrResult(text);
    }

    public synchronized void handleMsgChanged(String text) {
        if (!mContainer.isViewAdded(mItemGMap) || TextUtils.isEmpty(text)) {
            return;
        }
        removeTipView();
        showMsgView(text);
    }

    private synchronized void handleTipView(String title, String text) {
        if (!mContainer.isViewAdded(mItemGMap) || TextUtils.isEmpty(title) || TextUtils.isEmpty(text)) {
            return;
        }
        removeTipView();
        showTipView(title, text);
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
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                mItemGMap.setViewVisibility(View.GONE);
                mItemWakeUpTip.setViewVisibility(View.GONE);
                mItemTip.setViewVisibility(View.GONE);
                mItemAsrResult.setViewVisibility(View.GONE);
                mItemMsg.setViewVisibility(View.GONE);
            }
        });
    }

    public synchronized void showAllItems() {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                mItemGMap.setViewVisibility(View.VISIBLE);
                mItemWakeUpTip.setViewVisibility(View.VISIBLE);
                mItemTip.setViewVisibility(View.VISIBLE);
                mItemAsrResult.setViewVisibility(View.VISIBLE);
                mItemMsg.setViewVisibility(View.VISIBLE);

                if (!isWakeUpTipViewShown) {
                    showWakeUpTipView();
                    isWakeUpTipViewShown = true;
                }
            }
        });
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
