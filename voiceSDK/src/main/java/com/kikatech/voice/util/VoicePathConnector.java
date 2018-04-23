package com.kikatech.voice.util;

import com.kikatech.voice.core.debug.FileWriter;
import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.core.hotword.WakeUpDetector;
import com.kikatech.voice.core.recorder.IVoiceSource;
import com.kikatech.voice.core.recorder.VoiceSource;
import com.kikatech.voice.core.vad.VoiceDetector;
import com.kikatech.voice.service.conf.VoiceConfiguration;
import com.kikatech.voice.util.log.Logger;

/**
 * Created by ryanlin on 20/12/2017.
 * Update by ryanlin on 25/12/2017.
 */

public class VoicePathConnector {

    public static IDataPath genDataPath(VoiceConfiguration conf,
                                        WakeUpDetector wakeUpDetector, IDataPath finalPath) {

        if (conf.getSpeechMode() == VoiceConfiguration.SpeechMode.AUDIO_UPLOAD) {
            return wrapFileWriter(finalPath, conf, "_upload");
        }

        boolean isUsbVoiceSource = conf.getVoiceSource() != null;
        boolean isSupportWakeUpMode = conf.isSupportWakeUpMode() && wakeUpDetector != null;

        IDataPath dataPath = new VoiceDetector(wrapFileWriter(finalPath, conf, "_speex"));
        Logger.d("VoicePathConnector isSupportWakeUpMode = " + isSupportWakeUpMode);
        if (isSupportWakeUpMode) {
            wakeUpDetector.setNextDataPath(wrapFileWriter(dataPath, conf, "_AWAKE"));
            dataPath = wakeUpDetector.getDataPath();
        }

        Logger.d("VoicePathConnector isUsbVoiceSource = " + isUsbVoiceSource);
        dataPath = wrapFileWriter(dataPath, conf, isUsbVoiceSource ? "_NC" : "_SRC");
        // TODO : This is for debug.
        dataPath.dump();
        return dataPath;
    }

    private static IDataPath wrapFileWriter(IDataPath nextPath,
                                            VoiceConfiguration conf, String additional) {
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
