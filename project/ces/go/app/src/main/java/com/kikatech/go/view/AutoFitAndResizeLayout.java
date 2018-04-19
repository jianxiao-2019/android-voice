package com.kikatech.go.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.WindowInsets;
import android.widget.FrameLayout;

/**
 * <p>A workaround for windowSoftInputMode=“adjustResize” not working with translucent action/navbar</p>
 *
 * @author SkeeterWang Created on 2018/4/19.
 * @see <a href="https://stackoverflow.com/questions/21092888/windowsoftinputmode-adjustresize-not-working-with-translucent-action-navbar">StackOverflow</href>
 */
public class AutoFitAndResizeLayout extends FrameLayout {

    public AutoFitAndResizeLayout(@NonNull Context context) {
        super(context);
    }

    public AutoFitAndResizeLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoFitAndResizeLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public final WindowInsets onApplyWindowInsets(WindowInsets insets) {
        // Intentionally do not modify the bottom inset. For some reason,
        // if the bottom inset is modified, window resizing stops working.
        // TODO: Figure out why.
        return super.onApplyWindowInsets(insets.replaceSystemWindowInsets(0, 0, 0, insets.getSystemWindowInsetBottom()));
    }
}
