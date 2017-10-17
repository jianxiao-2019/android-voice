package com.kikatech.vkb;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC;

public class KikaVoiceActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;

    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    private Toast mToast;
    private Button mDebugButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kika_voice);

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        }

        findViewById(R.id.button_permission).setOnClickListener(this);
        findViewById(R.id.button_accessibility).setOnClickListener(this);
        findViewById(R.id.button_enable).setOnClickListener(this);
        findViewById(R.id.button_active).setOnClickListener(this);
        mDebugButton = (Button) findViewById(R.id.debug_button);
        mDebugButton.setOnClickListener(this);
        updateDebugButtonText();

        TextView versionText = (TextView) findViewById(R.id.version_text);
        versionText.setText("Version : " + getVersionName(this));
    }

    private void updateDebugButtonText() {
        if (mDebugButton == null) {
            return;
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isDebugMode = preferences.getBoolean(KikaVoiceService.PREF_KEY_DEBUG, false);
        String text = isDebugMode ? "Close debug mode" : "Enable debug mode";
        showToast(isDebugMode ? "Debug mode is enabled." : "Debug mode is closed.");

        mDebugButton.setText(text);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_permission) {
            int permissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
            } else {
                showToast("You already have this permission.");
            }
        } else if (v.getId() == R.id.button_accessibility) {
            if (isAccessibilityServiceActivated()) {
                showToast("You already have this permission.");
            } else {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                v.getContext().startActivity(intent);
            }
        } else if (v.getId() == R.id.button_enable) {
            final Intent intent = new Intent();
            intent.setAction(Settings.ACTION_INPUT_METHOD_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivityForResult(intent, 1001);
        } else if (v.getId() == R.id.button_active) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.showInputMethodPicker();
        } else if (v.getId() == R.id.debug_button) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            preferences.edit().putBoolean(KikaVoiceService.PREF_KEY_DEBUG,
                    !preferences.getBoolean(KikaVoiceService.PREF_KEY_DEBUG, false)).apply();

            updateDebugButtonText();
        }
    }

    private void showToast(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
        }
        mToast.show();
    }

    private boolean isAccessibilityServiceActivated() {
        String packageName = this.getPackageName();
        AccessibilityManager manager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> accessibilityServiceInfoList = manager.getEnabledAccessibilityServiceList(FEEDBACK_GENERIC);

        for (AccessibilityServiceInfo accessibilityServiceInfo : accessibilityServiceInfoList) {
            String id = accessibilityServiceInfo.getId();
            if (id != null) {
                String[] id_parts = id.split("/");
                if (id_parts[0] != null && packageName != null) {
                    if (packageName.equals(id_parts[0])) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public String getVersionName(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
