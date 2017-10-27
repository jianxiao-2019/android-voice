package com.kikatech.go.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.kikatech.go.R;

/**
 * Created by tianli on 17-10-23.
 */

public class KikaGoActivity extends Activity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_kika_go);

        findViewById(R.id.button_accessibility_im).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KikaGoActivity.this, KikaAccessibilityActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

		findViewById( R.id.button_telephony ).setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View v )
			{
				Intent intent = new Intent(KikaGoActivity.this, TelephonyActivity.class);
				startActivity(intent);
			}
		} );
    }
}
