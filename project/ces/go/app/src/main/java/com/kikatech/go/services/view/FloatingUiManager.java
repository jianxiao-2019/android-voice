package com.kikatech.go.services.view;

import android.content.res.Configuration;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.kikatech.go.util.LogUtil;

/**
 * @author SkeeterWang Created on 2017/12/15.
 */

@SuppressWarnings("SuspiciousNameCombination")
public class FloatingUiManager {
    private static final String TAG = "FloatingUiManager";

    private int DEVICE_WIDTH;
    private int DEVICE_HEIGHT;

    private WindowManager mWindowManager;
    private LayoutInflater mLayoutInflater;
    private Configuration mConfiguration;
    private Handler mUiHandler;

    private FloatingUiManager(WindowManager manager, LayoutInflater inflater, Configuration configuration, Handler uiHandler) {
        this.mWindowManager = manager;
        this.mLayoutInflater = inflater;
        this.mUiHandler = uiHandler;

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
    }


    public synchronized void addView(View view, WindowManager.LayoutParams params) {
        try {
            mWindowManager.addView(view, params);
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }

    public synchronized void removeView(View view) {
        try {
            mWindowManager.removeView(view);
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }

    public synchronized void updateView(View view, WindowManager.LayoutParams params) {
        try {
            mWindowManager.updateViewLayout(view, params);
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }

    public synchronized boolean isViewAdded(View view) {
        try {
            return view != null && view.getWindowToken() != null;
        } catch (Exception ignore) {
        }
        return true;
    }


    public synchronized void updateConfiguration(Configuration configuration) {
        this.mConfiguration = configuration;
    }

    public int getDeviceWidthByOrientation() {
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

    public int getDeviceHeightByOrientation() {
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


    public synchronized View inflate(int resId) {
        return inflate(resId, null);
    }

    public synchronized View inflate(int resId, ViewGroup root) {
        return mLayoutInflater.inflate(resId, root);
    }


    public void post(Runnable runnable) {
        try {
            mUiHandler.post(runnable);
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }

    public void postDelay(Runnable runnable, long delayMillis) {
        try {
            mUiHandler.postDelayed(runnable, delayMillis);
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }

    public void removeCallbacks(Runnable runnable) {
        try {
            mUiHandler.removeCallbacks(runnable);
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }


    public static final class Builder {
        private WindowManager mWindowManager;
        private LayoutInflater mLayoutInflater;
        private Configuration mConfiguration;
        private Handler mUiHandler;

        public Builder setWindowManager(WindowManager manager) {
            this.mWindowManager = manager;
            return this;
        }

        public Builder setLayoutInflater(LayoutInflater inflater) {
            this.mLayoutInflater = inflater;
            return this;
        }

        public Builder setConfiguration(Configuration configuration) {
            this.mConfiguration = configuration;
            return this;
        }

        public Builder setUiHandler(Handler handler) {
            this.mUiHandler = handler;
            return this;
        }

        public FloatingUiManager build() {
            return new FloatingUiManager(mWindowManager, mLayoutInflater, mConfiguration, mUiHandler);
        }
    }
}
