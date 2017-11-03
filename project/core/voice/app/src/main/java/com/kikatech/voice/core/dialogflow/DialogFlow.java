package com.kikatech.voice.core.dialogflow;

import com.kikatech.voice.VoiceConfiguration;

/**
 * Created by tianli on 17-10-28.
 */

public class DialogFlow {

    private Agent mAgent;

    private DialogFlow(Agent agent){
        mAgent = agent;
    }

    public static DialogFlow getInstance(VoiceConfiguration conf){
        DialogFlow flow = new DialogFlow(conf.getAgent().create());
        return flow;
    }

    public void talk(String words){
    }

    public void register(String scene, DialogObserver observer) {
    }

}
