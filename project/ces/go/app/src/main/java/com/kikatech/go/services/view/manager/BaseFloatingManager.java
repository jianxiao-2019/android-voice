package com.kikatech.go.services.view.manager;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.kikatech.go.services.view.WindowManagerContainer;
import com.kikatech.go.util.LogUtil;

/**
 * @author SkeeterWang Created on 2018/1/16.
 */

@SuppressWarnings("SuspiciousNameCombination")
class BaseFloatingManager {
    private static final String TAG = "BaseFloatingManager";
    private int DEVICE_WIDTH;
    private int DEVICE_HEIGHT;

    final Context mContext;
    final WindowManager mWindowManager;
    final LayoutInflater mLayoutInflater;
    Configuration mConfiguration;
    final Handler mUiHandler = new Handler(Looper.getMainLooper());

    WindowManagerContainer mContainer;

    BaseFloatingManager(Context context, WindowManager manager, LayoutInflater inflater, Configuration configuration) {
        this.mContext = context;
        this.mWindowManager = manager;
        this.mLayoutInflater = inflater;

        mConfiguration = configuration;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);

        switch (mConfiguration.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                DEVICE_HEIGHT = displayMetrics.widthPixels;
                DEVICE_WIDTH = displayMetrics.heightPixels;
                break;
            default:
            case Configuration.ORIENTATION_PORTRAIT:
                DEVICE_WIDTH = displayMetrics.widthPixels;
                DEVICE_HEIGHT = displayMetrics.heightPixels;
                break;
        }

        mContainer = new WindowManagerContainer(mWindowManager);
    }


    public synchronized void updateConfiguration(Configuration configuration) {
        this.mConfiguration = configuration;
    }


    int getDeviceWidth() {
        return DEVICE_WIDTH;
    }

    int getDeviceHeight() {
        return DEVICE_HEIGHT;
    }

    int getDeviceWidthByOrientation() {
        try {
            switch (mConfiguration.orientation) {
                default:
                case Configuration.ORIENTATION_PORTRAIT:
                    return DEVICE_WIDTH;
                case Configuration.ORIENTATION_LANDSCAPE:
                    return DEVICE_HEIGHT;
            }
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
        return DEVICE_WIDTH;
    }

    int getDeviceHeightByOrientation() {
        try {
            switch (mConfiguration.orientation) {
                case Configuration.ORIENTATION_PORTRAIT:
                    return DEVICE_HEIGHT;
                case Configuration.ORIENTATION_LANDSCAPE:
                    return DEVICE_WIDTH;
            }
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
        return DEVICE_HEIGHT;
    }


    synchronized View inflate(int resId) {
        return inflate(resId, null);
    }

    @SuppressWarnings("SameParameterValue")
    synchronized View inflate(int resId, ViewGroup root) {
        return mLayoutInflater.inflate(resId, root);
    }


    synchronized void post(Runnable runnable) {
        mUiHandler.post(runnable);
    }

    synchronized void postDelay(Runnable runnable, long delayMillis) {
        mUiHandler.postDelayed(runnable, delayMillis);
    }

    synchronized void removeCallbacks(Runnable runnable) {
        mUiHandler.removeCallbacks(runnable);
    }


    @SuppressWarnings("unchecked")
    static class Builder<T extends Builder> {
        WindowManager mWindowManager;
        LayoutInflater mLayoutInflater;
        Configuration mConfiguration;

        public T setWindowManager(WindowManager manager) {
            this.mWindowManager = manager;
            return (T) this;
        }

        public T setLayoutInflater(LayoutInflater inflater) {
            this.mLayoutInflater = inflater;
            return (T) this;
        }

        public T setConfiguration(Configuration configuration) {
            this.mConfiguration = configuration;
            return (T) this;
        }

        public BaseFloatingManager build(Context context) {
            return new BaseFloatingManager(context, mWindowManager, mLayoutInflater, mConfiguration);
        }
    }
}
