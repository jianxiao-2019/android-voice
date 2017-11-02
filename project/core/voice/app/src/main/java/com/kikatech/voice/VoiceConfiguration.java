package com.kikatech.voice;

import com.kikatech.voice.core.dialogflow.AgentCreator;
import com.kikatech.voice.core.recorder.IVoiceSource;

/**
 * Created by tianli on 17-10-28.
 */

public class VoiceConfiguration {

    private IVoiceSource mVoiceRecorder;

    private AgentCreator mAgentCreator;

    public VoiceConfiguration(){
    }

    public VoiceConfiguration recorder(IVoiceSource recorder){
        mVoiceRecorder = recorder;
        return this;
    }

    public VoiceConfiguration dialogAgent(AgentCreator creator){
        mAgentCreator = creator;
        return this;
    }

    public AgentCreator getAgentCreator(){
        return mAgentCreator;
    }
}
