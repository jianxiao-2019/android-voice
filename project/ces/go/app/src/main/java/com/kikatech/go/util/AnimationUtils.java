package com.kikatech.go.util;

import android.view.View;
import android.view.animation.Animation;

/**
 * @author wangskeeter Created on 16/8/26.
 */
public class AnimationUtils {
    private static AnimationUtils sIns;

    public static synchronized AnimationUtils getIns() {
        if (sIns == null) {
            sIns = new AnimationUtils();
        }
        return sIns;
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
