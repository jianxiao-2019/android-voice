package com.kikatech.voicesdktester.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.kikatech.voicesdktester.R;
import com.kikatech.voicesdktester.fragments.LocalPlayBackFragment;

/**
 * Created by ryanlin on 03/01/2018.
 */

public class LocalPlayBackActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private LocalPlayBackFragment mPlayBackUSBFragment = LocalPlayBackFragment.getInstance(LocalPlayBackFragment.FragmentType.LOCAL_USB);
    private LocalPlayBackFragment mPlayBackNCFragment = LocalPlayBackFragment.getInstance(LocalPlayBackFragment.FragmentType.LOCAL_NC);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tester);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);

        mTabLayout.addTab(mTabLayout.newTab().setText("USB"));
        mTabLayout.addTab(mTabLayout.newTab().setText("NC/SRC"));
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(new LocalPlayBackActivity.ContentPagerAdapter(getSupportFragmentManager()));

        mViewPager.addOnPageChangeListener(
                new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    mPlayBackUSBFragment.scanFiles();
                } else if (position == 1) {
                    mPlayBackNCFragment.scanFiles();
                } else {
                    mPlayBackUSBFragment.scanFiles();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        mViewPager.addOnPageChangeListener(pageChangeListener);
        mTabLayout.addOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        mViewPager.post(new Runnable() {
            @Override
            public void run() {
                pageChangeListener.onPageSelected(mViewPager.getCurrentItem());
            }
        });
    }

    private class ContentPagerAdapter extends FragmentPagerAdapter {

        public ContentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return mPlayBackUSBFragment;
            } else if (position == 1) {
                return mPlayBackNCFragment;
            }
            return mPlayBackUSBFragment;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
