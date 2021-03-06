package com.kikatech.voice.core.tts.impl;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Pair;

import com.kikatech.voice.core.tts.TtsSource;
import com.kikatech.voice.util.AsyncThread;
import com.kikatech.voice.util.log.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by ryanlin on 29/11/2017.
 */

public class KikaTtsSource implements TtsSource {

    private static final String TAG = "KikaTtsSource";

    private Context mContext;
    private TtsStateChangedListener mListener;
    private final MediaPlayer mMediaPlayer;
    private final TtsSource mBackupAndroidTts;

    private boolean mIsTtsInterrupted = false;
    private float mVolume = INVALID_VOLUME;

    static class TtsInfo {
        private final String jsonString;
        private Pair<String, Integer>[] inputSentences = null;
        private String inputText = null;

        TtsInfo(String jsonQueryString, Pair<String, Integer>[] sentences) {
            jsonString = jsonQueryString;
            inputSentences = sentences;
        }

        TtsInfo(String jsonQueryString, String text) {
            jsonString = jsonQueryString;
            inputText = text;
        }
    }

    public KikaTtsSource() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mBackupAndroidTts = new AndroidTtsSource();
    }

    @Override
    public void init(Context context, final OnTtsInitListener listener) {
        // TODO : if context == null
        mContext = context;
        KikaTtsCacheHelper.init(context);
        mBackupAndroidTts.init(context, new OnTtsInitListener() {
            @Override
            public void onTtsInit(int state) {
                if (Logger.DEBUG) {
                    Logger.i(TAG, "backupAndroidTts init complete");
                }
            }
        });
        if (listener != null) {
            listener.onTtsInit(INIT_SUCCESS);
        }
    }

    @Override
    public void close() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer.release();
        mBackupAndroidTts.close();
    }

    @Override
    public void setVolume(float volume) {
        mVolume = volume;
        mBackupAndroidTts.setVolume(volume);
    }

    @Override
    public void speak(String text) {
        if (Logger.DEBUG) {
            Logger.i(TAG, "start to speak : " + text);
        }

        String jsonQueryString = "";
        try {
            jsonQueryString = getQueryJsonString(new JSONArray().put(genJsonData(text, 0)));
        } catch (JSONException e) {
            if (Logger.DEBUG) {
                Logger.printStackTrace(TAG, e.getMessage(), e);
            }
        }
        TtsInfo ttsInfo = new TtsInfo(jsonQueryString, text);
        speakImp(ttsInfo);
    }

    @Override
    public void speak(Pair<String, Integer>[] sentences) {
        if (Logger.DEBUG) {
            Logger.i(TAG, " sentences count : " + sentences.length + ", 0:" + sentences[0]);
        }

        String jsonQueryString = "";
        try {
            JSONArray jsonArray = new JSONArray();
            for (Pair<String, Integer> sentence : sentences) {
                jsonArray.put(genJsonData(sentence.first, sentence.second));
            }
            jsonQueryString = getQueryJsonString(jsonArray);
        } catch (JSONException e) {
            if (Logger.DEBUG) {
                Logger.printStackTrace(TAG, e.getMessage(), e);
            }
        }
        TtsInfo ttsInfo = new TtsInfo(jsonQueryString, sentences);
        speakImp(ttsInfo);
    }

    private String getQueryJsonString(Object contentsValue) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("language", "en_us");
            jsonObject.put("timezone", TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT, Locale.ENGLISH));
            jsonObject.put("contents", contentsValue);
        } catch (JSONException e) {
            if (Logger.DEBUG) {
                Logger.printStackTrace(TAG, e.getMessage(), e);
            }
        }
        return jsonObject.toString();
    }

    private void speakImp(TtsInfo ttsInfo) {
        if (Logger.DEBUG) {
            Logger.i(TAG, "[speakImp] jsonString:" + ttsInfo.jsonString);
        }

        String cacheJson = null;
        try {
            cacheJson = KikaTtsCacheHelper.getCacheListJsonString(ttsInfo.jsonString);
        } catch (JSONException e) {
            if (Logger.DEBUG) {
                Logger.printStackTrace(TAG, e.getMessage(), e);
            }
        }

        if (!TextUtils.isEmpty(cacheJson)) {
            if (Logger.DEBUG) {
                Logger.i(TAG, "[speakImp] Cache(s) hit, use :" + cacheJson);
            }
            playTtsByMediaPlayer(cacheJson);
        } else {
            if (Logger.DEBUG) {
                Logger.i(TAG, "[speakImp] NO cache, speak via android google tts");
            }
            playTtsByAndroidTts(ttsInfo);
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
        mBackupAndroidTts.interrupt();
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
        if (Logger.DEBUG) {
            Logger.i(TAG, "jsonString:" + jsonString);
        }
        List<KikaTtsCacheHelper.MediaSource> playList = KikaTtsCacheHelper.parseMediaSource(jsonString);
        if (playList != null && playList.size() > 0) {
            playTtsByMediaPlayer(playList, 0);
        } else if (mListener != null) {
            mListener.onTtsError();
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
                if (Logger.DEBUG) Logger.i(TAG, "[player] source:" + ms.getPathSource());
                mMediaPlayer.setDataSource(ms.getPathSource());
            }

            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if (Logger.DEBUG)
                        Logger.i(TAG, "[player] onError, what:" + what + ", extra:" + extra);
                    if (mListener != null) {
                        mListener.onTtsError();
                    }
                    return false;
                }
            });
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (Logger.DEBUG) {
                        Logger.i(TAG, "[player] onPrepared, spend:" + (System.currentTimeMillis() - start_t) + " ms");
                    }
                    mp.start();
                    if (mListener != null && idx == 0) {
                        mListener.onTtsStart();
                        if (Logger.DEBUG) {
                            Logger.i(TAG, "[player] onTtsStart");
                        }
                    }
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mListener != null && !mIsTtsInterrupted && idx == playList.size() - 1) {
                        mListener.onTtsComplete();
                        mIsTtsInterrupted = false;
                        if (Logger.DEBUG) {
                            Logger.i(TAG, "[player] onTtsComplete");
                        }
                    } else {
                        playTtsByMediaPlayer(playList, idx + 1);
                    }
                    if (Logger.DEBUG) {
                        Logger.i(TAG, "[player] setOnCompletionListener, spend:" + (System.currentTimeMillis() - start_t) + " ms");
                    }
                }
            });
            mMediaPlayer.prepareAsync();
            if (mVolume >= 0 && mVolume <= 1) {
                mMediaPlayer.setVolume(mVolume, mVolume);
            }
        } catch (Exception e) {
            if (Logger.DEBUG) {
                Logger.printStackTrace(TAG, e.getMessage(), e);
            }
            if (mListener != null) {
                mListener.onTtsError();
            }
        }
    }

    private void playTtsByAndroidTts(final TtsInfo ttsInfo) {
        mBackupAndroidTts.setTtsStateChangedListener(new TtsStateChangedListener() {
            @Override
            public void onTtsStart() {
                if (Logger.DEBUG) {
                    Logger.i(TAG, "[BackUpAndroidTts] onTtsStart");
                }
                if (mListener != null) {
                    mListener.onTtsStart();
                }
            }

            @Override
            public void onTtsComplete() {
                if (Logger.DEBUG) {
                    Logger.i(TAG, "[BackUpAndroidTts] onTtsComplete");
                }
                if (mListener != null) {
                    mListener.onTtsComplete();
                }
            }

            @Override
            public void onTtsInterrupted() {
                if (Logger.DEBUG) {
                    Logger.i(TAG, "[BackUpAndroidTts] onTtsInterrupted");
                }
                if (mListener != null) {
                    mListener.onTtsInterrupted();
                }
            }

            @Override
            public void onTtsError() {
                if (Logger.DEBUG) {
                    Logger.i(TAG, "[BackUpAndroidTts] onTtsError");
                }
                if (Logger.DEBUG) {
                    Logger.i(TAG, "[speakImp] Android google tts error occurs, query server :" + ttsInfo.jsonString);
                }
                playTTsByDownload(ttsInfo);
            }
        });
        if (ttsInfo.inputText != null) {
            mBackupAndroidTts.speak(ttsInfo.inputText);
        } else if (ttsInfo.inputSentences != null) {
            mBackupAndroidTts.speak(ttsInfo.inputSentences);
        } else if (mListener != null) {
            mListener.onTtsError();
        }
    }

    private void playTTsByDownload(final TtsInfo ttsInfo) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                if (Logger.DEBUG) {
                    Logger.i(TAG, "start >>>>>>>>>>>> ");
                }

                String ttsUrl = KikaTtsServerHelper.fetchTssUrl(mContext, ttsInfo.jsonString);

                if (Logger.DEBUG) {
                    Logger.i(TAG, "fetchTssUrl " + (TextUtils.isEmpty(ttsUrl) ? "Fail" : "OK"));
                }

                if (TextUtils.isEmpty(ttsUrl)) {
                    if (mListener != null) {
                        mListener.onTtsError();
                    }
                    return;
                }

                KikaTtsCacheHelper.TaskInfo task = new KikaTtsCacheHelper.TaskInfo(ttsUrl, ttsInfo.jsonString);
                boolean ret = KikaTtsCacheHelper.downloadWithTask(task);
                //boolean ret = false;

                if (Logger.DEBUG) {
                    Logger.i(TAG, "downloadWithTask, result:" + ret);
                    Logger.i(TAG, "ttsUrl:" + ttsUrl);
                    Logger.i(TAG, "ttsInfo.jsonString:" + ttsInfo.jsonString);
                }

                if ((!ret || TextUtils.isEmpty(ttsUrl)) && mListener != null) {
                    if (Logger.DEBUG) {
                        Logger.i(TAG, "Error occurs !!");
                    }
                    mListener.onTtsError();
                    return;
                }

                playTtsByMediaPlayer(KikaTtsCacheHelper.composeUrlVoiceSource(ttsUrl));

                if (Logger.DEBUG) {
                    Logger.i(TAG, "end <<<<<<<<<<<<");
                }
            }
        };

        if (!AsyncThread.getIns().isBusy()) {
            AsyncThread.getIns().execute(task);
        } else {
            if (Logger.DEBUG) {
                Logger.i(TAG, "playTTsByDownload on NEW THREAD");
            }
            new Thread(task).start();
        }
    }
}