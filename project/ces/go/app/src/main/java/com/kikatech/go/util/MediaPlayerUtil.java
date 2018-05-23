package com.kikatech.go.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * @author SkeeterWang Created on 2017/11/19.
 */

public class MediaPlayerUtil {
    private static final String TAG = "MediaPlayerUtil";

    private static final int STREAM_TYPE_ALERT = AudioManager.STREAM_MUSIC;


    private static MediaPlayerUtil sIns;

    public static synchronized MediaPlayerUtil getIns() {
        if (sIns == null) {
            sIns = new MediaPlayerUtil();
        }
        return sIns;
    }


    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private IPlayStatusListener playStatusListener;
    private IPlayStatusListener mVolumeControlListener = new IPlayStatusListener() {
        @Override
        public void onStart() {
            AudioManagerUtil.getIns().maximumVolume();
        }

        @Override
        public void onStop() {
            AudioManagerUtil.getIns().recoveryVolume();
        }
    };
    private float mVolume = -1;

    private MediaPlayerUtil() {
        mMediaPlayer = new MediaPlayer();
    }


    public synchronized void playAlert(Context context, int alertRes, IPlayStatusListener listener) {
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

                    onStop();

                    return false;
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (LogUtil.DEBUG) {
                        LogUtil.log(TAG, "Audio onCompletion()");
                    }

                    onStop();

                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                }
            });
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    onStart();
                }
            });
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            if (mVolume >= 0 && mVolume <= 1) {
                mMediaPlayer.setVolume(mVolume, mVolume);
            }
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
            onStop();
        }
    }


    private synchronized void onStart() {
        mVolumeControlListener.onStart();
        if (playStatusListener != null) {
            playStatusListener.onStart();
        }
    }

    private synchronized void onStop() {
        mVolumeControlListener.onStop();
        if (playStatusListener != null) {
            playStatusListener.onStop();
            playStatusListener = null;
        }
    }


    /**
     * @param volume range 0.0 to 1.0
     */
    public void setVolume(float volume) {
        mVolume = volume;
    }


    private synchronized void safeSetMediaPlayerSource(MediaPlayer mediaPlayer, FileDescriptor fd, long offset, long length) throws IOException {
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

    public synchronized void safeSetMediaPlayerSource(MediaPlayer mediaPlayer, String path) throws IOException {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.setDataSource(path);
            } catch (IllegalStateException e) {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(path);
            }
        }
    }

    public interface IPlayStatusListener {
        void onStart();

        void onStop();
    }
}
