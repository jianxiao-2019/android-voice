package com.kikatech.voice.core.tts.impl;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Pair;

import com.kikatech.voice.core.tts.TtsSpeaker;
import com.kikatech.voice.util.log.Logger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;

/**
 * Created by ryanlin on 07/11/2017.
 */

public class AndroidTtsSpeaker implements TtsSpeaker {

    private Context mContext;

    private TextToSpeech mTts;
    private TtsStateChangedListener mListener;

    private boolean mIsInitialized = false;
    private Runnable mDelayTts = null;

    private Voice[] mVoices = new Voice[SUPPORTED_VOICE_COUNT];
    private final LinkedList<Pair<String, Integer>> mPlayList = new LinkedList<>();
    private int mPlayListSize;

    public void setContext(Context context) {
        endTts();
        if (context == null) {
            return;
        }

        mContext = context;
        mTts = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int arg0) {
                if (mTts == null) {
                    return;
                }
                // TTS 初始化成功
                if (arg0 == TextToSpeech.SUCCESS) {
                    mIsInitialized = true;
                    // 指定的語系: 英文(美國)
                    Locale l = Locale.US;  // 不要用 Locale.ENGLISH, 會預設用英文(印度)

                    // 目前指定的【語系+國家】TTS, 已下載離線語音檔, 可以離線發音
                    if (mTts.isLanguageAvailable(l) == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                        mTts.setLanguage(l);
                    }
                    if (mDelayTts != null) {
                        mDelayTts.run();
                        mDelayTts = null;
                    }

                    mVoices[0] = mTts.getDefaultVoice();
                    for (Voice voice : mTts.getVoices()) {
                        if ("en-us-x-sfg#male_3-local".equals(voice.getName())) {
                            mVoices[1] = voice;
                        }
                    }
                }
            }
        });

        mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                if (mPlayListSize == mPlayList.size() + 1) {
                    if (mListener != null) {
                        mListener.onTtsStart();
                    }
                }
            }

            @Override
            public void onDone(String utteranceId) {
                Logger.d("AndroidTtsSpeaker onDone mPlayList.size() = " + mPlayList.size());
                if (mPlayList.size() > 0) {
                    playSingleList();
                } else if (mListener != null) {
                    mListener.onTtsComplete();
                }
            }

            @Override
            public void onError(String utteranceId) {
                if (mListener != null) {
                    mListener.onTtsError();
                }
            }
        });
    }

    @Override
    public void speak(final String text) {
        mPlayList.clear();
        mPlayListSize = 0;
        Logger.d("Android TtsSpeaker speak text = " + text + " mTts = " + mTts);
        if (mTts == null) {
            return;
        }

        mDelayTts = null;
        if (mIsInitialized) {
            mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "");
        } else {
            mDelayTts = new Runnable() {
                @Override
                public void run() {
                    mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "");
                }
            };
        }
    }

    @Override
    public void speak(Pair<String, Integer>[] sentences) {
        if (sentences == null) {
            return;
        }
        mPlayList.clear();
        mPlayListSize = sentences.length;
        Collections.addAll(mPlayList, sentences);
        if (mPlayList.size() > 0) {
            playSingleList();
        }
    }

    private void playSingleList() {
        Pair<String, Integer> pair = mPlayList.pollFirst();
        if (pair != null) {
            String text = pair.first;
            int voiceId = pair.second;
            mTts.setVoice(getVoice(voiceId));
            mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "");
        }
    }

    private Voice getVoice(int voiceId) {
        if (voiceId < 0 || mVoices == null || voiceId > mVoices.length) {
            mTts.getDefaultVoice();
        }
        return mVoices[voiceId];
    }

    private void endTts() {
        if (mTts != null) {
            mTts.shutdown();
        }
        mTts = null;
        mDelayTts = null;
    }

    @Override
    public void interrupt() {
        mTts.stop();
        if (mListener != null) {
            mListener.onTtsInterrupted();
        }
    }

    @Override
    public void setTtsStateChangedListener(TtsStateChangedListener listener) {
        mListener = listener;
    }
}
