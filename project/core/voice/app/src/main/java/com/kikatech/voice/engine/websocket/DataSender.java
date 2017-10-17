package com.kikatech.voice.engine.websocket;

import android.text.TextUtils;

import com.kikatech.voice.util.log.Logger;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ryanlin on 06/10/2017.
 */

public class DataSender {
    //     private static final String WEB_SOCKET_URL_DEV = "ws://172.16.3.168:8080/v2/speech";
    private static final String WEB_SOCKET_URL_DEV = "ws://speech0-dev-mvp.kikakeyboard.com/v2/speech";

    private static final String VERSION = "2";

    private static final int WEB_SOCKET_CONNECT_TIMEOUT = 5000;
    private static final int MAX_RECONNECT_TIME = 3;

    private int mReconnectTime = 0;

    private URI mUri;
    private VoiceWebSocketClient mWebSocketClient;

    private final OnDataReceiveListener mOnDataReceiveListener;

    public DataSender(OnDataReceiveListener listener) {
        mOnDataReceiveListener = listener;
        try {
            mUri = new URI(WEB_SOCKET_URL_DEV);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnDataReceiveListener {
        void onResult(KikaVoiceMessage message);
        void onWebSocketClosed();
        void onWebSocketError();
    }

    public void connect(String locale, String sign, String userAgent) {
        Logger.d("connect mWebSocketClient = " + mWebSocketClient);
        if (mWebSocketClient != null) {
            Logger.d("connect isConnecting = " + mWebSocketClient.isConnecting() + " isOpen = " + mWebSocketClient.isOpen());
        }
        if (mWebSocketClient != null && (mWebSocketClient.isConnecting() || mWebSocketClient.isOpen())) {
            return;
        }
        Logger.i("connect mUri = " + mUri);
        Logger.i("connect locale = " + locale + " sign = " + sign + " userAgent = " + userAgent);

        Map<String, String> httpHeaders = new HashMap<String, String>();
        httpHeaders.put("sign", sign);
        httpHeaders.put("version", VERSION);
        httpHeaders.put("lang", locale);
        httpHeaders.put("locale", locale);
        httpHeaders.put("User-Agent", userAgent);
        Draft draft = new Draft_6455();
        mWebSocketClient = new VoiceWebSocketClient(mUri, draft, httpHeaders, WEB_SOCKET_CONNECT_TIMEOUT);
        mWebSocketClient.connect();
    }

    public void disconnect() {
        if (mWebSocketClient == null || !mWebSocketClient.isOpen()) {
            return;
        }
        mWebSocketClient.close();
        mWebSocketClient = null;
    }

    // TODO : 由sender來保證 在connect() 之後的 sendData or SendCommand都能成功傳送？
    public void sendData(byte[] data) {
        if (mWebSocketClient == null) {
            Logger.e("Send data error: mWebSocketClient is null");
            return;
        }

        Logger.v("DataSender sendData, data.length = " + data.length + " isConnecting = " + mWebSocketClient.isConnecting() + ", isOpen = " + mWebSocketClient.isOpen());
        if (mWebSocketClient.isOpen()) {
            try {
                mWebSocketClient.send(data);
            } catch (Exception e) {
                Logger.e("sendData error: " + e.getMessage());
                e.printStackTrace();
                mWebSocketClient = null;
            }
        } else {
            Logger.w("Sending data when mWebSocketClient is closed.");
        }
    }

    public void sendCommand(String command, String payload) {
        String jsonCommand = genCommand(command, payload);
        if (TextUtils.isEmpty(jsonCommand) || mWebSocketClient == null) {
            Logger.e("Send command error : generate command failed.");
            return;
        }

        Logger.v("sendCommand, cmd: " + jsonCommand + ", isConnecting = " + mWebSocketClient.isConnecting() + ", isOpen = " + mWebSocketClient.isOpen());
        if (mWebSocketClient.isOpen()) {
            try {
                mWebSocketClient.send(jsonCommand);
            } catch (Exception e) {
                Logger.e("sendCommand error: " + e.getMessage());
                e.printStackTrace();
                mWebSocketClient = null;
            }
        } else {
            Logger.w("Send command when mWebSocketClient is closed.");
        }
    }

    private String genCommand(String command, String payload) {
        try {
            JSONObject cmdObj = new JSONObject();
            cmdObj.put("cmd", command);
            if (!TextUtils.isEmpty(payload)) {
                cmdObj.put("payload", payload);
            }
            return cmdObj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private class VoiceWebSocketClient extends WebSocketClient {

        VoiceWebSocketClient(URI serverUri, Draft protocolDraft,
                                    Map<String, String> httpHeaders, int connectTimeout) {
            super(serverUri, protocolDraft, httpHeaders, connectTimeout);
        }

        @Override
        public void onOpen(ServerHandshake handshakeData) {
            Logger.i("VoiceWebSocketClient onOpen");
            mReconnectTime = 0;
        }

        @Override
        public void onMessage(String message) {
            Logger.i("VoiceWebSocketClient onMessage message = " + message);
            final KikaVoiceMessage result = parseMessage(message);
            if (mOnDataReceiveListener != null) {
                mOnDataReceiveListener.onResult(result);
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Logger.i("VoiceWebSocketClient onClose");
            if (code == 1006 && mReconnectTime <= MAX_RECONNECT_TIME) {
//                DataSender.this.connect();
                mReconnectTime++;
            } else if (mOnDataReceiveListener != null) {
                mOnDataReceiveListener.onWebSocketClosed();
            }
        }

        @Override
        public void onError(Exception ex) {
            Logger.w("VoiceWebSocketClient onError ex = " + ex);
            ex.printStackTrace();
            if (mOnDataReceiveListener != null) {
                mOnDataReceiveListener.onWebSocketError();
            }
        }

        private KikaVoiceMessage parseMessage(String s) {
            try {
                JSONObject msgObj = new JSONObject(s);
                KikaVoiceMessage voiceMessage = new KikaVoiceMessage();

                // TODO : some of these name changed;
                voiceMessage.state = msgObj.optInt("state");
                voiceMessage.payload = msgObj.optString("payload");
                voiceMessage.serverEngine = msgObj.optString("engine");
                voiceMessage.sessionId = msgObj.optString("cid");
                voiceMessage.seqId = msgObj.optInt("seq");
                voiceMessage.url = msgObj.optString("url");
                voiceMessage.text = msgObj.optString("text");
                voiceMessage.alterStart = msgObj.optInt("alterStart", 0);
                voiceMessage.alterEnd = msgObj.optInt("alterEnd", 0);
                String type = msgObj.optString("type");
                voiceMessage.resultType = getResultType(type);

                return voiceMessage;
            } catch (Exception e) {
                e.printStackTrace();
                Logger.i("DataSender parseMessage error: " + e.getMessage());
            }
            return null;
        }

        private KikaVoiceMessage.ResultType getResultType(String type) {
            if (KikaVoiceMessage.ResultType.SEND.toString().equals(type)) {
                return KikaVoiceMessage.ResultType.SEND;
            }
            if (KikaVoiceMessage.ResultType.PRESEND.toString().equals(type)) {
                return KikaVoiceMessage.ResultType.PRESEND;
            }
            if (KikaVoiceMessage.ResultType.REPEAT.toString().equals(type)) {
                return KikaVoiceMessage.ResultType.REPEAT;
            }
            if (KikaVoiceMessage.ResultType.CANCEL.toString().equals(type)) {
                return KikaVoiceMessage.ResultType.CANCEL;
            }
            if (KikaVoiceMessage.ResultType.TTSURL.toString().equals(type)) {
                return KikaVoiceMessage.ResultType.TTSURL;
            }
            if (KikaVoiceMessage.ResultType.TTSMARKS.toString().equals(type)) {
                return KikaVoiceMessage.ResultType.TTSMARKS;
            }
            if (KikaVoiceMessage.ResultType.ALTER.toString().equals(type)) {
                return KikaVoiceMessage.ResultType.ALTER;
            }
            if (KikaVoiceMessage.ResultType.NOTALTERED.toString().equals(type)) {
                return KikaVoiceMessage.ResultType.NOTALTERED;
            }

            return KikaVoiceMessage.ResultType.SPEECH;
        }
    }
}
