package com.kikatech.go.tutorial;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.IntDef;

import com.kikatech.go.R;
import com.kikatech.go.util.preference.GlobalPref;

/**
 * @author SkeeterWang Created on 2018/5/14.
 */

public class TutorialUtil {

    private static final int STAGE_ACTION_TYPE_ASR = 1;
    private static final int STAGE_ACTION_TYPE_ALERT = 2;
    private static final int STAGE_ACTION_TYPE_ALERT_DONE = 3;

    @IntDef({STAGE_ACTION_TYPE_ASR, STAGE_ACTION_TYPE_ALERT, STAGE_ACTION_TYPE_ALERT_DONE})
    public @interface StageActionType {
        int ASR = STAGE_ACTION_TYPE_ASR;
        int ALERT = STAGE_ACTION_TYPE_ALERT;
        int ALERT_DONE = STAGE_ACTION_TYPE_ALERT_DONE;
    }

    public static final class StageEvent {
        public static final String KEY_UI_INDEX = "key_ui_index";
        public static final String KEY_UI_TITLE = "key_ui_title";
        public static final String KEY_UI_DESCRIPTION = "key_ui_description";
        public static final String KEY_OPTION_LIST = "key_option_list";
        public static final String KEY_TYPE = "key_type";
        public static final String KEY_ACTION = "key_action";
        public static final String KEY_SCENE = "key_scene";
    }

    public static void setHasDoneTutorial() {
        GlobalPref.getIns().setHasDoneTutorial(true);
    }

    public static boolean hasDoneTutorial() {
        return GlobalPref.getIns().getHasDoneTutorial();
    }


    public static String[] getAskWakeUp(Context context) {
        Resources resource = context.getResources();
        String title = resource.getString(R.string.tutorial_wake_up_title);
        String description = resource.getString(R.string.tutorial_wake_up_description);
        return new String[]{title, description};
    }

    public static String[] getAskCommand(Context context) {
        Resources resource = context.getResources();
        String title = resource.getString(R.string.tutorial_command_title);
        String description = resource.getString(R.string.tutorial_command_description);
        return new String[]{title, description};
    }

    public static String[] getAskAddress(Context context) {
        Resources resource = context.getResources();
        String title = resource.getString(R.string.tutorial_address_title);
        String description = resource.getString(R.string.tutorial_address_description);
        return new String[]{title, description};
    }

    public static String[] getAskConfirm(Context context) {
        Resources resource = context.getResources();
        String title = resource.getString(R.string.tutorial_confirm_title);
        String description = resource.getString(R.string.tutorial_confirm_description);
        return new String[]{title, description};
    }

    public static String[] getTutorialDone(Context context) {
        Resources resource = context.getResources();
        String title = resource.getString(R.string.tutorial_done_title);
        String description = resource.getString(R.string.tutorial_done_description);
        return new String[]{title, description};
    }
}
