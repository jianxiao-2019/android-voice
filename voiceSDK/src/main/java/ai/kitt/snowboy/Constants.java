package ai.kitt.snowboy;

import android.os.Environment;

import java.io.File;

public class Constants {
    private static final String UMDL_HI_KIKA = "hotword_hi-kika-gstyle.umdl";
    public static final String ASSETS_RES_DIR = "snowboy";
    public static final String DEFAULT_WORK_SPACE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/snowboy/";
    public static final String ACTIVE_UMDL = UMDL_HI_KIKA;
    public static final String ACTIVE_RES = "common.res";
    public static final String SAVE_AUDIO = Constants.DEFAULT_WORK_SPACE + File.separatorChar + "recording.pcm";
    public static final int SAMPLE_RATE = 16000;
}