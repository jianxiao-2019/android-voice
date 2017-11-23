package com.kikatech.go.dialogflow;

import android.content.Context;
import android.content.res.Resources;

import com.kikatech.go.R;

/**
 * @author SkeeterWang Created on 2017/11/22.
 */

public class SceneUtil {
    private static final String TAG = "SceneUtil";

    public static final String EXTRA_OPTIONS_LIST = "extra_options_list";
    public static final String EXTRA_UI_TEXT = "extra_ui_text";

    public static String[] getAskAddress(Context context) {
        Resources resource = context.getResources();
        String ui = resource.getString(R.string.ui_ask_address);
        String[] ttsArray = resource.getStringArray(R.array.tts_ask_address);
        return new String[]{ui, getStringFromArray(ttsArray)};
    }

    public static String[] getConfirmAddress(Context context, String address) {
        Resources resource = context.getResources();
        String ui = resource.getString(R.string.ui_confirm_address);
        String[] ttsArray = resource.getStringArray(R.array.tts_confirm_address);
        return new String[]{tryFormat(ui, address), tryFormat(getStringFromArray(ttsArray), address)};
    }

    public static String[] getAskAddressAgain(Context context) {
        Resources resource = context.getResources();
        String ui = resource.getString(R.string.ui_ask_address_again);
        String[] ttsArray = resource.getStringArray(R.array.tts_ask_address_again);
        return new String[]{ui, getStringFromArray(ttsArray)};
    }

    public static String[] getStartNavigation(Context context) {
        Resources resource = context.getResources();
        String ui = resource.getString(R.string.ui_start_navigation);
        String[] ttsArray = resource.getStringArray(R.array.tts_start_navigation);
        return new String[]{ui, getStringFromArray(ttsArray)};
    }

    public static String getStopNavigation(Context context) {
        Resources resource = context.getResources();
        String[] ttsArray = resource.getStringArray(R.array.tts_stop_navigation);
        return getStringFromArray(ttsArray);
    }

    public static String[] getAskContact(Context context) {
        Resources resource = context.getResources();
        String ui = resource.getString(R.string.ui_ask_contact);
        String[] ttsArray = resource.getStringArray(R.array.tts_ask_contact);
        return new String[]{ui, getStringFromArray(ttsArray)};
    }

    // TODO: display common options
    public static String[] getConfirmContact(Context context, String contact) {
        Resources resource = context.getResources();
        String ui = resource.getString(R.string.ui_confirm_contact);
        String[] ttsArray = resource.getStringArray(R.array.tts_confirm_contact);
        return new String[]{tryFormat(ui, contact), tryFormat(getStringFromArray(ttsArray), contact)};
    }

    public static String[] getContactNotFound(Context context) {
        Resources resource = context.getResources();
        String ui = resource.getString(R.string.ui_contact_not_found);
        String[] ttsArray = resource.getStringArray(R.array.tts_contact_not_found);
        return new String[]{ui, getStringFromArray(ttsArray)};
    }

    public static String[] getAskMsg(Context context) {
        Resources resource = context.getResources();
        String ui = resource.getString(R.string.ui_ask_msg);
        String[] ttsArray = resource.getStringArray(R.array.tts_ask_msg);
        return new String[]{ui, getStringFromArray(ttsArray)};
    }

    public static String[] getConfirmMsg(Context context, String msg) {
        Resources resource = context.getResources();
        String ui = resource.getString(R.string.ui_confirm_msg);
        String[] ttsArray = resource.getStringArray(R.array.tts_confirm_msg);
        return new String[]{tryFormat(ui, msg), tryFormat(getStringFromArray(ttsArray), msg)};
    }

    public static String[] getConfirmMsgOptions(Context context) {
        Resources resource = context.getResources();
        return resource.getStringArray(R.array.options_confirm_msg);
    }

    public static String[] getAskEmoji(Context context) {
        Resources resource = context.getResources();
        String ui = resource.getString(R.string.ui_ask_emoji);
        String[] ttsArray = resource.getStringArray(R.array.tts_ask_emoji);
        return new String[]{ui, getStringFromArray(ttsArray)};
    }

