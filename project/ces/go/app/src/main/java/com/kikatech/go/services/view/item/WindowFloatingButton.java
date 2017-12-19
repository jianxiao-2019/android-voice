package com.kikatech.go.services.view.item;

import android.support.v4.view.ViewCompat;
import android.view.View;

/**
 * @author SkeeterWang Created on 2017/12/18.
 */

@SuppressWarnings("WeakerAccess")
public abstract class WindowFloatingButton extends WindowFloatingItem implements IFloatingBtn {

    protected static final long ANIMATION_DURATION = 200;

    public WindowFloatingButton(View view, View.OnTouchListener listener) {
        super(view, listener);
    }

    @Override
    public void show() {
        mItemView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hide() {
        mItemView.setVisibility(View.GONE);
    }

    @Override
    public boolean isShowing() {
        return mItemView.getVisibility() == View.VISIBLE;
    }

    protected void animatePopUp() {
        mItemView.setVisibility(View.VISIBLE);
        mItemView.setY(getViewHeight());
        ViewCompat.animate(mItemView)
                .translationY(-1.0f)
                .setStartDelay(10)
                .setDuration(ANIMATION_DURATION);
    }

    protected void animatePopDown() {
        mItemView.setY(0);
        ViewCompat.animate(mItemView)
                .translationY(getViewHeight())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mItemView.setVisibility(View.GONE);
                    }
                })
                .setStartDelay(10)
                .setDuration(ANIMATION_DURATION);
    }
}
