package com.kikatech.voice.service.conf;

import android.os.Bundle;
import android.text.TextUtils;

import com.kikatech.voice.core.dialogflow.AgentCreator;
import com.kikatech.voice.core.recorder.IVoiceSource;
import com.kikatech.voice.core.tts.TtsService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tianli on 17-10-28.
 */

public class VoiceConfiguration {

    public static final class HostUrl {
        public static final String DEV_HAO              = "ws://speech.orctom.com:8080/v3/speech";  // for Hao dev testing
        public static final String DEV_ASR              = "ws://api-dev.kika.ai/v3/speech";         // the dev environment for server team
        public static final String DEV_KIKAGO           = "ws://kikago-sq.kika.ai/v3/speech";       // for KikaGo client dev
        public static final String DEV_KEYBOARD         = "ws://api-sq.kika.ai/v3/speech";          // for Kika keyboard client dev
        public static final String PRODUCTION_KIKAGO    = "ws://kikago.kika.ai/v3/speech";          // final KikaGo release production server
        public static final String PRODUCTION_KEYBOARD  = "ws://api.kika.ai/v3/speech";             // final Kika keyboard release production server
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

    private IVoiceSource mVoiceSource;
    private AgentCreator mAgentCreator;

    private ConnectionConfiguration mConnConf;

    private int bosDuration = DEFAULT_BOS_DURATION;
    private int eosDuration = DEFAULT_EOS_DURATION;

    private TtsService.TtsSourceType mTtsSource = TtsService.TtsSourceType.KIKA_WEB;

    private boolean mSupportWakeUpMode = false;
    private boolean mSupportNBest = true;

    private SpeechMode mSpeechMode = SpeechMode.CONVERSATION;


    private boolean mIsDebugMode = false;
    private String mDebugFileTag = "Unknown";

    private ExternalConfig mExternalConfig;


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

    public int getEosDuration() {
        return eosDuration;
    }

    public void setSupportWakeUpMode(boolean supportWakeUpMode) {
        mSupportWakeUpMode = supportWakeUpMode;
    }

    public boolean isSupportWakeUpMode() {
        return false;
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

    public void setExternalConfig(ExternalConfig externalConfig) {
        this.mExternalConfig = externalConfig;
    }

    public ExternalConfig getExternalConfig() {
        return mExternalConfig;
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

    public static class ExternalConfig {
        private long debugLogAliveDays;
        private List<FolderConfig> mFileFolders = new ArrayList<>();

        private ExternalConfig(long debugLogAliveDays, List<FolderConfig> folderConfigs) {
            this.debugLogAliveDays = debugLogAliveDays;
            if (folderConfigs != null && !folderConfigs.isEmpty()) {
                mFileFolders.addAll(folderConfigs);
            }
        }

        public long getDebugLogAliveDays() {
            return debugLogAliveDays;
        }

        public List<FolderConfig> getFolderConfigs() {
            return mFileFolders;
        }

        public static class Builder {
            private long debugLogAliveDays = -1;
            private List<FolderConfig> mFileFolders = new ArrayList<>();

            public Builder setDebugLogAliveDays(long aliveDays) {
                debugLogAliveDays = aliveDays;
                return this;
            }

            public Builder addFolderConfig(String dir) {
                mFileFolders.add(new FolderConfig.Builder().setFolderDir(dir).build());
                return this;
            }

            @SuppressWarnings("SameParameterValue")
            public Builder addFolderConfig(String dir, long aliveDays) {
                mFileFolders.add(new FolderConfig.Builder().setFolderDir(dir).setAliveDays(aliveDays).build());
                return this;
            }

            public ExternalConfig build() {
                return new ExternalConfig(debugLogAliveDays, mFileFolders);
            }
        }
    }
}
