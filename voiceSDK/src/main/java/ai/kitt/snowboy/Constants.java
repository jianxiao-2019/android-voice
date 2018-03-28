package ai.kitt.snowboy;

import android.os.Environment;

import java.io.File;

public class Constants {
    private static final String UMDL_HI_KIKA = "hotword_hi-kika-gstyle.umdl";
    private static final String UMDL_HI_KIKA_20171227 = "20171227_hotword_hi-kika_gstyle_2lables.umdl";
    private static final String UMDL_HI_KIKA_20180328 = "hotword_hi-kika-gstyle-expires-9-14-2018.umdl";
    public static final String ASSETS_RES_DIR = "snowboy";
    public static final String DEFAULT_WORK_SPACE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/snowboy/";
    public static final String ACTIVE_UMDL = UMDL_HI_KIKA_20180328;
    public static final String ACTIVE_RES = "common.res";
    public static final String SAVE_AUDIO = Constants.DEFAULT_WORK_SPACE + File.separatorChar + "recording.pcm";
    public static final int SAMPLE_RATE = 16000;
}