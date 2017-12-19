package com.kikatech.go.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.kikatech.go.util.LogUtil;

/**
 * @author wangskeeter Created on 16/9/5.
 */
public class NoPredictiveAnimationManager extends LinearLayoutManager {
    private static final String TAG = "NoPredictiveAnimationManager";

    public NoPredictiveAnimationManager(Context context) {
        super(context);
    }

    public NoPredictiveAnimationManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public NoPredictiveAnimationManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Disable predictive animations. There is a bug in RecyclerView which causes views that
     * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
     * adapter size has decreased since the ViewHolder was recycled.
     */
    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }

    /**
     * due to issue: <p>
     * IndexOutOfBoundsException: Inconsistency detected. Invalid item position <p>
     * <a href="http://blog.csdn.net/qq_28055429/article/details/60879680">IndexOutOfBoundsException: Inconsistency detected. Invalid item position</a>
     */
    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (Exception e) {
            if (LogUtil.DEBUG) LogUtil.printStackTrace(TAG, e.getMessage(), e);
            LogUtil.reportToFabric(e);
        }
    }
}