package com.kikatech.go.services.view;

import com.kikatech.go.services.view.item.IFloatingItem;

/**
 * @author SkeeterWang Created on 2017/12/18.
 */

public interface IFloatingContainer<T extends IFloatingItem> {
    void setItemX(T item, int xPosition);

    void setItemY(T item, int yPosition);

    int getItemX(T item);

    int getItemY(T item);

    void addItem(T item);

    void removeItem(T item);

    void moveItem(T item, int xPosition, int yPosition);

    void requestLayout(T item);

    double distance(T item1, T item2);

    boolean isViewAdded(T item);
}