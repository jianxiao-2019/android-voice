package com.kikatech.voice.core.webservice;

import android.text.TextUtils;

import com.kikatech.voice.service.VoiceConfiguration.ConnectionConfiguration;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.util.log.Logger;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by tianli on 17-10-28.
 */

public class WebSocket {
    private static final String VERSION = "3";

    private static final int WEB_SOCKET_CONNECT_TIMEOUT = 5000;
    private static final int MAX_RECONNECT_TIME = 3;

    private VoiceWebSocketClient mClient;
    private OnWebSocketListener mListener;

    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private AtomicBoolean mReleased = new AtomicBoolean(false);
    private boolean mOpened = false;
    private int mReconnectTimes = 0;

    private ConnectionConfiguration mConf;

    public static WebSocket openConnection(OnWebSocketListener l) {
        return new WebSocket(l);
    }

    public interface OnWebSocketListener {
        void onOpen();

        void onMessage(Message message);

        void onWebSocketClosed();

        void onWebSocketError();
    }

    private WebSocket(OnWebSocketListener l) {
        mListener = l;
    }

    public void connect(final ConnectionConfiguration conf) {
        if (mReleased.get()) {
            Logger.e("WebSocket already released, can not connect again");
            return;
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mClient != null && (mClient.isConnecting() || mClient.isOpen())) {
                    Logger.i("WebSocket is connecting.");
                    return;
                }
                mConf = conf;
                Map<String, String> httpHeaders = new HashMap<>();
                httpHeaders.put("version", VERSION);
                httpHeaders.put("sign", conf.sign);
                httpHeaders.put("User-Agent", conf.userAgent);
                httpHeaders.put("lang", conf.locale);
                httpHeaders.put("locale", conf.locale);
                httpHeaders.put("engine", conf.engine);
                httpHeaders.put("app-name", conf.appName);
                Logger.d("appName = " + conf.appName);

                httpHeaders.put("alter-enabled", String.valueOf(conf.isAlterEnabled));
                httpHeaders.put("emoji-enabled", String.valueOf(conf.isEmojiEnabled));
                httpHeaders.put("punctuation-enabled", String.valueOf(conf.isPunctuationEnabled));
                Logger.d("sign = " + conf.sign + " agent = " + conf.userAgent + " engine = " + conf.engine);
                for (String key : conf.bundle.keySet()) {
                    httpHeaders.put(key, conf.bundle.getString(key));
                }
                Draft draft = new Draft_6455();
                try {
                    Logger.i("WebSocket connect url = " + conf.url);
                    mClient = new VoiceWebSocketClient(new URI(conf.url), draft,
                            httpHeaders, WEB_SOCKET_CONNECT_TIMEOUT);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                mClient.connect();
            }
        });
    }

    private boolean reconnect() {
        if (mReconnectTimes < MAX_RECONNECT_TIME && !mReleased.get()) {
            mReconnectTimes++;
            connect(mConf);
            return true;
        }
        return false;
    }

    public boolean isConnecting() {
        Logger.d("isConnecting mClient = " + mClient + " mOepned = " + mOpened);
        return mClient != null && mClient.isOpen() && mOpened;
    }

    public void release() {
        if (mReleased.compareAndSet(false, true)) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (mClient != null) {
                        mClient.close();
                    }
                    mClient = null;
                }
            });
        }
    }

    // TODO : 由sender來保證 在connect() 之後的 sendData or SendCommand都能成功傳送？
    public void sendData(final byte[] data) {
        if (mReleased.get()) {
            Logger.e("WebSocket already released, ignore data");
            return;
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Logger.v("WebSocket sendData, data.length = " + data.length + " isConnecting = " + mClient.isConnecting() + ", isOpen = " + mClient.isOpen() + " mOpened = " + mOpened);
                if (mClient != null && mOpened) {
                    try {
                        mClient.send(data);
                    } catch (Exception e) {
                        // TODO: 17-10-31 how to handle this case?
                        Logger.e("sendData error: " + e.getMessage());
                        e.printStackTrace();
                        mClient = null;
                    }
                } else {
                    Logger.w("Sending data when mWebSocketClient is closed.");
                }
            }
        });
    }

    public void sendCommand(final String command, final String payload) {
        if (mReleased.get()) {
            Logger.e("WebSocket already released, ignore data");
            return;
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                String jsonCommand = genCommand(command, payload);
                if (TextUtils.isEmpty(jsonCommand) || mClient == null) {
                    Logger.e("Send command error : generate command failed.");
                    return;
                }
                Logger.v("sendCommand, cmd: " + jsonCommand + ", isConnecting = " + mClient.isConnecting() + ", isOpen = " + mClient.isOpen());
                if (mClient != null && mOpened) {
                    try {
                        mClient.send(jsonCommand);
                    } catch (Exception e) {
                        Logger.e("sendCommand error: " + e.getMessage());
                        e.printStackTrace();
                        // TODO: 17-10-31 how to handle this case?
                        mClient = null;
                    }
                } else {
                    Logger.w("Send command when mWebSocketClient is closed.");
                }
            }
        });
    }

    // TODO: 17-10-31 命令的json生成放在外面比较好
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

    private Message handleMessage(String msg) {
        try {
            JSONObject json = new JSONObject(msg);
            Message message = Message.create(json.optString("type", "NONE"));
            if (message != null) {
                message.fromJson(json);
            }
            return message;
        } catch (Exception e) {
            e.printStackTrace();
            Logger.i("WebSocket parseMessage error: " + e.getMessage());
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
            Logger.i("VoiceWebSocketClient onOpen mListener = " + mListener);
            mOpened = true;
            mReconnectTimes = 0;
            if (mListener != null) {
                mListener.onOpen();
            }
        }

        @Override
        public void onMessage(String message) {
            Logger.i("VoiceWebSocketClient onMessage message = " + message);
            final Message msg = handleMessage(message);
            if (msg != null && mListener != null) {
                mListener.onMessage(msg);
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Logger.i("VoiceWebSocketClient onClose code = [" + code + "]");
            mOpened = false;
            if (!reconnect()) {
                if (mListener != null) {
                    mListener.onWebSocketClosed();
                }
            }
        }

        @Override
        public void onError(Exception ex) {
            Logger.w("VoiceWebSocketClient onError ex = " + ex);
            ex.printStackTrace();
            mOpened = false;
            if (!reconnect()) {
                if (mListener != null) {
                    mListener.onWebSocketError();
                }
            }
        }
    }

}
