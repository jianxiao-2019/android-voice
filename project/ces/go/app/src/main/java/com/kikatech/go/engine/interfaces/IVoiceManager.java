package com.kikatech.go.engine.interfaces;

/**
 * Created by ryanlin on 06/10/2017.
 */

public interface IVoiceManager {

    boolean DEBUG = true;

    void setVoiceView(IVoiceView voiceView);
    void startListening();
    void stopListening();
    void sendCommand(String command, String payload);
}
