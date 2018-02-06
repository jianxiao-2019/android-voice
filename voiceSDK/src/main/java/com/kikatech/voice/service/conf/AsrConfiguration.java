package com.kikatech.voice.service.conf;

import com.google.gson.Gson;

/**
 * Created by ryanlin on 12/12/2017.
 */

public class AsrConfiguration {

    private boolean spellingEnabled;
    private boolean alterEnabled;
    private boolean emojiEnabled;
    private boolean punctuationEnabled;
    private boolean vprEnabled;
    private int eosPackets;

    private AsrConfiguration(boolean spellingEnabled, boolean alterEnabled,
                             boolean emojiEnable, boolean punctuationEnabled, boolean vprEnabled, int eosPackets) {
        setConfig(spellingEnabled, alterEnabled,
                emojiEnable, punctuationEnabled, vprEnabled, eosPackets);
    }

    public void copyConfig(AsrConfiguration conf) {
        setConfig(conf.spellingEnabled, conf.alterEnabled, conf.emojiEnabled,
                conf.punctuationEnabled, conf.vprEnabled, conf.eosPackets);
    }

    private void setConfig(boolean spellingEnabled, boolean alterEnabled,
                           boolean emojiEnable, boolean punctuationEnabled, boolean vprEnabled, int eosPackets) {
        this.spellingEnabled = spellingEnabled;
        this.alterEnabled = alterEnabled;
        this.emojiEnabled = emojiEnable;
        this.punctuationEnabled = punctuationEnabled;
        this.vprEnabled = vprEnabled;
        this.eosPackets = eosPackets;
    }

    private boolean sameValue(AsrConfiguration asrConfig) {
        return alterEnabled == asrConfig.alterEnabled &&
                emojiEnabled == asrConfig.emojiEnabled &&
                punctuationEnabled == asrConfig.punctuationEnabled &&
                spellingEnabled == asrConfig.spellingEnabled &&
                vprEnabled == asrConfig.vprEnabled &&
                eosPackets == asrConfig.eosPackets;
    }

    public void setSpellingEnabled(boolean spellingEnabled) {
        this.spellingEnabled = spellingEnabled;
    }

    public boolean getSpellingEnabled() {
        return this.spellingEnabled;
    }

    public void setAlterEnabled(boolean alterEnabled) {
        this.alterEnabled = alterEnabled;
    }

    public boolean getAlterEnabled() {
        return this.alterEnabled;
    }

    public void setEmojiEnabled(boolean emojiEnabled) {
        this.emojiEnabled = emojiEnabled;
    }

    public boolean getEmojiEnabled() {
        return this.emojiEnabled;
    }

    public void setEosPackets(int eosPackets) {
        this.eosPackets = eosPackets;
    }

    public int getEosPackets() {
        return eosPackets;
    }

    public void setPunctuationEnabled(boolean punctuationEnabled) {
        this.punctuationEnabled = punctuationEnabled;
    }

    public boolean getVprEnabled() {
        return this.vprEnabled;
    }

    public void setVprEnabled(boolean vprEnabled) {
        this.vprEnabled = vprEnabled;
    }

    public boolean getPunctuationEnabled() {
        return this.punctuationEnabled;
    }

    public String toJsonString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public boolean update(AsrConfiguration asrConfig) {
        if (sameValue(asrConfig)) {
            return false;
        } else {
            copyConfig(asrConfig);
            return true;
        }
    }

    public static class Builder {
        private boolean spellingEnabled = false;
        private boolean alterEnabled = false;
        private boolean emojiEnabled = false;
        private boolean punctuationEnabled = false;
        private boolean vprEnabled = false;
        private int eosPackets = 1;

        public Builder setSpellingEnabled(boolean spellingEnabled) {
            this.spellingEnabled = spellingEnabled;
            return this;
        }

        public Builder setAlterEnabled(boolean alterEnabled) {
            this.alterEnabled = alterEnabled;
            return this;
        }

        public Builder setEmojiEnabled(boolean emojiEnabled) {
            this.emojiEnabled = emojiEnabled;
            return this;
        }

        public Builder setPunctuationEnabled(boolean punctuationEnabled) {
            this.punctuationEnabled = punctuationEnabled;
            return this;
        }

        public Builder setVprEnabled(boolean vprEnabled) {
            this.vprEnabled = vprEnabled;
            return this;
        }

        public Builder setEosPackets(int eosPackets) {
            this.eosPackets = eosPackets;
            return this;
        }

        public AsrConfiguration build() {
            return new AsrConfiguration(spellingEnabled, alterEnabled, emojiEnabled, punctuationEnabled, vprEnabled, eosPackets);
        }
    }
}