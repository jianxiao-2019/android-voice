package com.kikatech.voicesdktester.source;

import com.kikatech.usb.buffer.KikaBuffer;
import com.kikatech.usb.nc.KikaNcBuffer;

/**
 * Created by ryanlin on 03/01/2018.
 */

public class LocalNcVoiceSource extends LocalVoiceSource {

    @Override
    public int getBufferSize() {
        return KikaNcBuffer.BUFFER_SIZE;
    }

    @Override
    protected KikaBuffer getKikaBuffer() {
        return new KikaNcBuffer();
    }
}
