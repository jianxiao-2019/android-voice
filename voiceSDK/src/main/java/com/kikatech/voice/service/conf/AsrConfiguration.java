package com.kikatech.voice.service.conf;

import android.os.Build;

import com.google.gson.Gson;

/**
 * Created by ryanlin on 12/12/2017.
 */

public class AsrConfiguration {

    public enum SpeechMode {
        ONE_SHOT,
        CONVERSATION
    }

    private SpeechMode speechMode = SpeechMode.ONE_SHOT;
    private boolean alterEnabled = false;
    private boolean emojiEnabled = true;
    private boolean punctuationEnabled = true;

    private AsrConfiguration(SpeechMode speechMode, boolean alterEnabled,
                             boolean emojiEnable, boolean punctuationEnabled) {
        this.speechMode = speechMode;
        this.alterEnabled = alterEnabled;
        this.emojiEnabled = emojiEnable;
        this.punctuationEnabled = punctuationEnabled;
    }

    public void setSpeechMode(SpeechMode speechMode) {
        this.speechMode = speechMode;
    }

    public SpeechMode getSpeechMode() {
        return this.speechMode;
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

    public void setPunctuationEnabled(boolean punctuationEnabled) {
        this.punctuationEnabled = punctuationEnabled;
    }

    public boolean getPunctuationEnabled() {
        return this.punctuationEnabled;
    }

    public String toJsonString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static class Builder {
        private SpeechMode speechMode = SpeechMode.CONVERSATION;
        private boolean alterEnabled = false;
        private boolean emojiEnabled = false;
        private boolean punctuationEnabled = false;

        public Builder setSpeechMode(SpeechMode speechMode) {
            this.speechMode = speechMode;
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

        public AsrConfiguration build() {
            return new AsrConfiguration(speechMode, alterEnabled, emojiEnabled, punctuationEnabled);
        }
    }
}
