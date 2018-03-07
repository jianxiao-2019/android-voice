package com.kikatech.voice.util;

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
        mStartTimeStamp = true;
        resetValues();

        logTimeStamp(title);
    }

    public void logTimeStamp(String title) {
        if (mStartTimeStamp == false) {
            return;
        }

        Long ts = System.currentTimeMillis();

        if (mStartTS != null) {
            Long costTS = ts - mStartTS;
            String costTimeStr = String.format("%.3f", (double)costTS/1000) + "s";
            String titleStr = title!=null? title:"";
            mTsList.add(costTimeStr);
            mTsList.add(titleStr);
            Logger.d("[report] " + titleStr + costTimeStr);
        } else {
            String titleStr = title!=null? title:"Start";
            mTsList.add(titleStr);
            Logger.d("[report] " + titleStr);
        }

        mStartTS = ts;
    }

    private void resetValues() {
        mTsList.clear();
        mStartTS = null;
        mIsEverDetectedVad = false;
        mIsEverSentDataToWeb = false;
    }

    public ArrayList<String> getTsList() {
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
