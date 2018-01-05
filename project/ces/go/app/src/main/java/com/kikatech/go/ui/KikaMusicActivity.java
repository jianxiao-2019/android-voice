package com.kikatech.go.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.kikatech.go.R;
import com.kikatech.go.music.MusicManager;

/**
 * @author SkeeterWang Created on 2018/1/5.
 */

public class KikaMusicActivity extends Activity {
    private static final String TAG = "KikaMusicActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kika_music);
        findViewById(R.id.btn_play_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicManager.getIns().play();
            }
        });
        findViewById(R.id.btn_pause_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicManager.getIns().pause();
            }
        });
        findViewById(R.id.btn_resume_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicManager.getIns().resume();
            }
        });
        findViewById(R.id.btn_stop_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicManager.getIns().stop();
            }
        });
        findViewById(R.id.btn_mute_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicManager.getIns().mute();
            }
        });
        findViewById(R.id.btn_unmute_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicManager.getIns().unmute();
            }
        });
    }
}