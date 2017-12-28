package com.kikatech.voice.util;

import com.kikatech.voice.core.debug.FileWriter;
import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.core.hotword.WakeUpDetector;
import com.kikatech.voice.core.ns.NoiseSuppression;
import com.kikatech.voice.core.recorder.IVoiceSource;
import com.kikatech.voice.core.recorder.VoiceSource;
import com.kikatech.voice.core.vad.VoiceDetector;
import com.kikatech.voice.service.VoiceConfiguration;
import com.kikatech.voice.util.log.Logger;

/**
 * Created by ryanlin on 20/12/2017.
 * Update by ryanlin on 25/12/2017.
 */

public class VoicePathConnector {

    private static final boolean IS_DEBUG = true;

    public static IDataPath genDataPath(VoiceConfiguration conf,
                                        WakeUpDetector wakeUpDetector, IDataPath finalPath) {

        boolean isUsbVoiceSource = conf.getVoiceSource() != null;
        boolean isSupportWakeUpMode = conf.isSupportWakeUpMode() && wakeUpDetector != null;

        IDataPath dataPath = new VoiceDetector(wrapFileWriter(finalPath, conf, "_speex"), null);
        Logger.d("VoicePathConnector isSupportWakeUpMode = " + isSupportWakeUpMode);
        if (isSupportWakeUpMode) {
            wakeUpDetector.setNextDataPath(wrapFileWriter(dataPath, conf, "_AWAKE"));
            dataPath = wakeUpDetector.getDataPath();
        }

        Logger.d("VoicePathConnector isUsbVoiceSource = " + isUsbVoiceSource);
        // TODO : NoiseSuppression is bind with USB driver, but this NoiseSuppression is in this Voice SDK.
        if (isUsbVoiceSource) {
            dataPath = new NoiseSuppression(wrapFileWriter(dataPath, conf, "_NC"));
        }

        dataPath = wrapFileWriter(dataPath, conf, isUsbVoiceSource ? "_USB" : "_SRC");
        // TODO : This is for debug.
        dataPath.dump();
        return dataPath;
    }

    private static IDataPath wrapFileWriter(IDataPath nextPath,
                                            VoiceConfiguration conf, String additional) {
        if (IS_DEBUG) {
            return new FileWriter(conf.getDebugFilePath() + additional, nextPath);
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