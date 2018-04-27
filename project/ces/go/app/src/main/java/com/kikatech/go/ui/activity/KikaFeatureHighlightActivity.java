package com.kikatech.go.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kikatech.go.R;
import com.kikatech.go.util.preference.GlobalPref;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author SkeeterWang Created on 2018/2/8.
 */

public class KikaFeatureHighlightActivity extends BaseActivity {
    private static final String TAG = "KikaFeatureHighlightActivity";

    private ViewPager mPager;
    private TabLayout mTabLayout;
    private FeatureAdapter mAdapter;

    private View mBtnSkip;
    private View mBtnNext;
    private View mBtnStart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kika_feature_highlight);
        bindView();
        bindListener();
        loadFeatureHighlightPages();
    }

    private void bindView() {
        mPager = (ViewPager) findViewById(R.id.feature_highlight_pager);
        mTabLayout = (TabLayout) findViewById(R.id.feature_highlight_pager_indicators);
        mBtnSkip = findViewById(R.id.feature_highlight_btn_skip);
        mBtnNext = findViewById(R.id.feature_highlight_btn_next);
        mBtnStart = findViewById(R.id.feature_highlight_btn_start);
    }

    private void bindListener() {
        mBtnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoPermissionsPage();
            }
        });
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = mPager.getCurrentItem();
                if (currentPosition + 1 < mAdapter.getCount()) {
                    mPager.setCurrentItem(currentPosition + 1);
                }
            }
        });
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoPermissionsPage();
            }
        });

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == mAdapter.getCount() - 1) {
                    mBtnNext.setVisibility(View.GONE);
                    mBtnStart.setVisibility(View.VISIBLE);
                } else if (position < mAdapter.getCount()) {
                    mBtnStart.setVisibility(View.GONE);
                    mBtnNext.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void gotoPermissionsPage() {
        GlobalPref.getIns().setIsFirstLaunch(false);
        startAnotherActivity(KikaPermissionsActivity.class, true);
    }

    private void loadFeatureHighlightPages() {
        mAdapter = new FeatureAdapter(this);
        mPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mPager);
    }

    private static class FeatureAdapter extends PagerAdapter {
        private ArrayList<View> mList = new ArrayList<>();

        private static final int[] IMG_RESOURCES = new int[]{
                R.drawable.kika_oobe_graphic_started,
                R.drawable.ic_feature_navi_illustration,
                R.drawable.ic_feature_msg_illustration,
                R.drawable.ic_feature_call_illustration,
                R.drawable.ic_feature_music_illustration,
        };

        private FeatureAdapter(Context context) {
            String[] TITLES = context.getResources().getStringArray(R.array.feature_highlight_titles);
            String[] SUBTITLES = context.getResources().getStringArray(R.array.feature_highlight_subtitles);
            int[] counts = new int[]{TITLES.length, SUBTITLES.length, IMG_RESOURCES.length};
            Arrays.sort(counts);
            int pageCount = counts[0];
            for (int i = 0; i < pageCount; i++) {
                View mItemView = LayoutInflater.from(context).inflate(R.layout.activity_kika_feature_highlight_item, null);
                ((TextView) mItemView.findViewById(R.id.feature_highlight_item_title)).setText(TITLES[i]);
                ((TextView) mItemView.findViewById(R.id.feature_highlight_item_subtitle)).setText(SUBTITLES[i]);
                ((ImageView) mItemView.findViewById(R.id.feature_highlight_item_img)).setImageResource(IMG_RESOURCES[i]);
                mList.add(mItemView);
            }
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mList.get(position));
            return mList.get(position);
        }
    }
}
