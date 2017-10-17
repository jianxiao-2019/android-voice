package com.kikatech.voice.engine.tts;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.kikatech.voice.util.TimerUtil;
import com.kikatech.voice.util.log.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ryanlin on 21/09/2017.
 */

public class TtsManager {

    private final MediaPlayer mMediaPlayer;

    private final List<TtsMark> mTtsMarks;

    private String mTtsUrl; // = "http://mms.blog.xuite.net/cf/7b/11732000/blog_698/dv/169811/169811.mp3";
    private long mTtsStartTime = -1;
    private long mTtsEndTime = -1;

    private TtsListener mTtsListener;

    private static TtsManager sTtsManager;
    public static TtsManager getInstance() {
        if (sTtsManager == null) {
            sTtsManager = new TtsManager();
        }
        return sTtsManager;
    }

    private TtsManager() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mTtsMarks = Collections.synchronizedList(new ArrayList<TtsMark>());
    }

    public void setTtsMasks(String ttsMasks) {
        Log.d("Ryan", "TtsManager setTtsMasks ttsMasks = " + ttsMasks);
        parsingTtsMarks(ttsMasks);
    }

    public void setTtsListener(TtsListener listener) {
        mTtsListener = listener;
    }

    public void startTts(String url, final boolean canBeBargingIn, TtsListener listener) {
        mTtsListener = listener;
        synchronized (mMediaPlayer) {
            mTtsUrl = url;
            mTtsMarks.clear();

            mMediaPlayer.reset();
            try {
                mMediaPlayer.setDataSource(url);
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        // TODO : Move this callback to other place? or add a 'TTS start' listener.
                        TimerUtil.logTag("onPrepared");
                        TimerUtil.sReadyToPlayTtsTime = SystemClock.elapsedRealtime();
                        mp.start();
                        if (canBeBargingIn) {
                            mTtsStartTime = SystemClock.elapsedRealtime();
                        } else {
                            mTtsStartTime = -1;
                        }
                        if (mTtsListener != null) {
                            mTtsListener.OnTtsStart();
                        }
                    }
                });
                mMediaPlayer.prepareAsync();
                Log.d("Ryan", "startTts listener = " + listener);
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (mTtsListener != null) {
                            mTtsListener.OnTtsFinish();
                        }
                    }
                });
                TimerUtil.logTag("prepareAsync");
                TimerUtil.sPrepareTtsTime = SystemClock.elapsedRealtime();
            } catch (IOException e) {
                e.printStackTrace();
                if (mTtsListener != null) {
                    mTtsListener.OnTtsStartError();
                }
            }
        }
    }

    public boolean bargeIn() {
        synchronized (mMediaPlayer) {
            Log.i("Ryan", "TtsManager bargeIn!!");
            if (mTtsStartTime == -1) {
                Log.e("Ryan", "Can't be barging in!!");
                mTtsEndTime = -1;
                return false;
            }
            mMediaPlayer.setOnCompletionListener(null);
            mMediaPlayer.stop();
            mTtsEndTime = SystemClock.elapsedRealtime();
        }
        return true;
    }

    public String getBargeInTtsMark() {
        if (mTtsMarks.size() == 0) {
            return null;
        }
        if (mTtsEndTime == -1) {
            Log.e("Ryan", "getBargeInTtsMark set time error, or can't be barging in.");
        }
        long duration = mTtsEndTime - mTtsStartTime;
        Log.d("Ryan", "getBargeInTtsMark duration = " + duration);
        for (TtsMark ttsMark : mTtsMarks) {
            if (ttsMark.time > duration) {
                return ttsMark.jsonStr;
            }
        }
        return mTtsMarks.get(mTtsMarks.size() - 1).jsonStr;
    }

    public boolean isPlayingTts() {
        synchronized (mMediaPlayer) {
            return mMediaPlayer.isPlaying();
        }
    }

    private void parsingTtsMarks(String ttsMarks) {
        if (TextUtils.isEmpty(ttsMarks)) {
            mTtsMarks.add(new TtsMark(0, ""));
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray(ttsMarks);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
//                TtsMark ttsMark = new TtsMark(
//                        obj.getLong("time"),
//                        obj.getInt("start"),
//                        obj.getInt("end"),
//                        obj.getString("value"));
                TtsMark ttsMark = new TtsMark(obj.getLong("time"), obj.toString());
                Log.d("Ryan", "TtsManager parsingTtsMarks time = " + obj.getLong("time") + " str = " + obj);
                mTtsMarks.add(ttsMark);
            }
        } catch (JSONException e) {
            Logger.w("Some error occurred when parsing json at TtsManager");
            mTtsMarks.add(new TtsMark(0, ""));
        }
    }

    static class TtsMark {
        long time;
//        int start;
//        int end;
//        String value;
        String jsonStr;

//        public TtsMark(long time, int start, int end, String value) {
//            this.time = time;
//            this.start = start;
//            this.end = end;
//            this.value = value;
//        }
    TtsMark(long time, String jsonStr) {
            this.time = time;
            this.jsonStr = jsonStr;
        }
    }

    public interface TtsListener {
        void OnTtsStart();
        void OnTtsStartError();
        void OnTtsFinish();
    }
}
