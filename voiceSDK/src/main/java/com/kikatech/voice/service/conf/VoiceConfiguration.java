package com.kikatech.voice.service.conf;

import android.os.Bundle;
import android.text.TextUtils;

import com.kikatech.voice.core.dialogflow.AgentCreator;
import com.kikatech.voice.core.hotword.WakeUpDetector;
import com.kikatech.voice.core.recorder.IVoiceSource;
import com.kikatech.voice.core.tts.TtsService;
import com.kikatech.voice.core.webservice.IWebSocket;

/**
 * Created by tianli on 17-10-28.
 */

public class VoiceConfiguration {

    public static final class HostUrl {
        public static final String HAO_DEV = "ws://speech.orctom.com:8080/v3/speech";  // for Hao dev testing
        public static final String API_DEV = "ws://api-dev.kika.ai/v3/speech";         // the dev environment for server team

        public static final String API_SQ = "ws://api-sq.kika.ai/v3/speech";          // for Kika keyboard client dev
        public static final String API_PRODUCTION = "ws://api.kika.ai/v3/speech";             // final Kika keyboard release production server

        public static final String KIKAGO_SQ = "ws://kikago-sq.kika.ai/v3/speech";       // for KikaGo client dev
        public static final String KIKAGO_PRODUCTION = "ws://kikago.kika.ai/v3/speech";          // final KikaGo release production server
    }

    public interface Engine {
        String KIKA_KIKA = "kika";
        String KIKA_GOOGLE = "google";
    }

    public enum SpeechMode {
        ONE_SHOT,
        CONVERSATION,
        AUDIO_UPLOAD,
    }

    private static final String DEFAULT_ENGINE = Engine.KIKA_GOOGLE;
    private static final String DEFAULT_LOCALE = "en_US";

    private static final int DEFAULT_BOS_DURATION = -1;
    private static final int DEFAULT_EOS_DURATION = -1;


    private WakeUpDetector mWakeUpDetector = null;
    private IVoiceSource mVoiceSource;
    private AgentCreator mAgentCreator;

    private boolean mIsClientVadEnabled = false;

    private ConnectionConfiguration mConnConf;

    private int bosDuration = DEFAULT_BOS_DURATION;
    private int eosDuration = DEFAULT_EOS_DURATION;

    private TtsService.TtsSourceType mTtsSource = TtsService.TtsSourceType.KIKA_WEB;

    private boolean mSupportNBest = true;

    private SpeechMode mSpeechMode = SpeechMode.CONVERSATION;

    private IWebSocket mWebSocket;

    private boolean mIsDebugMode = false;
    private String mDebugFileTag = "Unknown";

    public VoiceConfiguration() {
    }

    public VoiceConfiguration source(IVoiceSource source) {
        mVoiceSource = source;
        return this;
    }

    public IVoiceSource getVoiceSource() {
        return mVoiceSource;
    }

    public VoiceConfiguration agent(AgentCreator creator) {
        mAgentCreator = creator;
        return this;
    }

    public AgentCreator getAgent() {
        return mAgentCreator;
    }

    public VoiceConfiguration tts(TtsService.TtsSourceType type) {
        mTtsSource = type;
        return this;
    }

    public TtsService.TtsSourceType getTtsType() {
        return mTtsSource;
    }

    public VoiceConfiguration setWebSocket(IWebSocket webSocket) {
        mWebSocket = webSocket;
        return this;
    }

    public IWebSocket getWebSocket() {
        return mWebSocket;
    }

    public void setBosDuration(int bosDuration) {
        this.bosDuration = bosDuration;
    }

    public int getBosDuration() {
        return bosDuration;
    }

    public void setEosDuration(int eosDuration) {
        this.eosDuration = eosDuration;
    }

    public int getEosDuration() {
        return eosDuration;
    }

    public void setWakeUpDetector(WakeUpDetector wakeUpDetector) {
        mWakeUpDetector = wakeUpDetector;
    }

