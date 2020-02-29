package com.kikatech.voice.core.recorder;

import android.support.annotation.NonNull;

import java.io.IOException;

/**
 * Created by tianli on 17-10-28.
 * Update by ryanlin on 25/12/2017.
 */

public interface IVoiceSource {

    boolean open();

    void start();

    void stop();

    void close();

    int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) throws IOException;

    int getBufferSize();
}
