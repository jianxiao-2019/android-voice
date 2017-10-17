package com.kikatech.voice.engine.websocket;

/**
 * Created by xm009 on 17/7/18.
 */

public class KikaVoiceMessage {
    public static final int STATE_ERROR = 0; // 0:服务器返回异常
    public static final int STATE_SUCCESS = 1; // 1:服务器返回正常
    public static final int STATE_CONN_ERROR = -1; // -1:连接错误（包括常见的联网超时错误）
    public static final int STATE_EMPTY_DATA = -2; // -2:空包

    public int state; // 状态
    public String payload; // server返回的识别结果
    public String serverEngine; // server使用的引擎
    public String sessionId; // server分配的session id
    public int seqId; // server处理的顺序号，负数表示处理完成
    public String url;
    public String text;
    public ResultType resultType;
    public int alterStart;
    public int alterEnd;

    public enum ResultType {
        SEND("SEND"),
        PRESEND("PRESEND"),
        REPEAT("REPEAT"),
        CANCEL("CANCEL"),
        SPEECH("SPEECH"),
        ALTER("ALTER"),
        NOTALTERED("NOTALTERED"),
        TTSURL("TTS-URL"),
        TTSMARKS("TTS-MARKS");

        private String name;
        ResultType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
