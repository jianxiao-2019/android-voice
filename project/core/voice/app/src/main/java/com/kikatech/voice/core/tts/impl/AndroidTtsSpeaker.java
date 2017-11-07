package com.kikatech.voice.core.tts.impl;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.kikatech.voice.core.tts.TtsSpeaker;

import java.util.Locale;

/**
 * Created by ryanlin on 07/11/2017.
 */

public class AndroidTtsSpeaker implements TtsSpeaker {

    private Context mContext;

    private TextToSpeech mTts;
    private TtsStateChangedListener mListener;

    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public void speak(final String text) {
        if (mContext == null) {
            return;
        }
        if (mTts == null) {
            mTts = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int arg0) {
                    if (mTts == null) {
                        return;
                    }
                    // TTS 初始化成功
                    if (arg0 == TextToSpeech.SUCCESS) {
                        // 指定的語系: 英文(美國)
                        Locale l = Locale.US;  // 不要用 Locale.ENGLISH, 會預設用英文(印度)

                        // 目前指定的【語系+國家】TTS, 已下載離線語音檔, 可以離線發音
                        if (mTts.isLanguageAvailable(l) == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                            mTts.setLanguage(l);
                        }
                    }
                    mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "");
                }
            });
        }
        mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                if (mListener != null) {
                    mListener.onTtsStart();
                }
            }

            @Override
            public void onDone(String utteranceId) {
                if (mListener != null) {
                    mListener.onTtsComplete();
                }
                endTts();
            }

            @Override
            public void onError(String utteranceId) {
                if (mListener != null) {
                    mListener.onTtsError();
                }
                endTts();
            }
        });
    }

    private void endTts() {
        if (mTts != null) {
            mTts.shutdown();
        }
        mTts = null;
    }

    @Override
    public void interrupt() {
        mTts.stop();
        endTts();
        if (mListener != null) {
            mListener.onTtsInterrupted();
        }
    }

    @Override
    public void setTtsStateChangedListener(TtsStateChangedListener listener) {
        mListener = listener;
    }
}
