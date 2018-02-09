package com.kikatech.go.services.view.manager;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.kikatech.go.R;
import com.kikatech.go.services.view.item.ItemGMap;
import com.kikatech.go.services.view.item.ItemOobe;
import com.kikatech.go.ui.KikaPermissionsActivity;
import com.kikatech.go.ui.ResolutionUtil;
import com.kikatech.go.util.IntentUtil;
import com.kikatech.go.view.FlexibleOnTouchListener;
import com.kikatech.go.view.GoLayout;

/**
 * @author SkeeterWang Created on 2018/2/9.
 */

public class FloatingOobeManager extends BaseFloatingManager {

    private ItemOobe mItemOobe;

    private FlexibleOnTouchListener mOnTouchListener = new FlexibleOnTouchListener(100, new FlexibleOnTouchListener.ITouchListener() {
        @Override
        public void onLongPress(View view, MotionEvent event) {
        }

        @Override
        public void onShortPress(View view, MotionEvent event) {
        }

        @Override
        public void onClick(View view, MotionEvent event) {
            Intent intent = new Intent(mContext, KikaPermissionsActivity.class);
            IntentUtil.sendPendingIntent(mContext, intent);
        }

        @Override
        public void onDown(View view, MotionEvent event) {
        }

        @Override
        public void onMove(View view, MotionEvent event, long timeSpentFromStart) {
        }

        @Override
        public void onUp(View view, MotionEvent event, long timeSpentFromStart) {
        }
    });


    FloatingOobeManager(Context context, WindowManager manager, LayoutInflater inflater, Configuration configuration) {
        super(context, manager, inflater, configuration);
        initItems();
    }

    private void initItems() {
        mItemOobe = new ItemOobe(inflate(R.layout.floating_oobe_item, null), mOnTouchListener);
    }


    public synchronized void addOobeUi() {
        if (mContainer.isViewAdded(mItemOobe)) {
            return;
        }

        int deviceHeight = getDeviceHeightByOrientation();
        int itemWidth = mItemOobe.getMeasuredWidth();

        mItemOobe.setGravity(Gravity.TOP | Gravity.LEFT);
        mItemOobe.setViewX(0);
        mItemOobe.setViewY(deviceHeight / 3 - itemWidth / 2);
        mItemOobe.setViewVisibility(View.GONE);
        mContainer.addItem(mItemOobe);
    }

    public synchronized void removeOobeUi() {
        mContainer.removeItem(mItemOobe);
    }

    public synchronized void showOobeUi() {
        if (!mContainer.isViewAdded(mItemOobe) || mItemOobe.getViewVisibility() == View.VISIBLE) {
            return;
        }
        mItemOobe.setViewVisibility(View.VISIBLE);
    }

    public synchronized void hideOobeUi() {
        if (!mContainer.isViewAdded(mItemOobe) || mItemOobe.getViewVisibility() == View.GONE) {
            return;
        }
        mItemOobe.setViewVisibility(View.GONE);
    }


    public static final class Builder extends BaseFloatingManager.Builder<Builder> {
        public FloatingOobeManager build(Context context) {
            return new FloatingOobeManager(context, mWindowManager, mLayoutInflater, mConfiguration);
        }
    }
}
