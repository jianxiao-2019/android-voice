package com.kikatech.go.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;

/**
 * @author jasonli Created on 2017/10/24.
 */

public abstract class BaseActivity extends FragmentActivity {

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUI();
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

    @Override
    protected void onResume() {
        super.onResume();
        KikaMultiDexApplication.onActivityResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        KikaMultiDexApplication.onActivityPause(this);
    }


    public void startAnotherActivity(Class<?> cls, boolean isFinishSelf) {
        startAnotherActivity(cls, isFinishSelf, 0, 0);
    }

    public void startAnotherActivity(Class<?> cls, boolean isFinishSelf, int enterAnimRes, int finishAnimRes) {
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
        if (isFinishSelf) {
            finish();
        }
        overridePendingTransition(enterAnimRes, finishAnimRes);
    }


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
