package com.kikatech.voice.core.tts.impl;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Pair;

import com.kikatech.voice.core.tts.TtsSource;
import com.kikatech.voice.util.log.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;

/**
 * Created by ryanlin on 07/11/2017.
 */

public class AndroidTtsSource implements TtsSource, TextToSpeech.OnInitListener {
    private static final String TAG = "AndroidTtsSource";

    private static final String GOOGLE_TTS_ENGINE_PACKAGE_NAME = "com.google.android.tts";

    private static final String FIRST_VOICE = "female_1-local";
    private static final String SECOND_VOICE = "male_1-local";

    private static final String TEMP_UTTERANCE_ID = "temp_utterance_id";
    private static final int DEFAULT_STREAM = AudioManager.STREAM_MUSIC;

    private Voice[] mAvailableVoices = new Voice[SUPPORTED_VOICE_COUNT];

    private TextToSpeech mTts;
    private OnTtsInitListener mOnTtsInitListener;
    private TtsStateChangedListener mStateChangedListener;
    private final LinkedList<Pair<String, Integer>> mPlayList = new LinkedList<>();
    private int mPlayListSize;
    private float mVolume = INVALID_VOLUME;

    private boolean mIsTtsInterrupted;

    private UtteranceProgressListener mUtteranceProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
            if (mPlayListSize == 0 || mPlayListSize == mPlayList.size() + 1) {
                if (mStateChangedListener != null) {
                    mStateChangedListener.onTtsStart();
                }
            }
        }

        @Override
        public void onDone(String utteranceId) {
            if (Logger.DEBUG) {
                Logger.i(TAG, String.format("AndroidTtsSource onDone mPlayList.size() = %s", mPlayList.size()));
            }
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
    };


    // TtsSource impl

    @Override
    public void init(Context context, @Nullable final OnTtsInitListener listener) {
        mOnTtsInitListener = listener;
        if (context == null) {
            if (mOnTtsInitListener != null) {
                mOnTtsInitListener.onTtsInit(INIT_FAIL);
            }
            return;
        }

        boolean isTTSAvailable = checkTTSAbility(context);

        if (!isTTSAvailable) {
            if (mOnTtsInitListener != null) {
                mOnTtsInitListener.onTtsInit(INIT_FAIL);
            }
            return;
        }

        mTts = new TextToSpeech(context, this, GOOGLE_TTS_ENGINE_PACKAGE_NAME);
    }

    @Override
    public void onInit(int status) {
        if (Logger.DEBUG) {
            Logger.i(TAG, "onInit");
        }

        switch (status) {
            case TextToSpeech.SUCCESS:
                Locale locale = Locale.US;
                boolean isLanguageAvailable = checkTTSLanguageAbility(mTts, locale);
                if (Logger.DEBUG) {
                    Logger.v(TAG, "isLanguageAvailable: " + isLanguageAvailable);
                }
                if (!isLanguageAvailable) {
                    mTts = null;
                    if (mOnTtsInitListener != null) {
                        mOnTtsInitListener.onTtsInit(INIT_FAIL);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        initAvailableVoices();
                    }
                    mTts.setOnUtteranceProgressListener(mUtteranceProgressListener);
                    if (mOnTtsInitListener != null) {
                        mOnTtsInitListener.onTtsInit(INIT_SUCCESS);
                    }
                }
                break;
            default:
                mTts = null;
                if (mOnTtsInitListener != null) {
                    mOnTtsInitListener.onTtsInit(INIT_FAIL);
                }
                break;
        }
    }

    /**
     * <p>Check if Google TTS installed and enabled
     *
     * @return true if Google TTS installed and enabled
     * @see <a href="http://stackoverflow.com/questions/11550746/why-is-the-action-check-tts-data-intent-awkward-to-use">StackOverflow</href>
     **/
    private boolean checkTTSAbility(Context context) {
        try {
            ApplicationInfo googleTTSInfo;

            PackageManager packageManager = context.getPackageManager();
            googleTTSInfo = packageManager.getApplicationInfo(GOOGLE_TTS_ENGINE_PACKAGE_NAME, 0);

            boolean isTTSInstalled = googleTTSInfo != null;
            boolean isTTSAvailable = isTTSInstalled && googleTTSInfo.enabled;

            if (Logger.DEBUG) {
                Logger.v(TAG, String.format("[checkTTSAbility] isTTSInstalled: %s, isTTSAvailable: %s", isTTSInstalled, isTTSAvailable));
            }

            return isTTSAvailable;
        } catch (Exception e) {
            if (Logger.DEBUG) {
                Logger.printStackTrace(TAG, e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * <p>Check if target language(locale) and data available
     *
     * @return false if data is missing or language is not supported
     **/
    private boolean checkTTSLanguageAbility(TextToSpeech tts, Locale locale) {
        if (tts != null) {
            int result = tts.setLanguage(locale);
            switch (result) {
                case TextToSpeech.LANG_MISSING_DATA:
                case TextToSpeech.LANG_NOT_SUPPORTED:
                    return false;
                default:
                    return true;
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initAvailableVoices() {
        Voice DEFAULT_VOICE = mTts.getDefaultVoice();
        for (Voice voice : mTts.getVoices()) {
            String voiceName = voice.getName();
            if (voiceName.startsWith("en-us")) {
                if (Logger.DEBUG) {
                    Logger.i(TAG, String.format("voice name: %s", voiceName));
                }
                if (voiceName.endsWith(FIRST_VOICE)) {
                    mAvailableVoices[0] = voice;
                } else if (voiceName.endsWith(SECOND_VOICE)) {
                    mAvailableVoices[1] = voice;
                }
            }
        }
        if (Logger.DEBUG) {
            if (Logger.DEBUG) {
                Logger.v(TAG, String.format("default voice: %s", DEFAULT_VOICE != null ? DEFAULT_VOICE.getName() : "<null>"));
            }
        }
        if (mAvailableVoices[0] == null) {
            mAvailableVoices[0] = DEFAULT_VOICE;
        }
        for (int i = 1; i < mAvailableVoices.length; i++) {
            if (mAvailableVoices[i] == null) {
                mAvailableVoices[i] = mAvailableVoices[0];
            }
        }
        if (Logger.DEBUG) {
            for (int i = 0; i < mAvailableVoices.length; i++) {
                Logger.d(TAG, String.format("mAvailableVoices[%s] = %s", String.valueOf(i), mAvailableVoices[i] != null ? mAvailableVoices[i].getName() : "<null>"));
            }
        }
    }

    @Override
    public void close() {
        if (mTts != null) {
            mTts.shutdown();
        }
        mTts = null;
    }

    @Override
    public void setVolume(float volume) {
        mVolume = volume;
    }

    @Override
    public void speak(final String text) {
        mPlayList.clear();
        mPlayListSize = 0;
        mIsTtsInterrupted = false;
        if (mTts == null || TextUtils.isEmpty(text)) {
            if (mStateChangedListener != null) {
                mStateChangedListener.onTtsError();
            }
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            speakGreaterLollipop(mTts, getVoice(TTS_SPEAKER_1), text, TEMP_UTTERANCE_ID);
        } else {
            speakUnderLollipop(mTts, text, TEMP_UTTERANCE_ID);
        }
    }

    @Override
    public void speak(Pair<String, Integer>[] sentences) {
        mPlayList.clear();
        mIsTtsInterrupted = false;
        mPlayListSize = sentences != null ? sentences.length : 0;
        if (mTts == null || mPlayListSize <= 0) {
            if (mStateChangedListener != null) {
                mStateChangedListener.onTtsError();
            }
            return;
        }
        Collections.addAll(mPlayList, sentences);
        playSingleList();
    }

    private void playSingleList() {
        Pair<String, Integer> pair = mPlayList.pollFirst();
        if (pair != null) {
            String text = pair.first;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int voiceId = pair.second;
                Voice voice = getVoice(voiceId);
                speakGreaterLollipop(mTts, voice, text, TEMP_UTTERANCE_ID);
            } else {
                speakUnderLollipop(mTts, text, TEMP_UTTERANCE_ID);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Voice getVoice(int voiceId) {
        if (mAvailableVoices == null || voiceId < 0 || voiceId >= mAvailableVoices.length || mAvailableVoices[voiceId] == null) {
            return mTts != null ? mTts.getDefaultVoice() : null;
        }
        return mAvailableVoices[voiceId];
    }

    @SuppressWarnings("deprecation")
    private void speakUnderLollipop(@NonNull TextToSpeech tts, String text, String speakerUid) {
        HashMap<String, String> args = new HashMap<>();
        args.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, speakerUid);
        args.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(DEFAULT_STREAM));
        if (mVolume >= 0 && mVolume <= 1) {
            args.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, String.valueOf(mVolume));
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, args);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void speakGreaterLollipop(@NonNull TextToSpeech tts, Voice voice, String text, String speakerUid) {
        Bundle args = new Bundle();
        args.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, DEFAULT_STREAM);
        if (mVolume >= 0 && mVolume <= 1) {
            args.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, mVolume);
        }
        if (voice != null) {
            int voiceAvailable = tts.setVoice(voice);
            switch (voiceAvailable) {
                case TextToSpeech.ERROR:
                    if (Logger.DEBUG) {
                        Logger.w(TAG, "setVoice: ERROR");
                    }
                    break;
                case TextToSpeech.SUCCESS:
                    if (Logger.DEBUG) {
                        Logger.d(TAG, "setVoice: SUCCESS");
                    }
                    break;
            }
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, args, speakerUid);
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
        return mTts != null && mTts.isSpeaking();
    }

    @Override
    public void setTtsStateChangedListener(TtsStateChangedListener listener) {
        mStateChangedListener = listener;
    }

    public Voice[] getVoice() {
        return mAvailableVoices;
    }

    private boolean isVolumeValid() {
        return mVolume >= 0 && mVolume <= 1;
    }
}