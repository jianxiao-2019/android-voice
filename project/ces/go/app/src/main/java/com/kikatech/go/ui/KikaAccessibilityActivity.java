package com.kikatech.go.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.kikatech.go.R;
import com.kikatech.go.accessibility.AccessibilityManager;
import com.kikatech.go.accessibility.AccessibilityUtils;
import com.kikatech.go.accessibility.im.MessageEventDispatcher;
import com.kikatech.go.message.processor.IMProcessor;
import com.kikatech.go.util.AppInfo;
import com.kikatech.go.util.PackageManagerUtil;

/**
 * @author jasonli Created on 2017/10/24.
 */

public class KikaAccessibilityActivity extends BaseActivity {

    MessageEventDispatcher mMessageEventDispatcher;
    AppInfo mAppInfo = AppInfo.MESSENGER;

    private AppInfo[] mSupportIM = new AppInfo[]{
            AppInfo.MESSENGER,
            AppInfo.WHATSAPP,
            AppInfo.WECHAT,
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_send_base);

        final Context context = KikaAccessibilityActivity.this;

        Button buttonCheck = (Button) findViewById(R.id.button_check);
        final EditText editTarget = (EditText) findViewById(R.id.edit_target);
        final EditText editMessage = (EditText) findViewById(R.id.edit_message);
        Button buttonSend = (Button) findViewById(R.id.button_send_message);

        buttonCheck.setText("Check accessibility setting");
        editTarget.setHint("Enter target user/group name");
        buttonSend.setText("Send by Accessibility");

        final Spinner spinnerIM = (Spinner) findViewById(R.id.spinner_im);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String packageName = mAppInfo.getPackageName();
                if (!PackageManagerUtil.isAppInstalled(context, packageName)) {
                    showToast("App not installed!");
                    return;
                }

                String target = editTarget.getText().toString();
                String message = editMessage.getText().toString();
                if (TextUtils.isEmpty(target) || TextUtils.isEmpty(message)) {
                    showToast("Empty target or message!");
                    return;
                }

                if (!AccessibilityUtils.isSettingsOn(context)) {
                    showToast("Please enable Accessibility permission!");
                    return;
                }

                IMProcessor processor = IMProcessor.createIMProcessor(context, packageName, target, message);
                if (processor != null) {
                    processor.start();
                }
            }
        });

        buttonCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AccessibilityUtils.isSettingsOn(context)) {
                    showToast("The Accessibility setting is ON.");
                } else {
                    AccessibilityUtils.openAccessibilitySettings(context);
                }
            }
        });

        String[] imNames = new String[mSupportIM.length];
        for (int i = 0; i < imNames.length; i++) {
            imNames[i] = mSupportIM[i].getAppName();
        }
        ArrayAdapter dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, imNames);
        spinnerIM.setAdapter(dataAdapter);
        spinnerIM.setOnItemSelectedListener(mSpinnerSelectedListener);

        mMessageEventDispatcher = new MessageEventDispatcher();
        AccessibilityManager.getInstance().registerDispatcher(mMessageEventDispatcher);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AccessibilityManager.getInstance().unregisterDispatcher(mMessageEventDispatcher);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    private Spinner.OnItemSelectedListener mSpinnerSelectedListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mAppInfo = mSupportIM[position];
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };
}