    public WakeUpDetector getWakeUpDetector() {
        return mWakeUpDetector;
    }

    public void setIsClientVadEnabled(boolean isClientVadEnabled) {
        mIsClientVadEnabled = isClientVadEnabled;
    }

    public boolean getIsClientVadEnabled() {
        return mIsClientVadEnabled;
    }

    public void setIsSupportNBest(boolean isNBest) {
        mSupportNBest = isNBest;
    }

    public boolean getIsSupportNBest() {
        return mSupportNBest;
    }

    public void setSpeechMode(SpeechMode speechMode) {
        mSpeechMode = speechMode;
    }

    public SpeechMode getSpeechMode() {
        return mSpeechMode;
    }

    public void setConnectionConfiguration(ConnectionConfiguration connConf) {
        mConnConf = connConf;
    }

    public ConnectionConfiguration getConnectionConfiguration() {
        return mConnConf;
    }

    public void updateAsrConfiguration(AsrConfiguration conf) {
        if (conf == null || mConnConf == null) {
            return;
        }
        mConnConf.mAsrConfiguration = conf;
    }


    public void setDebugFileTag(String fileTag) {
        mDebugFileTag = fileTag;
    }

    public String getDebugFileTag() {
        return mDebugFileTag;
    }

    public void setIsDebugMode(boolean isDebugMode) {
        mIsDebugMode = isDebugMode;
    }

    public boolean getIsDebugMode() {
        return mIsDebugMode;
    }


    public static class ConnectionConfiguration {
        public String url;
        public final String locale;
        public final String sign;
        public final String userAgent;
        public final String engine;
        public final String appName;

        private AsrConfiguration mAsrConfiguration;

        public final Bundle bundle = new Bundle();

        private ConnectionConfiguration(String appName, String url, String locale, String sign, String userAgent,
                                        String engine, AsrConfiguration asrConfiguration, Bundle bundle) {

            this.appName = appName;
            this.url = url;
            this.locale = locale;
            this.sign = sign;
            this.userAgent = userAgent;
            this.engine = engine;

            this.mAsrConfiguration = asrConfiguration;

            this.bundle.putAll(bundle);
        }

        public AsrConfiguration getAsrConfiguration() {
            return mAsrConfiguration;
        }

        public static class Builder {
            String appName;
            String url;
            String locale;
            String sign;
            String userAgent;
            String engine;
            AsrConfiguration asrConfiguration = new AsrConfiguration.Builder().build();

            Bundle bundle = new Bundle();

            public Builder setAppName(String appName) {
                this.appName = appName;
                return this;
            }

            public Builder setUrl(String url) {
                this.url = url;
                return this;
            }

            public Builder setLocale(String locale) {
                this.locale = locale;
                return this;
            }

            public Builder setSign(String sign) {
                this.sign = sign;
                return this;
            }

            public Builder setUserAgent(String userAgent) {
                this.userAgent = userAgent;
                return this;
            }

            public Builder setEngine(String engine) {
                this.engine = engine;
                return this;
            }

            public Builder setAsrConfiguration(AsrConfiguration asrConfiguration) {
                if (asrConfiguration != null) {
                    this.asrConfiguration = asrConfiguration;
                }
                return this;
            }

            public Builder putString(String key, String value) {
                this.bundle.putString(key, value);
                return this;
            }

            public ConnectionConfiguration build() {
                if (TextUtils.isEmpty(url) || TextUtils.isEmpty(sign)
                        || TextUtils.isEmpty(userAgent)) {
                    throw new IllegalArgumentException();
                }
                if (TextUtils.isEmpty(engine)) {
                    engine = DEFAULT_ENGINE;
                }
                if (TextUtils.isEmpty(locale)) {
                    locale = DEFAULT_LOCALE;
                }
                return new ConnectionConfiguration(appName, url, locale, sign, userAgent, engine, asrConfiguration, bundle);
            }
        }
    }
}
