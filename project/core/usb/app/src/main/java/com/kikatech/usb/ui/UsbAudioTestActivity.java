package com.kikatech.usb.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.kikatech.usb.R;
import com.kikatech.usb.UsbAudioService;
import com.kikatech.usb.driver.interfaces.IUsbAudioTransferListener;
import com.kikatech.usb.driver.interfaces.IUsbStatusListener;

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
                UsbAudioService.processStartAudioTransfer(UsbAudioTestActivity.this, new IUsbAudioTransferListener() {
                    @Override
                    public void onAudioTransferStart() {
                        btnStart.setEnabled(false);
                        btnStop.setEnabled(true);
                    }

                    @Override
                    public void onAudioTransferStop(String filePath) {
                        btnStart.setEnabled(true);
                        btnStop.setEnabled(false);
                        String log = logTv.getText().toString() + "\n\nfile saved at: \n" + filePath;
                        logTv.setText(log);
                    }

                    @Override
                    public void onAudioTransferBufferResult(short[] data) {
                    }
                });
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsbAudioService.processStopAudioTransfer(UsbAudioTestActivity.this);
            }
        });

        UsbAudioService.processStart(UsbAudioTestActivity.this, new IUsbStatusListener() {
            @Override
            public void onAttached() {
            }

            @Override
            public void onDetached() {
                btnStart.setEnabled(false);
                btnStop.setEnabled(false);
            }

            @Override
            public void onNoDevices() {
                btnStart.setEnabled(false);
                btnStop.setEnabled(false);
            }

            @Override
            public void onInitialized() {
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
            }

            @Override
            public void onInitializedFailed(String errorMsg) {
                btnStart.setEnabled(false);
                btnStop.setEnabled(false);
            }

            @Override
            public void onOpenFailed() {
                btnStart.setEnabled(false);
                btnStop.setEnabled(false);
            }

            @Override
            public void onServiceStarted() {
            }

            @Override
            public void onServiceStopped() {
                btnStart.setEnabled(false);
                btnStop.setEnabled(false);
            }
        });
    }
}