package com.kikatech.voice.core.recorder;

/**
 * Created by tianli on 17-11-20.
 */

public interface IVoiceReader {

    void onRead(byte[] audioData, int sizeInBytes);

}
