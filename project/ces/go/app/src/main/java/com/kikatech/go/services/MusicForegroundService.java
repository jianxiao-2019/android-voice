package com.kikatech.go.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.kikatech.go.R;
import com.kikatech.go.eventbus.ToMusicServiceEvent;
import com.kikatech.go.music.MusicManager;
import com.kikatech.go.util.LogUtil;
import com.kikatech.usb.util.ImageUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author jason Created on 2018/1/10.
 */

public class MusicForegroundService extends BaseForegroundService {

    private static final String TAG = MusicForegroundService.class.getName();

    private static class Commands extends BaseForegroundService.Commands {
        private static final String PRI_MUSIC_SERVICE = "music_service_";
        private static final String MUSIC_PLAY_PAUSE = PRI_MUSIC_SERVICE + "music_play_pause";
    }

    /**
     * <p>Reflection subscriber method used by EventBus,
     * <p>do not remove this except the subscriber is no longer needed.
     *
     * @param event event sent to {@link com.kikatech.go.services.MusicForegroundService}
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onToMusicServiceEvent(ToMusicServiceEvent event) {
        if (event == null) {
            return;
        }
        String action = event.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        switch (action) {
            case ToMusicServiceEvent.ACTION_MUSIC_CHANGE:
                updateNotification();
                break;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            try {
                //noinspection ConstantConditions
                switch (intent.getAction()) {
                    case BaseForegroundService.Commands.START_FOREGROUND:
                        doStartMusic();
                        break;
                    case BaseForegroundService.Commands.STOP_FOREGROUND:
                        doStopMusic();
                        break;
                    case Commands.MUSIC_PLAY_PAUSE:
                        doPlayPauseMusic();
                        break;
                }
            } catch (Exception e) {
                if (LogUtil.DEBUG) LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }

        super.onStartCommand(intent, flags, startId);

        return START_NOT_STICKY;
    }

    @Override
    protected void onStartForeground() {
        registerReceiver();
    }

    @Override
    protected void onStopForeground() {
        unregisterReceiver();
    }

    @Override
    protected void onStopForegroundWithConfirm() {

    }

    @Override
    protected int getServiceId() {
        return ServiceIds.MUSIC_SERVICE;
    }

    @Override
    protected Notification getForegroundNotification() {
        Context packageContext = MusicForegroundService.this;

        Intent playPauseIntent = new Intent(packageContext, MusicForegroundService.class);
        playPauseIntent.setAction(Commands.MUSIC_PLAY_PAUSE);
        PendingIntent playPausePendingIntent = PendingIntent.getService(packageContext, getServiceId(), playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent closeIntent = new Intent(packageContext, MusicForegroundService.class);
        closeIntent.setAction(Commands.STOP_FOREGROUND);
        PendingIntent closePendingIntent = PendingIntent.getService(packageContext, getServiceId(), closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_music_foreground_service);// set your custom layout

        MusicManager musicManager = MusicManager.getIns();
        if (musicManager.isPlaying()) {
            contentView.setImageViewResource(R.id.btn_play_pause, android.R.drawable.ic_media_pause);
        } else {
            contentView.setImageViewResource(R.id.btn_play_pause, android.R.drawable.ic_media_play);
        }

        contentView.setOnClickPendingIntent(R.id.notification_btn_close, closePendingIntent);
        contentView.setOnClickPendingIntent(R.id.btn_play_pause, playPausePendingIntent);

        return new NotificationCompat.Builder(packageContext)
                .setContent(contentView)
                .setCustomBigContentView(contentView)
                .setSmallIcon(R.mipmap.app_icon)
                .setLargeIcon(ImageUtil.safeDecodeFile(getResources(), R.mipmap.app_icon))
                .setAutoCancel(true)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void doStartMusic() {
        MusicManager musicManager = MusicManager.getIns();
        if (!musicManager.isPrepared()) {
            MusicManager.getIns().play();
        }
    }

    private void doStopMusic() {
        MusicManager musicManager = MusicManager.getIns();
        if (musicManager.isPrepared()) {
            MusicManager.getIns().stop();
        }
    }

    private void doPlayPauseMusic() {
        MusicManager musicManager = MusicManager.getIns();

        if (musicManager.isPlaying()) {
            musicManager.pause();
        } else {
            musicManager.resume();
        }
    }

    private void updateNotification() {
        startForeground(getServiceId(), getForegroundNotification());
    }

    private void registerReceiver() {
        unregisterReceiver();
        try {
            EventBus.getDefault().register(this);
        } catch (Exception ignore) {
        }
    }

    private void unregisterReceiver() {
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception ignore) {
        }
    }



    public static synchronized void startMusic(Context context) {
        processStart(context, MusicForegroundService.class);
    }

    public static synchronized void stopMusic(Context context) {
        processStop(context, MusicForegroundService.class);
    }

    public static synchronized void processMusicChanged() {
        ToMusicServiceEvent event = new ToMusicServiceEvent(ToMusicServiceEvent.ACTION_MUSIC_CHANGE);
        EventBus.getDefault().post(event);
    }

}
