package com.kikatech.go.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.kikatech.go.R;
import com.kikatech.go.util.dialog.DialogUtil;

/**
 * @author SkeeterWang Created on 2018/3/30.
 */

public class KikaFAQsActivity extends BaseActivity {
    private static final String TAG = "KikaFAQsActivity";

    private View mItemQ1Content;
    private ImageView mItemQ1BtnMore;
    private View mItemQ2Content;
    private ImageView mItemQ2BtnMore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kika_faq);
        bindView();
        bindListener();
    }

    private void bindView() {
        mItemQ1Content = findViewById(R.id.faq_q1_content);
        mItemQ1BtnMore = (ImageView) findViewById(R.id.faq_q1_btn_more);
        mItemQ2Content = findViewById(R.id.faq_q2_content);
        mItemQ2BtnMore = (ImageView) findViewById(R.id.faq_q2_btn_more);
    }

    private void bindListener() {
        findViewById(R.id.drawer_title_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.faq_q1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isExpanded = mItemQ1Content.getVisibility() == View.VISIBLE;
                if (isExpanded) {
                    mItemQ1Content.setVisibility(View.GONE);
                    mItemQ1BtnMore.setImageResource(R.drawable.kika_settings_ic_more);
                } else {
                    mItemQ1Content.setVisibility(View.VISIBLE);
                    mItemQ1BtnMore.setImageResource(R.drawable.kika_settings_ic_more_expand);
                }
            }
        });

        findViewById(R.id.faq_q2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isExpanded = mItemQ2Content.getVisibility() == View.VISIBLE;
                if (isExpanded) {
                    mItemQ2Content.setVisibility(View.GONE);
                    mItemQ2BtnMore.setImageResource(R.drawable.kika_settings_ic_more);
                } else {
                    mItemQ2Content.setVisibility(View.VISIBLE);
                    mItemQ2BtnMore.setImageResource(R.drawable.kika_settings_ic_more_expand);
                }
            }
        });

        findViewById(R.id.faq_t1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.showFAQ(KikaFAQsActivity.this, null);
            }
        });
    }
}
