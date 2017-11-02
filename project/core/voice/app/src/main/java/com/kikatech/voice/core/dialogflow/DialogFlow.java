package com.kikatech.voice.core.dialogflow;

import com.kikatech.voice.VoiceConfiguration;
import com.kikatech.voice.core.dialogflow.intent.Scene;

/**
 * Created by tianli on 17-10-28.
 */

public class DialogFlow {

    private Agent mAgent;

    private DialogFlow(Agent agent){
        mAgent = agent;
    }

    public static DialogFlow getInstance(VoiceConfiguration conf){
        DialogFlow flow = new DialogFlow(conf.getAgentCreator().create());
        return flow;
    }

    public void talk(String text){
    }

    public void register(Class<? extends Scene> clazz, Object object) {
    }

}
