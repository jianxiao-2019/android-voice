package com.kikatech.go.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.kikatech.go.R;
import com.kikatech.go.navigation.location.LocationMgr;
import com.kikatech.go.util.PermissionUtil;
import com.kikatech.voice.util.contact.ContactManager;

/**
 * Created by tianli on 17-10-23.
 */

public class KikaGoActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContactManager.getIns().init(this);
        LocationMgr.init(this);

        setContentView(R.layout.activity_kika_go);

        findViewById(R.id.button_accessibility_im).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KikaGoActivity.this, KikaAccessibilityActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        findViewById(R.id.button_telephony).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KikaGoActivity.this, TelephonyActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.button_notification_im).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KikaGoActivity.this, KikaIMReplyActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        findViewById(R.id.button_sms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KikaGoActivity.this, KikaSMSActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        findViewById(R.id.button_hotword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PermissionUtil.hasPermissions(KikaGoActivity.this, PermissionUtil.Permission.WRITE_EXTERNAL_STORAGE, PermissionUtil.Permission.RECORD_AUDIO)) {
                    PermissionUtil.checkPermission(KikaGoActivity.this, PermissionUtil.Permission.WRITE_EXTERNAL_STORAGE, PermissionUtil.Permission.RECORD_AUDIO);
                } else {
                    Intent intent = new Intent(KikaGoActivity.this, HotWordActivity.class);
                    startActivity(intent);
                }
            }
        });

        findViewById(R.id.button_apiai).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KikaGoActivity.this, KikaApiAiActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        findViewById(R.id.button_navigation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KikaGoActivity.this, KikaNavigationActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.button_voice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KikaGoActivity.this, VoiceTestingActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.button_dialog_flow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KikaGoActivity.this, KikaDialogFlowActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.button_alpha_ui).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KikaGoActivity.this, KikaAlphaUiActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.button_debug_log).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KikaGoActivity.this, KikaDebugLogActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.button_voice_source).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KikaGoActivity.this, KikaUsbVoiceSourceActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.button_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KikaGoActivity.this, KikaMusicActivity.class);
                startActivity(intent);
            }
        });
    }
}
