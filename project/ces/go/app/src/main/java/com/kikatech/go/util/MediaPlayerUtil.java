package com.kikatech.go.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.kikatech.go.util.LogUtil;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * @author SkeeterWang Created on 2017/11/19.
 */

public class MediaPlayerUtil {
    private static final String TAG = "MediaPlayerUtil";

    private static final int STREAM_TYPE_ALERT = AudioManager.STREAM_MUSIC;

    private static MediaPlayer mMediaPlayer = new MediaPlayer();
    private static IPlayStatusListener playStatusListener;

    public synchronized static void playAlert(Context context, int alertRes, IPlayStatusListener listener) {
        try {
            playStatusListener = listener;

            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
            }

            mMediaPlayer.setAudioStreamType(STREAM_TYPE_ALERT);

            AssetFileDescriptor afd = context.getResources().openRawResourceFd(alertRes);
            safeSetMediaPlayerSource(mMediaPlayer, afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if (LogUtil.DEBUG) {
                        LogUtil.log(TAG, "Audio onError()");
                    }

                    mMediaPlayer.stop();
                    mMediaPlayer.reset();

                    if (playStatusListener != null) {
                        playStatusListener.onStop();
                        playStatusListener = null;
                    }

                    return false;
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (LogUtil.DEBUG) {
                        LogUtil.log(TAG, "Audio onCompletion()");
                    }

                    if (playStatusListener != null) {
                        playStatusListener.onStop();
                        playStatusListener = null;
                    }

                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                }
            });
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (playStatusListener != null) {
                        playStatusListener.onStart();
                    }
                }
            });
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
            if (playStatusListener != null) {
                playStatusListener.onStop();
                playStatusListener = null;
            }

        }
    }

    private synchronized static void safeSetMediaPlayerSource(MediaPlayer mediaPlayer, FileDescriptor fd, long offset, long length) throws IOException {
        // http://stackoverflow.com/questions/7816551/java-lang-illegalstateexception-what-does-it-mean
        if (mediaPlayer != null) {
            try {
                mediaPlayer.setDataSource(fd, offset, length);
            } catch (IllegalStateException e) {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(fd, offset, length);
            }
        }
    }

    public interface IPlayStatusListener {
        void onStart();

        void onStop();
    }
}
