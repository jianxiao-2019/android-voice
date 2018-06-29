package com.kikatech.voice.service.dialogflow;

/**
 * @author SkeeterWang Created on 2018/4/9.
 */

public interface IDialogFlowVoiceService {

    void wakeUp(String wakeupFrom);

    void sleep();


    void setAsrAudioFilePath(String path, String fileName);


    void startListening();

    void startListening(int bosDuration);

    void stopListening();

    void completeListening();

    void cancelListening();


    void enableWakeUpDetector();

    void disableWakeUpDetector();


    void requestAsrAlignment(String[] alignment);

    void cancelAsrAlignment();


    void releaseVoiceService();
}
