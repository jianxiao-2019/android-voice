package com.kikatech.voice.util;

import com.kikatech.voice.core.debug.FileWriter;
import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.core.hotword.WakeUpDetector;
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

    public static final boolean IS_DEBUG = true;

    public static IDataPath genDataPath(VoiceConfiguration conf,
                                        WakeUpDetector wakeUpDetector, IDataPath finalPath) {

        boolean isUsbVoiceSource = conf.getVoiceSource() != null;
        boolean isSupportWakeUpMode = conf.isSupportWakeUpMode() && wakeUpDetector != null;

        int packageInterval = getPacketInterval(conf);
        IDataPath dataPath = new VoiceDetector(wrapFileWriter(finalPath, conf, "_speex"), packageInterval);
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

    private static int getPacketInterval(VoiceConfiguration conf) {
        return conf.getConnectionConfiguration().getAsrConfiguration().getPacketInterval();
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
