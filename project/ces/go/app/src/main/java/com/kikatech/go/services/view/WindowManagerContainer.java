package com.kikatech.go.services.view;

import android.view.View;
import android.view.WindowManager;

import com.kikatech.go.services.view.item.WindowFloatingItem;
import com.kikatech.go.util.MathUtil;

/**
 * @author SkeeterWang Created on 2017/12/18.
 */

@SuppressWarnings("SuspiciousNameCombination")
public class WindowManagerContainer implements IFloatingContainer<WindowFloatingItem> {
    private static final String TAG = "WindowManagerContainer";

    private WindowManager mWindowManager;

    public WindowManagerContainer(WindowManager manager) {
        this.mWindowManager = manager;
    }

    @Override
    public synchronized void setItemX(WindowFloatingItem item, int xPosition) {
        if (isViewAdded(item)) {
            item.setViewX(xPosition);
            try {
                mWindowManager.updateViewLayout(item.getItemView(), item.getLayoutParams());
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public synchronized void setItemY(WindowFloatingItem item, int yPosition) {
        if (isViewAdded(item)) {
            item.setViewX(yPosition);
            try {
                mWindowManager.updateViewLayout(item.getItemView(), item.getLayoutParams());
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public synchronized int getItemX(WindowFloatingItem item) {
        if (isViewAdded(item)) {
            return item.getViewX();
        }
        return 0;
    }

    @Override
    public synchronized int getItemY(WindowFloatingItem item) {
        if (isViewAdded(item)) {
            return item.getViewY();
        }
        return 0;
    }

    @Override
    public synchronized void addItem(WindowFloatingItem item) {
        if (!isViewAdded(item)) {
            try {
                mWindowManager.addView(item.getItemView(), item.getLayoutParams());
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public synchronized void removeItem(WindowFloatingItem item) {
        if (isViewAdded(item)) {
            try {
                mWindowManager.removeView(item.getItemView());
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public synchronized void moveItem(WindowFloatingItem item, int xPosition, int yPosition) {
        if (isViewAdded(item)) {
            item.setViewX(xPosition);
            item.setViewY(yPosition);
            try {
                mWindowManager.updateViewLayout(item.getItemView(), item.getLayoutParams());
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public synchronized void requestLayout(WindowFloatingItem item) {
        if (isViewAdded(item)) {
            try {
                mWindowManager.updateViewLayout(item.getItemView(), item.getLayoutParams());
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public double distance(WindowFloatingItem item1, WindowFloatingItem item2) {
        if (isViewAdded(item1) && isViewAdded(item2)) {
            return MathUtil.distance(item1.getViewX(), item1.getViewY(), item2.getViewX(), item2.getViewY());
        }
        return Double.MAX_VALUE;
    }

    @Override
    public boolean isViewAdded(WindowFloatingItem item) {
        View view = item != null ? item.getItemView() : null;
        return view != null && view.getWindowToken() != null;
    }
}
