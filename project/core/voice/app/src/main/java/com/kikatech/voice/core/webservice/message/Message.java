package com.kikatech.voice.core.webservice.message;

/**
 * Created by tianli on 17-10-31.
 */

public class Message{

    public static final int STATUS_ERROR = 0; // 0:服务器返回异常
    public static final int STATUS_SUCCESS = 1; // 1:服务器返回正常
    public static final int STATUS_CONN_ERROR = -1; // -1:连接错误（包括常见的联网超时错误）
    public static final int STATUS_EMPTY_DATA = -2; // -2:空包

    public MessageType type;

    public int status; // 状态
    public String text; // server返回的识别结果
    public String engine; // server使用的引擎
    public String sessionId; // server分配的session id
    public int seqId; // server处理的顺序号，负数表示处理完成

    public Message(){
    }



}
