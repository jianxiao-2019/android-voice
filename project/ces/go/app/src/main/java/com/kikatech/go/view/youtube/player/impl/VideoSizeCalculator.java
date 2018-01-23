package com.kikatech.go.view.youtube.player.impl;

import android.view.View;

/**
 * @author SkeeterWang Created on 2018/1/18.
 */

class VideoSizeCalculator {
    private static final String TAG = "VideoSizeCalculator";

    private int mTargetWidth;
    private int mTargetHeight;


    void setTargetVideoSize(int mVideoWidth, int mVideoHeight) {
        this.mTargetWidth = mVideoWidth;
        this.mTargetHeight = mVideoHeight;
    }

    boolean isTargetSizeEqual(int width, int height) {
        return mTargetWidth == width && mTargetHeight == height;
    }

    boolean isTargetSizeValid() {
        return mTargetWidth > 0 && mTargetHeight > 0;
    }


    Dimens measure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.getDefaultSize(mTargetWidth, widthMeasureSpec);
        int height = View.getDefaultSize(mTargetHeight, heightMeasureSpec);
        if (isTargetSizeValid()) {
            int widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec);

            if (widthSpecMode == View.MeasureSpec.EXACTLY && heightSpecMode == View.MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize;
                height = heightSpecSize;

                // for compatibility, we adjust size based on aspect ratio
                if (mTargetWidth * height < width * mTargetHeight) {
                    width = height * mTargetWidth / mTargetHeight;
                } else if (mTargetWidth * height > width * mTargetHeight) {
                    height = width * mTargetHeight / mTargetWidth;
                }
            } else if (widthSpecMode == View.MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize;
                height = width * mTargetHeight / mTargetWidth;
                if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize;
                }
            } else if (heightSpecMode == View.MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize;
                width = height * mTargetWidth / mTargetHeight;
                if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize;
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = mTargetWidth;
                height = mTargetHeight;
                if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize;
                    width = height * mTargetWidth / mTargetHeight;
                }
                if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize;
                    height = width * mTargetHeight / mTargetWidth;
                }
            }
        }
        return new Dimens(width, height);
    }


    static class Dimens {
        private int width;
        private int height;

        private Dimens(int width, int height) {
            this.width = width;
            this.height = height;
        }

        int getWidth() {
            return width;
        }

        int getHeight() {
            return height;
        }
    }
}
