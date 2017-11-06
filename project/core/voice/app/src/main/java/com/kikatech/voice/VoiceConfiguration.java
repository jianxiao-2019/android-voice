package com.kikatech.voice;

import android.os.Bundle;

import com.kikatech.voice.core.dialogflow.AgentCreator;
import com.kikatech.voice.core.recorder.IVoiceSource;
import com.kikatech.voice.core.webservice.WebSocket;

/**
 * Created by tianli on 17-10-28.
 */

public class VoiceConfiguration {

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
        public final Bundle bundle = new Bundle();

        public ConnectionConfiguration(String url, String locale, String sign, String userAgent,
                                       String engine, Bundle bundle) {
            this.url = url;
            this.locale = locale;
            this.sign = sign;
            this.userAgent = userAgent;
            this.engine = engine;

            this.bundle.putAll(bundle);
        }

        public static class Builder {
            String url;
            String locale;
            String sign;
            String userAgent;
            String engine;
            Bundle bundle = new Bundle();

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

            public ConnectionConfiguration build() {
                return new ConnectionConfiguration(url, locale, sign, userAgent, engine, bundle);
            }
        }
    }
}
