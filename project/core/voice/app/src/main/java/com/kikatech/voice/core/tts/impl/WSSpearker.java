package com.kikatech.voice.core.tts.impl;

import android.text.TextUtils;

import com.kikatech.voice.core.tts.TTSSpeaker;
import com.kikatech.voice.core.webservice.WebSocket;
import com.kikatech.voice.util.log.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by tianli on 17-10-31.
 */

public class WSSpearker extends TTSSpeaker{

    private WebSocket mSocket;

    private final List<Mark> mTtsMarks;
    private long mStartTime = -1;
    private long mEndTime = -1;

    private WSPlayer mPlayer = null;

    public WSSpearker(WebSocket ws){
        mSocket = ws;
        mTtsMarks = Collections.synchronizedList(new ArrayList<Mark>());
    }

    @Override
    public void speak(String text) {
        super.speak(text);
    }

    @Override
    public void interrupt() {
        super.interrupt();
    }

    private List<Mark> parseMarks(String json) {
        List<Mark> marks = new ArrayList<>();
        if (!TextUtils.isEmpty(json)) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    Mark mark = new Mark(obj.getLong("time"), obj.toString());
                    marks.add(mark);
                }
            } catch (Exception e) {
                Logger.w("Some error occurred when parsing json at TtsManager");

            }
        }
//        if(marks.isEmpty()){
//            mTtsMarks.add(new Mark(0, ""));
//        }
        return marks;
    }

    static class Mark {
        long time;
        String jsonStr;

        Mark(long time, String jsonStr) {
            this.time = time;
            this.jsonStr = jsonStr;
        }
    }

}
