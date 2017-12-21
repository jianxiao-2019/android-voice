package com.kikatech.go.services.view.item;

/**
 * @author SkeeterWang Created on 2017/12/18.
 */

public interface IFloatingBtn extends IFloatingItem {
    void show();

    void hide();

    void onEnter();

    void onLeaved();

    void onSelected();

    boolean isShowing();
}
