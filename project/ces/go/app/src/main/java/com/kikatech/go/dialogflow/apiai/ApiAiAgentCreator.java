package com.kikatech.go.dialogflow.apiai;

import android.content.Context;

import com.kikatech.voice.core.dialogflow.Agent;
import com.kikatech.voice.core.dialogflow.AgentCreator;


/**
 * @author SkeeterWang Created on 2017/11/3.
 */
public class ApiAiAgentCreator extends AgentCreator {

    @Override
    public Agent create(Context context) {
        return new ApiAiAgent(context);
    }
}
