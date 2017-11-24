package com.kikatech.go.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

/**
 * @author jasonli Created on 2017/10/24.
 */

public abstract class BaseActivity extends Activity {

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUI();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public void startAnotherActivity(Class<?> cls, boolean isFinishSelf) {
        Intent startActivityIntent = new Intent(this, cls);
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getData() != null) {
                startActivityIntent.setData(intent.getData());
            }
            if (intent.getAction() != null) {
                startActivityIntent.setAction(intent.getAction());
            }
            if (intent.getExtras() != null) {
                startActivityIntent.putExtras(intent.getExtras());
            }
            if (intent.getType() != null) {
                startActivityIntent.setType(intent.getType());
            }
        }
        startActivity(startActivityIntent);
        overridePendingTransition(0, 0);
        if (isFinishSelf) {
            finish();
        }
    }
}
