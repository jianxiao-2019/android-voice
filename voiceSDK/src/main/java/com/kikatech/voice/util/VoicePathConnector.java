package com.kikatech.voice.util;

import com.kikatech.voice.core.debug.FileWriter;
import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.core.hotword.WakeUpDetector;
import com.kikatech.voice.core.recorder.IVoiceSource;
import com.kikatech.voice.core.recorder.VoiceSource;
import com.kikatech.voice.core.vad.VoiceDetector;
import com.kikatech.voice.service.conf.VoiceConfiguration;

/**
 * Created by ryanlin on 20/12/2017.
 * Update by ryanlin on 25/12/2017.
 */

public class VoicePathConnector {

    public static IDataPath genDataPath(VoiceConfiguration conf, IDataPath finalPath) {

        if (conf.getSpeechMode() == VoiceConfiguration.SpeechMode.AUDIO_UPLOAD) {
            return wrapFileWriter(finalPath, conf, "_upload");
        }

        boolean isUsbVoiceSource = conf.getVoiceSource() != null;

        IDataPath dataPath = finalPath;

//        IWebSocket configSocket = conf.getWebSocket();
//        if (configSocket instanceof WebSocket) {
//            dataPath = new SpeexEncoder(wrapFileWriter(dataPath, conf, "_speex"));
//        }

        if (conf.getIsClientVadEnabled()) {
            dataPath = new VoiceDetector(dataPath);
        }

        WakeUpDetector wakeUpDetector = conf.getWakeUpDetector();
        if (wakeUpDetector != null) {
//            wakeUpDetector.setNextDataPath(wrapFileWriter(dataPath, conf, "_AWAKE"));
            wakeUpDetector.setNextDataPath(dataPath);
            dataPath = wakeUpDetector.getDataPath();
        }

        dataPath = wrapFileWriter(dataPath, conf, isUsbVoiceSource ? "_NC" : "_SRC");


        dataPath.dump();

        return dataPath;
    }

    private static IDataPath wrapFileWriter(IDataPath nextPath, VoiceConfiguration conf, String additional) {
        if (conf.getIsDebugMode()) {
            return new FileWriter(additional, nextPath);
        }
        return nextPath;
    }

    public static IVoiceSource genVoiceSource(VoiceConfiguration conf) {
        IVoiceSource voiceSource = conf.getVoiceSource();
        if (voiceSource == null) {
            return new VoiceSource();
        }
        return voiceSource;
    }
}
