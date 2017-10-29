package com.kikatech.voice.core.recorder;

import android.support.annotation.NonNull;

/**
 * Created by tianli on 17-10-28.
 */

public interface IVoiceSource {

    void start();

    void stop();

    int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes);

    int getBufferSize();

}
