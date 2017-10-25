package com.kikatech.go.ui;

import android.app.Activity;
import android.widget.Toast;

/**
 * @author jasonli Created on 2017/10/24.
 */

public class BaseActivity extends Activity {

    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
