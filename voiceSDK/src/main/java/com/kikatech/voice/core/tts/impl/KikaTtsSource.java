package com.kikatech.voice.core.tts.impl;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Pair;

import com.kikatech.voice.core.tts.TtsSource;
import com.kikatech.voice.util.AsyncThread;
import com.kikatech.voice.util.log.LogUtil;
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
    private static final String TAG = "KikaTtsSource";

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
        if (LogUtil.DEBUG)
            LogUtil.log(TAG, "start to speak : " + text);

        String jsonQueryString = "";
        try {
            jsonQueryString = getQueryJsonString(new JSONArray().put(genJsonData(text, 0)));
        } catch (JSONException e) {
            if (LogUtil.DEBUG)
                LogUtil.log(TAG, " Err:" + e);
            e.printStackTrace();
        }
        speakImp(jsonQueryString);
    }

    @Override
    public void speak(Pair<String, Integer>[] sentences) {
        if (LogUtil.DEBUG)
            LogUtil.log(TAG, " sentences count : " + sentences.length + ", 0:" + sentences[0]);

        String jsonQueryString = "";
        try {
            JSONArray jsonArray = new JSONArray();
            for (Pair<String, Integer> sentence : sentences) {
                jsonArray.put(genJsonData(sentence.first, sentence.second));
            }
            jsonQueryString = getQueryJsonString(jsonArray);
        } catch (JSONException e) {
            if (LogUtil.DEBUG)
                LogUtil.log(TAG, " Err:" + e);
            e.printStackTrace();
        }
        speakImp(jsonQueryString);
    }

    private String getQueryJsonString(Object contentsValue) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("language", "en_us");
            jsonObject.put("timezone", TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT, Locale.ENGLISH));
            jsonObject.put("contents", contentsValue);
        } catch (JSONException e) {
            if (LogUtil.DEBUG)
                LogUtil.log(TAG, " Err:" + e);
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    private void speakImp(String jsonString) {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "[speakImp] jsonString:" + jsonString);
        String cacheJson = null;
        try {
            cacheJson = KikaTtsCacheHelper.getCacheListJsonString(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(cacheJson)) {
            if (LogUtil.DEBUG)
                LogUtil.log(TAG, "[speakImp] Cache(s) hit, use :" + cacheJson);
            playTtsByMediaPlayer(cacheJson);
        } else {
            if (LogUtil.DEBUG)
                LogUtil.log(TAG, "[speakImp] NO cache, query server :" + jsonString);
            //new SendPostTask().execute(jsonString);
            playTTsByDownload(jsonString);
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
        if (mMediaPlayer.isPlaying()) {
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
        if (LogUtil.DEBUG)
            LogUtil.log(TAG, "jsonString:" + jsonString);

        List<KikaTtsCacheHelper.MediaSource> playList = KikaTtsCacheHelper.parseMediaSource(jsonString);
        if (playList != null && playList.size() > 0) {
            playTtsByMediaPlayer(playList, 0);
        }
    }

    private void playTtsByMediaPlayer(final List<KikaTtsCacheHelper.MediaSource> playList, final int idx) {
        final long start_t = System.currentTimeMillis();
        try {
            if (idx >= playList.size()) {
                return;
            }

            KikaTtsCacheHelper.MediaSource ms = playList.get(idx);

            mMediaPlayer.reset();

            AssetFileDescriptor descriptor = ms.getAssetFileDescriptor(mContext);
            if (descriptor != null) {
                mMediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                descriptor.close();
            } else {
                if (LogUtil.DEBUG) LogUtil.log(TAG, "[player] source:" + ms.getPathSource());
                mMediaPlayer.setDataSource(ms.getPathSource());
            }

            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if (LogUtil.DEBUG)
                        LogUtil.log(TAG, "[player] onError, what:" + what + ", extra:" + extra);
                    if (mListener != null) {
                        mListener.onTtsError();
                    }
                    return false;
                }
            });
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (LogUtil.DEBUG)
                        LogUtil.log(TAG, "[player] onPrepared, spend:" + (System.currentTimeMillis() - start_t) + " ms");
                    mp.start();
                    if (mListener != null && idx == 0) {
                        mListener.onTtsStart();
                        if (LogUtil.DEBUG)
                            LogUtil.log(TAG, "[player] onTtsStart");
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
                        if (LogUtil.DEBUG)
                            LogUtil.log(TAG, "[player] onTtsComplete");
                    } else {
                        playTtsByMediaPlayer(playList, idx + 1);
                    }
                    if (LogUtil.DEBUG)
                        LogUtil.log(TAG, "[player] setOnCompletionListener, spend:" + (System.currentTimeMillis() - start_t) + " ms");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (LogUtil.DEBUG)
                LogUtil.log(TAG, "[player] onError : " + e);
            if (mListener != null) {
                mListener.onTtsError();
            }
        }
    }

    private void playTTsByDownload(final String jsonString) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                if (LogUtil.DEBUG)
                    LogUtil.log(TAG, "start >>>>>>>>>>>> ");

                String ttsUrl = fetchTssUrl(jsonString);

                if (LogUtil.DEBUG)
                    LogUtil.log(TAG, "fetchTssUrl OK");

                boolean ret = KikaTtsCacheHelper.syncDownloadTask(ttsUrl, jsonString);

                if (LogUtil.DEBUG)
                    LogUtil.log(TAG, "syncDownloadTask, result:" + ret);

                if(!ret && mListener != null) {
                    // The timeout mechanism of media player seems not working, return here
                    mListener.onTtsError();
                    return;
                }

                playTtsByMediaPlayer(KikaTtsCacheHelper.composeUrlVoiceSource(ttsUrl));

                if (LogUtil.DEBUG)
                    LogUtil.log(TAG, "end <<<<<<<<<<<<");
            }
        };

        if (!AsyncThread.getIns().isBusy()) {
            AsyncThread.getIns().execute(task);
        } else {
            if (LogUtil.DEBUG)
                LogUtil.log(TAG, "playTTsByDownload on NEW THREAD");
            new Thread(task).start();
        }
    }

    private String fetchTssUrl(String jsonString) {
        final long start_t = System.currentTimeMillis();

        final StringBuilder ttsUrl = new StringBuilder();
        BufferedReader in = null;
        try {
            URLConnection conn = new URL(WEB_SOCKET_URL_DEV + "?sign=" + RequestManager.getSign(mContext)).openConnection();

            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setRequestProperty("User-Agent", RequestManager.generateUserAgent(mContext));
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(jsonString);
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

        if (LogUtil.DEBUG)
            LogUtil.log(TAG, " Get Url end, spend:" + (System.currentTimeMillis() - start_t) + " ms, " + " ttsUrl = " + ttsUrl);

        return ttsUrl.toString();
    }
}