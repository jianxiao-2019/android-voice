package com.kikatech.voice.core.tts.impl;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Pair;

import com.kikatech.voice.core.tts.TtsSource;
import com.kikatech.voice.util.log.Logger;
import com.kikatech.voice.util.request.RequestManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by ryanlin on 29/11/2017.
 */

public class KikaTtsSource implements TtsSource {

    private static final String WEB_SOCKET_URL_DEV = "http://speech0-dev-mvp.kikakeyboard.com/v3/tts/sign";

    private Context mContext;
    private TtsStateChangedListener mListener;
    private final MediaPlayer mMediaPlayer;

    private boolean mIsTtsInterrupted = false;

    public KikaTtsSource() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void init(Context context, OnTtsInitListener listener) {
        // TODO : if context == null
        mContext = context;
        KikaTtsCacheHelper.init(context);
        if (listener != null) {
            listener.onTtsInit(INIT_SUCCESS);
        }
    }

    @Override
    public void close() {
        mMediaPlayer.stop();
        mMediaPlayer.release();
    }

    @Override
    public void speak(String text) {
        Logger.d("[KikaTtsSource] start to speak : " + text);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("language", "en_us");
            jsonObject.put("timezone", TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT, Locale.ENGLISH));
            jsonObject.put("contents", new JSONArray().put(genJsonData(text, 0)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        speakImp(jsonObject.toString());
    }

    @Override
    public void speak(Pair<String, Integer>[] sentences) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        try {
            for (Pair<String, Integer> sentence : sentences) {
                jsonArray.put(genJsonData(sentence.first, sentence.second));
            }
            jsonObject.put("language", "en_us");
            jsonObject.put("timezone", TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT, Locale.ENGLISH));
            jsonObject.put("contents", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        speakImp(jsonObject.toString());
    }

    private void speakImp(String jsonString) {
        Logger.d("[KikaTtsSource][speakImp] jsonString:" + jsonString);
        String cacheJson = null;
        try {
            cacheJson = KikaTtsCacheHelper.getCacheListJsonString(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(cacheJson)) {
            Logger.d("[KikaTtsSource][speakImp] Cache(s) hit, use :" + cacheJson);
            playTtsByMediaPlayer(cacheJson);
        } else {
            Logger.d("[KikaTtsSource][speakImp] NO cache, query server :" + jsonString);
            new SendPostTask().execute(jsonString);
        }

        mIsTtsInterrupted = false;
    }

    private JSONObject genJsonData(String text, int vid) throws JSONException {
        JSONObject dataJson = new JSONObject();

        dataJson.put("text", text);
        dataJson.put("vid", vid);

        return dataJson;
    }

    @Override
    public void interrupt() {
        mMediaPlayer.stop();
        mIsTtsInterrupted = true;
        if(mMediaPlayer.isPlaying()) {
            if (mListener != null) {
                mListener.onTtsInterrupted();
            }
        }
    }

    @Override
    public boolean isTtsSpeaking() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public void setTtsStateChangedListener(TtsStateChangedListener listener) {
        mListener = listener;
    }

    private void playTtsByMediaPlayer(String jsonString) {
        Logger.d("[KikaTtsSource][playTtsByMediaPlayer] jsonString:" + jsonString);

        List<KikaTtsCacheHelper.MediaSource> playList = KikaTtsCacheHelper.parseMediaSource(jsonString);
        if (playList != null && playList.size() > 0) {
            playTtsByMediaPlayer(playList, 0);
        }
    }

    private void playTtsByMediaPlayer(final List<KikaTtsCacheHelper.MediaSource> playList, final int idx) {
        final long start_t = System.currentTimeMillis();
        try {
            if(idx >= playList.size()) {
                return;
            }

            KikaTtsCacheHelper.MediaSource ms = playList.get(idx);

            mMediaPlayer.reset();

            AssetFileDescriptor descriptor = ms.getAssetFileDescriptor(mContext);
            if (descriptor != null) {
                mMediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                descriptor.close();
            } else {
                mMediaPlayer.setDataSource(ms.getPathSource());
            }

            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Logger.d("[KikaTtsSource] onPostExecute, spend:" + (System.currentTimeMillis() - start_t) + " ms");
                    mp.start();
                    if (mListener != null && idx == 0) {
                        mListener.onTtsStart();
                        Logger.d("[KikaTtsSource][playTtsByMediaPlayer] onTtsStart");
                    }
                }
            });
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mListener != null && !mIsTtsInterrupted && idx == playList.size() - 1) {
                        mListener.onTtsComplete();
                        mIsTtsInterrupted = false;
                        Logger.d("[KikaTtsSource][playTtsByMediaPlayer] onTtsComplete");
                    } else {
                        playTtsByMediaPlayer(playList, idx + 1);
                    }
                    Logger.d("[KikaTtsSource] setOnCompletionListener, spend:" + (System.currentTimeMillis() - start_t) + " ms");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (mListener != null) {
                mListener.onTtsError();
            }
        }
    }

    private class SendPostTask extends AsyncTask<String, Void, String> {

        private final long start_t = System.currentTimeMillis();

        @Override
        protected String doInBackground(String... params) {
            Logger.d("[KikaTtsSource] json = " + params[0]);
            final String jsonString = params[0];
            long t = System.currentTimeMillis();
            final StringBuilder ttsUrl = new StringBuilder();
            BufferedReader in = null;
            try {
                URLConnection conn = new URL(WEB_SOCKET_URL_DEV + "?sign=" + RequestManager.getSign(mContext)).openConnection();

                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestProperty("Accept", "*/*");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "application/octet-stream");
                conn.setRequestProperty("User-Agent", RequestManager.generateUserAgent(mContext));
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(params[0]);
                wr.flush();

                InputStream stream = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(stream));
                String line;
                while ((line = in.readLine()) != null) {
                    ttsUrl.append(line + "\n");
                }

            } catch (Exception e) {
                Logger.e(e.getMessage());
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            Logger.d("[KikaTtsSource] Get Url end, spend:" + (System.currentTimeMillis() - start_t) + " ms, " + " ttsUrl = " + ttsUrl);
            KikaTtsCacheHelper.submitDownloadTask(ttsUrl.toString(), jsonString);
            return ttsUrl.toString();
        }

        @Override
        protected void onPostExecute(String ttsUrl) {
            playTtsByMediaPlayer(KikaTtsCacheHelper.composeUrlVoiceSource(ttsUrl));
        }
    }
}