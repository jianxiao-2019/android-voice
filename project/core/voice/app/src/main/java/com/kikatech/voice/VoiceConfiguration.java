package com.kikatech.voice;

import com.kikatech.voice.core.dialogflow.AgentCreator;
import com.kikatech.voice.core.recorder.IVoiceSource;

/**
 * Created by tianli on 17-10-28.
 */

public class VoiceConfiguration {

    private IVoiceSource mVoiceSource;

    private AgentCreator mAgentCreator;

    public VoiceConfiguration(){
    }

    public VoiceConfiguration source(IVoiceSource source){
        mVoiceSource = source;
        return this;
    }

    public VoiceConfiguration agent(AgentCreator creator){
        mAgentCreator = creator;
        return this;
    }

    public AgentCreator getAgent(){
        return mAgentCreator;
    }
}
