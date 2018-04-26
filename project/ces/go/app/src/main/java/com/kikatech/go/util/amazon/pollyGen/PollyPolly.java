package com.kikatech.go.util.amazon.pollyGen;

import com.kikatech.go.util.BackgroundThread;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.tts.impl.KikaTtsCacheHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by brad_chang on 2017/12/15.
 */

public class PollyPolly {
    private static final String TAG = "PollyPolly";

    private static class AssetPolly {
        private String textToMd5;
        private String textToGenMp3;

        AssetPolly(String textToMd5) {
            this.textToMd5 = textToMd5;
            this.textToGenMp3 = PollyUtil.getIns().getDefaultSsmlText(textToMd5);
        }

        AssetPolly(String textToMd5, String textToGenMp3) {
            this.textToMd5 = textToMd5;
            this.textToGenMp3 = textToGenMp3;
        }
    }

    private static AssetPolly[] pollies = new AssetPolly[]{
            new AssetPolly("Where do you want to go?"),
            new AssetPolly("Tell me the address"),
            new AssetPolly("What's your destination?"),
            new AssetPolly("I couldn't find it. Please say again."),
            new AssetPolly("Please say the destination again."),
            new AssetPolly("I couldn't find it , try again please?"),
            new AssetPolly("OK! Start navigation."),
            new AssetPolly("Alright, Let's go!"),
            new AssetPolly("Navigation start."),
            new AssetPolly("OK! Stopping navigation."),
            new AssetPolly("No problem, stopping navigation."),
            new AssetPolly("Sure, canceling navigation."),
            new AssetPolly("Who would you like to message to?"),
            new AssetPolly("I couldn't find the contact. Please say again."),
            new AssetPolly("What's the message?"),
            new AssetPolly("Add the emoji to the message?"),
            new AssetPolly("Play the message?"),
            new AssetPolly("Do you want to reply?"),
            new AssetPolly("Who do you want to call?"),
            new AssetPolly("OK, calling now."),
            new AssetPolly("Which app do you want to use?"),
            new AssetPolly("Canceling conversation."),
            new AssetPolly("Please say again"),
            new AssetPolly("I didn't get what you say."),
            new AssetPolly("I didn't catch you."),
            new AssetPolly("OK, playing music."),
            new AssetPolly("What's the song do you like?"),
            new AssetPolly("Can not connect to KikaGo service."),
            new AssetPolly("Can not connect to language understanding service."),
    };

    public String removeDuplicateLetters(String s) {
        if (s == null || s.length() == 0) {
            return null;
        }
        Set<String> charSet = new HashSet<>();
        for (int i = 0; i < s.length(); i++) {
            String c = String.valueOf(s.charAt(i));
            charSet.add(c);
        }
        //String [] arr = charSet.toArray();
        List<String> list = new ArrayList<String>(charSet);
        //list.sort();
        Collections.sort(list);
        StringBuilder ret = new StringBuilder();
        for (String r : list) {
            ret.append(r);
        }
        return ret.toString();
    }

    public static void startQuery() {

        LogUtil.log("polly", "String count:" + pollies.length);

        BackgroundThread.post(new Runnable() {
            @Override
            public void run() {
                for (AssetPolly assetPolly : pollies) {
                    query(assetPolly.textToMd5, assetPolly.textToGenMp3);
                }
            }
        });
    }

    public static void query(final String textToMd5, final String textToGenMp3) {
        PollyUtil.getIns().getUrl(textToGenMp3, new PollyUtil.IPollySynthesizerListener() {
            @Override
            public void onUrlGet(String url) {
                processUrl(textToMd5, url);
            }
        });
    }

    private static void processUrl(String textToMd5, String url) {
        final JSONObject json = new JSONObject();
        JSONArray arr = new JSONArray();
        JSONObject item = new JSONObject();
        try {
            item.put("text", textToMd5);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        arr.put(item);
        try {
            json.put("contents", arr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JSONArray arrUrl = new JSONArray();
        arrUrl.put(url);


        //String jsonString = "{"language":"en_us","contents":[{"text":"famous Lake, right?","vid":0}]}"
        LogUtil.logw(TAG, "json:" + json.toString());
        LogUtil.logw(TAG, "arrUrl:" + arrUrl.toString());

        new Thread(new Runnable() {
            @Override
            public void run() {
                KikaTtsCacheHelper.TaskInfo task = new KikaTtsCacheHelper.TaskInfo(arrUrl.toString(), json.toString());
                KikaTtsCacheHelper.downloadWithTask(task);
            }
        }).start();
    }
}