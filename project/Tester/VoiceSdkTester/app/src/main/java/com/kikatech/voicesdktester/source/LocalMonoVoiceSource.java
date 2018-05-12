package com.kikatech.voicesdktester.source;


import com.kikatech.usb.buffer.KikaBuffer;
import com.kikatech.usb.buffer.KikaSimpleBuff;

/**
 * Created by ryanlin on 02/04/2018.
 */

public class LocalMonoVoiceSource extends LocalVoiceSource {

    private static final int BUFFER_SIZE = 1280;

    @Override
    protected KikaBuffer getKikaBuffer() {
        return new KikaSimpleBuff();
    }

    @Override
    public int getBufferSize() {
        return BUFFER_SIZE;
    }
}
