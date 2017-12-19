package com.kikatech.go.services.view.item;

import android.view.View;
import android.view.ViewGroup;

/**
 * @author SkeeterWang Created on 2017/12/18.
 */

public interface IFloatingItem {
    View getItemView();

    void setViewX(int x);

    void setViewY(int y);

    void setViewWidth(int width);

    void setViewHeight(int height);

    int getViewX();

    int getViewY();

    int getViewWidth();

    int getViewHeight();

    void setViewVisibility(int visibility);

    int getViewVisibility();

    <T extends ViewGroup.LayoutParams> T getLayoutParams();
}
