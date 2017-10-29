package com.kikatech.voice;

import com.kikatech.voice.core.recorder.IVoiceSource;

/**
 * Created by tianli on 17-10-28.
 */

public class VoiceConfiguration {

    private IVoiceSource mVoiceRecorder;

    public VoiceConfiguration(){
    }

    public VoiceConfiguration recorder(IVoiceSource recorder){
        mVoiceRecorder = recorder;
        return this;
    }


}