    public static String[] getAskReadMsg(Context context, String userName) {
        Resources resource = context.getResources();
        String ui = resource.getString(R.string.ui_ask_read_msg);
        String[] ttsArray = resource.getStringArray(R.array.tts_ask_read_msg);
        return new String[]{ui, tryFormat(getStringFromArray(ttsArray), userName)};
    }

    public static String[] getReadMsg(Context context, String userName, String msgContent) {
        Resources resource = context.getResources();
        String ui = resource.getString(R.string.ui_read_msg);
        String[] ttsArray = resource.getStringArray(R.array.tts_read_msg);
        return new String[]{tryFormat(ui, msgContent), tryFormat(getStringFromArray(ttsArray), userName, msgContent)};
    }

    public static String[] getAskReplyMsg(Context context) {
        Resources resource = context.getResources();
        String ui = resource.getString(R.string.ui_ask_reply_msg);
        String[] ttsArray = resource.getStringArray(R.array.tts_ask_reply_msg);
        return new String[]{ui, getStringFromArray(ttsArray)};
    }

    public static String[] getAskContactToCall(Context context) {
        Resources resource = context.getResources();
        String ui = resource.getString(R.string.ui_ask_contact_to_call);
        String[] ttsArray = resource.getStringArray(R.array.tts_ask_contact_to_call);
        return new String[]{ui, getStringFromArray(ttsArray)};
    }

    public static String[] getCallContact(Context context, String contact) {
        Resources resource = context.getResources();
        String ui = resource.getString(R.string.ui_call_contact);
        String[] ttsArray = resource.getStringArray(R.array.tts_call_contact);
        return new String[]{tryFormat(ui, contact), tryFormat(getStringFromArray(ttsArray), contact)};
    }

    public static String[] getCallNumber(Context context) {
        Resources resource = context.getResources();
        String ui = resource.getString(R.string.ui_call_number);
        String[] ttsArray = resource.getStringArray(R.array.tts_call_number);
        return new String[]{ui, getStringFromArray(ttsArray)};
    }

    public static String[] getAskActionForIncoming(Context context, String caller) {
        Resources resource = context.getResources();
        String ui = resource.getString(R.string.ui_ask_action_for_incoming);
        String[] ttsArray = resource.getStringArray(R.array.tts_ask_action_for_incoming);
        return new String[]{tryFormat(ui, caller), tryFormat(getStringFromArray(ttsArray), caller)};
    }

    public static String getResponseNotGet(Context context) {
        Resources resource = context.getResources();
        return resource.getString(R.string.tts_response_not_get);
    }

    public static String getIntentUnknown(Context context) {
        Resources resource = context.getResources();
        return resource.getString(R.string.tts_intent_unknown);
    }

    public static String[] getAskApp(Context context) {
        Resources resource = context.getResources();
        String ui = resource.getString(R.string.ui_ask_app);
        String[] ttsArray = resource.getStringArray(R.array.tts_ask_app);
        return new String[]{ui, getStringFromArray(ttsArray)};
    }

    public static String getEmojiAdded(Context context) {
        Resources resource = context.getResources();
        return resource.getString(R.string.tts_emoji_added);
    }

    public static String[] getOptionListCommon(Context context) {
        Resources resource = context.getResources();
        String uiTitle = resource.getString(R.string.ui_option_list_title_common);
        String ttsTitle = resource.getString(R.string.tts_option_list_title_common);
        String ttsSplit = resource.getString(R.string.tts_option_list_split_common);
        return new String[]{uiTitle, ttsTitle, ttsSplit};
    }

    public static String[] getStopCommon(Context context) {
        Resources resource = context.getResources();
        String ui = resource.getString(R.string.ui_stop_common);
        String[] ttsArray = resource.getStringArray(R.array.tts_stop_common);
        return new String[]{ui, getStringFromArray(ttsArray)};
    }

    public static String[] getOptionsCommon(Context context) {
        Resources resource = context.getResources();
        return resource.getStringArray(R.array.options_common);
    }

    private static String tryFormat(String string, Object... variables) {
        try {
            return String.format(string, variables);
        } catch (Exception ignore) {
            return string;
        }
    }

    private static String getStringFromArray(String[] stringArray) {
        return stringArray != null && stringArray.length > 0 ? stringArray[0] : null;
    }
}
