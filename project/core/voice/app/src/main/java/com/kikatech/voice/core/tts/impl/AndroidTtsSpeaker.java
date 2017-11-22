package com.kikatech.voice.core.tts.impl;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.annotation.Nullable;
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

    private TextToSpeech mTts;
    private TtsStateChangedListener mStateChangedListener;

    private boolean mIsInitialized = false;

    private Voice[] mVoices = new Voice[SUPPORTED_VOICE_COUNT];
    private final LinkedList<Pair<String, Integer>> mPlayList = new LinkedList<>();
    private int mPlayListSize;

    private boolean mIsTtsInterrupted;

    @Override
    public void init(Context context, @Nullable final OnTtsInitListener listener) {
        if (context == null && listener != null) {
            listener.onTtsInit(INIT_FAIL);
        }

        mIsInitialized = false;
        mTts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
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

                    mVoices[0] = mTts.getDefaultVoice();
                    for (Voice voice : mTts.getVoices()) {
                        if (voice.getName().startsWith("en-us")) {
                            Logger.d("voice name = " + voice.getName());
                            if (!mVoices[0].getName().startsWith("en-us")) {
                                mVoices[0] = voice;
                            } else {
                                mVoices[1] = voice;
                                break;
                            }
                        }
                    }
                    if (mVoices[1] == null) {
                        mVoices[1] = mVoices[0];
                    }

                    if (listener != null) {
                        listener.onTtsInit(INIT_SUCCESS);
                    }
                }
            }
        });

        mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                if (mPlayListSize == mPlayList.size() + 1) {
                    if (mStateChangedListener != null) {
                        mStateChangedListener.onTtsStart();
                    }
                }
            }

            @Override
            public void onDone(String utteranceId) {
                Logger.d("AndroidTtsSpeaker onDone mPlayList.size() = " + mPlayList.size());
                if (mPlayList.size() > 0) {
                    playSingleList();
                } else if (mStateChangedListener != null && !mIsTtsInterrupted) {
                    mStateChangedListener.onTtsComplete();
                }
                mIsTtsInterrupted = false;
            }

            @Override
            public void onError(String utteranceId) {
                if (mStateChangedListener != null) {
                    mStateChangedListener.onTtsError();
                }
            }
        });
    }

    @Override
    public void close() {
        if (mTts != null) {
            mTts.shutdown();
        }
        mTts = null;
    }


    @Override
    public void speak(final String text) {
        mPlayList.clear();
        mPlayListSize = 0;
        mIsTtsInterrupted = false;
        Logger.d("Android TtsSpeaker speak text = " + text + " mTts = " + mTts + " mIsInitialized = " + mIsInitialized);
        if (mTts == null) {
            return;
        }

        if (mIsInitialized) {
            mTts.setVoice(getVoice(0));
            mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "");
        } else {
            if (mStateChangedListener != null) {
                mStateChangedListener.onTtsError();
            }
        }
    }

    @Override
    public void speak(Pair<String, Integer>[] sentences) {
        if (sentences == null) {
            return;
        }
        mPlayList.clear();
        mPlayListSize = sentences.length;
        mIsTtsInterrupted = false;
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
            Voice voice = getVoice(voiceId);
            if (voice != null) {
                mTts.setVoice(getVoice(voiceId));
                mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "");
            } else {
                new Exception("playSingleList failed").printStackTrace();
            }
        }
    }

    private Voice getVoice(int voiceId) {
        if (voiceId < 0 || mVoices == null || voiceId > mVoices.length
                || mVoices[voiceId] == null) {
            mTts.getDefaultVoice();
        }
        return mVoices[voiceId];
    }

    @Override
    public void interrupt() {
        mPlayList.clear();
        mPlayListSize = 0;
        if (mTts != null && mTts.isSpeaking()) {
            mIsTtsInterrupted = true;
            mTts.stop();
            if (mStateChangedListener != null) {
                mStateChangedListener.onTtsInterrupted();
            }
        }
    }

    @Override
    public boolean isTtsSpeaking() {
        if (mTts != null) {
            return mTts.isSpeaking();
        }
        return false;
    }

    @Override
    public void setTtsStateChangedListener(TtsStateChangedListener listener) {
        mStateChangedListener = listener;
    }
}
