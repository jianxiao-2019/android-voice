package com.kikatech.go.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.RemoteViews;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.music.MusicSceneUtil;
import com.kikatech.go.eventbus.ToMusicServiceEvent;
import com.kikatech.go.music.MusicManager;
import com.kikatech.go.music.model.YouTubeVideoList;
import com.kikatech.go.services.presenter.YouTubeExtractorManager;
import com.kikatech.go.services.view.manager.FloatingPlayerManager;
import com.kikatech.go.util.IntentUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.ImageUtil;

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
        private static final String OPEN_KIKA_GO = PRI_MUSIC_SERVICE + "open_kika_go";
        private static final String MUSIC_PLAY_PAUSE = PRI_MUSIC_SERVICE + "music_play_pause";
        private static final String MUSIC_PREV_SONG = PRI_MUSIC_SERVICE + "music_prev_song";
        private static final String MUSIC_NEXT_SONG = PRI_MUSIC_SERVICE + "music_next_song";
    }


    private FloatingPlayerManager mManager;


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
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, String.format("onToMusicServiceEvent, action: %s", action));
        }
        switch (action) {
            case ToMusicServiceEvent.ACTION_MUSIC_CHANGE:
                updateNotification();
                break;
            case ToMusicServiceEvent.ACTION_VOLUME_CONTROL:
                @MusicSceneUtil.VolumeControlType int volumeControlType = event.getExtras().getInt(ToMusicServiceEvent.PARAM_VOLUME_CONTROL_TYPE);
                doVolumeControl(volumeControlType);
                break;
            case ToMusicServiceEvent.ACTION_PAUSE_MUSIC:
                mManager.pauseMusic();
                updateNotification();
                break;
            case ToMusicServiceEvent.ACTION_RESUME_MUSIC:
                mManager.resumeMusic();
                updateNotification();
                break;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mManager = new FloatingPlayerManager.Builder()
                .setWindowManager((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .setLayoutInflater((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .setConfiguration(getResources().getConfiguration())
                .build(MusicForegroundService.this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            try {
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, "onStartCommand: " + intent.getAction());
                }
                //noinspection ConstantConditions
                switch (intent.getAction()) {
                    case Commands.MUSIC_PLAY_PAUSE:
                        doPlayPauseMusic();
                        return START_STICKY;
                    case Commands.MUSIC_PREV_SONG:
                        doPlayPrevSong();
                        return START_STICKY;
                    case Commands.MUSIC_NEXT_SONG:
                        doPlayNextSong();
                        return START_STICKY;
                    case Commands.OPEN_KIKA_GO:
                        IntentUtil.openKikaGo(MusicForegroundService.this);
                        return START_STICKY;
                }
            } catch (Exception e) {
                if (LogUtil.DEBUG) LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        onStopForeground();
        super.onDestroy();
    }

    @Override
    protected void onStartForeground() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "onStartForeground");
        }
        registerReceiver();
        doStartMusic(YouTubeExtractorManager.getIns().getPlayingList());
    }

    @Override
    protected void onStopForeground() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "onStopForeground");
        }
        unregisterReceiver();
        doStopMusic();
    }

    @Override
    protected void onStopForegroundWithConfirm() {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mManager.updateConfiguration(newConfig);
    }

    @Override
    protected int getServiceId() {
        return ServiceIds.MUSIC_SERVICE;
    }

    @Override
    protected Notification getForegroundNotification() {
        Context packageContext = MusicForegroundService.this;

        Intent openIntent = new Intent(packageContext, MusicForegroundService.class);
        openIntent.setAction(Commands.OPEN_KIKA_GO);
        PendingIntent openPendingIntent = PendingIntent.getService(packageContext, getServiceId(), openIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playPauseIntent = new Intent(packageContext, MusicForegroundService.class);
        playPauseIntent.setAction(Commands.MUSIC_PLAY_PAUSE);
        PendingIntent playPausePendingIntent = PendingIntent.getService(packageContext, getServiceId(), playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent prevIntent = new Intent(packageContext, MusicForegroundService.class);
        prevIntent.setAction(Commands.MUSIC_PREV_SONG);
        PendingIntent prevPendingIntent = PendingIntent.getService(packageContext, getServiceId(), prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(packageContext, MusicForegroundService.class);
        nextIntent.setAction(Commands.MUSIC_NEXT_SONG);
        PendingIntent nextPendingIntent = PendingIntent.getService(packageContext, getServiceId(), nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent closeIntent = new Intent(packageContext, MusicForegroundService.class);
        closeIntent.setAction(Commands.STOP_FOREGROUND);
        PendingIntent closePendingIntent = PendingIntent.getService(packageContext, getServiceId(), closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_music_foreground_service);// set your custom layout

        boolean isPlaying = MusicManager.getIns().isPlaying(MusicManager.ProviderType.YOUTUBE);
        int imgRes = isPlaying ? R.drawable.selector_qs_ic_pause : R.drawable.selector_qs_ic_play;
        contentView.setImageViewResource(R.id.btn_play_pause, imgRes);

        contentView.setTextViewText(R.id.tv_video_title, mManager.getCurrentVideoTitle());

        contentView.setOnClickPendingIntent(R.id.btn_play_pause, playPausePendingIntent);
        contentView.setOnClickPendingIntent(R.id.btn_play_prev, prevPendingIntent);
        contentView.setOnClickPendingIntent(R.id.btn_play_next, nextPendingIntent);
        contentView.setOnClickPendingIntent(R.id.notification_btn_close, closePendingIntent);

        return new NotificationCompat.Builder(packageContext)
                .setContent(contentView)
                .setCustomBigContentView(contentView)
                .setSmallIcon(R.mipmap.app_icon)
                .setLargeIcon(ImageUtil.safeDecodeFile(getResources(), R.mipmap.app_icon))
                .setContentIntent(openPendingIntent)
                .setAutoCancel(true)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void doStartMusic(YouTubeVideoList listToPlay) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "doStartMusic");
        }
        if (listToPlay != null && !listToPlay.isEmpty()) {
            mManager.showPlayer(listToPlay);
        }
    }

    private void doStopMusic() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "doStopMusic");
        }
        mManager.removePlayer();
    }

    private void doPlayPauseMusic() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "doPlayPauseMusic");
        }
        mManager.pauseOrResume();
    }

    private void doPlayPrevSong() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "doPlayPrevSong");
        }
        mManager.prev();
    }

    private void doPlayNextSong() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "doPlayNextSong");
        }
        mManager.next();
    }

    private void doVolumeControl(@MusicSceneUtil.VolumeControlType int type) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "doVolumeControl");
        }
        mManager.volumeControl(type);
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


    public static synchronized void startMusic(Context context, final YouTubeVideoList listToPlay) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "startMusic");
        }
        processStart(context, MusicForegroundService.class);
        YouTubeExtractorManager.getIns().bindPlayList(listToPlay);
    }

    public static synchronized void stopMusic(Context context) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "stopMusic");
        }
        processStop(context, MusicForegroundService.class);
    }

    public static synchronized void pauseMusic() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "pauseMusic");
        }
        ToMusicServiceEvent event = new ToMusicServiceEvent(ToMusicServiceEvent.ACTION_PAUSE_MUSIC);
        sendToMusicServiceEvent(event);
    }

    public static synchronized void resumeMusic() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "resumeMusic");
        }
        ToMusicServiceEvent event = new ToMusicServiceEvent(ToMusicServiceEvent.ACTION_RESUME_MUSIC);
        sendToMusicServiceEvent(event);
    }

    public static synchronized void processMusicChanged() {
        ToMusicServiceEvent event = new ToMusicServiceEvent(ToMusicServiceEvent.ACTION_MUSIC_CHANGE);
        sendToMusicServiceEvent(event);
    }

    public static synchronized void processVolumeControl(@MusicSceneUtil.VolumeControlType int type) {
        ToMusicServiceEvent event = new ToMusicServiceEvent(ToMusicServiceEvent.ACTION_VOLUME_CONTROL);
        event.putExtra(ToMusicServiceEvent.PARAM_VOLUME_CONTROL_TYPE, type);
        sendToMusicServiceEvent(event);
    }

    private synchronized static void sendToMusicServiceEvent(ToMusicServiceEvent event) {
        EventBus.getDefault().post(event);
    }
}
