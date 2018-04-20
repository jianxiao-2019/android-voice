package com.kikatech.go.services.view.item;

import android.view.View;
import android.view.WindowManager;

/**
 * @author SkeeterWang Created on 2017/12/18.
 */

@SuppressWarnings("WeakerAccess")
public abstract class WindowFloatingItem implements IFloatingItem {

    protected abstract void bindView();

    protected abstract WindowManager.LayoutParams getDefaultParam();

    protected View mItemView;
    private WindowManager.LayoutParams mLayoutParams;

    public WindowFloatingItem(View view, View.OnTouchListener listener) {
        this.mItemView = view;
        WindowManager.LayoutParams defaultParams = getDefaultParam();
        mLayoutParams = new WindowManager.LayoutParams();
        if (defaultParams != null) {
            mLayoutParams.copyFrom(defaultParams);
        }
        bindView();
        mItemView.setOnTouchListener(listener);
    }

    @Override
    public View getItemView() {
        return mItemView;
    }

    @Override
    public void setViewX(int x) {
        this.mLayoutParams.x = x;
    }

    @Override
    public void setViewY(int y) {
        this.mLayoutParams.y = y;
    }

    public void setViewXY(int[] xy) {
        setViewXY(xy[0], xy[1]);
    }

    public void setViewXY(int x, int y) {
        setViewX(x);
        setViewY(y);
    }

    @Override
    public void setViewHeight(int height) {
        this.mLayoutParams.height = height;
    }

    @Override
    public void setViewWidth(int width) {
        this.mLayoutParams.width = width;
    }

    @Override
    public int getViewX() {
        return mLayoutParams.x;
    }

    @Override
    public int getViewY() {
        return mLayoutParams.y;
    }

    @Override
    public int getViewWidth() {
        return mLayoutParams.width;
    }

    @Override
    public int getViewHeight() {
        return mLayoutParams.height;
    }

    @Override
    public void setViewVisibility(int visibility) {
        try {
            mItemView.setVisibility(visibility);
        } catch (Exception ignore) {
        }
    }

    @Override
    public int getViewVisibility() {
        return mItemView.getVisibility();
    }

    public int[] getViewXY() {
        return new int[]{mLayoutParams.x, mLayoutParams.y};
    }

    public void setGravity(int gravity) {
        mLayoutParams.gravity = gravity;
    }

    public void setAnimation(int animation) {
        mLayoutParams.windowAnimations = animation;
    }

    public void setAlpha(float alpha) {
        mLayoutParams.alpha = alpha;
    }

    public int getMeasuredWidth() {
        mItemView.measure(0, 0);
        return mItemView.getMeasuredWidth();
    }

    public int getMeasuredHeight() {
        mItemView.measure(0, 0);
        return mItemView.getMeasuredHeight();
    }

    @SuppressWarnings("unchecked")
    @Override
    public WindowManager.LayoutParams getLayoutParams() {
        return mLayoutParams;
    }
}