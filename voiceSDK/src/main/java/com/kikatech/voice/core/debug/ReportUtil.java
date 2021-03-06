package com.kikatech.voice.core.debug;

import com.kikatech.voice.util.log.Logger;

import java.util.ArrayList;

/**
 * Created by ian.fan on 2018/2/27.
 */

public class ReportUtil {
    private static final ReportUtil sInstance = new ReportUtil();

    private ArrayList<String> mTsList = new ArrayList<String>();
    private boolean mStartTimeStamp = false;
    private Long mStartTS = null;
    private boolean mIsEverDetectedVad = false;
    private boolean mIsEverSentDataToWeb = false;

    public static ReportUtil getInstance() {
        return sInstance;
    }

    public void startTimeStamp(String title) {
        if (!DebugUtil.isDebug()) {
            return;
        }

        resetValues();

        mStartTimeStamp = true;
        logTimeStamp(title);
    }

    public void stopTimeStamp(String title) {
        if (!DebugUtil.isDebug()) {
            return;
        }

        logTimeStamp(title);
        mStartTimeStamp = false;
    }

    public void logTimeStamp(String title) {
        if (!DebugUtil.isDebug() || !mStartTimeStamp) {
            return;
        }

        Long ts = System.currentTimeMillis();

        if (mStartTS != null) {
            Long costTS = ts - mStartTS;
            String costTimeStr = String.format("%.3f", (double) costTS / 1000) + "s";
            String titleStr = title != null ? title : "";
            mTsList.add(costTimeStr);
            mTsList.add(titleStr);
            Logger.d("[report] " + titleStr + "" + costTimeStr);
        } else {
            String titleStr = title != null ? title : "Start";
            mTsList.add(titleStr);
            Logger.d("[report] " + titleStr);
        }

        mStartTS = ts;
    }

    public void logText(String title) {
        if (!DebugUtil.isDebug() || !mStartTimeStamp) {
            return;
        }

        if (title != null && title.length() > 0) {
            mTsList.add("        " + title);
        }
    }

    private void resetValues() {
        mTsList.clear();
        mStartTS = null;
        mIsEverDetectedVad = false;
        mIsEverSentDataToWeb = false;
    }

    public ArrayList<String> getTsList() {
        if (!DebugUtil.isDebug()) {
            mTsList.clear();
            mTsList.add("DebugMode is off");
        }
        return mTsList;
    }

    public void vadDetected() {
        mIsEverDetectedVad = true;
    }

    public boolean isEverDetectedVad() {
        return mIsEverDetectedVad;
    }

    public void sentDataToWeb() {
        mIsEverSentDataToWeb = true;
    }

    public boolean isEverSentDataToWeb() {
        return mIsEverSentDataToWeb;
    }
}
