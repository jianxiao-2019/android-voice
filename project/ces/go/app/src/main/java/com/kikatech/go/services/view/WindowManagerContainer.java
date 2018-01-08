package com.kikatech.go.services.view;

import android.view.View;
import android.view.WindowManager;

import com.kikatech.go.services.view.item.WindowFloatingItem;

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
    public void setItemX(WindowFloatingItem item, int xPosition) {
        if (isViewAdded(item)) {
            item.setViewX(xPosition);
            mWindowManager.updateViewLayout(item.getItemView(), item.getLayoutParams());
        }
    }

    @Override
    public void setItemY(WindowFloatingItem item, int yPosition) {
        if (isViewAdded(item)) {
            item.setViewX(yPosition);
            mWindowManager.updateViewLayout(item.getItemView(), item.getLayoutParams());
        }
    }

    @Override
    public int getItemX(WindowFloatingItem item) {
        if (isViewAdded(item)) {
            return item.getViewX();
        }
        return 0;
    }

    @Override
    public int getItemY(WindowFloatingItem item) {
        if (isViewAdded(item)) {
            return item.getViewY();
        }
        return 0;
    }

    @Override
    public void addItem(WindowFloatingItem item) {
        if (!isViewAdded(item)) {
            try {
                mWindowManager.addView(item.getItemView(), item.getLayoutParams());
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public void removeItem(WindowFloatingItem item) {
        if (isViewAdded(item)) {
            try {
                mWindowManager.removeView(item.getItemView());
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public void moveItem(WindowFloatingItem item, int xPosition, int yPosition) {
        if (isViewAdded(item)) {
            item.setViewX(xPosition);
            item.setViewY(yPosition);
            mWindowManager.updateViewLayout(item.getItemView(), item.getLayoutParams());
        }
    }

    @Override
    public void requestLayout(WindowFloatingItem item) {
        if (isViewAdded(item)) {
            mWindowManager.updateViewLayout(item.getItemView(), item.getLayoutParams());
        }
    }

    @Override
    public double distance(WindowFloatingItem item1, WindowFloatingItem item2) {
        if (isViewAdded(item1) && isViewAdded(item2)) {
            return distance(item1.getViewX(), item1.getViewY(), item2.getViewX(), item2.getViewY());
        }
        return Double.MAX_VALUE;
    }

    public double distance(int[] pair1, int[] pair2) {
        return distance(pair1[0], pair1[1], pair2[0], pair2[1]);
    }

    private double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    @Override
    public boolean isViewAdded(WindowFloatingItem item) {
        View view = item != null ? item.getItemView() : null;
        return view != null && view.getWindowToken() != null;
    }
}
