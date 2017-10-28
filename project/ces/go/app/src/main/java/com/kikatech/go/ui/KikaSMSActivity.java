package com.kikatech.go.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.kikatech.go.R;
import com.kikatech.go.message.sms.SmsManager;
import com.kikatech.go.message.sms.SmsObject;
import com.kikatech.go.util.PermissionUtil;

/**
 * @author jasonli Created on 2017/10/23.
 */

public class KikaSMSActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_send_base);

        final Activity activity = KikaSMSActivity.this;

        Button buttonCheck = (Button) findViewById(R.id.button_check);
        final EditText editTarget = (EditText) findViewById(R.id.edit_target);
        final EditText editMessage = (EditText) findViewById(R.id.edit_message);
        Button buttonSend = (Button) findViewById(R.id.button_send_message);

        buttonCheck.setText("Set SMS permissions");
        editTarget.setHint("Enter target phone number");
        buttonSend.setText("Send SMS");

        final Spinner spinnerIM = (Spinner) findViewById(R.id.spinner_im);
        spinnerIM.setVisibility(View.GONE);

        buttonCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionUtil.hasPermissionsSMS(activity)) {
                    showToast("SMS permission already ON");
                } else {
                    showToast("Get SMS permission");
                    PermissionUtil.checkPermissionsSMS(activity);
                }
            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PermissionUtil.hasPermissionsSMS(activity)) {
                    showToast("Get SMS permission first!");
                    return;
                }

                String phoneNum = editTarget.getText().toString();
                String msgContent = editMessage.getText().toString();
                if (TextUtils.isEmpty(phoneNum) || TextUtils.isEmpty(msgContent)) {
                    showToast("Empty target or message!");
                    return;
                }

                boolean sent = SmsManager.getInstance().sendMessage(activity, phoneNum, "", msgContent);
                showToast(sent ? "SMS Sent." : "SMS failed!");
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(SmsManager.ACTION_SMS_MESSAGE_UPDATED);
        registerReceiver(mBroadcastReceiver, filter);
    }

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(SmsManager.ACTION_SMS_MESSAGE_UPDATED.equals(intent.getAction())) {
                SmsObject smsObject = intent.getParcelableExtra(SmsManager.KEY_DATA_SMS_OBJECT);
                showToast("Received sms object: " + smsObject.getMsgContent());
            }
        }
    };


}
