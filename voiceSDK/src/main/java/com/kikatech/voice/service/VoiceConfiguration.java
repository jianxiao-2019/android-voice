package com.kikatech.voice.service;

import android.os.Bundle;
import android.text.TextUtils;

import com.kikatech.voice.core.dialogflow.AgentCreator;
import com.kikatech.voice.core.recorder.IVoiceSource;
import com.kikatech.voice.core.tts.TtsService;
import com.kikatech.voice.service.conf.AsrConfiguration;

/**
 * Created by tianli on 17-10-28.
 */

public class VoiceConfiguration {

    public static final class HostUrl {
        public static final String DEV_MVP = "ws://speech0-dev-mvp.kikakeyboard.com/v3/speech";
        public static final String DEV_JINCHENG = "ws://speech0-poc.kikakeyboard.com/v3/speech";
        public static final String DEV_HAO = "ws://speech.orctom.com:8080/v3/speech";
        public static final String DEV_KIKA = "ws://speech0-dev.kikakeyboard.com/v3/speech";
    }

    private static final String DEFAULT_ENGINE = "google";
    private static final String DEFAULT_LOCALE = "en_US";

    private static final int DEFAULT_BOS_DURATION = 6000;
    private static final int DEFAULT_EOS_DURATION = -1;

    private IVoiceSource mVoiceSource;
    private AgentCreator mAgentCreator;

    private ConnectionConfiguration mConnConf;

    private int bosDuration = DEFAULT_BOS_DURATION;
    private int eosDuration = DEFAULT_EOS_DURATION;
    private boolean mSupportWakeUpMode;
    private TtsService.TtsSourceType mTtsSource = TtsService.TtsSourceType.KIKA_WEB;

    private boolean mIsDebugMode = false;
    private String mDebugFileTag = "Unknown";

    public VoiceConfiguration() {
    }

    public VoiceConfiguration source(IVoiceSource source) {
        mVoiceSource = source;
        return this;
    }

    public VoiceConfiguration agent(AgentCreator creator) {
        mAgentCreator = creator;
        return this;
    }

    public VoiceConfiguration tts(TtsService.TtsSourceType type) {
        mTtsSource = type;
        return this;
    }

    public AgentCreator getAgent() {
        return mAgentCreator;
    }

    public IVoiceSource getVoiceSource() {
        return mVoiceSource;
    }

    public TtsService.TtsSourceType getTtsType() {
        return mTtsSource;
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

    int getEosDuration() {
        return eosDuration;
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

    public void setSupportWakeUpMode(boolean supportWakeUpMode) {
        mSupportWakeUpMode = supportWakeUpMode;
    }

    public boolean isSupportWakeUpMode() {
        return mSupportWakeUpMode;
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

    public static class ConnectionConfiguration {
        public final String url;
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
