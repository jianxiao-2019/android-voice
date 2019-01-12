package com.kikago.speech.baidu;

public class BaiduInputStreamFactory {

    private static ForBaiduInputStream sInputStream;

    public static synchronized ForBaiduInputStream getBaiduInputStream() {
        if (sInputStream == null) {
            sInputStream = new ForBaiduInputStream();
        }

        return sInputStream;
    }
}
