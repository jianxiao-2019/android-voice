package com.kikatech.voice.core.dialogflow.scene;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.voice.core.dialogflow.DialogObserver;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.util.log.LogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bradchang on 2017/11/3.
 */

public class SceneNavigation extends SceneBase implements DialogObserver {

    private final static String TAG = "SceneNavigation";

    private final static String ACTION_NAV_START = "navi.start";
    private final static String ACTION_NAV_NO = "Navigation.Navigation-no";
    private final static String ACTION_NAV_YES = "Navigation.Navigation-yes";
    private final static String ACTION_NAV_CHANGE = "Navigation.Navigation-change";
    private final static String ACTION_NAV_CANCEL = "Navigation.Navigation-cancel";


    private final static String PRM_ADDRESS = "address";
    private final static String PRM_ATTRACTION_US = "place-attraction-us";
    private final static String PRM_SPECIFIC_LOC = "specific-loc";
    private final static String[] PRM_ARRAY = {PRM_ADDRESS, PRM_ATTRACTION_US, PRM_SPECIFIC_LOC};

    public final static byte NAVI_CMD_ERR = 0;
    public final static byte NAVI_CMD_ASK_ADDRESS = 1;
    public final static byte NAVI_CMD_CONFIRM_ADDRESS = 2;
    public final static byte NAVI_CMD_START_NAVI = 3;
    public final static byte NAVI_CMD_ASK_ADDRESS_AGAIN = 4;

    public final static String NAVI_CMD_ADDRESS = PRM_ADDRESS;


    private boolean mStateNaviStart = false;
    private boolean mStateNaviConfirm = false;
    private String mStateNaviAddress = "";

    private final Bundle mCmdParms = new Bundle();

    public SceneNavigation(ISceneCallback callback) {
        super(callback);
    }

    @Override
    public void onIntent(Intent intent) {
        processIntent(intent);
    }

    private void processIntent(@NonNull Intent intent) {
        final String action = intent.getAction();

        switch (action) {
            case ACTION_NAV_START:
                mStateNaviStart = true;
                parseAddress(intent.getExtra());
                break;
            case ACTION_NAV_YES:
                if (mStateNaviStart)
                    mStateNaviConfirm = true;
                break;
            case ACTION_NAV_NO:
                resetContext();
                break;
            case ACTION_NAV_CHANGE:
                parseAddress(intent.getExtra());
                break;
            case ACTION_NAV_CANCEL:
                resetContext();
                break;
            default:
                break;
        }

        byte naviAction = checkState();
        if (LogUtil.DEBUG) LogUtil.log(TAG, "processIntent, naviAction:" + naviAction);

        if (mCallback != null) {
            mCmdParms.clear();
            mCmdParms.putString(NAVI_CMD_ADDRESS, mStateNaviAddress);
            mCallback.onCommand(naviAction, mCmdParms);
        }

        if(NAVI_CMD_START_NAVI == naviAction) {
            resetContext();
        }
    }

    private void resetContext() {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "resetContext >>>>>>> ");
        if (mCallback != null) mCallback.resetContextImpl();
        resetVariables();
    }

    private void resetVariables() {
        mStateNaviStart = false;
        mStateNaviConfirm = false;
        mStateNaviAddress = "";
    }

    private void parseAddress(@NonNull Bundle parm) {
        for (String key : PRM_ARRAY) {
            String addr = parm.getString(key);
            LogUtil.log(TAG, key + ":" + addr);
            if (!TextUtils.isEmpty(addr)) {
                mStateNaviAddress = addr;
                return;
            }
        }
    }

    private byte checkState() {
        if (!mStateNaviStart) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "[Err] Navigation is not started yet");
            return NAVI_CMD_ERR;
        }

        if (TextUtils.isEmpty(mStateNaviAddress)) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "[action] Ask address");
            return NAVI_CMD_ASK_ADDRESS;
        }

        if (mStateNaviConfirm) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "[action] Start navigation");
            return NAVI_CMD_START_NAVI;
        } else {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "[action] Confirm if given address is correct");
            return NAVI_CMD_CONFIRM_ADDRESS;
        }
    }
}