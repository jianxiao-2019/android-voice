package com.kika.usbasrtester;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.kika.usbasrtester.fragment.PlayerFragment;
import com.kika.usbasrtester.fragment.RecorderFragment;

public class TesterActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private RecorderFragment mRecorderFragment = new RecorderFragment();
    private PlayerFragment mPlayerFragment = new PlayerFragment();

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String[] mPermissions = {Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tester);

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        int storagePermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED
                || storagePermissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    mPermissions, REQUEST_RECORD_AUDIO_PERMISSION);
        }

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);

        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.tab_title_record));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.tab_title_player));
        mViewPager.setAdapter(new ContentPagerAdapter(getSupportFragmentManager()));

        mViewPager.addOnPageChangeListener(
                new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    mPlayerFragment.refreshFiles();
                    mPlayerFragment.onPageResume();
                    mRecorderFragment.onPagePause();
                } else {
                    mRecorderFragment.onPageResume();
                    mPlayerFragment.onPagePause();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mTabLayout.addOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }

    private class ContentPagerAdapter extends FragmentPagerAdapter {

        public ContentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return mRecorderFragment;
            }
            return mPlayerFragment;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
