package com.kikatech.go.util;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * @author wangskeeter Created on 16/8/26.
 */
public class AnimationUtils {
    private static AnimationUtils sIns;

    private static final long ANIMATION_DELAY = 150;

    private static ScaleAnimation clickScaleAnimation;
    private static AlphaAnimation playAudioAlphaAnimation;

    public static synchronized AnimationUtils getIns() {
        if (sIns == null)
            sIns = new AnimationUtils();
        return sIns;
    }

    private AnimationUtils() {
        clickScaleAnimation = new ScaleAnimation(
                1f, 0.8f,   // Start and end values for the X axis scaling
                1f, 0.8f,   // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f,   // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
        // scaleAnimation.setFillAfter(true); // Needed to keep the result of the animation
        clickScaleAnimation.setDuration(100);

        playAudioAlphaAnimation = new AlphaAnimation(1.0f, 0.15f);
        playAudioAlphaAnimation.setDuration(800);
        playAudioAlphaAnimation.setRepeatCount(-1);
    }


    public void startClickScaleAnimation(final View view, final Runnable onAnimationEnd) {
        view.startAnimation(clickScaleAnimation);
        view.postDelayed(onAnimationEnd, ANIMATION_DELAY);
    }

    public void startPlayAudioAlphaAnimation(final View view) {
        view.startAnimation(playAudioAlphaAnimation);
    }


    public void startBubbleSendingStatusAnimation(final View view, final AlphaAnimation animation, final Animation.AnimationListener listener) {
        animation.setStartOffset(400);
        animation.setDuration(300);
        animation.setAnimationListener(listener);
        view.startAnimation(animation);
    }

    public void clearBubbleSendingStatusAnimation(final View view, AlphaAnimation animation) {
        animation.setAnimationListener(null);
        view.clearAnimation();
    }


    public void startBubbleConfirmAnimation(final View view, TranslateAnimation animation, Animation.AnimationListener listener) {
        animation.setDuration(300);
        animation.setAnimationListener(listener);
        view.startAnimation(animation);
    }

    public void clearBubbleConfirmAnimation(final View view, TranslateAnimation animation) {
        animation.setAnimationListener(null);
        view.clearAnimation();
    }


    public void animate(final View view, Animation animation, final IAnimationEndCallback callBack) {
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (callBack != null && callBack instanceof IAnimationCallback) {
                    ((IAnimationCallback) callBack).onStart(view);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (callBack != null) {
                    callBack.onEnd(view);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(animation);
    }

    interface IAnimationCallback {
        void onStart(View view);
    }

    public interface IAnimationEndCallback {
        void onEnd(View view);
    }
}
