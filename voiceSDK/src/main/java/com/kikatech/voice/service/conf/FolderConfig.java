package com.kikatech.voice.service.conf;

/**
 * @author SkeeterWang Created on 2018/4/2.
 */

public class FolderConfig {
    private static final String TAG = "FolderConfig";

    private String dir;
    private long aliveDays;

    private FolderConfig(String dir, long aliveDays) {
        this.dir = dir;
        this.aliveDays = aliveDays;
    }

    public String getFolderDir() {
        return dir;
    }

    public long getAliveDays() {
        return aliveDays;
    }

    public static final class Builder {
        private String dir;
        private long aliveDays = -1;

        public Builder setFolderDir(String dir) {
            this.dir = dir;
            return this;
        }

        public Builder setAliveDays(long aliveDays) {
            this.aliveDays = aliveDays;
            return this;
        }

        public FolderConfig build() {
            return new FolderConfig(dir, aliveDays);
        }
    }
}
