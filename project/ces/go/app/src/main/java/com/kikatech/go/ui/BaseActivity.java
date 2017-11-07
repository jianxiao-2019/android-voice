package com.kikatech.go.ui;

import android.app.Activity;
import android.widget.Toast;

/**
 * @author jasonli Created on 2017/10/24.
 */

public class BaseActivity extends Activity {

    protected void showToast(final String message) {
        __showToast(message, Toast.LENGTH_SHORT);
    }

    protected void showLongToast(final String message) {
        __showToast(message, Toast.LENGTH_LONG);
    }

    private void __showToast(final String message, final int len) {
        BaseActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BaseActivity.this, message, len).show();
            }
        });
    }

}
