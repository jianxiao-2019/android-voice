package com.kikatech.go.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.kikatech.go.R;
import com.kikatech.go.music.MusicManager;
import com.kikatech.go.music.google.serivce.YouTubeAPI;
import com.kikatech.go.music.model.YouTubeVideoList;
import com.kikatech.go.services.MusicForegroundService;

/**
 * @author SkeeterWang Created on 2018/1/5.
 */

public class KikaMusicActivity extends Activity {
    private static final String TAG = "KikaMusicActivity";

    private static final int mProviderType = MusicManager.ProviderType.YOUTUBE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kika_music);
        findViewById(R.id.btn_play_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YouTubeAPI.getIns().searchVideo("Loser", new YouTubeAPI.IYoutubeApiCallback() {
                    @Override
                    public void onLoaded(YouTubeVideoList result) {
                        MusicForegroundService.startMusic(KikaMusicActivity.this, result);
                    }
                });
            }
        });
        findViewById(R.id.btn_pause_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicManager.getIns().pause(mProviderType);
            }
        });
        findViewById(R.id.btn_resume_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicManager.getIns().resume(mProviderType);
            }
        });
        findViewById(R.id.btn_stop_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicManager.getIns().stop(mProviderType);
            }
        });
        findViewById(R.id.btn_volume_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicManager.getIns().volumeUp(mProviderType);
            }
        });
        findViewById(R.id.btn_volume_down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicManager.getIns().volumeDown(mProviderType);
            }
        });
        findViewById(R.id.btn_mute_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicManager.getIns().mute(mProviderType);
            }
        });
        findViewById(R.id.btn_unmute_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicManager.getIns().unmute(mProviderType);
            }
        });
    }
}