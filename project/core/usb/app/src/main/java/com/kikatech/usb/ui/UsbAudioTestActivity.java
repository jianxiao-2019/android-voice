package com.kikatech.usb.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.kikatech.usb.R;
import com.kikatech.usb.UsbForegroundService;

/**
 * Created by tianli on 17-10-23.
 */
public class UsbAudioTestActivity extends AppCompatActivity {
    private View btnStart;
    private View btnStop;
    private TextView logTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb);

        btnStart = findViewById(R.id.start);
        btnStop = findViewById(R.id.stop);
        logTv = (TextView) findViewById(R.id.log_tv);

        logTv.setMovementMethod(new ScrollingMovementMethod());
        btnStart.setEnabled(false);
        btnStop.setEnabled(false);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

    }
}