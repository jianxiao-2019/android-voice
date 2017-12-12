package com.kikatech.voice.core.tts.impl;

import android.content.Context;
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
        if (listener != null) {
            listener.onTtsInit(INIT_SUCCESS);
        }
    }

    @Override
    public void close() {
        interrupt();
        mMediaPlayer.release();
    }

    @Override
    public void speak(String text) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("language", "en_us");
            jsonObject.put("contents", new JSONArray().put(genJsonData(text, 0)));
            new SendPostTask().execute(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mIsTtsInterrupted = false;
    }

    @Override
    public void speak(Pair<String, Integer>[] sentences) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (Pair<String, Integer> sentence : sentences) {
                jsonArray.put(genJsonData(sentence.first, sentence.second));
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("language", "en_us");
            jsonObject.put("contents", jsonArray);
            new SendPostTask().execute(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
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
        if (mListener != null) {
            mIsTtsInterrupted = true;
            mListener.onTtsInterrupted();
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

    private class SendPostTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Logger.d("doInBackground json = " + params[0]);
            StringBuilder ttsUrl = new StringBuilder();
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

            Logger.d("doInBackground end ttsUrl = " + ttsUrl);
            return ttsUrl.toString();
        }

        @Override
        protected void onPostExecute(final String ttsUrl) {
            String url = null;
            try {
                JSONArray array = new JSONArray(ttsUrl);
                url = array.getString(0);

                if (TextUtils.isEmpty(url)) {
                    return;
                }
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(url);
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        Logger.d("onPostExecute");
                        mp.start();
                        if (mListener != null) {
                            mListener.onTtsStart();
                        }
                    }
                });
                mMediaPlayer.prepareAsync();
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (mListener != null && !mIsTtsInterrupted) {
                            mListener.onTtsComplete();
                        }
                        mIsTtsInterrupted = false;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                if (mListener != null) {
                    mListener.onTtsError();
                }
            }
        }
    }
}
