package com.kikatech.voice.service;

import android.os.Bundle;
import android.text.TextUtils;

import com.kikatech.voice.core.dialogflow.AgentCreator;
import com.kikatech.voice.core.recorder.IVoiceSource;

/**
 * Created by tianli on 17-10-28.
 */

public class VoiceConfiguration {

    private static final String DEFAULT_ENGINE = "google";
    private static final String DEFAULT_LOCALE = "en_US";

    private IVoiceSource mVoiceSource;

    private AgentCreator mAgentCreator;

    private String mDebugFilePath;

    private ConnectionConfiguration mConnConf;

    public VoiceConfiguration(){
    }

    public VoiceConfiguration source(IVoiceSource source){
        mVoiceSource = source;
        return this;
    }

    public VoiceConfiguration agent(AgentCreator creator){
        mAgentCreator = creator;
        return this;
    }

    public AgentCreator getAgent(){
        return mAgentCreator;
    }

    public IVoiceSource getVoiceSource() {
        return mVoiceSource;
    }

    public void setDebugFilePath(String filePath) {
        mDebugFilePath = filePath;
    }

    public String getDebugFilePath() {
        return mDebugFilePath;
    }

    public void setConnectionConfiguration(ConnectionConfiguration connConf) {
        mConnConf = connConf;
    }

    public ConnectionConfiguration getConnectionConfiguration() {
        return mConnConf;
    }

    public static class ConnectionConfiguration {
        public final String url;
        public final String locale;
        public final String sign;
        public final String userAgent;
        public final String engine;
        public final String appName;

        public final boolean isAlterEnabled;
        public final boolean isEmojiEnabled;
        public final boolean isPunctuationEnabled;

        public final Bundle bundle = new Bundle();

        private ConnectionConfiguration(String appName, String url, String locale, String sign, String userAgent,
                                       String engine, boolean isAlterEnabled, boolean isEmojiEnabled, boolean isPunctuationEnabled, Bundle bundle) {

            this.appName = appName;
            this.url = url;
            this.locale = locale;
            this.sign = sign;
            this.userAgent = userAgent;
            this.engine = engine;

            this.isAlterEnabled = isAlterEnabled;
            this.isEmojiEnabled = isEmojiEnabled;
            this.isPunctuationEnabled = isPunctuationEnabled;

            this.bundle.putAll(bundle);
        }

        public static class Builder {
            String appName;
            String url;
            String locale;
            String sign;
            String userAgent;
            String engine;

            boolean isAlterEnabled = false;
            boolean isEmojiEnabled = false;
            boolean isPunctuationEnabled = false;

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

            public Builder setAlterEnabled(boolean isAlterEnabled) {
                this.isAlterEnabled = isAlterEnabled;
                return this;
            }

            public Builder setEmojiEnabled(boolean isEmojiEnabled) {
                this.isEmojiEnabled = isEmojiEnabled;
                return this;
            }

            public Builder setPunctuationEnabled(boolean isPunctuationEnabled) {
                this.isPunctuationEnabled = isPunctuationEnabled;
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
                return new ConnectionConfiguration(appName, url, locale, sign, userAgent, engine, isAlterEnabled, isEmojiEnabled, isPunctuationEnabled, bundle);
            }
        }
    }
}
