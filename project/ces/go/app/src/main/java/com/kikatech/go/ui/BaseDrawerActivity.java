package com.kikatech.go.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

import com.kikatech.go.R;

/**
 * @author SkeeterWang Created on 2017/12/19.
 */

public abstract class BaseDrawerActivity extends BaseActivity implements DrawerLayout.DrawerListener {

    protected abstract DrawerLayout getDrawerLayout();

    protected abstract View getDrawerView();

    protected abstract Fragment getMainDrawerFragment();

    private DrawerLayout mDrawerLayout;
    private View mDrawerView;

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        initDrawer();
    }

    @Override
    public void onBackPressed() {
        if (isDrawerOpen()) {
            closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    private void initDrawer() {
        mDrawerLayout = getDrawerLayout();
        mDrawerView = getDrawerView();
        updateDrawerContent(getMainDrawerFragment());
        if (isDrawerAvailable()) {
            mDrawerLayout.addDrawerListener(this);
        }
        openDrawer();
    }

    protected void openDrawer() {
        if (isDrawerAvailable()) {
            mDrawerLayout.openDrawer(mDrawerView);
        }
    }

    protected void closeDrawer() {
        if (isDrawerAvailable()) {
            mDrawerLayout.closeDrawer(mDrawerView);
        }
    }

    protected boolean isDrawerOpen() {
        return isDrawerAvailable() && mDrawerLayout.isDrawerOpen(mDrawerView);
    }

    protected void updateDrawerContent(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.drawer_content, fragment);
        ft.commitAllowingStateLoss();
    }

    private boolean isDrawerAvailable() {
        return mDrawerLayout != null && mDrawerView != null;
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
    }

    @Override
    public void onDrawerOpened(View drawerView) {
    }

    @Override
    public void onDrawerClosed(View drawerView) {
    }

    @Override
    public void onDrawerStateChanged(int newState) {
    }
}
